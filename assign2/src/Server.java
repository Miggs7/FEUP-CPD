

import java.io.*;
import java.net.*;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.Game;


public class Server {
    private static Map<String, String> userData = new HashMap<>();

    //server
    private ServerSocket server;
    
    //clients
    private static List<ServerThread> clients = new ArrayList<>();

    //game
    private Game game;



    public static void main(String args[]) throws Exception {
        loadUserData(); // load user database from file
        
        try (ServerSocket server = new ServerSocket(5000)) {
        System.out.println("Server started");
        while (true) {
            Socket s = server.accept();
            System.out.println("A client has connected.");
            ServerThread serverThread = new ServerThread(s, clients);

            clients.add(serverThread);
            serverThread.start();

            DataInputStream dis = new DataInputStream(s.getInputStream());
            DataOutputStream dos = new DataOutputStream(s.getOutputStream());
    
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            
            String str = "", str2 = "";
    
            while (!str.equals("stop")) {
                str = dis.readUTF();
    
                if (str.startsWith("login")) {
                    String[] tokens = str.split("\\s");
                    String username = tokens[1];
                    String password = tokens[2];
    
                    if (authenticate(username, password)) {
                        dos.writeUTF("login_success");
                    } else {
                        dos.writeUTF("login_failed");
                    }
                } else if (str.startsWith("register")) {
                    String[] tokens = str.split("\\s");
                    String username = tokens[1];
                    String password = tokens[2];
    
                    if (register(username, password)) {
                        dos.writeUTF("register_success");
                    } else {
                        dos.writeUTF("register_failed");
                    }
                } else {
                    System.out.println("Client says: " + str);
                    str2 = br.readLine();
                    dos.writeUTF(str2);
                    dos.flush();
                }
            }
            dis.close();
            s.close();
            server.close();
            saveUserData(); // save user data to a file
        }
        
        
        }
    
    }
    
    private static boolean authenticate(String username, String password) {
        String storedPassword = userData.get(username);
    
        if (storedPassword != null && storedPassword.equals(password)) {
            return true;
        } else {
            return false;
        }
    }
    
    private static boolean register(String username, String password) {
        if (userData.containsKey(username)) {
            return false; // username already exists
        } else {
            userData.put(username, password);
            return true;
        }
    }
    
    private static void loadUserData() throws IOException {
        userData = new HashMap<>();
        userData.put("admin", "admin123");
        userData.put("user", "user123");
    }
    
    private static void saveUserData() throws IOException{
        FileOutputStream fos = null;
        try {
            File file = new File("userData.txt");
            fos = new FileOutputStream(file);        
            for (Map.Entry<String, String> entry : userData.entrySet()) {
                String username = entry.getKey();
                String password = entry.getValue();
                String line = username + " " + password + "\n";
                fos.write(line.getBytes());
            }
        } finally {
            if (fos != null) {
                fos.close();
            }
        }

        private int authenticateMode() {

        }
    }
}
