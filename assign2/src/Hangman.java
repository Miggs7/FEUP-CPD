import java.util.Scanner;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.List;

public class Hangman{

    // word list
    private final String[] WORDS = {"hangman", "java", "programming", "computer", "algorithm", "optimization", "bananas", "total", "integer", "souto", "pedro"};
    private final int MAX_ATTEMPTS = 6;

    // game properties
    private String word;
    private char[] guessedLetters;
    private int numAttempts;
    private boolean isGameOver;
    private int playerScore;

    public Hangman(List<Player> connectedPlayers) {
        this.word = getRandomWord();
        this.guessedLetters = new char[word.length()];
        this.numAttempts = 0;
        this.isGameOver = false;
        this.playerScore = 0;
    }

    /* 
    public static String getUserById(int id) {
        for (Player player : connectedPlayers) {
            if (player.getId() == id) {
                return player.getName(); // Return the player if ID matches
            }
        }
        return null; // Player not found
    }

    public static Player findConnectedPlayer(SocketChannel sc){

        for(int i = 1; i <= connectedPlayers.size() ; i++){
            String username = getUserById(i);
            //System.out.println(username);
            if(sessionConnectedClients.get(username) == sc){
                return clientPlayer = connectedPlayers.get(i);
            }
        }
        return null;
    }
    */
/* 
    public void runGame(SocketChannel sc) {

        Scanner scanner = new Scanner(System.in);
        Player player = findConnectedPlayer(sc);
        
        while(true){
            System.out.print("Press 1 to start a game and 0 to return: ");
            int input = new Scanner(System.in).nextInt();
            
            if(input == 1){
                String word = getRandomWord();
                GameSession.word = word;

                char[] guessedLetters = new char[word.length()];
                int numAttempts = 0;
                int maxAttempts = 6;
                
                while (true) {
                    System.out.println("Guess a letter: ");
                    Character letter = scanner.next().charAt(0);
        
                    if (isLetterGuessed(letter, guessedLetters)) {
                        System.out.println("You have already guessed that letter!");
                        continue;
                    }
        
                    if (word.contains(String.valueOf(letter))) {
                        updateGuessedLetters(letter, word, guessedLetters);
                        //save the guessed letter
                        GameSession.saveLetter(letter);
                        if (isWordGuessed(guessedLetters)) {
                            int score = 0;
                            switch(numAttempts){
                                case 0:
                                    score = 100;
                                    break;
                                case 1: 
                                    score = 75;
                                    break;
                                case 2:
                                    score = 50;
                                    break;
                                case 3:
                                    score = 35;
                                    break;
                                case 4:
                                    score = 20;
                                    break;
                                case 5:
                                    score = 10;
                                    break;
                                default:
                                    break;
                            }
                            player.addScore(score);
                            System.out.println("Congratulations! You guessed the word: " + word);
                            GameSession.isGameOver = false;
                            System.out.println("You gained " + String.valueOf(score) + " points");
                            System.out.println("Your total score is " + String.valueOf(player.getScore()));
                            break;
                        }
                    } else {
                        numAttempts++;
                        System.out.println("Incorrect guess. Attempts remaining: " + (maxAttempts - numAttempts));
                        GameSession.saveRemainingAttempts(maxAttempts - numAttempts);
                        if (numAttempts == maxAttempts) {
                            System.out.println("Sorry, you lost! The word was: " + word);
                            break;
                        }
                    }
        
                    System.out.println("Current status: " + getWordStatus(word, guessedLetters));
                }
        
               
            }
            if(input == 0){
                System.out.println("Final Scores:");
                System.out.println("Player " + player.getName() + " has a final score of " +  String.valueOf(player.getScore()) + " points!");
                scanner.close();
                break;

            }

        
        }
            
    
    }*/

    public void processGuess(String guess) {
        char letter = guess.charAt(0);
        boolean isCorrectGuess = false;


        for (int i = 0; i < word.length(); i++) {
            if (word.charAt(i) == letter) {
                guessedLetters[i] = letter;
                isCorrectGuess = true;
            }
        }

        if (!isCorrectGuess) {
            numAttempts++;
        }

        if (numAttempts >= MAX_ATTEMPTS || isWordGuessed()) {
            isGameOver = true;
            updateScores();
        }
    }
        
    private String getRandomWord() {
        int index = (int) (Math.random() * WORDS.length);
        return WORDS[index];
    }

    private boolean isWordGuessed() {
        for (char c : guessedLetters) {
            if (c == '\0') {
                return false;
            }
        }
        return true;
    }

    private void updateScores() {
        if (isWordGuessed()) {
            playerScore += 10;
        }
    }

    public String formatGuessedWord(){
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < word.length();i++){
            if(guessedLetters.length == 0){
                if(word.charAt(i) == guessedLetters[i]){
                    sb.append(word.charAt(i) + " ");
                }else{
                    sb.append("_ ");
                }
            }else{
                sb.append("_ ");
            }
            
        }
        return sb.toString();
    }

    public String getGameStatus() {
        StringBuilder sb = new StringBuilder();
        sb.append("Current word: ").append(formatGuessedWord()).append("\n");
        sb.append("Attempts remaining: ").append(MAX_ATTEMPTS - numAttempts).append("\n");
        return sb.toString();
    }

/* 
    private static void updateGuessedLetters(char letter, String word, char[] guessedLetters) {
        for (int i = 0; i < word.length(); i++) {
            if (word.charAt(i) == letter) {
                guessedLetters[i] = letter;
            }
        }
    }

    private static String getWordStatus(String word, char[] guessedLetters) {
        StringBuilder status = new StringBuilder();
        for (int i = 0; i < word.length(); i++) {
            if (guessedLetters[i] != 0) {
                status.append(guessedLetters[i]);
            } else {
                status.append("_");
            }
            status.append(" ");
        }
        return status.toString();
    }*/

    public boolean isGameOver() {
        return isGameOver;
    }

    public int getScore() {
        return playerScore;
    }
}