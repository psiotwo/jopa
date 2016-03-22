/**
 * Copyright (C) 2016 Czech Technical University in Prague
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package cz.cvut.kbss.ontodriver.util;

public class ErrorUtils {

    /**
     * Error message stating that an argument cannot be null. </p>
     * <p>
     * This message contains a placeholder {@code arg}, which should be replaced with the actual argument name.
     */
    private static final String ARGUMENT_NULL = "Argument '$arg$' cannot be null.";

    private static final String PLACEHOLDER = "$arg$";

    private ErrorUtils() {
        throw new AssertionError();
    }

    /**
     * Creates message for NullPointerException, specifying name of method argument which was null.
     * <p>
     * The message has the following form: 'Argument argName cannot be null.'
     *
     * @param argName Name of the argument
     * @return String message for NPX exception
     */
    public static String npxMessage(String argName) {
        return ARGUMENT_NULL.replace(PLACEHOLDER, argName);
    }
}