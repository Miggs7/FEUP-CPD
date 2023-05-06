package model;

import java.net.*;
import java.util.List;

public class Game {
    public enum direction {CLOCKWISE, ANTICLOCKWISE};
    private int capacity;
    private int numPlayers;
    private List<Socket> players;
    
    //state
    private int state;

    public Game(int capacity) {
        this.capacity = capacity;
        this.numPlayers = 0; //required 2 at minimum 
    }

    
}
