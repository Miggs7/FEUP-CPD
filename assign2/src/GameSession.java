import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;
import java.util.concurrent.*;
import java.security.*;

public class GameSession {
    public static String word;
    public static List<Character> guessedLetters = new ArrayList<>();
    public static Map<String, SocketChannel> connectedClients;
    public static List<Player> connectedPlayers;
    public static int remainingAttempts = 0;
    public static boolean isGameOver = false;

    /* word */
    public static void saveWord(String newWord){
        word = newWord; 
    }

    public static String loadWord(){
        return word;
    }

    /*save Letter*/
    public static void saveLetter(Character letter){
        guessedLetters.add(letter);
    }

    public static List<Character> loadGuessedLetters(){
        return guessedLetters;
    }

    /*player info*/
    public static void savePlayers(Map<String, SocketChannel> newConnectedClients,List<Player> players){
        connectedPlayers = players;
        connectedClients = newConnectedClients;
    }

    public static Map<String,SocketChannel> loadClients(){
        return connectedClients;
    }

    public static List<Player> loadPlayers(){
        return connectedPlayers;
    }

    /*remaning attempts */
    public static void saveRemainingAttempts(int tries){
        remainingAttempts = tries;
    }

    public static int loadRemainingAttempts(){
        return remainingAttempts;
    }

    /*isGameOver */

    public static boolean isGameOver(){
        return isGameOver;
    }
    
}
