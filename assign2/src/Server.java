import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Set;
import java.util.Iterator;


public class Server {
   
    private ServerSocketChannel serverSocketChannel;
    private Selector selector;

    public Server(int port) throws IOException {
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(port));
        selector = Selector.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
    }

    public void start() throws IOException {
        System.out.println("Server started.");

        while (true) {
            selector.select();

            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

            while (keyIterator.hasNext()) {
                SelectionKey key = keyIterator.next();

                if (key.isAcceptable()) {
                    handleAccept(key);
                } else if (key.isReadable()) {
                    handleRead(key);
                } else if (key.isWritable()) {
                    handleWrite(key);
                }

                keyIterator.remove();
            }
        }
    }

    private void handleAccept(SelectionKey key) throws IOException {
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        SocketChannel clientChannel = serverChannel.accept();
        clientChannel.configureBlocking(false);
        clientChannel.register(selector, SelectionKey.OP_READ);
        System.out.println("New client connected: " + clientChannel.getRemoteAddress());
    }

    private void handleRead(SelectionKey key) throws IOException {
        SocketChannel clientChannel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        int bytesRead = clientChannel.read(buffer);

        if (bytesRead == -1) {
            System.out.println("Client disconnected: " + clientChannel.getRemoteAddress());
            clientChannel.close();
            return;
        }

        buffer.flip();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        String message = new String(bytes);
        System.out.println("Received message from " + clientChannel.getRemoteAddress() + ": " + message);

        clientChannel.register(selector, SelectionKey.OP_WRITE, ByteBuffer.wrap(bytes));
    }

    private void handleWrite(SelectionKey key) throws IOException {
        SocketChannel clientChannel = (SocketChannel) key.channel();
        ByteBuffer buffer = (ByteBuffer) key.attachment();
        clientChannel.write(buffer);
        clientChannel.register(selector, SelectionKey.OP_READ);
    }

    public static void main(String[] args) throws IOException {
        int port = 8080;
        Server server = new Server(port);
        server.start();
    }
}
