package logintime;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Login {
    private final SimpleStringProperty time;
    private final SimpleStringProperty username;
    private final SimpleStringProperty computerName;


    public Login(String t, String u, String c) {
        this.time = new SimpleStringProperty(t);
        this.username = new SimpleStringProperty(u);
        this.computerName = new SimpleStringProperty(c);
    }

    public String getTime() {
        return time.get();
    }

    public void setTime(String time){
        this.time.set(time);
    }

    public StringProperty timeProperty(){
        return time;
    }

    String getUsername() {
        return username.get();
    }

    public void setUsername(String username){
        this.username.set(username);
    }

    public StringProperty usernameProperty(){
        return username;
    }

    String getComputerName() {
        return computerName.get();
    }

    public void setComputerName(String computerName){
        this.computerName.set(computerName);
    }

    public StringProperty computerNameProperty(){
        return computerName;
    }
}
