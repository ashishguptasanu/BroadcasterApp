package broadcast.edge.com.broadcasterapp.Models;

/**
 * Created by Ashish on 19-01-2018.
 */

public class Comments {
    public Comments(String username, String commnets) {
        this.username = username;
        this.commnets = commnets;
    }
    public Comments(){}

    public String getUsername() {
        return username;
    }

    public String getCommnets() {
        return commnets;
    }

    String username, commnets;
}
