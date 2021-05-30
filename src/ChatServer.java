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
        //active_channels.add(new Channel("#Main"));
        //active_channels.add(new Channel("#2nd"));
    }

    //CHANNEL METHODS -------------------------------------------------------------------------------------------------

    public void getChannelUsers(Client target, Channel channel) {
        StringBuilder userList = new StringBuilder();
        for (Client client : channel.channelUsers) {
            userList.append("[" + client.nickname + "] ");
        }
        sendNotification(String.valueOf(userList), target);

    }

    public void createChannel(String name, Client client) {
        boolean channelExists = false;
        for (Channel channel : active_channels) {
            if (name.equals(channel.channelName)) {
                sendNotification("Channel with name " + name + " already exists.", client);
                channelExists = true;
                break;
            }
        }
        if (channelExists == false) {
            active_channels.add(new Channel(name));
            sendNotification("Channel successfully created.", client);
            joinChannelByName(client, name);
        }
    }

    public void joinChannelByName(Client client, String channelName) {
        Channel channelFound = findChannelByName(client, channelName);
        if (channelFound == null) {
            sendNotification("Channel not found!", client);
        } else {
            switchChannel(client, channelFound);
        }
    }


    public Channel findChannelByName(Client client, String channelName) {
        Channel channelFound = null;
        for (Channel channel : active_channels) {
            if (channelName.equals(channel.channelName) == true) {
                channelFound = channel;
                break;
            }
        }
        return channelFound;
    }

    public void getActiveChannelsList(Client client) {
        if (active_channels.size() == 0) {
            sendNotification("There is no active channels you can create one using !chancreate #channel_name", client);
        } else {
            for (Channel channel : active_channels) {
                sendNotification(channel.getChanelInfo(), client);
            }
        }
    }

    public void leaveChannel(Client target) {
        target.activeChannel.channelUsers.remove(target);
        leaveChannelNotification(target.activeChannel, target);
    }

    public void switchChannel(Client client, Channel channel) {
        if (client.activeChannel != null) {
            client.activeChannel.channelUsers.remove(client);
            leaveChannelNotification(client.activeChannel, client);
        }
        client.activeChannel = channel;
        client.activeChannel.channelUsers.add(client);
        sendNotification("Active channel: " + channel.channelName, client);
        joinChannelNotification(channel, client);
    }

    public void leaveChannelNotification(Channel channel, Client client) {
        sendChannelNotification(client.nickname + " left channel.", channel);
    }

    public void joinChannelNotification(Channel channel, Client client) {
        sendChannelNotification(client.nickname + " joined channel.", channel);
    }


    public String getChannelsList() {
        StringBuilder channelsList = new StringBuilder();
        for (Channel channel : active_channels) {
            channelsList.append(channel.channelName + ", ");
        }
        return String.valueOf(channelsList);
    }


    //SEND METHODS
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

    //USER METHODS ----------------------------------------------------------------------------------------------------
    public void clientExit(Client client) {
        leaveChannelNotification(client.activeChannel, client);
        client.activeChannel.channelUsers.remove(client);
        client.activeChannel = null;
        this.active_clients.remove(client);
        activeClientCount();
    }

    public void getHelp(Client target) {
        sendNotification(" List of available commands:\r\n" +
                "                !userlist - List all users on current channel\r\n" +
                "                !chanlist - List all active channels\r\n" +
                "                !chanleave - Leave current channel\r\n" +
                "                !chancreate - Create a new channel\r\n" +
                "                !clear - Clean terminal", target);
    }

    public boolean uniqueNickname(String nickname, Client target) {
        boolean isUnique = true;
        for (Client client : active_clients) {
            if (nickname.equals(client.nickname)) {
                isUnique = false;
                sendNotification("This nickname is already taken.", target);
            }
        }
        return isUnique;
    }

    //SERVER SIDE METHODS ---------------------------------------------------------------------------------------------

    public String getTimestamp() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat timestamp = new SimpleDateFormat("HH:mm:ss");
        return "[" + timestamp.format(calendar.getTime()) + "]";
    }

    public void activeClientCount() {
        System.out.println("Active clients: " + active_clients.size());
    }

    // RUN PSVM -------------------------------------------------------------------------------------------------------

    public void run() {
        while (true) {
            try {
                Socket socket = serversocket.accept();
                active_clients.add(new Client(socket, this));
                activeClientCount();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        ServerScanner serverScanner = new ServerScanner();
        new Thread(serverScanner).start();
        new ChatServer().run();


    }
}