package Netta.Connection.Server;

import Netta.Exceptions.ConnectionException;
import Netta.Exceptions.ConnectionInitializationException;
import Netta.Exceptions.HandShakeException;
import Netta.Exceptions.ServerInitializeException;

import java.io.*;
import java.net.SocketTimeoutException;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Austin on 2/20/2017.
 */
public class MediaServer extends SingleClientServer {

    private FileInputStream in;
    private OutputStream out;
    private File mediaFile;

    /**
     * Media Server. To start the server, simply create a new thread
     * object of this server and start it. Everything else takes care of itself.
     * The server does not automatically handle close connection. You must
     * do that yourself.
     *
     * @param port      the server must host on
     * @param mediaFile File the server will be playing
     * @throws NoSuchAlgorithmException when there is an issue creating the RSA cipher.
     */
    public MediaServer(int port, File mediaFile) throws NoSuchAlgorithmException {
        super(port);
        this.mediaFile = mediaFile;
    }

    @Override
    public void run() {
        if (isConnectionActive()) {
            System.err.println("Cannot initialize Media server. Server is already running: " + serverSocket);
            return;
        }

        System.out.println("MediaServer: Initializing Media server...");
        try {
            Init();
            System.out.println("MediaServer: Server Initialized.");
            threadActive = true;
        } catch (ServerInitializeException e) {
            System.err.println("MediaServer: " + e.getMessage());
            return;
        }

        System.out.println("MediaServer: Waiting for client connection...");

        while (threadActive) {
            try {
                connectedSocket = serverSocket.accept();
                openIOStreams();
                System.out.println("MediaServer: Client connection caught and initialized. Client: " + connectedSocket);
                System.out.println("MediaServer: Connection with " + connectedSocket + " now listening for incoming packets.");
                HandShake();
            } catch (SocketTimeoutException e) {
            } catch (IOException e) {
                System.err.println("MediaServer: Error accepting a client. Connection refused and reset.");
                connectedSocket = null;
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e1) {
                }
            } catch (ConnectionInitializationException e) {
                System.err.println("MediaServer: " + e.getMessage() + " Connection refused and reset.");
            } catch (HandShakeException e) {
                System.err.println("MediaServer: " + e.getMessage());
            }

            startStreaming();

            System.out.println("MediaServer: Stream complete. Closing down server.");
            try {
                this.closeIOStreams();
            } catch (ConnectionException e) {
            }
            try {
                this.closeServer();
            } catch (IOException e) {
            }
        }
    }

    private void startStreaming() {
        // Assign input and output streams for music
        try {
            in = new FileInputStream(mediaFile);
            out = this.connectedSocket.getOutputStream();
        } catch (FileNotFoundException e) {
            System.err.println("MediaServer: Media file not found!");
        } catch (IOException e) {
            System.err.println("MediaServer: Unable to read media file!");
        }

        byte buffer[] = new byte[2048];
        int count;
        try {
            while ((count = in.read(buffer)) != -1)
                out.write(buffer, 0, count);
        } catch (IOException e) {
            System.err.println("MediaServer: Error streaming media to Client. Details: ");
            e.printStackTrace();
        }
    }
}
