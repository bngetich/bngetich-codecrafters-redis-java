import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class RedisTestClient {
    public static void main(String[] args) throws Exception {
        try (Socket socket = new Socket("localhost", 6379)) {
            OutputStream out = socket.getOutputStream();
            InputStream in = socket.getInputStream();

            for (int i = 1; i <= 3; i++) {
                // Send a PING command (as plain text)
                out.write("PING\r\n".getBytes());
                out.flush();

                // Read the response
                byte[] buffer = new byte[1024];
                int bytesRead = in.read(buffer);
                String response = new String(buffer, 0, bytesRead);
                System.out.println("Response #" + i + " from server: " + response);
                Thread.sleep(500); // Optional: wait a bit between commands
            }
        }
    }
}
