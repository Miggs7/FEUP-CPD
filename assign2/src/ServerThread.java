
import java.io.*;
import java.net.*;
import java.util.List;

public class ServerThread extends Thread{
    private Socket socket;
    private List<ServerThread> clients;
    private PrintWriter writer;

    public ServerThread(Socket socket, List<ServerThread> clients) {
        this.socket = socket;
        this.clients = clients;
    }

    @Override
    public void run() {
        try {
            // using channel I/O
            
            

            socket.close();
        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    
}
