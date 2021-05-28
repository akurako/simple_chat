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

    public Client(Socket socket, ChatServer server) {
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

    public void printLineSeparator(){
        out.print("\r\n");
    }

    public void closeConnection(){
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void run() {
        try {
            InputStream is = socket.getInputStream();
            OutputStream os = socket.getOutputStream();

            in = new Scanner(is);
            out = new PrintStream(os);
            String input;
            while (true) {
                while (this.nickname == null) {
                    String checknickname;
                    out.print("Enter your nickname?");
                    printLineSeparator();
                    checknickname = in.nextLine();
                    if (server.uniqueNickname(checknickname, this) == true) {
                        this.nickname = checknickname;
                    }
                }
                out.print("Welcome to the chat " + this.nickname);
                printLineSeparator();

                while (this.activeChannel == null) {
                    out.print("Select channel: " + server.getChannelsList());
                    printLineSeparator();
                    server.findChannelByName(this, in.nextLine());
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
                        default -> server.sendToChannel(input, this, activeChannel);
                    }

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchElementException e) {
            server.clientExit(this);
            closeConnection();
        }

    }
}