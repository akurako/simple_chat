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

    public Client(Socket socket, ChatServer server) {
        new Thread(this).start();
        this.socket = socket;
        this.server = server;
    }

    public void recievedMessage(String message){
        out.println(message);
    }
    public void sendConfirmation(String timestamp){
        out.println("Send by you at"+timestamp);
    }

    public void run() {
        try {
            InputStream is = socket.getInputStream();
            OutputStream os = socket.getOutputStream();

            in = new Scanner(is);
            out = new PrintStream(os);

            out.println("Enter your nickname?");
            this.nickname = in.nextLine();
            out.println("Wellcome to the chat "+this.nickname);
            String input = in.nextLine();
            while (!input.equals("!quit")) {
                server.sendAll(input,this);
                input = in.nextLine();
            }
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}