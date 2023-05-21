import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.Scanner;

public class Player{
    // player info
    private String username;
    private SocketChannel socketChannel;
    
    private int mmr; //Matchmaking rating mmr >= 0 && mmr <= 12
    private long joinTime;

    


    public Player(String username, SocketChannel socketChannel) {
        this.username = username;
        this.socketChannel = socketChannel;

        this.mmr = obtainStats(username).get(0);
        this.joinTime = 0;
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

    public void setMmr(){
        this.mmr = 0;
    }

    public int getMmr(){
        return this.mmr;
    }

    public void addToMmr(){
        int maxMmr = 8;
        if((this.mmr + 2) > maxMmr){
            mmr = maxMmr;
        }
        else{
            this.mmr += 2;
        }
    }

    public void subtractToMmr(){
        int minMmr = 0;
        if((this.mmr - 1) < minMmr){
            mmr = minMmr;
        }
        else{
            this.mmr -= 1;
        }
    }

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
                    break;
                }
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            System.out.println("Error reading from stats.txt");
        }
        return stats;
    }

    public void sendMsg(String msg){
        try{
            ByteBuffer buffer = ByteBuffer.wrap(msg.getBytes());
            socketChannel.write(buffer);
            System.out.println("Message sent to client: " + msg);
        }
        catch(IOException e){
            System.out.println("Error sending message to client");
        }
    }

    public long getJoinTime(){
        return this.joinTime;
    }

    public void setJoinTime(long currentTimeMillis) {
        this.joinTime = currentTimeMillis;
    }

    public boolean hasExceededMaxWaitingTime(long maxWaitingTime) {
        long currentTime = System.currentTimeMillis();
        return (currentTime - joinTime) > maxWaitingTime;
    }
}
