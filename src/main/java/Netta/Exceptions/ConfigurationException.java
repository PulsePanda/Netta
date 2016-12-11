/*
 *  Netta is a Java based network library, to make handling IO between client and server easy and hassle free.
 * 
 *  Copyright (C) 2016  Austin VanAlstyne

 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package main.java.Netta.Exceptions;

public class ConfigurationException extends Exception {

	/**
	 * Thrown if there are issues handling a Configuration file. Details of the
	 * error are held within the exception object's message()
	 */
	private static final long serialVersionUID = 1L;

	public ConfigurationException() {
	}

	public ConfigurationException(String message) {
		super(message);
	}
}
