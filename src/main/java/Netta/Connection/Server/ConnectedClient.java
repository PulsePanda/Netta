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

package main.java.Netta.Connection.Server;

import java.net.Socket;

import Kript.Kript;
import main.java.Netta.Connection.Connection;
import main.java.Netta.Connection.Packet;
import main.java.Netta.Exceptions.ConnectionException;
import main.java.Netta.Exceptions.ConnectionInitializationException;
import main.java.Netta.Exceptions.HandShakeException;
import main.java.Netta.Exceptions.ReadPacketException;
import main.java.Netta.Exceptions.SendPacketException;

public class ConnectedClient extends Connection implements Runnable {

	public ConnectedClient(Socket socket, Kript kript) throws ConnectionInitializationException {
		super(kript);
		connectedSocket = socket;
		OpenIOStreams();
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
	}

	@Override
	public void run() {
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

	public void ThreadAction(Packet p) {
		if (p.packetType == Packet.PACKET_TYPE.Message)
			System.out.println(p.packetString);
		else if (p.packetType == Packet.PACKET_TYPE.CloseConnection) {
			System.out.println("Client wishes to close connection. Closing.");
			try {
				CloseIOStreams();
			} catch (ConnectionException e) {
				System.err.println(e.getMessage());
			}
		} else {
		}
	}

	protected void HandShake() throws HandShakeException {
		try {
			@SuppressWarnings("unused")
			Packet clientHello = ReceiveUnencryptedPacket();
		} catch (ReadPacketException e) {
			throw new HandShakeException("Unable to receive HandShake clientHello from connection. Terminating.");
		}

		try {
			Packet serverHello = new Packet(Packet.PACKET_TYPE.Handshake, null);
			SendUnencryptedPacket(serverHello);
		} catch (SendPacketException e) {
			throw new HandShakeException("Unable to send HandShake serverHello to connection. Terminating.");
		}

		try {
			Packet serverKeyExchange = new Packet(Packet.PACKET_TYPE.Handshake, null);
			serverKeyExchange.packetKey = kript.getPublicKey();
			SendUnencryptedPacket(serverKeyExchange);
		} catch (SendPacketException e) {
			throw new HandShakeException("Unable to send HandShake serverKeyExchange to connection. Terminating.");
		}

		try {
			Packet clientKeyExchange = ReceivePacket();
			kript.setRemotePublicKey(clientKeyExchange.packetKey);
		} catch (ReadPacketException e) {
			throw new HandShakeException("Unable to receive HandShake clientKeyExchange from connection. Terminating.");
		}

		try {
			Packet clientDone = ReceivePacket();
			if (!clientDone.packetString.equals("done")) {
				throw new HandShakeException(
						"Unable to decrypt PacketString from connection. HandShake failure. Terminating.");
			}
		} catch (ReadPacketException e) {
			throw new HandShakeException("Unable to receive HandShake clientDone from connection. Terminating.");
		}

		try{
			Packet serverDone = new Packet(Packet.PACKET_TYPE.Handshake, null);
			serverDone.packetString = "done";
			SendPacket(serverDone);
		}catch(SendPacketException e){
			throw new HandShakeException("Unable to send HandShake serverDone to connection. Terminating.");
		}
		
		System.out.println("HandShake with client complete!");
	}
}