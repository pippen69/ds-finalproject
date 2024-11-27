import java.io.*;
import java.net.*;
import java.util.Scanner;

public class FileClient {

    private static final int PORT = 1099; // Port to listen for incoming connections

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Start the peer server in a new thread to listen for incoming requests
        new Thread(new PeerServer()).start();

        // Peer can also request files from other peers
        while (true) {
            System.out.println("Enter a command:");
            System.out.println("1. Request file from peer");
            System.out.println("2. Exit");

            int choice = scanner.nextInt();
            scanner.nextLine();  // consume newline

            if (choice == 1) {
                System.out.print("Enter the IP address of the peer: ");
                String peerIP = scanner.nextLine();
                System.out.print("Enter the port of the peer: ");
                int peerPort = scanner.nextInt();
                scanner.nextLine();  // consume newline
                System.out.print("Enter the file name to download: ");
                String fileName = scanner.nextLine();

                // Request file from the other peer
                new Thread(new PeerClient(peerIP, peerPort, fileName)).start();
            } else {
                break;
            }
        }

        scanner.close();
    }

    // PeerServer listens for incoming file requests and sends the requested file
    static class PeerServer implements Runnable {
        @Override
        public void run() {
            try (ServerSocket serverSocket = new ServerSocket(PORT)) {
                // Get and print the local IP address of the server
                InetAddress localHost = InetAddress.getLocalHost();
                System.out.println("Peer server started on IP: " + localHost.getHostAddress() + " and listening on port " + PORT);

                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Connection established with " + clientSocket.getInetAddress());
                    new Thread(new FileTransferHandler(clientSocket)).start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // PeerClient connects to another peer and requests a file
    static class PeerClient implements Runnable {
        private final String peerIP;
        private final int peerPort;
        private final String fileName;

        public PeerClient(String peerIP, int peerPort, String fileName) {
            this.peerIP = peerIP;
            this.peerPort = peerPort;
            this.fileName = fileName;
        }

        @Override
        public void run() {
            try (Socket socket = new Socket(peerIP, peerPort)) {
                // Get and print the local IP address of the client
                InetAddress localHost = InetAddress.getLocalHost();
                System.out.println("Client (local IP: " + localHost.getHostAddress() + ") connected to peer: " + peerIP);

                // Send the file name to the peer
                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
                writer.println(fileName);

                // Receive the file from the peer
                InputStream inputStream = socket.getInputStream();
                FileOutputStream fileOutputStream = new FileOutputStream("downloaded_" + fileName);

                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    fileOutputStream.write(buffer, 0, bytesRead);
                }

                System.out.println("File received and saved as downloaded_" + fileName);
                fileOutputStream.close();
            } catch (IOException e) {
                System.err.println("Error while requesting file: " + e.getMessage());
            }
        }
    }

    // FileTransferHandler handles the file transfer to another peer
    static class FileTransferHandler implements Runnable {
        private final Socket clientSocket;

        public FileTransferHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                 OutputStream outputStream = clientSocket.getOutputStream()) {

                // Read the file name that the client requested
                String requestedFileName = reader.readLine();
                System.out.println("Requested file: " + requestedFileName);

                File file = new File(requestedFileName);
                if (file.exists() && !file.isDirectory()) {
                    // Send the file to the requesting peer
                    System.out.println("Sending file to peer...");
                    try (FileInputStream fileInputStream = new FileInputStream(file)) {
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }
                    }
                    System.out.println("File sent successfully!");
                } else {
                    // Send error message if file is not found
                    String errorMessage = "File not found: " + requestedFileName;
                    outputStream.write(errorMessage.getBytes());
                    System.out.println(errorMessage);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
