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

import Netta.Exceptions.ServerInitializeException;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public abstract class MultiClientServer extends ServerTemplate {

	private ArrayList<ConnectedClient> connectedClients;
	private boolean threadActive = false;

	/**
	 * Multiple Client Server. To start the server, simply create a new thread
	 * of this object and start the thread. When a new connection is received,
	 * the ThreadAction(ConnectedClient) method is called. By default, it does
	 * nothing. Designed for simple implementation, simply create a child of
	 * this class and overload the ThreadAction(ConnectedClient) method. The
	 * parameter passed into that method will contain the ConnectedClient object
	 * that was just received, which holds the connection socket, as well as IO
	 * methods. ConnectedClient is a Runnable class, and you can extend
	 * everything within it for customizability.
	 * 
	 * @param port
	 *            that you want the server to host on
	 * @throws NoSuchAlgorithmException
	 *             when there is an issue creating the RSA cipher.
	 */
	public MultiClientServer(int port) throws NoSuchAlgorithmException {
		super(port);
		connectedClients = new ArrayList<ConnectedClient>();
	}

	/**
	 * Thread.Run method. By default, this method will set up the connection
	 * objects, and call ThreadAction(ConnectedClient) any time there is a new
	 * connection, and only if the new connection was able to initialize
	 * successfully. If there is an error during the initialization steps, the
	 * System.err stream will be used detailing the error, and then the thread
	 * will be exited. If there is an error during the client accept steps, The
	 * System.err stream will be used to detail the problem, and then it will
	 * wait for the next connection.
	 * 
	 * Will not run if the connection is already active.
	 */
	public void run() {
		if (isConnectionActive()) {
			System.err.println("Cannot initialize server. Server is already running: " + serverSocket);
			return;
		}

		System.out.println("Initializing multiclient server...");
		try {
			Init();
			System.out.println("Server Initialized.");
			threadActive = true;
		} catch (ServerInitializeException e) {
			System.err.println(e.getMessage());
			return;
		}

		System.out.println("Waiting for client connections...");

		while (threadActive) {
			try {
				Socket s = serverSocket.accept();
				System.out.println("Client connection caught and initialized. Client: " + s);
				System.out.println("Connection with " + s + " now listening for incoming packets.");
				ThreadAction(s);
				// GENERATE NEW KRIPT OBJECT? SO AS TO HAVE SEPERATE KEYS PER
				// CONNECTION //////////
				CleanClientList();
			} catch (SocketTimeoutException e) {
			} catch (IOException e) {
				System.err.println("Error accepting a client. Connection refused and reset.");
			}
		}
	}

	/**
	 * By default, this method does nothing.
	 * 
	 * This method is called every time a new client is connected and
	 * initialized. The parameter is the connected client's socket. Override
	 * this function to be able to retrieve the newly connected client. This
	 * will also allow you to create a child class of ConnectedClient, and
	 * assign it to the newly accepted connections.
	 * 
	 * NOTE: The MultiClientServer has a built in ArrayList of type
	 * ConnectedClients to organize and store all connected clients. By default
	 * it is not used.
	 * 
	 * @param client
	 *            socket that was accepted by the server
	 */
	public void ThreadAction(Socket client) {

	}

	/**
	 * Retreive the ArrayList of connected clients.
	 * 
	 * @return ArrayList containing all ConnectedClient objects that are
	 *         connected.
	 */
	protected ArrayList<ConnectedClient> GetConnectedClients() {
		return connectedClients;
	}

	private void CleanClientList() {

	}
}
