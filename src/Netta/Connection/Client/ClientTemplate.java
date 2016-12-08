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

package Netta.Connection.Client;

import java.io.IOException;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;

import Kript.Kript;
import Netta.Connection.Connection;
import Netta.Connection.Packet;
import Netta.Exceptions.ConnectionException;
import Netta.Exceptions.ConnectionInitializationException;
import Netta.Exceptions.HandShakeException;
import Netta.Exceptions.ReadPacketException;
import Netta.Exceptions.SendPacketException;

public abstract class ClientTemplate extends Connection implements Runnable {

	protected String serverIP;
	protected int port;

	/**
	 * Basic client setup. By default to initialize this object, simply create
	 * an object of it, a thread containing that object, and start the thread.
	 * From there the client will connect to the specified server, and open it's
	 * IO streams with that server. From there, any time the server sends a
	 * packet to the client, the ThreadAction(Packet) method will be called. By
	 * default, this method does nothing, so overload it yourself. This class
	 * also does not handle closing connections itself, that usually will be
	 * done through the ThreadAction(Packet) method.
	 * 
	 * @param serverIP
	 *            The IP address of the server to connect to
	 * @param port
	 *            the port of the server to connect to
	 * @throws NoSuchAlgorithmException
	 *             when there is an issue creating the RSA keys.
	 */
	public ClientTemplate(String serverIP, int port) throws NoSuchAlgorithmException {
		super(new Kript());
		this.serverIP = serverIP;
		this.port = port;
	}

	public void run() {
		System.out.println("Initializing client...");

		try {
			connectedSocket = new Socket(serverIP, port);
			OpenIOStreams();
			System.out.println("Client connected to server. Server: " + connectedSocket);
		} catch (IOException e) {
			System.err.println(
					"Unable to connect to server. Check your network connection and try again. Closing client.");
			return;
		} catch (ConnectionInitializationException e) {
			System.err.println(e.getMessage() + " Closing client.");
			return;
		}

		System.out.println("Listening for packets from the server.");

		try {
			HandShake();
		} catch (HandShakeException e) {
			System.err.println(e.getMessage());
			try {
				CloseIOStreams();
			} catch (ConnectionException e1) {
				System.err.println(e1.getMessage());
			}
			return;
		}

		while (IsConnectionActive()) {
			try {
				Packet p = ReceivePacket();
				ThreadAction(p);
			} catch (ReadPacketException e) {
				System.err.println(e.getMessage() + " Closing connection.");
				try {
					CloseIOStreams();
				} catch (ConnectionException e1) {
				}
			}
		}

		System.out.println("Closing down client.");
		try {
			CloseIOStreams();
		} catch (ConnectionException e) {
		}
	}

	protected void ThreadAction(Packet p) {

	}

	protected void HandShake() throws HandShakeException {
		try {
			Packet clientHello = new Packet(Packet.PACKET_TYPE.Handshake, null);
			SendUnencryptedPacket(clientHello);
		} catch (SendPacketException e) {
			throw new HandShakeException("Unable to send HandShake clientHello to connection. Terminating.");
		}

		try {
			Packet serverHello = ReceiveUnencryptedPacket();
			if (serverHello.packetType != Packet.PACKET_TYPE.Handshake)
				throw new HandShakeException(
						"HandShake serverHello from connection is not a HandShake Packet. Error with connection. Terminating.");
		} catch (ReadPacketException e) {
			throw new HandShakeException("Unable to receive HandShake serverHello from connection. Terminating.");
		}

		try {
			Packet serverKeyExchange = ReceiveUnencryptedPacket();
			kript.setRemotePublicKey(serverKeyExchange.packetKey);
		} catch (ReadPacketException e) {
			throw new HandShakeException("Unable to receive HandShake serverKeyExchange from connection. Terminating.");
		}

		try {
			Packet clientKeyExchange = new Packet(Packet.PACKET_TYPE.Handshake, null);
			clientKeyExchange.packetKey = kript.getPublicKey();
			SendPacket(clientKeyExchange);
		} catch (SendPacketException e) {
			e.printStackTrace();
			throw new HandShakeException("Unable to send HandShake clientKeyExchange to connection. Terminating.");
		}

		try {
			Packet clientDone = new Packet(Packet.PACKET_TYPE.Handshake, null);
			clientDone.packetString = "done";
			SendPacket(clientDone);
		} catch (SendPacketException e) {
			throw new HandShakeException("Unable to send HandShake clientDone to connection. Terminating.");
		}

		System.out.println("HandShake with server complete!");
	}
}
