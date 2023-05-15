import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;
import java.util.concurrent.*;
import java.security.*;

public class GameSession {
    // session properties
    private List<Player> connectedPlayers;
    private int capacity;
    private ExecutorService threadPool;

    // game properties
    //private Hangman game;

    public GameSession(List<Player> connectedPlayers, int capacity){
        this.connectedPlayers = connectedPlayers;
        this.capacity = capacity;
        this.threadPool = Executors.newFixedThreadPool(capacity);
        //game.setup(connectedPlayers);

        // start game
        startGame();

        // end game
        endGame();
    }

    public void addPlayer(Player player){
        connectedPlayers.add(player);
    }

    public int getCapacity(){
        return capacity;
    }

    public void startGame(){
        // start game
        for (Player player : connectedPlayers) {
            //threadPool.execute(new GameHandler(player));
        }
    }

    public void endGame(){
        // end game
        threadPool.shutdown();
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
