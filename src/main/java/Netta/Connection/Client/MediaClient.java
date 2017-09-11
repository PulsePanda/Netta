package Netta.Connection.Client;

import Netta.Exceptions.ConnectionException;
import Netta.Exceptions.ConnectionInitializationException;
import Netta.Exceptions.HandShakeException;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Austin on 2/20/2017.
 */
public class MediaClient extends ClientTemplate {

    private String mediaType;

    /**
     * Basic client setup. By default to initialize this object, simply create
     * an object of it, a thread containing that object, and start the thread.
     * From there the client will connect to the specified server, and open it's
     * IO streams with that server. From there, any time the server sends a
     * packet to the client, the packetReceived(Packet) method will be called. By
     * default, this method does nothing, so overload it yourself. This class
     * also does not handle closing connections itself, that usually will be
     * done through the packetReceived(Packet) method.
     *
     * @param serverIP  The IP address of the server to connect to
     * @param port      the port of the server to connect to
     * @param mediaType String defining whether the media is Music or a Movie
     * @throws NoSuchAlgorithmException when there is an issue creating the RSA keys.
     */
    public MediaClient(String serverIP, int port, String mediaType) throws NoSuchAlgorithmException {
        super(serverIP, port, null);
        this.mediaType = mediaType;
    }

    @Override
    public void run() {
        System.out.println("MediaClient: Initializing client...");

        try {
            connectedSocket = new Socket(serverIP, port);
            openIOStreams();
            System.out.println("MediaClient: Client connected to server. Server: " + connectedSocket);
        } catch (IOException e) {
            System.err.println(
                    "MediaClient: Unable to connect to server. Check your network connection and try again. Closing client.");
            return;
        } catch (ConnectionInitializationException e) {
            System.err.println("MediaClient: " + e.getMessage() + " Closing client.");
            return;
        }

        System.out.println("MediaClient: Listening for packets from the server.");

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

        if (mediaType.toLowerCase().equals("music"))
            playMusic();

        System.out.println("MediaClient: Closing down Media client.");
        try {
            closeIOStreams();
        } catch (ConnectionException e) {
        }
    }

    /**
     * Play Music. Streams selected music from Server connection
     */
    protected void playMusic() {
        try {
            InputStream in = new BufferedInputStream(this.connectedSocket.getInputStream());
            AudioInputStream ais = AudioSystem.getAudioInputStream(in);
            Clip clip = AudioSystem.getClip();
            clip.open(ais);
            clip.start();
            Thread.sleep(100); // given clip.drain a chance to start
            clip.drain();
        } catch (IOException e) {
            System.err.println("MediaClient: ");
            e.printStackTrace();
        } catch (InterruptedException e) {
            System.err.println("MediaClient: ");
            e.printStackTrace();
        } catch (LineUnavailableException e) {
            System.err.println("MediaClient: ");
            e.printStackTrace();
        } catch (UnsupportedAudioFileException e) {
            System.err.println("MediaClient: ");
            e.printStackTrace();
        }
    }
}
