import java.io.*;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.Scanner;

public class Player{
    // player info
    private String username;
    private SocketChannel socketChannel;

    // player stats
    private int level;
    private int experience;

    public Player(String username, SocketChannel socketChannel) {
        this.username = username;
        this.socketChannel = socketChannel;

        this.level = obtainStats(username).get(0);
        this.experience = obtainStats(username).get(1);
    }

    public void setName(String name){
        this.username = name;
    }

    public String getName(){
        return this.username;        
    }

    public void setSocketChannel(SocketChannel socketChannel){
        this.socketChannel = socketChannel;
    }

    public SocketChannel getSocketChannel(){
        return this.socketChannel;
    }
/* 
    public void setId(int id){
        this.playerID = id;
    }

    public int getId(){
        return this.playerID;
    }

    public void addScore(int num){
        this.playerScore += num;
    }

    public int getScore(){
        return this.playerScore;
    }

    public static String getNameById(int id){
        return playerName;
    }
*/

    private List<Integer> obtainStats(String username) {
        List<Integer> stats = new ArrayList<>();
        try {
            File file = new File("server.txt");
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] info = line.split(" ");
                if (info[0].equals(username)) {
                    stats.add(Integer.parseInt(info[3]));
                    stats.add(Integer.parseInt(info[4]));
                    break;
                }
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            System.out.println("Error reading from stats.txt");
        }
        return stats;
    }
}
