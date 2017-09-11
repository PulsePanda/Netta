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
import Netta.Connection.Packet;
import Netta.Exceptions.*;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.security.NoSuchAlgorithmException;

public abstract class SingleClientServer extends ServerTemplate {

    protected boolean threadActive = false;
    protected boolean encryptedPacket = true;
    protected boolean handshakeComplete = false;

    /**
     * Single Client Server. To start the server, simply create a new thread
     * object of this server and start it. Everything else takes care of itself.
     * Any time the client sends a packet, the packetReceived(Packet) method is
     * called. By default, this does nothing, so you must overload it yourself.
     * The server also does not automatically handle close connection. You must
     * do that yourself in the packetReceived(Packet) method.
     *
     * @param port  the server must host on
     * @param kript Encryption kript object if the connection will be encrypted, otherwise null
     * @throws NoSuchAlgorithmException when there is an issue creating the RSA cipher.
     */
    public SingleClientServer(int port, Kript kript) throws NoSuchAlgorithmException {
        super(port, kript);
    }

    /**
     * Handles itself, do not over ride this function.
     */
    public void run() {
        if (isConnectionActive()) {
            System.err.println("Cannot initialize server. Server is already running: " + serverSocket);
            return;
        }

        System.out.println("Initializing single-client server...");
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
                openIOStreams();
                System.out.println("Client connection caught and initialized. Client: " + connectedSocket);
                System.out.println("Connection with " + connectedSocket + " now listening for incoming packets.");
                HandShake();
            } catch (SocketTimeoutException e) {
            } catch (IOException e) {
                System.err.println("Error accepting a client. Connection refused and reset.");
                connectedSocket = null;
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e1) {
                }
            } catch (ConnectionInitializationException e) {
                System.err.println(e.getMessage() + " Connection refused and reset.");
            } catch (HandShakeException e) {
                System.err.println(e.getMessage());
            }

            while (isConnectionActive()) {
                try {
                    packetReceived(receivePacket());
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
    }

    /**
     * Close the server socket
     *
     * @throws IOException thrown if there is an error closing the server socket
     */
    @Override
    public void closeServer() throws IOException {
        threadActive = false;
        super.closeServer();
    }

    /**
     * Method called by the run function each time the client sends something
     *
     * @param p Packet received by client
     */
    protected void packetReceived(Packet p) {

    }

    /**
     * Method called by the run function when a client connects to the server
     *
     * @param client Socket of the connected client
     */
    protected void clientConnected(Socket client) {

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
     * @param encrypted boolean. True will have Netta try to decrypt each packet.
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
     * @throws HandShakeException thrown if the handshake is unsuccessful. Details in
     *                            getMessage().
     */
    protected void HandShake() throws HandShakeException {
        if (handshakeComplete)
            throw new HandShakeException("Unable to HandShake with client. HandShake has already been completed.");

        try {
            @SuppressWarnings("unused")
            Packet clientHello = receivePacket();
        } catch (ReadPacketException e) {
            throw new HandShakeException("Unable to receive HandShake clientHello from connection. Terminating.");
        }

        try {
            Packet serverHello = new Packet(Packet.PACKET_TYPE.Handshake, null);
            sendPacket(serverHello);
        } catch (SendPacketException e) {
            throw new HandShakeException("Unable to send HandShake serverHello to connection. Terminating.");
        }

        try {
            Packet serverKeyExchange = new Packet(Packet.PACKET_TYPE.Handshake, null);
            serverKeyExchange.packetKey = kript.getPublicKey();
            sendPacket(serverKeyExchange);
        } catch (SendPacketException e) {
            throw new HandShakeException("Unable to send HandShake serverKeyExchange to connection. Terminating.");
        }

        try {
            Packet clientKeyExchange = receivePacket();
            kript.setRemotePublicKey(clientKeyExchange.packetKey);
        } catch (ReadPacketException e) {
            throw new HandShakeException("Unable to receive HandShake clientKeyExchange from connection. Terminating.");
        }

        try {
            Packet clientDone = receivePacket();
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
            sendPacket(serverDone);
        } catch (SendPacketException e) {
            throw new HandShakeException("Unable to send HandShake serverDone to connection. Terminating.");
        }

        handshakeComplete = true;
        System.out.println("HandShake with client complete!");
    }
}
