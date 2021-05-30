import java.util.Scanner;

public class ServerScanner implements Runnable {

    Scanner scanner = new Scanner(System.in);
    String input;
    @Override
    public void run() {
        while (true){
            input=scanner.nextLine();
            switch (input) {
                case "!help" -> {
                    System.out.println("Available commands:" +
                            "\r\n!shutdown");
                }
                case "!shutdown" -> {}
                    default -> {}
            }
        }

    }
}
