import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;
import java.util.concurrent.*;
import java.security.*;


public class Server {
   
    private ServerSocketChannel serverSocketChannel;
    private Selector selector;
    private Map<String, SocketChannel> connectedClients;

    private ExecutorService threadPool;

    public static String sessionWord = "";
    public static List<Character> sessionGuessedLetters = new ArrayList<>();
    public static Map<String,SocketChannel> sessionConnectedClients;
    public static List<Player> players;
    public static int sessionRemainingAttempts = 0;
    public static boolean gameSessionOver = false;
    
    public Server(int port) throws IOException {
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(port));
        selector = Selector.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        threadPool = Executors.newFixedThreadPool(10);
        connectedClients = new HashMap<String, SocketChannel>();

    }



    public String register(String username, String password) {
        // when registering, open the server.txt and retrieve the usernames, passwords and tokens
        Map<String, List<String>> users = obtainInfo();

        if (users.containsKey(username)) {
            return "Username already exists.";
        }

        SecureRandom random = new SecureRandom();
        byte bytes[] = new byte[20];
        random.nextBytes(bytes);
        String token = bytes.toString();

        try {
            File file = new File("server.txt");
            FileWriter fw = new FileWriter(file, true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(username + " " + password + " " + token);
            bw.newLine();
            bw.close();
        } catch (IOException e) {
            System.out.println("Error writing to server.txt");
        }
        
        return "Token:" + token;
    }

    public String authenticate(String username, String password, String token, SocketChannel clientChannel) {
        Map<String, List<String>> users = obtainInfo();

        if (!users.containsKey(username)) {
            return "Username does not exist.";
        }

        List<String> info = users.get(username);
        if (!info.get(0).equals(password)) {
            return "Incorrect password.";
        }

        if (!info.get(1).equals(token)) {
            return "Incorrect token.";
        }

        if (connectedClients.containsKey(username)) {
            return "User already logged in.";
        }

        connectedClients.put(username, clientChannel);

        Player connected = new Player(username, connectedClients.size() + 1); 
        GameSession.connectedPlayers.add(connected);

        GameSession.savePlayers(connectedClients,GameSession.connectedPlayers);
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
                String token = fields[3];
                
                response = authenticate(username, password, token, socketChannel);
            }

            // Handle registration
            else if (command.equals("register")) {
                String username = fields[1];
                String password = fields[2];

                response = register(username, password);
                System.out.println("Register response:" + response);
            }

            // Handle other requests
            else {
                response = "Invalid command.";
            }
            
        } catch (IOException e) {
            try{
                SelectionKey key = socketChannel.keyFor(selector);
                System.out.println("Client disconnected: " + socketChannel.getRemoteAddress());
                key.cancel();
                socketChannel.close();
                
            } catch (IOException ex) {
                System.out.println("Error closing socket channel.");
            }
            // remove the client from the connected clients
            for (Map.Entry<String, SocketChannel> entry : connectedClients.entrySet()) {
                if (entry.getValue().equals(socketChannel)) {
                    connectedClients.remove(entry.getKey());
                    break;
                }
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

                if (!key.isValid()) {
                    continue;
                }

                if (key.isAcceptable()) {
                    SocketChannel clientChannel = serverSocketChannel.accept();
                    System.out.println("New client connected: " + clientChannel.getRemoteAddress());
                    clientChannel.configureBlocking(false);
                    clientChannel.register(selector, SelectionKey.OP_READ);
                } else if (key.isReadable()) {
                    SocketChannel clientChannel = (SocketChannel) key.channel();
                    
                    System.out.println("New message from client: " + clientChannel.getRemoteAddress());
                    String response = handleRequest(clientChannel);
                    System.out.println("Sending message to client: " + response);
                    ByteBuffer buffer = ByteBuffer.wrap(response.getBytes());

                    if (!clientChannel.isConnected()) {
                        System.out.println("Client is disconnected.");
                        continue;
                    }
                    clientChannel.write(buffer);
                }
                iter.remove();
            }
        }
    }

    public void stop() throws IOException {
        threadPool.shutdown();
        serverSocketChannel.close();

        for (SocketChannel clientChannel : connectedClients.values()) {
            clientChannel.close();
        }
    }

    public static void main(String[] args) {
        try {
            int port = 5000;
            Server server = new Server(port);
            server.start();

        } catch (Exception e) {
            System.err.println("Error server: " + e.getMessage());
            e.printStackTrace();
        }
        
    }

    Map<String, List<String>> obtainInfo() {
        Map<String, List<String>> users = new HashMap<String, List<String>>();
        try {
            File file = new File("server.txt");
            FileInputStream fis = new FileInputStream(file);
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));

            String line = null;
            while ((line = br.readLine()) != null) {
                String[] fields = line.split(" ");
                String username_save = fields[0];
                String password_save = fields[1];
                String token_save = fields[2];

                //user info is a list that contains password and token
                List<String> userInfo = new ArrayList<String>();
                userInfo.add(password_save);
                userInfo.add(token_save);

                users.put(username_save, userInfo);
            }
            br.close();
        } catch (IOException e) {
            System.out.println("Error reading server.txt");
        }
        return users;
    }

    public static void gameHandler(){
          sessionWord = GameSession.loadWord();
          sessionGuessedLetters = GameSession.loadGuessedLetters();
          sessionConnectedClients = GameSession.loadClients();
          players = GameSession.loadPlayers();
          sessionRemainingAttempts = GameSession.loadRemainingAttempts();
          gameSessionOver = GameSession.isGameOver();

          //runGame();
    }   
}
