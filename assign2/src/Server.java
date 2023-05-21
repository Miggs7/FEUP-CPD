import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.channels.SelectionKey;
import java.util.*;

import java.security.*;

public class Server {
   
    private static final int MMR_THRESHOLD = 0;
    private static final long maxWaitingTime = 10000;

    private ServerSocketChannel serverSocketChannel;
    private static Selector selector;
    private List<Player> connectedPlayers;

    private Queue<Player> waitingPlayers;
    private Queue<Player> rankedWaitingPlayers;

    private final int playerPerGame = 2;

    private List<Map<Player, Hangman>> matches;

    // fault tolerance
    private List<Player> disconnectedPlayers;

    public Server(int port) throws IOException {
        this.serverSocketChannel = ServerSocketChannel.open();
        this.serverSocketChannel.bind(new InetSocketAddress(port));
        Server.selector = Selector.open();
        this.serverSocketChannel.configureBlocking(false);
        this.serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        this.connectedPlayers = new ArrayList<>();
        this.waitingPlayers = new LinkedList<>();
        this.rankedWaitingPlayers = new LinkedList<>();
        this.matches = new ArrayList<>();
        this.disconnectedPlayers = new ArrayList<>();
    }

    public static Selector getSelector(){
        return selector;
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
            bw.write(username + " " + password + " " + token + " " + "0");
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

        // check if there is any player with the same username in the disconnectedPlayers list
        // if the player was in a queue before disconnecting, add him back to the queue in the same position

        // client side exige dar skip a parte de escolher o tipo de jogo


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

        for (String inf : info) {
            System.out.println(inf);
        }

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
            case ("1"): {
                // check if the player is already in the queue
                for (Player p : waitingPlayers) {
                    if (p.getName().equals(username)) {
                        return "Already in queue.";
                    }
                }
                response = "Added to waiting queue.";
                break;
            }
            case ("2"): {
                // check if the player is already in the queue
                for (Player p : rankedWaitingPlayers) {
                    if (p.getName().equals(username)) {
                        return "Already in queue.";
                    }
                }
                response = "Added to waiting queue.";
                break;
            }
            default: {
                response = "Invalid match type.";
                break;
            }
        }
        player.setJoinTime(System.currentTimeMillis());
        addPlayerToQueue(player, Integer.parseInt(matchType) - 1);
        return response;
    }

    private String processGuess(String username, String guess, String token) {
        StringBuilder responseBuilder = new StringBuilder();

        Player player = findPlayer(username, token);
        if (player == null) {
            return "Invalid player.";
        }

        // find in matches the game that the player is in
        Hangman game = null;
        for (Map<Player, Hangman> match : matches) {
            if (match.containsKey(player)) {
                game = match.get(player);
                break;
            }
        }

        if (game == null) {
            return "Player is not in a game.";
        }

        // check if the letter is repeated
        if (game.getGuessedLetters().contains(guess.charAt(0))) {
            responseBuilder.append("Letter already guessed.")
                .append("\nTry to guess the word: " + game.getMaskedWord())
                .append("\nYou have " + game.getRemainingAttempts() + " attempts left.");
            return responseBuilder.toString();
        }

        if (game.makeGuess(guess)) {
            responseBuilder.append("Correct guess. ");
        } else {
            responseBuilder.append("Incorrect guess. ");
        }

        if (!game.isGameOver()) {
            responseBuilder.append("Try to guess the word: " + game.getMaskedWord())
                .append("\nYou have " + game.getRemainingAttempts() + " attempts left.");
        } else {
            if (game.isWordGuessed()) {
                responseBuilder.append("Congratulations! You guessed the word: " + game.getWord());
            } else {
                responseBuilder.append("You lost. The word was: " + game.getWord());
            }
        }
        return responseBuilder.toString();
    }

    private Player findPlayer(String username, String token) {
        Map<String, List<String>> users = obtainInfo();

        if (!users.containsKey(username)) {
            return null;
        }

        List<String> info = users.get(username);

        for (String inf : info) {
            System.out.println(inf);
        }

        if (!info.get(1).equals(token)) {
            return null;
        }
        Player player = null;
        for (Player p : connectedPlayers) {
            if (p.getName().equals(username)) {
                player = p;
                break;
            }
        }
        return player;
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
                case ("guess"): {
                    String username = fields[1];
                    String guess = fields[2];
                    String token = fields[3];

                    //process guess and masked word
                    response = processGuess(username, guess, token);

                    System.out.println("received guess:" + username + ":" + guess + ":" + token);
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

                // Add disconnected player to the list for fault tolerance
                Player disconnectedPlayer = findPlayerByChannel(socketChannel);
                if (disconnectedPlayer != null) {
                    disconnectedPlayers.add(disconnectedPlayer);
                    connectedPlayers.remove(disconnectedPlayer);
                }
                
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

    private Player findPlayerByChannel(SocketChannel channel) {
        for (Player player : connectedPlayers) {
            if (player.getSocketChannel().equals(channel)) {
                return player;
            }
        }
        return null;
    }

    private void checkDisconnectedPlayers() {
        Iterator<Player> iterator = disconnectedPlayers.iterator();
        while (iterator.hasNext()) {
            Player player = iterator.next();
            long lastActiveTime = player.getJoinTime();
            long currentTime = System.currentTimeMillis();

            if (currentTime - lastActiveTime > 60000) {
                iterator.remove();
                System.out.println("Player " + player.getName() + " removed due to inactivity.");
            }
        }
    }

    public void start() throws IOException {
        System.out.println("Server started.");

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

            if (waitingPlayers.size() >= this.playerPerGame) {
                System.out.println("There are enough players to build a match.");
                buildMatch(0);
            }
            
            if (rankedWaitingPlayers.size() >= this.playerPerGame) {
                System.out.println("There are enough players to build a match.");
                buildMatch(1);
            }

            if (matches.size() > 0) {
                checkMatches();
            }
        }
    }

    private void checkMatches() {
        Iterator<Map<Player, Hangman>> iterator = matches.iterator();
        while (iterator.hasNext()) {
            Map<Player, Hangman> match = iterator.next();

            boolean matchFinished = true;
            for (Map.Entry<Player, Hangman> entry : match.entrySet()) {
                Hangman hangman = entry.getValue();
                if (!hangman.isGameOver()) {
                    matchFinished = false;
                    break;
                }
            }

            if (matchFinished) {
                System.out.println("Match finished.");
                for (Map.Entry<Player, Hangman> entry : match.entrySet()) {
                    Player player = entry.getKey();
                    Hangman hangman = entry.getValue();
                    if (hangman.getType().equals("Ranked")) {
                        if (hangman.isWordGuessed()) {
                            player.sendMsg("You gained 2 points.");
                            player.addToMmr();
                        } else {
                            player.sendMsg("You lost 1 point.");
                            player.subtractToMmr();
                        }
                        updateInfo(player);
                    } else {
                        if (hangman.isWordGuessed()) {
                            player.sendMsg("You won!");
                        } else {
                            player.sendMsg("You lost!");
                        }
                    }
                }
                iterator.remove(); // Remove the match using the iterator's remove() method
            }
        }
    }

    private void updateInfo(Player player) {
        System.out.println("Updating info for player: " + player.getName() + "...");
        Map<String, List<String>> users = obtainInfo();

        List<String> info = users.get(player.getName());
        info.set(2, Integer.toString(player.getMmr()));

        try {
            File file = new File("server.txt");
            FileWriter fw = new FileWriter(file, false);
            BufferedWriter bw = new BufferedWriter(fw);

            for (Map.Entry<String, List<String>> entry : users.entrySet()) {
                String username = entry.getKey();
                List<String> data = entry.getValue();
                String line = username + " " + data.get(0) + " " + data.get(1) + " " + data.get(2);
                bw.write(line);
                bw.newLine();
            }

            bw.close();
            fw.close();

        } catch (IOException e) {
            System.out.println("Error updating info.");
        }
    }

    public void stop() throws IOException {
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
                String mmr = fields[3];

                //user info is a list that contains password and token
                List<String> userInfo = new ArrayList<String>();
                userInfo.add(password_save);
                userInfo.add(token_save);
                userInfo.add(mmr);

                users.put(username_save, userInfo);
            }
            br.close();
        } catch (IOException e) {
            System.out.println("Error reading server.txt");
        }
        return users;
    }

    public synchronized void addPlayerToQueue(Player player, int matchType) {
        if (matchType == 0) {
            waitingPlayers.add(player);
            System.out.println("Player " + player.getName() + " added to simple match queue.");
        } else {
            rankedWaitingPlayers.add(player);
            System.out.println("Player " + player.getName() + " added to ranked match queue.");
        }
    }

    //tries to build a match
    private synchronized void buildMatch(int matchType){
        String matchTypeStr = matchType==0 ? "simple" : "ranked";
        System.out.println("Building match for type " + matchTypeStr);

        Queue<Player> queue = matchType==0 ? waitingPlayers : rankedWaitingPlayers;

        Player player1 = findPlayerWithSimilarMMR(queue, null, maxWaitingTime);
        Player player2 = findPlayerWithSimilarMMR(queue, player1, maxWaitingTime);
    
        if (player1 != null && player2 != null) {
            queue.remove(player1);
            queue.remove(player2);
    
            Hangman game = new Hangman(matchType);
    
            // Create a copy of the game for each player
            Hangman game1 = game.clone();
            Hangman game2 = game.clone();
    
            Map<Player, Hangman> match = new HashMap<>();
            match.put(player1, game1);
            match.put(player2, game2);
    
            matches.add(match);
            player1.sendMsg("Game starting.");
            player2.sendMsg("Game starting.");
    
            StringBuilder msgBuilder = new StringBuilder();
            msgBuilder.append("Try to guess the word: " + game.getMaskedWord())
                    .append("\nYou have " + game.getRemainingAttempts() + " attempts left.");
            player1.sendMsg(msgBuilder.toString());
            player2.sendMsg(msgBuilder.toString());
        }
    }

    private Player findPlayerWithSimilarMMR(Queue<Player> queue, Player excludedPlayer, long maxWaitingTime) {
        Player foundPlayer = null;
    
        for (Player player : queue) {
            if (player != excludedPlayer && !player.hasExceededMaxWaitingTime(maxWaitingTime) && (foundPlayer == null || Math.abs(player.getMmr() - foundPlayer.getMmr()) < MMR_THRESHOLD)) {
                foundPlayer = player;
                if (Math.abs(player.getMmr() - foundPlayer.getMmr()) == 0) {
                    // Found a player with exact MMR match, no need to continue searching
                    System.out.println("Found player with exact MMR match.");
                    break;
                }
                System.out.println("Found player with similar MMR.");
            }
        }
    
        return foundPlayer;
    }








}

/**
 * func encontraPlayer(player1,player2){
 *      int tempmmr = player2.mmr;
 *      if(player1.mmr == tempmmr){
 *          match();
 *         }
 *      else{
 *          tempmmr--;
 *      }
 * }
 */