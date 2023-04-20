package model;

public class Connection {
    private String username;
    private String password;
    private String ip;
    private int port;

    public Connection(String username, String password, String ip, int port) {
        this.username = username;
        this.password = password;
        this.ip = ip;
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }
}
