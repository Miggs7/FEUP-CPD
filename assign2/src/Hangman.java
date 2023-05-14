import java.util.Scanner;

public class Hangman {
    private static final String[] WORDS = {"hangman", "java", "programming", "openai", "computer"};

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String word = getRandomWord();
        char[] guessedLetters = new char[word.length()];
        int numAttempts = 0;
        int maxAttempts = 6;

        while (true) {
            System.out.println("Guess a letter: ");
            char letter = scanner.next().charAt(0);

            if (isLetterGuessed(letter, guessedLetters)) {
                System.out.println("You have already guessed that letter!");
                continue;
            }

            if (word.contains(String.valueOf(letter))) {
                updateGuessedLetters(letter, word, guessedLetters);
                if (isWordGuessed(guessedLetters)) {
                    System.out.println("Congratulations! You guessed the word: " + word);
                    break;
                }
            } else {
                numAttempts++;
                System.out.println("Incorrect guess. Attempts remaining: " + (maxAttempts - numAttempts));
                if (numAttempts == maxAttempts) {
                    System.out.println("Sorry, you lost! The word was: " + word);
                    break;
                }
            }

            System.out.println("Current status: " + getWordStatus(word, guessedLetters));
        }

        scanner.close();
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