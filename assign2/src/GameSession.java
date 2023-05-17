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

    // game properties
    private Hangman game;
    private Map<Player, Integer> playerScores;

    public GameSession(List<Player> connectedPlayers, int capacity){
        this.status = Status.READY;
        this.connectedPlayers = connectedPlayers;
        this.capacity = capacity;
        this.threadPool = Executors.newFixedThreadPool(capacity);
        this.game = new Hangman(connectedPlayers);
        this.playerScores = new HashMap<>();
    }

    public void addPlayer(Player player){
        connectedPlayers.add(player);
    }

    public int getCapacity(){
        return capacity;
    }

    public void startGame(){
        this.status = Status.IN_PROGRESS;
        Lock scoresLock = new ReentrantLock();
        // start game
        for (Player player : connectedPlayers) {
            // game loop for each player
            threadPool.execute(() -> {
                GameHandler gameHandler = new GameHandler(player, game, playerScores, scoresLock);
                gameHandler.run();
            });
        }
    }

    public void checkStatus(){
        if(game.isGameOver()){
            this.status = Status.ENDED;
        }
    }

    public void shutDown(){
        // end game
        threadPool.shutdown();
    }

    public Status getStatus(){
        return this.status;
    }


    /* word 
    public static void saveWord(String newWord){
        word = newWord; 
    }

    public static String loadWord(){
        return word;
    }*/

    /*save Letter
    public static void saveLetter(Character letter){
        guessedLetters.add(letter);
    }

    public static List<Character> loadGuessedLetters(){
        return guessedLetters;
    }*/

    /*player info
    public static void savePlayers(Map<String, SocketChannel> newConnectedClients,Player player){
        connectedPlayers.add(player);
        connectedClients = newConnectedClients;
    }

    public static Map<String,SocketChannel> loadClients(){
        return connectedClients;
    }

    public static List<Player> loadPlayers(){
        return connectedPlayers;
    }
*/
    /*remaning attempts 
    public static void saveRemainingAttempts(int tries){
        remainingAttempts = tries;
    }

    public int loadRemainingAttempts(){
        return remainingAttempts;
    }
*/
    /*isGameOver
    public boolean isGameOver(){
        return isGameOver;
    }*/
}
