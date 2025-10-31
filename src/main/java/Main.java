import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        int port = 6379;

        try (ServerSocket server = new ServerSocket(port)) {
            System.out.println("Server starting...");
            while (true) {
                Socket client = server.accept();
                System.out.println("New client connected!");
                new Thread(() -> handleClient(client)).start();
            }
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }
    }

    private static void handleClient(Socket client) {
        try {
            InputStream in = client.getInputStream();
            OutputStream out = client.getOutputStream();

            while (true) {
                List<String> cmd = parseRESP(in);
                if (cmd == null)
                    break;

                String op = cmd.get(0).toUpperCase();
                String response;

                switch (op) {
                    case "PING":
                        response = "+PONG\r\n";
                        break;
                    case "ECHO":
                        String msg = cmd.size() > 1 ? cmd.get(1) : "";
                        response = "$" + msg.length() + "\r\n" + msg + "\r\n";
                        break;
                    default:
                        response = "-ERR unknown command\r\n";
                }

                out.write(response.getBytes());
                out.flush();
            }
        } catch (IOException e) {
            System.out.println("Connection closed: " + e.getMessage());
        }
    }

    private static List<String> parseRESP(InputStream in) throws IOException {
        String first = readLine(in); // *2
        if (first == null)
            return null;
        if (first.charAt(0) != '*')
            throw new IOException("Invalid RESP header");

        int count = Integer.parseInt(first.substring(1));
        List<String> args = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            String bulkHead = readLine(in); // $5
            int len = Integer.parseInt(bulkHead.substring(1));
            byte[] buf = in.readNBytes(len);
            readLine(in);
            args.add(new String(buf, StandardCharsets.UTF_8));
        }

        return args;
    }

    private static String readLine(InputStream in) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        int prev = -1, curr;

        while ((curr = in.read()) != -1) {
            if (prev == '\r' && curr == '\n') {
                break;
            }
            if (curr != '\r')
                baos.write(curr);
            prev = curr;
        }
        if (baos.size() == 0 && curr == -1)
            return null;
        return baos.toString(StandardCharsets.UTF_8);
    }

}