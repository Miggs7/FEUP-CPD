import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.*;

public class MatchThread implements Runnable{
    private Queue<Player> players;

    public MatchThread(Queue<Player> players){
        this.players = players;
    }

    @Override
    public void run() {
        Hangman game = new Hangman();

        for (Player player : players) {
            player.sendMsg("Game starting.");
    
            // wait for 1 second
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // Handle exception
            }

            player.sendMsg("Welcome to Hangman!\nGuess the word:" + game.getMaskedWord());
        }

        System.out.println("isGameOver() = " + game.isGameOver());

        while (game.isGameOver() == false) {
            System.out.println("players" + players);
            System.out.println("players size = " + players.size());

            for (Player player : players) {
                System.out.println("player" + player.getName());
                String guess = getPlayerGuess(player);
                System.out.println("guess = " + guess);
                boolean isCorrectGuess = game.makeGuess(guess);

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

    private String getPlayerGuess(Player player) {
        System.out.println("Waiting for player guess...");
        String guess = "";

        Selector selector = Server.selector;

        int channels;
        try {
            channels = selector.select();
            if (channels == 0) {
            return "";
        }
        } catch (IOException e) {
            e.printStackTrace();
        }
        

        Set<SelectionKey> keys = selector.selectedKeys();
        Iterator<SelectionKey> iterator = keys.iterator();

        while (iterator.hasNext()) {
            SelectionKey key = iterator.next();
            String response = null;

            if (key.isReadable()) {
                SocketChannel channel = (SocketChannel) key.channel();

                if (channel != player.getSocketChannel()) {
                    return "";
                }
                ByteBuffer responseBuffer = ByteBuffer.allocate(1024);
                try{
                    int bytesRead = channel.read(responseBuffer);
                    guess = new String(responseBuffer.array(), 0, bytesRead);
                } catch (IOException e) {
                    return "";
                }
                responseBuffer.clear();
            }
        }

        return guess;
        
        /*SocketChannel socketChannel = player.getSocketChannel();

        ByteBuffer buffer = ByteBuffer.allocate(1024);
        try {
            // Read from the socket channel into the buffer
            int bytesRead = socketChannel.read(buffer);
            if (bytesRead == -1) {
                // Socket channel closed
                throw new IOException("Socket channel closed");
            }

            String guess = new String(buffer.array(), 0, bytesRead);
            System.out.println("Player guess: " + guess);
            return guess;
        } catch (IOException e) {
            // Handle IO exception
            // You may choose to log the error, remove the player from the match, etc.
            return ""; // Return a default value or handle the exception according to your needs
        }*/
    }
}
