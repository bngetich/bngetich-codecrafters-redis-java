import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
  public static void main(String[] args) {
    System.out.println("Server starting...");

    int port = 6379;
    if (args.length > 0) {
      try {
        port = Integer.parseInt(args[0]);
      } catch (NumberFormatException e) {
        System.out.println("Invalid port argument, using default 6379.");
      }
    }

    try (ServerSocket listeningSocket = new ServerSocket(port)) {
      listeningSocket.setReuseAddress(true);

      System.out.println("Waiting for a client to connect on port " + port + "...");
      
      // ✅ When a client connects, the server gets a new Socket to talk to that specific client
      Socket connectionToClient = listeningSocket.accept();
      System.out.println("Client connected!");

      // ✅ These are the server’s communication streams for this client
      InputStream serverInput = connectionToClient.getInputStream();   // Server listens 🔊
      OutputStream serverOutput = connectionToClient.getOutputStream(); // Server talks 🎤

      byte[] buffer = new byte[1024];

      while (true) {
        // ✅ Wait for client to send data (blocks until something arrives)
        int bytesRead = serverInput.read(buffer);

        // ✅ If client disconnects, input stream returns -1
        if (bytesRead == -1) {
          System.out.println("Client disconnected.");
          break;
        }

        // ✅ Convert only the received bytes to a string
        String input = new String(buffer, 0, bytesRead).trim();
        System.out.println("Received from client: " + input);

        // ✅ Respond with PONG to any command (PING or otherwise)
        serverOutput.write("+PONG\r\n".getBytes());
        serverOutput.flush(); // 🔥 Make sure the client receives it immediately
      }

      connectionToClient.close(); // ✅ Close connection when client is done
      System.out.println("Connection closed. Server shutting down.");

    } catch (IOException e) {
      System.out.println("IOException: " + e.getMessage());
    }
  }
}
