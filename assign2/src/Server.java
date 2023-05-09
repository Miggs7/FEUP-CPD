import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;
import java.util.concurrent.*;


public class Server {
   
    private ServerSocketChannel serverSocketChannel;
    private Selector selector;
    private Map<String, String> users = new HashMap<String, String>();
    private List<SocketChannel> connectedClients;

    private ExecutorService threadPool;
    
    public Server(int port) throws IOException {
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(port));
        selector = Selector.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        threadPool = Executors.newFixedThreadPool(10);
        connectedClients = new ArrayList<SocketChannel>();
    }

    public String register(String username, String password) {
        if (users.containsKey(username)) {
            return "Username already exists.";
        }
        return "Registration successful.";
    }

    public String authenticate(String username, String password, SocketChannel clientChannel) {
        if (!users.containsKey(username) || !users.get(username).equals(password)) {
            return "Invalid username or password.";
        }

        connectedClients.add(clientChannel);
        return "Authentication successful.";
    }

    private String handleRequest(SocketChannel socketChannel) {
        String response = "";
        try {
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            int bytesRead = socketChannel.read(buffer);
            if (bytesRead == -1) {
                throw new IOException("Client has disconnected.");
            }

            // Read
            buffer.flip();

            // Print
            System.out.println("Received message from client: " + new String(buffer.array(), 0, buffer.limit()));
            String[] fields = new String(buffer.array(), 0, buffer.limit()).split(":");
            String command = fields[0];

            // Handle authentication
            if (command.equals("authenticate")) {
                String username = fields[1];
                String password = fields[2];

                response = authenticate(username, password, socketChannel);
            }

            // Handle registration
            else if (command.equals("register")) {
                String username = fields[1];
                String password = fields[2];

                response = register(username, password);
            }

            // Handle other requests
            else {
                response = "Invalid command.";
            }
            

        } catch (IOException e) {
            System.err.println("Client has disconnected abruptly: " + e.getMessage());
            try {
                socketChannel.close();
            } catch (IOException ex) {
                System.err.println("Error closing client socket: " + ex.getMessage());
            }
        }
        
        return response;
       
    }

    public void start() throws IOException {
        System.out.println("Server started.");

        // authenticate or register
        while (true) {
            selector.select();

            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> iter = selectedKeys.iterator();

            while (iter.hasNext()) {
                SelectionKey key = iter.next();

                if (key.isAcceptable()) {
                    SocketChannel clientChannel = serverSocketChannel.accept();
                    System.out.println("New client connected: " + clientChannel.getRemoteAddress());
                    clientChannel.configureBlocking(false);
                    clientChannel.register(selector, SelectionKey.OP_READ);
                } else if (key.isReadable()) {
                    SocketChannel clientChannel = (SocketChannel) key.channel();
                    System.out.println("New message from client: " + clientChannel.getRemoteAddress());
                    String response = handleRequest(clientChannel);
                    ByteBuffer buffer = ByteBuffer.wrap(response.getBytes());
                    clientChannel.write(buffer);
                }
                iter.remove();
            }
        }

        // game?

    }

    public void stop() throws IOException {
        threadPool.shutdown();
        serverSocketChannel.close();

        for (SocketChannel clientChannel : connectedClients) {
            clientChannel.close();
        }
    }

    public static void main(String[] args) {
        try {
            int port = 5000;
            Server server = new Server(port);
            server.register("user1", "password1");
            server.register("user2", "password2");
            server.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
