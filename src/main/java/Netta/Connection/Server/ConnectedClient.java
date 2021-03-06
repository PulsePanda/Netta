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

import Kript.Kript;
import Netta.Connection.Connection;
import Netta.Connection.Packet;
import Netta.Exceptions.*;

import java.net.Socket;

public class ConnectedClient extends Connection implements Runnable {

	private boolean handshakeComplete = false;
	private boolean encryptedPacket = true;

	/**
	 * ConnectedClient is designed to be used to handle each client on a server
	 * that has connected with a MultiClientServer. The server's packetReceived
	 * accepts a ConnectedClient object. This is when you take control over what
	 * client will be able to do.
	 * 
	 * @param socket
	 *            connection received by the server.
	 * @param kript
	 *            object being used by the server. Either create a new one per
	 *            connection, or have the same for all connections.
	 * @throws ConnectionInitializationException
	 *             thrown if there is an error initializing the client. Details
	 *             will be in getMessage().
	 */
	public ConnectedClient(Socket socket, Kript kript) throws ConnectionInitializationException {
		super(kript);
		connectedSocket = socket;
		openIOStreams();
		try {
			HandShake();
		} catch (HandShakeException e) {
			System.err.println(e.getMessage());
			try {
				closeIOStreams();
			} catch (ConnectionException e1) {
				System.err.println(e1.getMessage());
			}
			return;
		}
	}

	@Override
	public void run() {
		while (isConnectionActive()) {
			try {
				ThreadAction(receivePacket(encryptedPacket));
			} catch (ReadPacketException e) {
				System.err.println(e.getMessage() + " Closing connection.");
				try {
					closeIOStreams();
				} catch (ConnectionException e1) {
					System.err.println(e.getMessage());
				}
			}
		}
	}

	/**
	 * Called every time the server receives a packet from a connected client.
	 * 
	 * @param p
	 *            packet received by client.
	 */
	public void ThreadAction(Packet p) {
		if (p.packetType == Packet.PACKET_TYPE.Message)
			System.out.println(p.packetString);
		else if (p.packetType == Packet.PACKET_TYPE.CloseConnection) {
			System.out.println("Client wishes to close connection. Closing.");
			try {
				closeIOStreams();
			} catch (ConnectionException e) {
				System.err.println(e.getMessage());
			}
		} else {
		}
	}

	/**
	 * Returns the value of EncryptedPacket. This value is what determines
	 * whether the ReadPacket method will try to decrypt the data.
	 * 
	 * @return boolean if true the data is going to be decrypted
	 */
	public boolean getPacketEncrypted() {
		return encryptedPacket;
	}

	/**
	 * Sets the EncryptedPacket variable. Determines whether incoming packets
	 * are going to need to be decrypted.
	 * 
	 * @param encrypted
	 *            boolean. True will have Netta try to decrypt each packet.
	 */
	public void setPacketEncrypted(boolean encrypted) {
		encryptedPacket = encrypted;
	}

	/**
	 * Handshake helper method to initialize connection with Server. This method
	 * is called by the constructor to initialize the HandShake with the client.
	 * After the HandShake is successful, this method will be unable to be
	 * called again for this connection. If called, a HandShakeException will be
	 * thrown.
	 * 
	 * @throws HandShakeException
	 *             thrown if the handshake is unsuccessful. Details in
	 *             getMessage().
	 */
	protected void HandShake() throws HandShakeException {
		if (handshakeComplete)
			throw new HandShakeException("Unable to HandShake with client. HandShake has already been completed.");

		try {
			@SuppressWarnings("unused")
			Packet clientHello = receivePacket(false);
		} catch (ReadPacketException e) {
			throw new HandShakeException("Unable to receive HandShake clientHello from connection. Terminating.");
		}

		try {
			Packet serverHello = new Packet(Packet.PACKET_TYPE.Handshake, null);
			sendPacket(serverHello, false);
		} catch (SendPacketException e) {
			throw new HandShakeException("Unable to send HandShake serverHello to connection. Terminating.");
		}

		try {
			Packet serverKeyExchange = new Packet(Packet.PACKET_TYPE.Handshake, null);
			serverKeyExchange.packetKey = kript.getPublicKey();
			sendPacket(serverKeyExchange, false);
		} catch (SendPacketException e) {
			throw new HandShakeException("Unable to send HandShake serverKeyExchange to connection. Terminating.");
		}

		try {
			Packet clientKeyExchange = receivePacket(true);
			kript.setRemotePublicKey(clientKeyExchange.packetKey);
		} catch (ReadPacketException e) {
			throw new HandShakeException("Unable to receive HandShake clientKeyExchange from connection. Terminating.");
		}

		try {
			Packet clientDone = receivePacket(true);
			if (!clientDone.packetString.equals("done")) {
				throw new HandShakeException(
						"Unable to decrypt PacketString from connection. HandShake failure. Terminating.");
			}
		} catch (ReadPacketException e) {
			throw new HandShakeException("Unable to receive HandShake clientDone from connection. Terminating.");
		}

		try {
			Packet serverDone = new Packet(Packet.PACKET_TYPE.Handshake, null);
			serverDone.packetString = "done";
			sendPacket(serverDone, true);
		} catch (SendPacketException e) {
			throw new HandShakeException("Unable to send HandShake serverDone to connection. Terminating.");
		}

		handshakeComplete = true;
		System.out.println("HandShake with client complete!");
	}
}
