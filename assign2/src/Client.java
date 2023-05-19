import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.channels.SelectionKey;
import java.util.*;
import java.util.concurrent.*;


public class Client {
    private SocketChannel socketChannel;
    private Selector selector;
    private boolean authenticated = false;
    private String username;

    public Client(String host, int port) throws IOException {
        socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);
        socketChannel.connect(new InetSocketAddress(host, port));
        selector = Selector.open();
        socketChannel.register(selector, SelectionKey.OP_READ);
    }

    public void start() throws IOException {
        Boolean playing = false, waiting = true;

        System.out.println("Connected to server.");
        Scanner scanner = new Scanner(System.in);
        while (!socketChannel.finishConnect()) {
            // wait for connection to be established
        }

        while (!authenticated) {
            // authentication
            
            String username = null;
            String password = null;
            String token = null;

            System.out.print("Enter 'register' to register or 'authenticate' to authenticate: ");
            String command = scanner.nextLine();

            // Validate the command
            switch (command) {
                case "register":
                case "authenticate":
                    System.out.println("Valid command. Processing");
                    break;
                default:
                    System.out.println("Invalid command.");
                    continue;
            }

            System.out.print("Enter username: ");
            username = scanner.nextLine();
            System.out.print("Enter password: ");
            password = scanner.nextLine();
            
            if (command.equals("authenticate")) {
                token = obtainToken(username);
            }

            // Send the message to the server
            switch (command) {
                case "register":
                    String message = command + ":" + username + ":" + password;
                    System.out.println("Sending message to server: " + message);
                    ByteBuffer buffer = ByteBuffer.wrap(message.getBytes());
                    socketChannel.write(buffer);
                    break;
                case "authenticate":
                    message = command + ":" + username + ":" + password + ":" + token;
                    System.out.println("Sending message to server: " + message);
                    buffer = ByteBuffer.wrap(message.getBytes());
                    socketChannel.write(buffer);
                    break;
            }

            // Read the response from the server
            int channels = selector.select();
                if (channels == 0) {
                    continue;
            }

            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

            //System.out.println(keyIterator);

            while (keyIterator.hasNext()) {
                SelectionKey key = keyIterator.next();
                String response = null;

                if (key.isReadable()) {
                    SocketChannel serverSocketChannel = (SocketChannel) key.channel();
                    ByteBuffer responseBuffer = ByteBuffer.allocate(1024);
                    int bytesRead = serverSocketChannel.read(responseBuffer);
                    if (bytesRead == -1) {
                        serverSocketChannel.close();
                        key.cancel();
                        continue;
                    }
                    response = new String(responseBuffer.array(), 0, bytesRead);
                    responseBuffer.clear();
                }
                keyIterator.remove();
                System.out.println("Received response: " + response);
                if (response.contains("Token")) {
                    System.out.println("Registration successful.");
                    saveToken(username, response);
                    break;
                }
                if (response.equals("Authentication successful.")) {
                    System.out.println("Authentication successful.");
                    authenticated = true;
                    this.username = username;
                    break;
                }
            }

        }

        // lobby

        while (authenticated && !playing) {
            System.out.println("Match Type:");
            System.out.println("1. Simple");
            System.out.println("2. Ranked");
            System.out.println("Please enter the number of the match type you would like to play: ");

            String matchType = scanner.nextLine();
            String token = obtainToken(username);

            String message = "match:" + username + ":" + matchType + ":" + token;
            System.out.println("Sending message to server: " + message);
            ByteBuffer buffer = ByteBuffer.wrap(message.getBytes());
            socketChannel.write(buffer);

            // Read the response from the server
            int channels = selector.select();
            if (channels == 0) {
                continue;
            }

            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

            while (keyIterator.hasNext()) {
                SelectionKey key = keyIterator.next();
                String response = null;

                if (key.isReadable()) {
                    SocketChannel serverSocketChannel = (SocketChannel) key.channel();
                    ByteBuffer responseBuffer = ByteBuffer.allocate(1024);
                    int bytesRead = serverSocketChannel.read(responseBuffer);
                    if (bytesRead == -1) {
                        serverSocketChannel.close();
                        key.cancel();
                        continue;
                    }
                    response = new String(responseBuffer.array(), 0, bytesRead);
                    System.out.println("Received response: " + response);
                    responseBuffer.clear();
                }
                keyIterator.remove();
                
                // response parsing
                if (response.equals("Added to waiting queue.")) {
                    waiting = true;
                    break;
                } else {
                    System.out.println("Invalid response from server.");
                    waiting = false;
                    continue;
                }
            }

            while (waiting) {
                // Always read from the server until the game starts
                channels = selector.select();
                if (channels == 0) {
                    continue;
                }

                selectedKeys = selector.selectedKeys();
                keyIterator = selectedKeys.iterator();

                while(keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    String response = null;

                    if (key.isReadable()) {
                        SocketChannel serverSocketChannel = (SocketChannel) key.channel();
                        ByteBuffer responseBuffer = ByteBuffer.allocate(1024);
                        int bytesRead = serverSocketChannel.read(responseBuffer);
                        if (bytesRead == -1) {
                            serverSocketChannel.close();
                            key.cancel();
                            continue;
                        }
                        response = new String(responseBuffer.array(), 0, bytesRead);
                        System.out.println("Received response: " + response);
                        responseBuffer.clear();
                    }
                    keyIterator.remove();

                    if (response.equals("Game starting.")) {
                        waiting = false;
                        playing = true;
                        break;
                    }
                }
            }
            buffer.clear();

            while (playing) {
                
                
            }
        }

        scanner.close();
    }

    public static void main(String[] args) {
        try{
            String host = "localhost";
            int port = 5000;
            Client client = new Client(host, port);
            client.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    String obtainToken(String username) {
        String token = null;

        try {
            File file = new File("local.txt");
            FileInputStream fis = new FileInputStream(file);
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));

            String line = null;
            while ((line = br.readLine()) != null) {
                String[] fields = line.split(" ");
                if (fields[0].equals(username)) {
                    token = fields[1];
                    break;
                }
            }

            br.close();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return token;
    }

    void saveToken(String username, String response) {
        String token = response.substring(6, response.length());
        try {
            File file = new File("local.txt");
            FileWriter fw = new FileWriter(file, true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(username + " " + token + " \n");
            bw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

