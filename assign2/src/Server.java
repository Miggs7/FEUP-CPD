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
    private List<Player> connectedPlayers;

    private ExecutorService threadPool;

    private Queue<Player> waitingPlayers;
    private Queue<Player> rankedWaitingPlayers;

    private List<GameSession> gameSessions;
    private List<GameSession> rankedGameSessions;








/* 
    public static String sessionWord = "";
    public static List<Character> sessionGuessedLetters = new ArrayList<>();
    public static Map<String,SocketChannel> sessionConnectedClients;
    public static List<Player> players = new ArrayList<>();
    public static int sessionRemainingAttempts = 0;
    public static boolean gameSessionOver = false;
    */

    public Server(int port) throws IOException {
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(port));
        selector = Selector.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        threadPool = Executors.newFixedThreadPool(10);
        connectedPlayers = new ArrayList<>();
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
            bw.write(username + " " + password + " " + token + "1" + "0");
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

        for (Player player : connectedPlayers) {
            if (player.getName().equals(username)) {
                return "User already logged in.";
            }
        }
        Player connected = new Player(username, clientChannel);
        connectedPlayers.add(connected);
        return "Authentication successful.";
    }

    private String match(String username, String matchType, String token, SocketChannel clientChannel) {
        String response = "";

        Map<String, List<String>> users = obtainInfo();

        if (!users.containsKey(username)) {
            return "Username does not exist.";
        }

        List<String> info = users.get(username);
        if (!info.get(1).equals(token)) {
            return "Incorrect token.";
        }
        Player player = null;
        for (Player p : connectedPlayers) {
            if (p.getSocketChannel().equals(clientChannel)) {
                player = p;
                break;
            }
        }
        switch (matchType) {
            case ("ranked"): {
                // obtain the corresponding player object by socket channel
                rankedWaitingPlayers.add(player);
                response = "Added to ranked waiting queue.";
                break;
            }
            case ("simple"): {
                // add the player to the unranked waiting queue
                waitingPlayers.add(player);
                response = "Added to simple waiting queue.";
                break;
            }
        }
        return response;
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

            switch (command){
                // Handle authentication
                case ("authenticate"): {
                    String username = fields[1];
                    String password = fields[2];
                    String token = fields[3];
                    response = authenticate(username, password, token, socketChannel);
                    break;
                }
                // Handle registration
                case ("register"): {
                    String username = fields[1];
                    String password = fields[2];
                    response = register(username, password);
                    break;
                }
                // Handle match request
                case ("match"): {
                    String username = fields[1];
                    String matchType = fields[2];
                    String token = fields[3];
                    response = match(username, matchType, token, socketChannel);
                    break;
                }
                // default
                default: {
                    response = "Invalid command.";
                    break;
                }
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
            for (Player player : connectedPlayers) {
                if (player.getSocketChannel().equals(socketChannel)) {
                    connectedPlayers.remove(player);
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

                    // threadPool should be used here
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

        for (Player player : connectedPlayers) {
            player.getSocketChannel().close();
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
                String level = fields[3];
                String exp = fields[4];

                //user info is a list that contains password and token
                List<String> userInfo = new ArrayList<String>();
                userInfo.add(password_save);
                userInfo.add(token_save);
                userInfo.add(level);
                userInfo.add(exp);

                users.put(username_save, userInfo);
            }
            br.close();
        } catch (IOException e) {
            System.out.println("Error reading server.txt");
        }
        return users;
    }
    /* 
    public static void gameHandler(){
          sessionWord = GameSession.loadWord();
          sessionGuessedLetters = GameSession.loadGuessedLetters();
          sessionConnectedClients = GameSession.loadClients();
          players = GameSession.loadPlayers();
          sessionRemainingAttempts = GameSession.loadRemainingAttempts();
          gameSessionOver = GameSession.isGameOver();

          //runGame();
    }*/
}
