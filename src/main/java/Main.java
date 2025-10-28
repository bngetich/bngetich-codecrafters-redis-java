import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
  public static void main(String[] args) {
    // You can use print statements as follows for debugging, they'll be visible
    // when running tests.
    System.out.println("Logs from your program will appear here!");

    ServerSocket serverSocket = null;
    Socket clientSocket = null;
    int port = 6379;
    try {
      serverSocket = new ServerSocket(port);
      // Since the tester restarts your program quite often, setting SO_REUSEADDR
      // ensures that we don't run into 'Address already in use' errors
      serverSocket.setReuseAddress(true);
      // Wait for connection from client.
      clientSocket = serverSocket.accept();
      System.out.println("Client connected!");

      InputStream in = clientSocket.getInputStream();
      OutputStream out = clientSocket.getOutputStream();

      byte[] buffer = new byte[1024];

      while (true) {
        // Keep the server running to accept multiple commands.
        int bytesRead = in.read();

        if(bytesRead == -1){
          break;
        }
        String received = new String(buffer, 0, bytesRead);
        if(received.contains("PING")){
           out.write("+PONG\r\n".getBytes());
           out.flush();
        }      
      }
    } catch (IOException e) {
      System.out.println("IOException: " + e.getMessage());
    } finally {
      try {
        if (clientSocket != null) {
          clientSocket.close();
        }
      } catch (IOException e) {
        System.out.println("IOException: " + e.getMessage());
      }
    }
  }
}
