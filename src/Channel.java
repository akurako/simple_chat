import java.util.ArrayList;

public class Channel {
    String channelName;
    ArrayList<Client> channelUsers = new ArrayList<>();

    public Channel(String name){
        this.channelName = name;
    }

}
