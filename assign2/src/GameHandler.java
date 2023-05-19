import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.SelectionKey;
import java.util.*;
import java.util.concurrent.locks.Lock;

public class GameHandler implements Runnable {
    private Player player;
    private Hangman game;
    private Map<Player, Integer> playerScores = new HashMap<>();
    private Lock scoresLock;
    private Selector selector;

    public GameHandler(Player player, Hangman game, Map<Player, Integer> playerScores, Lock scoresLock, Selector selector) {
        this.player = player;
        this.game = game;
        this.playerScores = playerScores;
        this.scoresLock = scoresLock;
        this.selector = selector;
    }

    @Override
    public void run() {
        System.out.println("Starting game in GameHandler");
        SocketChannel socketChannel = player.getSocketChannel();

        try {
            ByteBuffer buffer = ByteBuffer.allocate(1024);

            while (!game.isGameOver()) {
                System.out.println("Game is not over");

                //send game status
                String gameStatus = game.getGameStatus() + "\n";
                buffer.clear();
                buffer = ByteBuffer.wrap(gameStatus.getBytes());
                socketChannel.write(buffer);

                buffer.clear();
                // wait for an user input
                String userInput = "";
                while (userInput.equals("")) {
                    selector.select();
                    Set<SelectionKey> selectedKeys = selector.selectedKeys();
                    Iterator<SelectionKey> iter = selectedKeys.iterator();
                    while (iter.hasNext()) {
                        SelectionKey key = iter.next();
                        if (key.isReadable()) {
                            SocketChannel channel = (SocketChannel) key.channel();
                            buffer.clear();
                            int bytesRead = channel.read(buffer);
                            userInput = new String(buffer.array(), 0, bytesRead);
                            System.out.println("User input: " + userInput);
                            iter.remove();
                        }
                    }
                }

                // update game
                game.processGuess(userInput);
            }

            String finalScore = "Game Over. Your final score: " + game.getScore() + "\n";
            buffer.clear();
            buffer = ByteBuffer.wrap(finalScore.getBytes());
            socketChannel.write(buffer);

            // update scores
            synchronized (scoresLock) {
                playerScores.put(player, game.getScore());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
