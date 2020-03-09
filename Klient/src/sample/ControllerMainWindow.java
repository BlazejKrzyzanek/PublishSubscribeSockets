package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class ControllerMainWindow implements Initializable{
    @FXML
    private Text helloNick = new Text();
    @FXML
    private ListView<String> mesView = new ListView<String>();
    @FXML
    private ComboBox comboBox = new ComboBox();
    @FXML
    private TextArea textArea = new TextArea();
    @FXML
    private Button sendButton = new Button();
    @FXML
    private Button readButton = new Button();

    private ObservableList<String> observableMessages = FXCollections.observableArrayList();
    ObservableList<String> topicList = FXCollections.observableArrayList("News", "Social media", "Technology", "Animals", "Music", "Film", "Lifestyle", "Sport", "Business", "Science");


    public void refresh(ArrayList<String> topicList){
        System.out.println("odswiezam");
        observableMessages.clear();
        for (String i:topicList) {
            //observableMessages.add(i.substring(0,10) + "...");
            observableMessages.add(i);
        }
        mesView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        mesView.setItems(observableMessages);
        mesView.refresh();

    }

    public void onActionSend() throws IOException {
        String message = "W";
        message += comboBox.getValue().toString();

        String content = textArea.getText();

        if(content.contains(";") || content.contains("|") || content.contains("{") || content.contains("}")){
            FXMLLoader loader2 = new FXMLLoader();
            loader2.setLocation(this.getClass().getResource("errorWindow.fxml"));
            Parent root = (Parent) loader2.load();
            ControllerError controllerError = loader2.getController();
            controllerError.initializeData("You can't use special characters: ; | { }");
            Stage stage2 = new Stage();
            stage2.setTitle("Error");
            stage2.setScene(new Scene(root));
            stage2.show();
        }
        else{
            message += ";" + content;

            //WYSLIJ message do serwera
            Main.client.sendMessage(message);
            System.out.println("wiadomosc: " + Main.client.recieveMessage());
            System.out.println(message);
        }


    }

    public void onActionRead() throws IOException {
        FXMLLoader loader2 = new FXMLLoader();
        loader2.setLocation(this.getClass().getResource("messageWindow.fxml"));
        Parent root = (Parent) loader2.load();
        ControllerMessage controllerMessage = loader2.getController();
        controllerMessage.initializeMessage(mesView.getSelectionModel().getSelectedItem());
        Stage stage2 = new Stage();
        stage2.setTitle("Message");
        stage2.setScene(new Scene(root));
        stage2.show();
    }

    public void initialize(URL location, ResourceBundle resources){
        helloNick.setText("Hello " + Main.user.getNickname() + "!");
        comboBox.setItems(topicList);
        comboBox.setValue("News");
    }
}
