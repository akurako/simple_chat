import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;


public class ChatServer {
    ServerSocket serversocket;
    ArrayList<Client> active_clients = new ArrayList<>();
    ArrayList<Channel> active_channels = new ArrayList<>();


    public ChatServer() throws IOException {
        serversocket = new ServerSocket(1234);
        active_channels.add(new Channel("#Main"));
        active_channels.add(new Channel("#2nd"));
    }

    public void findChannelByName(Client client, String channelName) {
        Channel channelFound = null;
        for (Channel channel : active_channels) {
            if (channelName.equals(channel.channelName) == true) {
                channelFound = channel;
                break;
            }
        }
        if (channelFound == null) {
            sendNotification("Channel not found!", client);
        } else {
            switchChannel(client, channelFound);
        }
    }

    public void getActiveChannelsList(Client client){
        for (Channel channel : active_channels) {
            sendNotification(channel.getChanelInfo(),client);
        }
    }

    public void switchChannel(Client client, Channel channel) {
        if (client.activeChannel != null) {
            client.activeChannel.channelUsers.remove(client);
            leftChannelNotification(client.activeChannel, client);
        }
        client.activeChannel = channel;
        client.activeChannel.channelUsers.add(client);
        sendNotification("Active channel: " + channel.channelName, client);
        joinChannelNotification(channel, client);
    }

    public void leftChannelNotification(Channel channel, Client client) {
        sendChannelNotification(client.nickname + " left channel.", channel);
    }

    public void joinChannelNotification(Channel channel, Client client) {
        sendChannelNotification(client.nickname + " joined channel.", channel);
    }

    public void clientExit(Client client) {
        leftChannelNotification(client.activeChannel, client);
        client.activeChannel.channelUsers.remove(client);
        client.activeChannel = null;
        this.active_clients.remove(client);
        activeClientCount();
    }

    public String getChannelsList() {
        StringBuilder channelsList = new StringBuilder("");
        for (Channel channel : active_channels) {
            channelsList.append(channel.channelName + ", ");
        }
        return String.valueOf(channelsList);
    }

    public String getTimestamp() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat timestamp = new SimpleDateFormat("HH:mm:ss");
        return "[" + timestamp.format(calendar.getTime()) + "]";
    }

    public void sendToAll(String message, Client sender) {
        for (Client client : active_clients) {
            if (client == sender) {
                client.sendConfirmation(getTimestamp());
            } else {
                client.receivedMessage(getTimestamp() + sender.nickname + ": " + message);
            }
        }
    }

    public void sendPM(String message, Client sender, Client receiver) {
        receiver.receivedMessage(getTimestamp() + "[PM]" + sender.nickname + ": " + message);
    }

    public void sendNotification(String message, Client receiver) {
        receiver.receivedMessage(getTimestamp() + " SERVER: " + message);
    }

    public void sendChannelNotification(String message, Channel channel) {
        for (Client client : channel.channelUsers) {
            client.receivedMessage(getTimestamp() + " SERVER: " + message);
        }
    }

    public void sendToChannel(String message, Client sender, Channel channel) {
        for (Client client : channel.channelUsers) {
            if (client == sender) {
                client.sendConfirmation(getTimestamp());
            } else {
                client.receivedMessage(getTimestamp() + sender.nickname + ": " + message);
            }
        }
    }

    public void activeClientCount() {
        System.out.println("Active clients: " + active_clients.size());
    }

    public void run() {
        while (true) {
            System.out.println("Waiting...");
            try {
                Socket socket = serversocket.accept();
                System.out.println("Client connected!");
                active_clients.add(new Client(socket, this));
                activeClientCount();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        new ChatServer().run();


    }
}