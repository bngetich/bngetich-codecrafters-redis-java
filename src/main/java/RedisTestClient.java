import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class RedisTestClient {
    static class TestClient extends Thread {
        private final int clientId;
        
        public TestClient(int id) {
            this.clientId = id;
        }
        
        public void run() {
            try (Socket socket = new Socket("localhost", 6379)) {
                OutputStream out = socket.getOutputStream();
                InputStream in = socket.getInputStream();

                for (int i = 1; i <= 3; i++) {
                    out.write("PING\r\n".getBytes());
                    out.flush();

                    byte[] buffer = new byte[1024];
                    int bytesRead = in.read(buffer);
                    String response = new String(buffer, 0, bytesRead);
                    System.out.println("Client " + clientId + " - Response #" + i + ": " + response);
                    Thread.sleep(500);
                }
            } catch (Exception e) {
                System.out.println("Error in client " + clientId + ": " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) throws Exception {
        // Create and start 3 concurrent clients
        for (int i = 1; i <= 3; i++) {
            new TestClient(i).start();
            Thread.sleep(100); // Small delay between starting clients
        }
    }
}
