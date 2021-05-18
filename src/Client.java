import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
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
        out.println(message);
    }

    public void clear() {
        this.out.println("\033[H\033[2J");
    }

    public void sendConfirmation(String timestamp) {
        out.println("Send by you at" + timestamp);
    }


    public void run() {
        try {
            InputStream is = socket.getInputStream();
            OutputStream os = socket.getOutputStream();

            in = new Scanner(is);
            out = new PrintStream(os);
            String input;
            while (this.nickname == null) {
                String checknickname;
                out.println("Enter your nickname?");
                checknickname = in.nextLine();
                if (server.uniqueNickname(checknickname,this)== true){
                    this.nickname = checknickname;
                }
            }
            out.println("Welcome to the chat " + this.nickname);

            while (activeChannel == null) {
                out.println("Select channel: " + server.getChannelsList());
                server.findChannelByName(this, in.nextLine());
            }


            while (activeChannel != null) {
                input = in.nextLine();

                switch (input) {
                    case "!quit" -> {
                        server.clientExit(this);
                        socket.close();
                    }
                    case "!clear" -> clear();
                    case "!chanlist" -> server.getActiveChannelsList(this);
                    default -> server.sendToChannel(input, this, activeChannel);
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchElementException e) {
            try {
                socket.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
            server.clientExit(this);
        }

    }
}