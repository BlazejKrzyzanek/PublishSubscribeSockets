package sample;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.text.Text;

import java.net.URL;
import java.util.ResourceBundle;

public class ControllerMessage{
    @FXML
    private Text topicText = new Text();
    @FXML
    private Text contentText = new Text();

    public void initializeMessage(String message){
        String topic = "";
        int i=0;
        while(!message.substring(i,i+1).equals(";")) {
            System.out.println((message.substring(i, i + 1)));
            topic += message.substring(i, i + 1);
            //message = message.substring(i);
            System.out.println(message.substring(i));
            i++;
        }
        topicText.setText(topic);
        contentText.setText(message.substring(i+1));
    }

}
