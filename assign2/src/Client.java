import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

public class Client {

    private SocketChannel socketChannel;
    private boolean authenticated = false;

    public Client(String host, int port) throws IOException {
        socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);
        socketChannel.connect(new InetSocketAddress(host, port));
    }

    public void start() throws IOException {
        System.out.println("Connected to server.");

        while (!socketChannel.finishConnect()) {
            // wait for connection to be established
        }

        // authentication
        Scanner scanner = new Scanner(System.in);
        String username = null;
        String password = null;


        do {
            System.out.print("Enter 'register' to register or 'authenticate' to authenticate: ");
            String command = scanner.nextLine();

            if (command.equals("register")) {
                System.out.print("Enter username: ");
                username = scanner.nextLine();
                System.out.print("Enter password: ");
                password = scanner.nextLine();

                // Send the registration request to the server
                String message = "register:" + username + ":" + password;
                ByteBuffer buffer = ByteBuffer.wrap(message.getBytes());
                socketChannel.write(buffer);

                // Wait for the response from the server

                buffer.clear();
                socketChannel.read(buffer);

                byte[] bytes = new byte[buffer.position()];
                buffer.flip();
                buffer.get(bytes);
                String response = new String(bytes);
                System.out.println(response);
            } else if (command.equals("authenticate")) {
                System.out.print("Enter username: ");
                username = scanner.nextLine();
                System.out.print("Enter password: ");
                password = scanner.nextLine();

                // Send the authentication request to the server
                String message = "authenticate:" + username + ":" + password;
                ByteBuffer buffer = ByteBuffer.wrap(message.getBytes());
                socketChannel.write(buffer);

                // Wait for the response from the server

                buffer.clear();
                socketChannel.read(buffer);

                byte[] bytes = new byte[buffer.position()];
                buffer.flip();
                buffer.get(bytes);
                String response = new String(bytes);
                System.out.println(response);

                if (response.equals("Authentication successful.")) {
                    authenticated = true;
                }
            } else {
                System.out.println("Invalid command.");
            }
        } while (!authenticated);

        // game

        while (true) {
            System.out.println("Waiting for game to start...");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // disconnect
        // socketChannel.close();
        // System.out.println("Disconnected from server.");
    }

    public static void main(String[] args) throws IOException {
        String host = "localhost";
        int port = 5000;
        Client client = new Client(host, port);
        client.start();
    }
}
