import java.util.*;

class Hangman {
    private static final String[] WORDS = {"APPLE", "BANANA", "ORANGE", "MANGO", "KIWI"};
    private static final int MAX_GUESSES = 6;

    private String word;
    private StringBuilder maskedWord;
    private Set<Character> guessedLetters;
    private int incorrectGuesses;

    public Hangman() {
        // Select a random word
        Random random = new Random();
        int index = random.nextInt(WORDS.length);
        word = WORDS[index];

        // Initialize masked word and guessed letters
        maskedWord = new StringBuilder();
        guessedLetters = new HashSet<>();
        for (int i = 0; i < word.length(); i++) {
            maskedWord.append("_");
        }

        incorrectGuesses = 0;
    }

    public boolean makeGuess(char guess) {
        if (guessedLetters.contains(guess)) {
            // Letter has already been guessed
            return false;
        }

        guessedLetters.add(guess);

        boolean isCorrectGuess = false;
        for (int i = 0; i < word.length(); i++) {
            if (word.charAt(i) == guess) {
                maskedWord.setCharAt(i, guess);
                isCorrectGuess = true;
            }
        }

        if (!isCorrectGuess) {
            incorrectGuesses++;
        }

        return isCorrectGuess;
    }

    public boolean isGameOver() {
        return isWordGuessed() || incorrectGuesses >= MAX_GUESSES;
    }

    public boolean isWordGuessed() {
        return maskedWord.indexOf("_") == -1;
    }

    public String getWord() {
        return word;
    }

    public String getMaskedWord() {
        return maskedWord.toString();
    }
}