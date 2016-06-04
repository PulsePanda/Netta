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

package Netta.Connection;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import Netta.Exceptions.ConnectionException;
import Netta.Exceptions.ConnectionInitializationException;
import Netta.Exceptions.ReadPacketException;
import Netta.Exceptions.SendPacketException;

public abstract class Connection {
	private boolean connectionActive = false;
	private ObjectInputStream in;
	private ObjectOutputStream out;
	protected Socket connectedSocket;

	public Connection() {
	}

	/**
	 * Starts the connection and it's objects. Initializes Input and Output
	 * streams, and sets Connection Active to true only if no exception is
	 * thrown
	 * 
	 * @throws ConnectionInitializationException
	 *             if there is an error creating client input and/or output
	 *             streams
	 */
	public void OpenIOStreams() throws ConnectionInitializationException {
		if (connectionActive)
			throw new ConnectionInitializationException(
					"Connection is already active. Cannot OpenIOStreams on an active connection.");
		if (!connectedSocket.isConnected() || connectedSocket == null)
			throw new ConnectionInitializationException(
					"Socket is listed as not-connected. Cannot open streams on a disconnected socket.");

		try {
			out = new ObjectOutputStream(connectedSocket.getOutputStream());
			out.flush();
		} catch (IOException e) {
			throw new ConnectionInitializationException("Error creating client output stream on initialization.");
		}

		try {
			in = new ObjectInputStream(connectedSocket.getInputStream());
		} catch (IOException e) {
			throw new ConnectionInitializationException("Error creating client input stream on initialization.");
		}

		connectionActive = true;
	}

	/**
	 * Closes the Input and Output streams Sets the active boolean value to
	 * false regardless if there is an exception. This is to ensure the IO
	 * objects are no longer used if there is an error
	 * 
	 * @throws ConnectionException
	 *             if there is an issue closing the connection streams or
	 *             connected socket. Details are in the exception object's
	 *             message(). If an error is thrown, force close the connection.
	 */
	public void CloseIOStreams() throws ConnectionException {
		if (!connectionActive)
			throw new ConnectionException("Connection is already closed. Cannot close a closed connection.");

		connectionActive = false;

		try {
			out.flush();
			out.close();
		} catch (IOException e) {
			throw new ConnectionException("Unable to terminate the connection output stream.");
		} catch (NullPointerException e) {
		}

		try {
			in.close();
		} catch (IOException e) {
			throw new ConnectionException("Unable to terminate the connection input stream.");
		}

		try {
			connectedSocket.close();
		} catch (IOException e) {
			throw new ConnectionException("Unable to terminate the connection socket.");
		}
	}

	/**
	 * Check whether the connection is still active or not
	 * 
	 * @return True if the connection is still active, else false;
	 */
	public boolean IsConnectionActive() {
		return connectionActive;
	}

	/**
	 * Get the Socket object associated with this connection object
	 * 
	 * @return Socket associated with this object
	 */
	// public Socket GetSocket() {
	// return socket;
	// }

	// protected boolean SetSocket(Socket s) {
	// if (!active)
	// connectedSocket = s;
	// return !active;
	// }

	/**
	 * Send Packet. This method sends a packet p to the connected socket. It is
	 * important to note that this send is NOT converted to bytes, it is only
	 * sent as a packet. This function cannot be called if the connection is not
	 * active.
	 * 
	 * @param p
	 *            packet being sent to the socket connection
	 * 
	 * @return boolean value based on the success of the send. True if object
	 *         sent successfully, else false.
	 * 
	 * @throws SendPacketException
	 *             thrown when there is an error sending a packet to the socket.
	 *             Details in the exception object's message()
	 */
	public boolean SendPacket(Packet p) throws SendPacketException {
		if (!connectionActive)
			return false;

		try {
			out.writeObject(p);
			out.flush();
			return true;
		} catch (IOException e) {
			throw new SendPacketException("Error sending packet to socket. PacketType: " + p.packetType.toString()
					+ ". PacketMessage: " + p.packetString);
		}
	}

	/**
	 * Read Packet. This method reads a packet from the connected sockets input
	 * stream. It is important to note, this method reads the Packet object
	 * DIRECTLY, there is no byte conversion. This function cannot be called if
	 * the connection is not active.
	 * 
	 * @return packet from the sockets input stream. If there is an error or
	 *         anything else goes wrong, returns Packet.PACKET_TYPE.NULL packet
	 * 
	 * @throws ReadPacketException
	 *             thrown when there is an error reading a packet from the
	 *             socket. Details in the exception object's message()
	 */
	public Packet ReceivePacket() throws ReadPacketException {
		Packet p = new Packet(Packet.PACKET_TYPE.NULL, "");

		if (!connectionActive)
			return p;

		try {
			p = (Packet) in.readObject();
		} catch (IOException e) {
			throw new ReadPacketException(
					"Error reading the received data. Possible causes: Wrong Object Type; Incomplete Send;");
		} catch (ClassNotFoundException e) {
			throw new ReadPacketException(
					"Unable to find class Packet when reading in the data from the socket stream! Fatal Error.");
		}

		return p;
	}
}
