
public class Player{
    private int playerID;
    private static String playerName = null;
    private int playerScore = 0;


    public Player(String name, int id){
        setId(id);
        setName(name);
    }

    public void setName(String name){
        playerName = name;
    }

    public void setId(int id){
        this.playerID = id;
    }

    public int getId(){
        return this.playerID;
    }

    public String getName(){
        return playerName;        
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

    public Player getPlayerById(int id){
        return this;
    }

}
