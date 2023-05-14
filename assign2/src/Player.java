import java.net.*;
import java.util.*;


public class Player{
    private int playerID;
    private String playerName = null;
    private int playerScore = 0;


    public Player(String name, int id){
        setId(id);
        setName(name);
    }

    public void setName(String name){
        this.playerName = name;
    }

    public void setId(int id){
        this.playerID = id;
    }

    public int getId(){
        return this.playerID;
    }

    public String getName(){
        return this.playerName;        
    }

    public void addScore(int num){
        this.playerScore += num;
    }

    public int getScore(){
        return this.playerScore;
    }

}
