package sample;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ControllerRegister {
    @FXML
    private Text nickText = new Text();
    @FXML
    private TextField nickField = new TextField();
    @FXML
    private Text topicText = new Text();
    @FXML
    private CheckBox newsCheck = new CheckBox();
    @FXML
    private CheckBox socialCheck = new CheckBox();
    @FXML
    private CheckBox technoCheck = new CheckBox();
    @FXML
    private CheckBox animalsCheck = new CheckBox();
    @FXML
    private CheckBox musicCheck = new CheckBox();
    @FXML
    private CheckBox filmCheck = new CheckBox();
    @FXML
    private CheckBox lifeCheck = new CheckBox();
    @FXML
    private CheckBox sportCheck = new CheckBox();
    @FXML
    private CheckBox businessCheck = new CheckBox();
    @FXML
    private CheckBox scienceCheck = new CheckBox();
    @FXML
    private Button registerButton = new Button();
    @FXML
    private Text passText = new Text();
    @FXML
    private TextField passField = new TextField();
    @FXML
    private TextField ipField = new TextField();

    public void onActionRegister() throws IOException, InterruptedException {
        Main.user.setNickname(nickField.getText());
        String log = nickField.getText();
        String pass = passField.getText();

        if(log.contains(";") || log.contains("|") || log.contains("{") || log.contains("}") || pass.contains(";") || pass.contains("|") || pass.contains("{") || pass.contains("}")){
            FXMLLoader loader2 = new FXMLLoader();
            loader2.setLocation(this.getClass().getResource("errorWindow.fxml"));
            Parent root = (Parent) loader2.load();
            ControllerError controllerError = loader2.getController();
            controllerError.initializeData("You can't use special characters: ; | { } in login or password!");
            Stage stage2 = new Stage();
            stage2.setTitle("Error");
            stage2.setScene(new Scene(root));
            stage2.show();
        }
        else {
            Main.client.setIp(ipField.getText());

            String codeLogin = "L" + nickField.getText() + ";" + passField.getText();

            ArrayList<String> tList = new ArrayList<String>();

            if (newsCheck.isSelected()) {
                tList.add("News");
            }
            if (socialCheck.isSelected()) {
                tList.add("Social media");
            }
            if (technoCheck.isSelected()) {
                tList.add("Technology");
            }
            if (animalsCheck.isSelected()) {
                tList.add("Animals");
            }
            if (musicCheck.isSelected()) {
                tList.add("Music");
            }
            if (filmCheck.isSelected()) {
                tList.add("Film");
            }
            if (lifeCheck.isSelected()) {
                tList.add("Lifestyle");
            }
            if (sportCheck.isSelected()) {
                tList.add("Sport");
            }
            if (businessCheck.isSelected()) {
                tList.add("Business");
            }
            if (scienceCheck.isSelected()) {
                tList.add("Science");
            }


            //WYSYLA wiadomosc codeLogin do serwer
            Main.client.startConnection(8887);
            Thread.sleep(1000);
            Main.client.sendMessage(codeLogin);
            System.out.println(Main.client.recieveMessage());
            Thread.sleep(1000);


            String codedRegister = "R";

            for (int i = 0; i < tList.size() - 1; i++) {
                codedRegister = codedRegister + tList.get(i) + ";";
            }
            codedRegister += tList.get(tList.size() - 1);

            System.out.println(codedRegister);

            //WYSLIJ wiadomosc codeRegister do serwera
            Main.client.sendMessage(codedRegister);
            Thread.sleep(1000);

            System.out.println(Main.client.recieveMessage());

            Main.user.setNickname(nickField.getText());
            Stage stage = (Stage) registerButton.getScene().getWindow();
            stage.close();

            Main.startMainWindowReg(this);
            Main.startClient();
        }
    }


}
