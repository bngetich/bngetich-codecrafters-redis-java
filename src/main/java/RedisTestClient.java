import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
                    String pong = readRespStringOrBulk(in);
                    reportEq("PING #" + i, "PONG", pong);
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
                    String echoed = readRespStringOrBulk(in);
                    reportEq("ECHO '" + msg + "'", msg, echoed);
                    Thread.sleep(300);
                }

                // Test SET/GET commands
                System.out.println("Client " + clientId + " - Testing SET/GET:");
                String key = "mykey-" + clientId;
                String val = "value-" + clientId;

                // SET key val -> +OK
                sendCommand(out, "SET", key, val);
                String ok = readRespStringOrBulk(in);
                reportEq("SET " + key, "OK", ok);

                // GET key -> $len val
                sendCommand(out, "GET", key);
                String got = readRespStringOrBulk(in);
                reportEq("GET " + key, val, got);

                // GET missing -> $-1
                sendCommand(out, "GET", "missing-" + clientId);
                String missing = readRespStringOrBulk(in);
                reportNull("GET missing", missing);
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

        // Minimal RESP reader for Simple Strings (+OK) and Bulk Strings ($len\r\n...)
        private String readRespStringOrBulk(InputStream in) throws IOException {
            int prefix = in.read();
            if (prefix == -1) return null;
            switch (prefix) {
                case '+': // Simple String
                    return readLine(in);
                case '-': { // Error
                    String err = readLine(in);
                    throw new IOException("RESP error: " + err);
                }
                case '$': { // Bulk String
                    String lenLine = readLine(in);
                    int len = Integer.parseInt(lenLine);
                    if (len == -1) return null; // Null bulk string
                    byte[] data = in.readNBytes(len);
                    // consume CRLF
                    in.read(); in.read();
                    return new String(data, StandardCharsets.UTF_8);
                }
                case ':': { // Integer (not used here)
                    return readLine(in);
                }
                default:
                    // Consume rest of line to keep stream in sync
                    readLine(in);
                    return null;
            }
        }

        private String readLine(InputStream in) throws IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int prev = -1, curr;
            while ((curr = in.read()) != -1) {
                if (prev == '\r' && curr == '\n') {
                    break;
                }
                if (curr != '\r') baos.write(curr);
                prev = curr;
            }
            return baos.toString(StandardCharsets.UTF_8);
        }

        private void reportEq(String label, String expected, String actual) {
            boolean ok = expected != null && expected.equals(actual);
            System.out.println("  " + label + " -> " + (ok ? "[PASS]" : "[FAIL]") +
                    " (expected='" + expected + "', actual='" + actual + "')");
        }

        private void reportNull(String label, String actual) {
            boolean ok = (actual == null);
            System.out.println("  " + label + " -> " + (ok ? "[PASS]" : "[FAIL]") +
                    " (expected=null, actual='" + actual + "')");
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
