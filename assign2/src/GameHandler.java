import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;

public class GameHandler implements Runnable {
    private Player player;
    private Hangman game;
    private Map<Player, Integer> playerScores;
    private Lock scoresLock;

    public GameHandler(Player player, Hangman game, Map<Player, Integer> playerScores, Lock scoresLock) {
        this.player = player;
        this.game = game;
        this.playerScores = playerScores;
        this.scoresLock = scoresLock;
    }

    @Override
    public void run() {
        SocketChannel socketChannel = player.getSocketChannel();

        try {
            ByteBuffer buffer = ByteBuffer.allocate(1024);

            while (!game.isGameOver()) {

                String gameStatus = game.getGameStatus();
                buffer.clear();
                buffer.put(gameStatus.getBytes());
                buffer.flip();
                socketChannel.write(buffer);

                buffer.clear();
                socketChannel.read(buffer);
                String guess = new String(buffer.array()).trim();
                game.processGuess(guess);
            }

            String finalScore = "Game Over. Your final score: " + game.getScore() + "\n";
            buffer.clear();
            buffer.put(finalScore.getBytes());
            buffer.flip();
            socketChannel.write(buffer);

            // update scores
            scoresLock.lock();
            try {
                playerScores.put(player, game.getScore());
            } finally {
                scoresLock.unlock();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
