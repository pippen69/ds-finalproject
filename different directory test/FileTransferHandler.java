import java.io.*;
import java.net.*;

public class FileTransferHandler implements Runnable {

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
