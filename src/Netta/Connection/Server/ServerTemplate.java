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

package Netta.Connection.Server;

import java.io.IOException;
import java.net.ServerSocket;

import Netta.Connection.Connection;
import Netta.Exceptions.ServerInitializeException;

public abstract class ServerTemplate extends Connection implements Runnable {

	protected int port;
	protected ServerSocket serverSocket;
	private int SoTimeoutMilli = 1000;

	public ServerTemplate(int port) {
		this.port = port;
	}

	/**
	 * Initialize server elements. Sets up the serverSocket, socket timeout in
	 * milli's
	 * 
	 * @throws ServerInitializeException
	 *             thrown if there is an error creating the serverSocket object.
	 *             Details in the exception object's message()
	 */
	protected void Init() throws ServerInitializeException {
		try {
			serverSocket = new ServerSocket(port);
			serverSocket.setSoTimeout(SoTimeoutMilli);
		} catch (IOException e) {
			throw new ServerInitializeException(
					"Unable to create a server on this port. It is likely that the port is already in use.");
		}
	}

	/**
	 * Override this function
	 */
	public void run() {

	}

	/**
	 * Override this function
	 * 
	 * Thread that is called from within the Server object during it's thread
	 * cycle. Called right after the ServerSocket object accepts a socket, and
	 * passes it into the parameter
	 */
	protected void ThreadAction() {

	}
}
