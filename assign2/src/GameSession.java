import java.util.Scanner;

import java.util.Map;
import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;
import java.util.concurrent.*;
import java.security.*;

public class GameSession {
    public static String word;
    public static List<Character> guessedLetters;
    public static Map<String, SocketChannel> connectedClients;
    public static int remainingAttempts = 0;

    /* word */
    public static void saveWord(String newWord){
        newWord = word; 
    }

    public static String loadWord(){
        return word;
    }

    /*save Letter*/
    public static void saveLetter(char letter){
        guessedLetters.add(letter);
    }

    public static List<Character> loadGuessedLetters(){
        return guessedLetters;
    }

    /*player info*/
    public static void savePlayers(Map<String, SocketChannel> newConnectedClients){
        connectedClients = newConnectedClients;
    }

    public static Map<String,SocketChannel> loadPlayers(){
        return connectedClients;
    }

    /*remaning attempts */
    public static void saveRemainingAttempts(int tries){
        remainingAttempts = tries;
    }

    public static int loadRemainingAttempts(){
        return remainingAttempts;
    }
    
}
