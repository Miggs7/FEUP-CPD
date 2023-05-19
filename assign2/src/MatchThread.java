import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Queue;

public class MatchThread implements Runnable{
    private Queue<Player> players;

    public MatchThread(Queue<Player> players){
        this.players = players;
    }

    @Override
    public void run() {
        Hangman game = new Hangman();

        for (Player player : players) {
            player.sendMsg("Welcome to Hangman! Guess the word:");
            player.sendMsg(game.getMaskedWord());
        }

        while (!game.isGameOver()) {
            Player currentPlayer = players.poll();
            char guess = getPlayerGuess(currentPlayer);

            boolean isCorrectGuess = game.makeGuess(guess);

            for (Player player : players) {
                if (isCorrectGuess) {
                    player.sendMsg("Correct guess!");
                } else {
                    player.sendMsg("Incorrect guess!");
                }
                player.sendMsg(game.getMaskedWord());
            }
        }

        for (Player player : players) {
            if (game.isWordGuessed()) {
                player.sendMsg("Congratulations! You guessed the word: " + game.getWord());
            } else {
                player.sendMsg("Game over! The word was: " + game.getWord());
            }
        }
        
    }

    private char getPlayerGuess(Player player) {
        SocketChannel socketChannel = player.getSocketChannel();

        ByteBuffer buffer = ByteBuffer.allocate(1);
        try {
            int bytesRead = socketChannel.read(buffer);
            if (bytesRead == -1) {
                // Socket channel closed
                throw new IOException("Socket channel closed");
            }

            buffer.flip();
            char guess = (char) buffer.get();
            buffer.clear();

            return guess;
        } catch (IOException e) {
            // Handle IO exception
            // You may choose to log the error, remove the player from the match, etc.
            return 0; // Return a default value or handle the exception according to your needs
        }
    }
}
