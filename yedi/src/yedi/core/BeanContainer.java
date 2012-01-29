/**
 Copyright 2012 Matej Nagliæ

    This file is part of Yedi.

    Yedi is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Yedi is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with Yedi.  If not, see <http://www.gnu.org/licenses/>.
 */
package yedi.core;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import yedi.exceptions.BeanLoadingException;
import yedi.exceptions.ConfigurationException;

/**
 * BeanContainer is the central point of Yedi. It holds a Map of beans, which can be fetched
 * using getBean(String beanName). 
 * 
 * @author mnaglic
 *
 */
public class BeanContainer {

    private BeanDescriptorContainer descriptorContainer;
    private Map<String, Object> beans;
    private LoadingPolicy globalLoadingPolicy = LoadingPolicy.EAGER;
    private List<String> allowedLoadingPolicies =
        Arrays.asList(LoadingPolicy.EAGER.toString(), LoadingPolicy.LAZY.toString());

    public BeanContainer(String yediConfigurationPath) {
        InputStream configurationStream;

        try {
            configurationStream = new FileInputStream(yediConfigurationPath);
        } catch (FileNotFoundException e) {
            throw new ConfigurationException("No Yedi configuration found on path " +
                    yediConfigurationPath, e);
        }

        Yaml yaml = new Yaml();

        @SuppressWarnings("rawtypes")
        Map yediConfiguration = (Map) yaml.load(configurationStream);

        String loadingPolicy = (String) yediConfiguration.get("global-loading-policy");

        if (loadingPolicy != null) {
            if (!allowedLoadingPolicies.contains(loadingPolicy)) {
                throw new ConfigurationException("Illegal value for global loading policy: " +
                        loadingPolicy);
            } else if (loadingPolicy.equals(LoadingPolicy.LAZY.toString())) {
                globalLoadingPolicy = LoadingPolicy.LAZY;
            }
        }

        @SuppressWarnings("unchecked")
        List<String> beanConfigLocations =
            (List<String>) yediConfiguration.get("bean-configuration-locations");

        if (beanConfigLocations == null || beanConfigLocations.isEmpty()) {
            throw new ConfigurationException(
                    "No defined bean configuration locations found in Yedi configuration file");
        }

        InputStream beanConfigLocationStream;

        try {
            beanConfigLocationStream = new FileInputStream(beanConfigLocations.get(0));
        } catch (FileNotFoundException e) {
            throw new ConfigurationException("Could not find bean configuration on path " +
                    beanConfigLocations.get(0), e);
        }

        if (beanConfigLocations.size() > 1) {
            for (int i = 1; i < beanConfigLocations.size(); i++) {
                try {
                    FileInputStream fis = new FileInputStream(beanConfigLocations.get(i));
                    fis.skip(7);
                    beanConfigLocationStream =
                        new SequenceInputStream(beanConfigLocationStream, fis);
                } catch (FileNotFoundException e) {
                    throw new ConfigurationException("Could not find bean configuration on path " +
                            beanConfigLocations.get(i), e);
                } catch (IOException e) {
                    throw new ConfigurationException("Could not read bean configuration on path " +
                            beanConfigLocations.get(i), e);
                }
            }
        }

        Constructor yediConstructor = new Constructor(BeanDescriptorContainer.class);
        TypeDescription yediTypeDescription = new TypeDescription(BeanDescriptorContainer.class);
        yediConstructor.addTypeDescription(yediTypeDescription);

        yaml = new Yaml(yediConstructor);

        descriptorContainer = (BeanDescriptorContainer) yaml.load(beanConfigLocationStream);
        beans = new HashMap<String, Object>();
    }

    /**
     * Fetches a bean from the container. The bean will have its fields injected according to the
     * configuration.
     * 
     * @param beanName The name of the bean to fetch.
     * @return An object with fields populated according to bean configuration.
     */
    @SuppressWarnings("unchecked")
    public <T> T getBean(String beanName) {
        String exceptionMessagePrefix = "Could not load bean " + beanName + ": ";
        T bean = null;
        Map<String, BeanDescriptor> descriptorMap = descriptorContainer.getBeans();
        BeanDescriptor descriptor = descriptorMap.get(beanName);

        if (descriptor == null) {
            throw new BeanLoadingException(exceptionMessagePrefix + "Definition for bean " +
                    beanName + " was not found in any of the configuration files.");
        }

        //If the scope of the bean is "singleton" try to fetch it from the internal beans list.
        if (descriptor.getScope().equals(BeanScope.SINGLETON.toString())) {
            bean = (T) beans.get(beanName);
        }

        if (bean == null) {
            //The bean wasn't found in the internal bean list, or it's scope is 'prototype'.
            //A new instance needs to be created.
            try {
                Class<?> beanClass = Class.forName(descriptor.getType());

                //Try to inject properties through a suitable constructor first
                List<?> descriptorConstructorParameters = descriptor.getConstructorParameters();

                if (descriptorConstructorParameters != null &&
                        !descriptorConstructorParameters.isEmpty()) {
                    bean = injectThroughConstructor(bean, descriptorMap, beanClass,
                            descriptorConstructorParameters);
                }

                //if no suitable constructor was found, use a default one and continue;
                if (bean == null) bean = (T) beanClass.newInstance();

                Map<String, ?> properties = descriptor.getProperties();

                if (properties != null && !properties.isEmpty()) {
                    injectInFields(bean, descriptorMap, properties);
                }
            } catch (ClassNotFoundException e) {
                throw new BeanLoadingException(exceptionMessagePrefix + "Cannot find class " +
                        descriptor.getType(), e);
            } catch (IllegalAccessException e) {
                throw new BeanLoadingException(exceptionMessagePrefix + "No access rights", e);
            } catch (InstantiationException e) {
                throw new BeanLoadingException(exceptionMessagePrefix +
                        "Cannot create new instance of class " + descriptor.getType() +
                        ". Did you provide proper parameters for the constructor or a " +
                        "default constructor?", e);
            } catch (IllegalArgumentException e) {
                throw new BeanLoadingException(exceptionMessagePrefix +
                        "see cause exception for more information", e);
            } catch (InvocationTargetException e) {
                throw new BeanLoadingException(exceptionMessagePrefix +
                        "Could not invoke setter for a property", e);
            } catch (NoSuchFieldException nsfe) {
                throw new BeanLoadingException(exceptionMessagePrefix +
                        "Cannot find declared field of class " +
                        descriptor.getType(), nsfe);
            }

            if (!descriptor.getScope().equals(BeanScope.PROTOTYPE.toString())) {
                beans.put(beanName, bean);
            }
        }

        return bean;
    }

    private <T> Object createProxy(final Field field, final Object bean,
            final String proxiedBeanName, Map<String, BeanDescriptor> descriptorMap)
            throws ClassNotFoundException {
        BeanDescriptor proxiedBeanDescriptor = descriptorMap.get(proxiedBeanName);

        if (!field.getType().isInterface()) {
            throw new BeanLoadingException("Bean " + proxiedBeanName +
                    " is configured to load lazily and be injected into field " + field.getName() +
            " but the type of field is not an interface.");
        }

        InvocationHandler handler = new InvocationHandler() {

            @Override
            public Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable {
                //Set the real object instead of proxy
                boolean isAccessible = field.isAccessible();
                field.setAccessible(true);
                T proxiedBean = getBean(proxiedBeanName);
                field.set(bean, proxiedBean);
                field.setAccessible(isAccessible);

                //Invoke real method
                Class<?> parameterTypes[] = null;

                if (args != null) {
                    parameterTypes = new Class<?>[args.length];

                    for (int i = 0; i < args.length; i++) {
                        parameterTypes[i] = args[i].getClass();
                    }
                }

                Method realMethod = proxiedBean.getClass().getMethod(method.getName(),
                        parameterTypes);

                return realMethod.invoke(proxiedBean, args);
            }
        };

        ClassLoader loader = Class.forName(proxiedBeanDescriptor.getType()).getClassLoader();
        Object proxy = Proxy.newProxyInstance(loader, new Class[] {field.getType()},
                handler);

        return proxy;
    }

    /**
     * @param <T>
     * @param bean
     * @param descriptorMap
     * @param properties
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws NoSuchFieldException
     * @throws ClassNotFoundException
     */
    private <T> void injectInFields(T bean, Map<String, BeanDescriptor> descriptorMap,
            Map<String, ?> properties) throws IllegalAccessException, InvocationTargetException,
            NoSuchFieldException {
        for (String propertyName : properties.keySet()) {
            Object value = properties.get(propertyName);

            if (value instanceof BeanDescriptor) {
                value = (BeanDescriptor) value;

                for (Entry<String, BeanDescriptor> descriptorEntry : descriptorMap.entrySet()) {
                    BeanDescriptor descriptor = descriptorEntry.getValue();

                    if (descriptor.equals(value)) {

                        if ((descriptor.getLoadingPolicy() == null &&
                                globalLoadingPolicy.equals(LoadingPolicy.LAZY)) ||
                                LoadingPolicy.LAZY.toString().equals(
                                        descriptor.getLoadingPolicy())) {
                            Field property = bean.getClass().getDeclaredField(propertyName);
                            String proxiedBeanName = descriptorEntry.getKey();
                            try {
                                value = createProxy(property, bean, proxiedBeanName, descriptorMap);
                            } catch (ClassNotFoundException e) {
                                throw new BeanLoadingException("Could not create lazy bean " +
                                        proxiedBeanName + ". See cause for more information.");
                            }
                        } else {
                            value = getBean(descriptorEntry.getKey());
                        }
                    }
                }
            }

            boolean injectionCompleted = false;

            //Use the setter instead of injecting the bean directly into the field,
            //if it exists
            Method[] methods = bean.getClass().getDeclaredMethods();

            for (int i = 0; i < methods.length; i++) {
                Method method = methods[i];

                if (method.getName().equals("set" +
                        Character.toUpperCase(propertyName.charAt(0)) +
                        propertyName.substring(1)) &&
                        method.getParameterTypes().length == 1 &&
                        method.getParameterTypes()[0].equals(value.getClass())) {

                    boolean isSetterAccessible = method.isAccessible();
                    method.setAccessible(true);
                    method.invoke(bean, value);
                    method.setAccessible(isSetterAccessible);
                    injectionCompleted = true;
                    break;
                }
            }

            if (injectionCompleted) continue;

            //If no other way of setting the value is possible, try to set it directly
            Field property = bean.getClass().getDeclaredField(propertyName);
            boolean isAccessible = property.isAccessible();
            property.setAccessible(true);
            property.set(bean, value);
            property.setAccessible(isAccessible);
        }
    }

    /**
     * @param <T>
     * @param bean
     * @param descriptorMap
     * @param beanClass
     * @param descriptorConstructorParameters
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    @SuppressWarnings("unchecked")
    private <T> T injectThroughConstructor(T bean, Map<String, BeanDescriptor> descriptorMap,
            Class<?> beanClass, List<?> descriptorConstructorParameters)
    throws InstantiationException, IllegalAccessException, InvocationTargetException {
        Class<?>[] descriptorConstructorParameterTypes =
            new Class<?>[descriptorConstructorParameters.size()];

        for (int i = 0; i < descriptorConstructorParameters.size(); i++) {
            descriptorConstructorParameterTypes[i] =
                descriptorConstructorParameters.get(i).getClass();
        }

        java.lang.reflect.Constructor<?>[] constructors = beanClass.getDeclaredConstructors();

        for (java.lang.reflect.Constructor<?> constructor : constructors) {
            Class<?>[] constructorParameterTypes = constructor.getParameterTypes();

            //Try and see if we're lucky and there are no primitives in the constructor,
            //no bean references, and everything matches perfectly
            if (Arrays.equals(constructor.getParameterTypes(),
                    descriptorConstructorParameterTypes)) {
                boolean isConstructorAccessible = constructor.isAccessible();
                constructor.setAccessible(true);
                bean = (T) constructor.newInstance(descriptorConstructorParameters.toArray());
                constructor.setAccessible(isConstructorAccessible);
                break;
            }

            //We're not lucky.
            if (constructorParameterTypes.length != descriptorConstructorParameterTypes.length) {
                //This is not the right constructor, continue.
                continue;
            }

            Object[] objectsToInject = new Object[descriptorConstructorParameters.size()];

            boolean wrongConstructor = false;

            for (int i = 0; i < constructorParameterTypes.length; i++) {
                Class<?> constructorParameterType = constructorParameterTypes[i];

                if (constructorParameterType.isAssignableFrom(
                        descriptorConstructorParameterTypes[i])) {
                    objectsToInject[i] = descriptorConstructorParameters.get(i);
                } else if (descriptorConstructorParameterTypes[i].equals(BeanDescriptor.class)) {
                    Object parameterBean = null;

                    for (Entry<String, BeanDescriptor> descriptorEntry : descriptorMap.entrySet()) {
                        if (descriptorEntry.getValue().equals(
                                descriptorConstructorParameters.get(i))) {
                            parameterBean = getBean(descriptorEntry.getKey());
                        }
                    }

                    if (constructorParameterType.isAssignableFrom(parameterBean.getClass())) {
                        objectsToInject[i] = parameterBean;
                    }
                } else if(constructorParameterType.isPrimitive()) {
                    if (constructorParameterType.equals(Boolean.TYPE) &&
                            descriptorConstructorParameterTypes[i].equals(Boolean.class)){
                        objectsToInject[i] =
                            ((Boolean) descriptorConstructorParameters.get(i)).booleanValue();
                    } else if (constructorParameterType.equals(Character.TYPE) &&
                            descriptorConstructorParameterTypes[i].equals(Character.class)){
                        objectsToInject[i] =
                            ((Character) descriptorConstructorParameters.get(i)).charValue();
                    } else if (constructorParameterType.equals(Byte.TYPE) &&
                            descriptorConstructorParameterTypes[i].equals(Byte.class)){
                        objectsToInject[i] =
                            ((Byte) descriptorConstructorParameters.get(i)).byteValue();
                    } else if (constructorParameterType.equals(Short.TYPE) &&
                            descriptorConstructorParameterTypes[i].equals(Short.class)){
                        objectsToInject[i] =
                            ((Short) descriptorConstructorParameters.get(i)).shortValue();
                    } else if (constructorParameterType.equals(Integer.TYPE) &&
                            descriptorConstructorParameterTypes[i].equals(Integer.class)){
                        objectsToInject[i] =
                            ((Integer) descriptorConstructorParameters.get(i)).intValue();
                    } else if (constructorParameterType.equals(Long.TYPE) &&
                            descriptorConstructorParameterTypes[i].equals(Long.class)){
                        objectsToInject[i] =
                            ((Long) descriptorConstructorParameters.get(i)).longValue();
                    } else if (constructorParameterType.equals(Float.TYPE) &&
                            descriptorConstructorParameterTypes[i].equals(Float.class)){
                        objectsToInject[i] =
                            ((Float) descriptorConstructorParameters.get(i)).floatValue();
                    } else if (constructorParameterType.equals(Double.TYPE) &&
                            descriptorConstructorParameterTypes[i].equals(Double.class)){
                        objectsToInject[i] =
                            ((Double) descriptorConstructorParameters.get(i)).doubleValue();
                    } else {
                        wrongConstructor = true;
                    }
                } else {
                    wrongConstructor = true;
                }

                if (wrongConstructor) break;
            }

            if (wrongConstructor) {
                continue;
            } else {
                boolean isConstructorAccessible = constructor.isAccessible();
                constructor.setAccessible(true);
                bean = (T) constructor.newInstance(objectsToInject);
                constructor.setAccessible(isConstructorAccessible);
                break;
            }
        }
        return bean;
    }
}
