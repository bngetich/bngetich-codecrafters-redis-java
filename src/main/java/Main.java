import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    // Simple handler for client connections
    static class ClientHandler extends Thread {
        private final Socket clientSocket;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        public void run() {
            try {
                InputStream serverInput = clientSocket.getInputStream();
                OutputStream serverOutput = clientSocket.getOutputStream();
                byte[] buffer = new byte[1024];

                while (true) {
                    int bytesRead = serverInput.read(buffer);
                    if (bytesRead == -1) {
                        System.out.println("Client disconnected.");
                        break;
                    }

                    String input = new String(buffer, 0, bytesRead).trim();
                    System.out.println("Received from client: " + input);

                    serverOutput.write("+PONG\r\n".getBytes());
                    serverOutput.flush();
                }

                clientSocket.close();
            } catch (IOException e) {
                System.out.println("IOException in client handler: " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        System.out.println("Server starting...");

        int port = 6379;

        try (ServerSocket listeningSocket = new ServerSocket(port)) {
            listeningSocket.setReuseAddress(true);
            System.out.println("Waiting for clients to connect on port " + port + "...");
            
            while (true) {
                Socket clientSocket = listeningSocket.accept();
                System.out.println("New client connected!");
                new ClientHandler(clientSocket).start();
            }
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }
    }
}