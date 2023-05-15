import java.util.Scanner;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.List;

public class Hangman {
    private static final String[] WORDS = {"hangman", "java", "programming", "openai", "computer", "algorithm", "optimization"};
    public static Player clientPlayer;
    public static List<Player> connectedPlayers = Server.players;

    public static void findConnectedPlayer(Map<String,SocketChannel> p){
        for(int i = 1; i <= connectedPlayers.size() ; i++){
            String username = Player.getNameById(i);
            if(p.get(username) != null){
                clientPlayer = connectedPlayers.get(i-1);
            }
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Player player = new Player("UsernameDefault", 1);
        
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
                        /*save the guessed letter*/
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
            
    
    }
        
    private static String getRandomWord() {
        int index = (int) (Math.random() * WORDS.length);
        return WORDS[index];
    }

    private static boolean isLetterGuessed(char letter, char[] guessedLetters) {
        for (char c : guessedLetters) {
            if (c == letter) {
                return true;
            }
        }
        return false;
    }

    private static void updateGuessedLetters(char letter, String word, char[] guessedLetters) {
        for (int i = 0; i < word.length(); i++) {
            if (word.charAt(i) == letter) {
                guessedLetters[i] = letter;
            }
        }
    }

    private static boolean isWordGuessed(char[] guessedLetters) {
        for (char c : guessedLetters) {
            if (c == 0) {
                return false;
            }
        }
        return true;
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
    }
}