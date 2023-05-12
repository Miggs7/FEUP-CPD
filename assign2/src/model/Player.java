package model;

import java.net.*;
import java.util.*;
import java.util.LinkedList;


public class Player{
    private int playerID;
    private String playerName = null;
    private boolean MyTurn = false;
    private boolean saidUNO = false;
    private LinkedList<String> myCards; //to Do String as a placeholder for UNOcard


    public Player(String name, int id){
        setId(id);
        setName(name);
        myCards = new LinkedList<String>();
    }

    public void setName(String name){
        playerName = name;
    }

    public void setId(int id){
        playerID = id;
    }

    public int getId(){
        return this.playerID;
    }

    public String getName(){
        return this.playerName;        
    }

    public int getNumCards(){
        return myCards.size();
    }

    public boolean isMyturn(){
        return MyTurn;
    }

    public void setMyTurn(){
        if(!MyTurn){
            MyTurn = true;
        }
        else{
            MyTurn = false;
        }
    }

    public boolean hasCards(){
        if (myCards.isEmpty()){
            return false;
        }
        else{
            return true;
        }
    }

    public boolean getSaidUNO(){
        return saidUNO;
    }

    public void saidUNO(){
        saidUNO = true;
    }
    
    public void dontSaidUNO(){
        saidUNO = false;
    }





    

    

}

