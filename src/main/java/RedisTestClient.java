import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

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

                // Test PING command
                System.out.println("Client " + clientId + " - Testing PING:");
                for (int i = 1; i <= 2; i++) {
                    sendCommand(out, "PING");

                    byte[] buffer = new byte[1024];
                    int bytesRead = in.read(buffer);
                    String response = new String(buffer, 0, bytesRead);
                    System.out.println("  PING #" + i + ": " + response.trim());
                    Thread.sleep(300);
                }

                // Test ECHO command
                System.out.println("Client " + clientId + " - Testing ECHO:");
                String[] echoMessages = {
                    "Hello World",
                    "Redis Test " + clientId,
                    "Java 25 Rules!"
                };
                
                for (String msg : echoMessages) {
                    sendCommand(out, "ECHO", msg);

                    byte[] buffer = new byte[1024];
                    int bytesRead = in.read(buffer);
                    String response = new String(buffer, 0, bytesRead);
                    System.out.println("  ECHO '" + msg + "': " + response.trim());
                    Thread.sleep(300);
                }
            } catch (Exception e) {
                System.out.println("Error in client " + clientId + ": " + e.getMessage());
            }
        }

        // Send a RESP-formatted command
        private void sendCommand(OutputStream out, String... args) throws Exception {
            StringBuilder resp = new StringBuilder();
            resp.append("*").append(args.length).append("\r\n");
            for (String arg : args) {
                byte[] argBytes = arg.getBytes(StandardCharsets.UTF_8);
                resp.append("$").append(argBytes.length).append("\r\n");
                resp.append(arg).append("\r\n");
            }
            out.write(resp.toString().getBytes(StandardCharsets.UTF_8));
            out.flush();
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
