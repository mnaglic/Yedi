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

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import yedi.exceptions.ConfigurationException;

/**
 * BeanDescriptor represents a description of a bean. It is used by the bean container to create 
 * the actual bean.
 * 
 * @author mnaglic
 * @see BeanContainer
 */
public class BeanDescriptor {

    private String type;
    private String scope = BeanScope.SINGLETON.toString();
    private List<?> constructorParameters;
    private Map<String, ?> properties;
    private static List<String> allowedScopeValues =
        Arrays.asList(BeanScope.SINGLETON.toString(), BeanScope.PROTOTYPE.toString());
    private String loadingPolicy;
    private static List<String> allowedLoadingPolicies =
        Arrays.asList(LoadingPolicy.EAGER.toString(), LoadingPolicy.LAZY.toString());

    List<?> getConstructorParameters() {
        return constructorParameters;
    }
    
    public void setConstructor(List<?> constructorParameters) {
        this.constructorParameters = constructorParameters;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public void setProperties(Map<String, ?> properties) {
        this.properties = properties;
    }
    
    String getType() {
        return type;
    }
    
    Map<String, ?> getProperties() {
        return properties;
    }
    
    String getScope() {
        return scope;
    }
   
    public void setScope(String scope) {
        if (BeanDescriptor.allowedScopeValues.contains(scope)) {
            this.scope = scope;
        } else {
            throw new ConfigurationException("Illegal bean scope: " + scope +
                    ". Allowed values are 'singleton' and 'prototype'");
        }
    }
    
    public String getLoadingPolicy() {
        return loadingPolicy;
    }
    
    public void setLoadingPolicy(String loadingPolicy) {

        if (BeanDescriptor.allowedLoadingPolicies.contains(loadingPolicy)) {
            this.loadingPolicy = loadingPolicy;
        } else {
            throw new ConfigurationException("Illegal loading policy: " + loadingPolicy +
            ". Allowed values are 'eager' and 'lazy'");
        }
    }
}
