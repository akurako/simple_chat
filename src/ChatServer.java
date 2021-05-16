import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;


public class ChatServer {
    ServerSocket serversocket;
    ArrayList<Client> clients = new ArrayList<>();

    public ChatServer() throws IOException {
        serversocket = new ServerSocket(1234);
    }

    public String getTimestamp() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat timestamp = new SimpleDateFormat("HH:mm:ss");
        return "[" + timestamp.format(calendar.getTime()) + "]";
    }

    public void sendAll(String message, Client sender) {
        for (Client client : clients) {
            if (client == sender) {
                client.sendConfirmation(getTimestamp());
            } else {
                client.recievedMessage(getTimestamp() + sender.nickname + ": " + message);
            }
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