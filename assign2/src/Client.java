

import java.io.*;
import java.net.*;

class Client {
    public static void main(String args[]) throws Exception {
        Socket s = new Socket("localhost", 5000);
        DataInputStream dis = new DataInputStream(s.getInputStream());
        DataOutputStream dos = new DataOutputStream(s.getOutputStream());

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        String str = "", str2 = "";

        while (!str.equals("stop")) {
            System.out.println("Enter command (login or register), followed by username and password (separated by space): ");
            str = br.readLine();
            dos.writeUTF(str);
            dos.flush();
            str2 = dis.readUTF();
            System.out.println("Server says: " + str2);
        }

        dis.close();
        s.close();
    }
}