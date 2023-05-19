import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

enum Status{
    READY,
    IN_PROGRESS,
    ENDED
}

public class GameSession {

    // session properties
    private Status status;
    private List<Player> connectedPlayers;
    private int capacity;
    private ExecutorService threadPool;
    private Selector selector;

    // game properties
    private Hangman game;
    private Map<Player, Integer> playerScores;

    public GameSession(List<Player> connectedPlayers, int capacity, Selector selector){
        this.status = Status.READY;
        this.connectedPlayers = connectedPlayers;
        this.capacity = capacity;
        this.threadPool = Executors.newFixedThreadPool(capacity);
        this.playerScores = new HashMap<>();
        this.selector = selector;

        for (Player player : connectedPlayers) {
            playerScores.put(player, 0);
        }
        //System.out.println("Selector" + selector);
    }

    public void addPlayer(Player player){
        connectedPlayers.add(player);
    }

    public int getCapacity(){
        return capacity;
    }

    public void startGame(){
        System.out.println("Starting game in SESSION");
        this.status = Status.IN_PROGRESS;
        Lock scoresLock = new ReentrantLock();
        // start game
        for (Player player : connectedPlayers) {
            // game loop for each player
            threadPool.execute(() -> {

                Player currentPlayer = player;
                ByteBuffer buffer = ByteBuffer.allocate(1024);

                try {
                    int bytesRead = currentPlayer.getSocketChannel().read(buffer);

                    if (bytesRead == -1) {
                        System.out.println("Client closed connection");
                        currentPlayer.getSocketChannel().close();
                        return;
                    } else {
                        buffer.flip();
                        byte[] data = new byte[buffer.remaining()];
                        buffer.get(data);
                        String receivedMessage = new String(data);

                        System.out.println("Received message: " + receivedMessage);

                        buffer.clear();

                        // send message to player
                        currentPlayer.getSocketChannel().write(ByteBuffer.wrap("Game is starting...".getBytes()));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                
            });
        }
    }

    public void checkStatus(){
        this.status = Status.ENDED;
    }

    public void shutDown(){
        // end game
        threadPool.shutdown();
    }

    public Status getStatus(){
        return this.status;
    }
}
