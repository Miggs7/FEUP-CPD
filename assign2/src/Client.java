import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

public class Client {

    private SocketChannel socketChannel;

    public Client(String host, int port) throws IOException {
        socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);
        socketChannel.connect(new InetSocketAddress(host, port));
    }

    public void start() throws IOException {
        System.out.println("Connected to server.");

        while (!socketChannel.finishConnect()) {
            // Wait for the connection to be established
        }

        Scanner scanner = new Scanner(System.in);

        while (true) {
            String message = scanner.nextLine();

            if (message.equals("exit")) {
                break;
            }

            ByteBuffer buffer = ByteBuffer.wrap(message.getBytes());
            socketChannel.write(buffer);

            buffer.clear();
            socketChannel.read(buffer);

            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            String response = new String(bytes);
            System.out.println("Server response: " + response);
        }

        socketChannel.close();
        System.out.println("Disconnected from server.");
    }

    public static void main(String[] args) throws IOException {
        String host = "localhost";
        int port = 8080;
        Client client = new Client(host, port);
        client.start();
    }
}
