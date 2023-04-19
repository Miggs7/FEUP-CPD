package server;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;


public class Server {
    private static final String DB_FILE = "data/users.db"; // path to user database file
    private static Map<String, String> userDB = new HashMap<>();

    public static void main(String args[]) throws Exception {
        loadUserDB(); // load user database from file
    
        ServerSocket server = new ServerSocket(5000);
        System.out.println("Server started");
    
        Socket s = server.accept();
        System.out.println("Connected");
    
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
    
        saveUserDB(); // save user database to file
    }
    
    private static boolean authenticate(String username, String password) {
        String storedPassword = userDB.get(username);
    
        if (storedPassword != null && storedPassword.equals(password)) {
            return true;
        } else {
            return false;
        }
    }
    
    private static boolean register(String username, String password) {
        if (userDB.containsKey(username)) {
            return false; // username already exists
        } else {
            userDB.put(username, password);
            return true;
        }
    }
    
    private static void loadUserDB() throws IOException {
        File file = new File(DB_FILE);
    
        if (file.exists()) {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
    
            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split(":");
                String username = tokens[0];
                String password = tokens[1];
                userDB.put(username, password);
            }
    
            reader.close();
        }
    }
    
    private static void saveUserDB() throws IOException {
        File file = new File(DB_FILE);
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
    
        for (Map.Entry<String, String> entry : userDB.entrySet()) {
            String username = entry.getKey();
            String password = entry.getValue();
            writer.write(username + ":" + password);
            writer.newLine();
        }
    
        writer.close();   
    }
}
