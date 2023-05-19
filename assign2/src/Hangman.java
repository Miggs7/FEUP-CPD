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

    public boolean makeGuess(String guess) {
        // check if the guess is the same as the word
        if (guess.length() == word.length()) {
            if (guess.equals(word)) {
                // guessed the word
                maskedWord = new StringBuilder(word);
                return true;
            } else {
                // incorrect guess
                incorrectGuesses++;
                return false;
            }
        }

        if (guess.length() == 1) {
            // check if the guess is a letter
            if (!Character.isLetter(guess.charAt(0))) {
                return false;
            }

            // check if the letter has already been guessed
            if (guessedLetters.contains(guess.charAt(0))) {
                return false;
            }
        }

        // check if the guess is a letter in the word
        boolean isCorrectGuess = false;
        for (int i = 0; i < word.length(); i++) {
            if (word.charAt(i) == guess.charAt(0)) {
                maskedWord.setCharAt(i, guess.charAt(0));
                isCorrectGuess = true;
            }
        }

        // update guessed letters
        guessedLetters.add(guess.charAt(0));

        // update incorrect guesses
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
        String aux = "Word:";
        for (int i = 0; i < maskedWord.length(); i++) {
            aux += " " + maskedWord.charAt(i);
        }
        return aux;
    }
}