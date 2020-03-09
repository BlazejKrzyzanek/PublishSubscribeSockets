package sample;

import java.util.ArrayList;

public class User {
    private String nickname;

    private ArrayList<String> messsagesList = new ArrayList<String>();

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public ArrayList<String> getMesssagesList() {
        return messsagesList;
    }

    public void setMesssagesList(ArrayList<String> messsagesList) {
        this.messsagesList = messsagesList;
    }
}
