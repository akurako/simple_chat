import java.io.*;
import java.net.Socket;
import java.util.NoSuchElementException;
import java.util.Scanner;

class Client implements Runnable {
    Socket socket;
    Scanner in;
    PrintStream out;
    ChatServer server;
    String nickname;
    Channel activeChannel;
    InputStream is;
    OutputStream os;

    public Client(Socket socket, ChatServer server) throws IOException {
        new Thread(this).start();
        this.socket = socket;
        this.server = server;

    }

    public void receivedMessage(String message) {
        out.print(message);
        printLineSeparator();
    }

    public void clear() {
        this.out.println("\033[H\033[2J");
    }

    public void sendConfirmation(String timestamp) {
        out.print("Send by you at" + timestamp);
        printLineSeparator();
    }

    public void nicknameChangeDialog() {
        String checknickname;
        this.out.print("Enter your nickname?");
        printLineSeparator();
        checknickname = in.nextLine();
        if (server.uniqueNickname(checknickname, this) == true) {
            this.nickname = checknickname;
        }
    }

    public void channelCreateDialog() {
        out.print("Choose a name for a new channel starting with #");
        printLineSeparator();
        server.createChannel(in.nextLine(), this);
    }

    public void printLineSeparator() {
        out.print("\r\n");
    }

    public void closeConnection() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void run() {
        try {
            is = socket.getInputStream();
            os = socket.getOutputStream();
            in = new Scanner(is);
            out = new PrintStream(os);
            String input;
            while (true) {
                while (this.nickname == null) {
                    nicknameChangeDialog();
                }
                out.print("Welcome to the chat " + this.nickname);
                printLineSeparator();

                while (this.activeChannel == null) {
                    if (server.active_channels.size() == 0) {
                        channelCreateDialog();
                    } else {
                        out.print("Select channel: " + server.getChannelsList());
                        printLineSeparator();
                        server.joinChannelByName(this, in.nextLine());
                    }
                }


                while (activeChannel != null) {
                    input = in.nextLine();

                    switch (input) {
                        case "!quit" -> {
                            server.clientExit(this);
                            closeConnection();
                        }
                        case "" -> {
                        }
                        case "!chanleave" -> {
                            server.leaveChannel(this);
                            this.activeChannel = null;
                        }
                        case "!clear" -> clear();
                        case "!userlist" -> {
                            server.getChannelUsers(this, this.activeChannel);
                        }
                        case "!chanlist" -> {
                            server.getActiveChannelsList(this);
                        }
                        case "!help" -> {
                            server.getHelp(this);
                        }
                        case "!chancreate" -> {
                            channelCreateDialog();
                        }
                        default -> server.sendToChannel(input, this, activeChannel);
                    }

                }
            }
        } catch (NoSuchElementException e) {
            server.clientExit(this);
            closeConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}