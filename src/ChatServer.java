import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;


public class ChatServer {
    ServerSocket serversocket;
    ArrayList<Client> clients = new ArrayList<>();
    ArrayList<Channel> active_channels = new ArrayList<>();



    public ChatServer() throws IOException {
        serversocket = new ServerSocket(1234);
        active_channels.add(new Channel("Main"));
    }

    public void findChannelByName(Client client,String channelName) {
        Channel channelFound = null;
        for (Channel channel : active_channels) {
            if (channelName.equals(channel.channelName)==true) {
                channelFound = channel;
                break;
            }
        }
        if (channelFound == null) {
            sendNotification("Channel not found!",client);
        } else {
            switchChannel(client, channelFound);
        }
    }

    public void switchChannel(Client client, Channel channel) {
        client.activeChannel = channel;
        sendNotification("Active channel: "+channel.channelName,client);
    }

    public String getChannelsList() {
        StringBuilder channelsList = new StringBuilder("");
        for (Channel channel : active_channels) {
            channelsList.append(channel.channelName + " ");
        }
        return String.valueOf(channelsList);
    }

    public String getTimestamp() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat timestamp = new SimpleDateFormat("HH:mm:ss");
        return "[" + timestamp.format(calendar.getTime()) + "]";
    }

    public void sendToAll(String message, Client sender) {
        for (Client client : clients) {
            if (client == sender) {
                client.sendConfirmation(getTimestamp());
            } else {
                client.receivedMessage(getTimestamp() + sender.nickname + ": " + message);
            }
        }
    }

    //TODO send to one client
    public void sendTo(String message, Client sender, Client receiver) {

    }

    public void sendNotification(String message, Client receiver) {
        receiver.receivedMessage(getTimestamp() +" SERVER : " + message);
    }

    //TODO send to specific channel
    public void sendToChannel(String message, Client sender, Channel channel) {
        for (Client client : channel.channelUsers) {
            if (client == sender) {
                client.sendConfirmation(getTimestamp());
            } else {
                client.receivedMessage(getTimestamp() + sender.nickname + ": " + message);
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
                System.out.println("Active clients: " + clients.size());

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        new ChatServer().run();


    }
}