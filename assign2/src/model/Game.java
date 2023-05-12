package model;

import java.net.*;
import java.util.Scanner; 
import java.util.List;
import java.util.Stack;

public class Game {
    public enum direction {CLOCKWISE, ANTICLOCKWISE};
    private direction dir = direction.CLOCKWISE;
    private int capacity;
    private boolean isOver;
    private int numPlayers;
    private List<Player> players; // List<sockets>
    private List<Spectator> spectators; // List<sockets>
    private Stack<String> cardStack;
    
    //state
    private int state;

    public Game(int numPlayers, int numSpectators) { //initialize the game with the right amount of players
        for(int i = 0; i < numPlayers; i++){
            Scanner sc= new Scanner(System.in); 
            System.out.print("Enter a Username: ");  
            String str= sc.nextLine();    
            
            Player player = new Player(str, i);
            players.add(i, player);
        }

        for(int i = 0; i < numSpectators; i++){
            Scanner sc= new Scanner(System.in); 
            System.out.print("Enter a Username: ");  
            String str= sc.nextLine();    
            
            Spectator spectator = new Spectator(str, i);
            spectators.add(i, spectator);
        }
    }



    public List<Player> getPlayers(){ //List<sockets>
        return players;
    }


    public direction getDirection(){
        return dir;
    }

    public void changeDirection(){
        if(dir == direction.CLOCKWISE){
            dir = direction.ANTICLOCKWISE;
        }
        else{ //direction == direction.ANTICLOCKWISE
            dir = direction.CLOCKWISE;
        }
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
