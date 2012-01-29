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

import java.util.Map;

/**
 * BeanDescriptorContainer holds bean descriptors.
 * 
 * @author mnaglic
 * @see BeanDescriptor
 *
 */
public class BeanDescriptorContainer {

    private Map <String, BeanDescriptor> beans;

    public Map<String, BeanDescriptor> getBeans() {
        return beans;
    }

    public void setBeans(Map<String, BeanDescriptor> beans) {
        this.beans = beans;
    }
}
