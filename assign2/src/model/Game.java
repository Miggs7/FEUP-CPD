package model;

import java.net.*;
import java.util.Scanner; 
import java.util.List;
import java.util.Stack;

public class Game {
    public enum direction {CLOCKWISE, ANTICLOCKWISE};
    private int capacity;
    private boolean isOver;
    private int numPlayers;
    private List<Player> players; // List<sockets>
    private Stack<String> cardStack;
    
    //state
    private int state;

    public Game(int num) { //initialize the game with the right amount of players

        for(int i = 0; i < num; i++){
            Scanner sc= new Scanner(System.in); 
            System.out.print("Enter a Username: ");  
            String str= sc.nextLine();    
            
            Player player = new Player(str);

            players.add(i, player);
        }
    }



    public List<Player> getPlayers(){ //List<sockets>
        return players;
    }

    public boolean isOver(){

        if(cardStack.isEmpty()){
            isOver = true;
            return isOver;
        }
        for (Player player : players)
        {
            if(!player.hasCards()){
                isOver = true;
                break;
            }
        }
        



        return isOver;
    }


    
}
