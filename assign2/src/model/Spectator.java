package model;

public class Spectator {
    private String spectatorName = null;
    private int spectatorID;


    public Spectator(String name, int id){
        setId(id);
        setName(name);
    }

    public void setName(String name){
        spectatorName = name;
    }

    public String getName(){
        return this.spectatorName;
    }

    public void setId(int id){
        spectatorID = id;
    }

    public int getId(){
        return this.spectatorID;
    }
}

