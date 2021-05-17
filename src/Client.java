import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
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
            out.println("Enter your nickname?");
            this.nickname = in.nextLine();
            out.println("Welcome to the chat " + this.nickname);

            while (activeChannel == null) {
                out.println("Select channel: " + server.getChannelsList());
                server.findChannelByName(this, in.nextLine());
            }
            input = in.nextLine();
//            switch (input) {
//                case "!quit":
//            }
            while (!input.equals("!quit")) {
                server.sendToChannel(input, this,activeChannel);
                input = in.nextLine();
            }
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}