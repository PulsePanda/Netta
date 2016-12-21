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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.PublicKey;
import java.util.ArrayList;

public class Packet implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public ArrayList<String> gData;
	public String packetString, senderID;
	public int packetInt;
	public boolean packetBool;
	public PublicKey packetKey;
	public PACKET_TYPE packetType;
	public byte[] packetByteArray;

	/**
	 * Default Constructor. Used to create a new packet for sending.
	 * 
	 * @param type
	 *            PACKET_TYPE. Used to set the function of the packet being
	 *            received.
	 * @param senderID
	 *            String. For Sender Identification
	 */
	public Packet(PACKET_TYPE type, String senderID) {
		gData = new ArrayList<String>();
		packetType = type;
		if (type != PACKET_TYPE.NULL)
			this.senderID = senderID;
		else
			this.senderID = "";
	}

	/**
	 * Alternate Constructor. Used to de-serialize a packet that was received.
	 * 
	 * @param packetBytes
	 *            Byte Array Object that contains the serialized version of the
	 *            packet (Usually the object that was received by the connection
	 * @throws IOException
	 *             Thrown if the constructor has an issue reading the
	 *             packetBytes
	 * @throws ClassNotFoundException
	 *             Thrown if the constructor has an issue using the Packet class
	 *             (Should never happen)
	 */
	public Packet(byte[] packetBytes) throws IOException, ClassNotFoundException {
		Packet p;
		ByteArrayInputStream bis = new ByteArrayInputStream(packetBytes);
		ObjectInput in = new ObjectInputStream(bis);
		p = (Packet) in.readObject();

		this.gData = p.gData;
		this.packetInt = p.packetInt;
		this.packetBool = p.packetBool;
		this.senderID = p.senderID;
		this.packetType = p.packetType;
		this.packetString = p.packetString;
		this.packetKey = p.packetKey;
		this.packetByteArray = p.packetByteArray;
	}

	/**
	 * Converts this object to a Byte array, otherwise known as Serialization
	 * method
	 * 
	 * @return byte[] Byte array that is the serialized version of the object
	 *         the method is being called on
	 * @throws IOException
	 *             Thrown if the method has an issue serializing the object
	 */
	public byte[] ToBytes() throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream os = new ObjectOutputStream(bos);
		os.writeObject(this);
		return bos.toByteArray();
	}

	public enum PACKET_TYPE {
		Registration, CloseConnection, Command, Handshake, Error, NULL, Message
	}
}
