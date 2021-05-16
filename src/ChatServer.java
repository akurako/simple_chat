import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;


public class ChatServer {
    ServerSocket serversocket;
    ArrayList<Client> clients = new ArrayList<>();

    public ChatServer() throws IOException {
        serversocket = new ServerSocket(1234);
    }

    public void sendAll(String message, Client sender) {
        for (Client client : clients) {
            client.recieve(sender.nickname + ": " + message);
        }
    }

    public void run() {
        while (true) {
            System.out.println("Waiting...");
            try {
                Socket socket = serversocket.accept();
                System.out.println("Client connected!");
                clients.add(new Client(socket, this));

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        new ChatServer().run();


    }
}