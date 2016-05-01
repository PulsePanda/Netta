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

import java.net.Socket;

import Netta.Connection.Connection;
import Netta.Connection.Packet;
import Netta.Exceptions.ConnectionException;
import Netta.Exceptions.ConnectionInitializationException;
import Netta.Exceptions.ReadPacketException;

public class ConnectedClient extends Connection implements Runnable {

	public ConnectedClient(Socket socket) throws ConnectionInitializationException {
		connectedSocket = socket;
		OpenIOStreams();
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

	protected void ThreadAction(Packet p) {
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
}
