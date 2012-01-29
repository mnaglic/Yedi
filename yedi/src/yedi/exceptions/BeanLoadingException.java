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
package yedi.exceptions;

/**
 * Thrown when loading a bean.
 * 
 * @author mnaglic
 *
 */
public class BeanLoadingException extends RuntimeException {

    private static final long serialVersionUID = -6692097000258703392L;

    public BeanLoadingException() {
        super();
    }

    public BeanLoadingException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

    public BeanLoadingException(String arg0) {
        super(arg0);
    }

    public BeanLoadingException(Throwable arg0) {
        super(arg0);
    }
}
