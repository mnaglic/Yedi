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
 * Thrown when reading configuration.
 * 
 * @author mnaglic
 *
 */
public class ConfigurationException extends RuntimeException {

    private static final long serialVersionUID = -2517993346587032472L;

    public ConfigurationException() {
        super();
    }

    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConfigurationException(String message) {
        super(message);
    }

    public ConfigurationException(Throwable cause) {
        super(cause);
    }


}
