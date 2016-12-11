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
import java.net.SocketTimeoutException;
import java.security.NoSuchAlgorithmException;

import Netta.Connection.Packet;
import Netta.Exceptions.ConnectionException;
import Netta.Exceptions.ConnectionInitializationException;
import Netta.Exceptions.HandShakeException;
import Netta.Exceptions.ReadPacketException;
import Netta.Exceptions.ServerInitializeException;

public abstract class SingleClientServer extends ServerTemplate {

	private boolean threadActive = false;

	/**
	 * Single Client Server. To start the server, simply create a new thread
	 * object of this server and start it. Everything else takes care of itself.
	 * Any time the client sends a packet, the ThreadAction(Packet) method is
	 * called. By default, this does nothing, so you must overload it yourself.
	 * The server also does not automatically handle close connection. You must
	 * do that yourself in the ThreadAction(Packet) method.
	 * 
	 * @param port
	 *            the server must host on
	 * @throws NoSuchAlgorithmException
	 *             when there is an issue creating the RSA cipher.
	 * @throws ServerInitializeException
	 *             if there is an error creating the server for any reason.
	 *             Details are in the exceptions message.
	 */
	public SingleClientServer(int port) throws NoSuchAlgorithmException {
		super(port);
	}

	public void run() {
		if (IsConnectionActive()) {
			System.err.println("Cannot initialize server. Server is already running: " + serverSocket);
			return;
		}

		System.out.println("Initializing singleclient server...");
		try {
			Init();
			System.out.println("Server Initialized.");
			threadActive = true;
		} catch (ServerInitializeException e) {
			System.err.println(e.getMessage());
			return;
		}

		System.out.println("Waiting for client connection...");

		while (threadActive) {
			try {
				connectedSocket = serverSocket.accept();
				OpenIOStreams();
				System.out.println("Client connection caught and initialized. Client: " + connectedSocket);
				System.out.println("Connection with " + connectedSocket + " now listening for incoming packets.");
				HandShake();
			} catch (SocketTimeoutException e) {
			} catch (IOException e) {
				System.err.println("Error accepting a client. Connection refused and reset.");
			} catch (ConnectionInitializationException e) {
				System.err.println(e.getMessage() + " Connection refused and reset.");
			} catch (HandShakeException e) {
				System.err.println(e.getMessage());
			}

			while (IsConnectionActive()) {
				try {
					ThreadAction(ReceivePacket());
				} catch (ReadPacketException e) {
					System.err.println(e.getMessage() + " Closing connection.");
					try {
						CloseIOStreams();
					} catch (ConnectionException e1) {
						System.err.println(e.getMessage());
					}
				}
			}
		}
	}

	/**
	 * Method called by the run function each time the client sends something
	 */
	protected void ThreadAction(Packet p) {

	}
}
