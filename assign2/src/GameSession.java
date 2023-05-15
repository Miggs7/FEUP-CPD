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

    // game properties
    private Hangman game;

    public GameSession(List<Player> connectedPlayers, int capacity){
        this.connectedPlayers = connectedPlayers;
        this.capacity = capacity;
        game.setup();
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
    /*isGameOver*/
    public boolean isGameOver(){
        return isGameOver;
    }
}
