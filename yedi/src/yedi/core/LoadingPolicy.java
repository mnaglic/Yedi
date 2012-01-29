/**
 Copyright 2012 Matej Nagli�

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

/**
 * LoadingPolicy defines whether a bean will be created in an eager or a lazy manner.
 * 
 * @author mnaglic
 *
 */
enum LoadingPolicy {

    EAGER("eager"),
    LAZY("lazy");

    private String policy;

    private LoadingPolicy(String policy) {
        this.policy = policy;
    }

    @Override
    public String toString() {
        return policy;
    }
}
