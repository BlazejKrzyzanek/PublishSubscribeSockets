package sample;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;

public class ControllerLogin {
    @FXML
    private Button loginButton = new Button();
    @FXML
    private TextField loginText = new TextField();
    @FXML
    private TextField passField = new TextField();
    @FXML
    private Button registerButton = new Button();
    @FXML
    private Text nickText = new Text();
    @FXML
    private Text passText = new Text();
    @FXML
    private TextField ipField = new TextField();

    public void onActionLogin () throws IOException, InterruptedException {

        Main.user.setNickname(loginText.getText());

        String log = loginText.getText();
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

            String codeLogin = "L" + log + ";" + pass;

            //WYSLIJ wiadomosc do codeLogin do serwer
            Main.client.startConnection(8887);
            Thread.sleep(1000);
            Main.client.sendMessage(codeLogin);
            System.out.println("wiadomosc:" + Main.client.recieveMessage());
            System.out.println(codeLogin);
            Thread.sleep(1000);

            String response;
            response = Main.client.recieveMessage();
            if (response.equals("Wrong password!") || response.equals("You can use only one client at the same time!") || response.equals("You are already logged in!")) {
                FXMLLoader loader2 = new FXMLLoader();
                loader2.setLocation(this.getClass().getResource("errorWindow.fxml"));
                Parent root = (Parent) loader2.load();
                ControllerError controllerError = loader2.getController();
                controllerError.initializeData(response);
                Stage stage2 = new Stage();
                stage2.setTitle("Error");
                stage2.setScene(new Scene(root));
                stage2.show();
            } else {
                Main.startMainWindow(this);
                Main.startClient();
                Stage stage = (Stage) loginButton.getScene().getWindow();
                stage.close();
            }
        }
    }

    public void onActionRegister() throws IOException {
        Stage stage = (Stage) registerButton.getScene().getWindow();
        stage.close();

        FXMLLoader loader2 = new FXMLLoader();
        loader2.setLocation(this.getClass().getResource("registerWindow.fxml"));
        Parent root = (Parent) loader2.load();
        ControllerRegister controllerRegister = loader2.getController();
        Stage stage2 = new Stage();
        stage2.setTitle("Register");
        stage2.setScene(new Scene(root));
        stage2.show();
    }

}
