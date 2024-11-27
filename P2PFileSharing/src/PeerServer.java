import java.io.*;
import java.net.*;

public class PeerServer implements Runnable {

    private static final int PORT = 1099; // Port to listen for incoming connections

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            // Get and print the local IP address of the server
            InetAddress localHost = InetAddress.getLocalHost();
            System.out.println("Peer server started on IP: " + localHost.getHostAddress() + " and listening on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Connection established with " + clientSocket.getInetAddress());
                // Handle the file transfer request in a new thread
                new Thread(new FileTransferHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // Start the server in a new thread so that the peer can listen for incoming file requests
        new Thread(new PeerServer()).start();
    }
}
