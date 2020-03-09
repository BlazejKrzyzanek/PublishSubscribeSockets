package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;

public class Main extends Application {

    public static User user = new User();
    public static  ControllerMainWindow controllerMainWindow;
    public static Client client = new Client();
    public static Thread clientThread = new Thread(client);
    public static ArrayList<String> messList = new ArrayList<>();

    public static void startMainWindow(ControllerLogin controllerLogin) throws IOException {
        FXMLLoader loader2 = new FXMLLoader();
        loader2.setLocation(controllerLogin.getClass().getResource("mainWindow.fxml"));
        Parent root = (Parent) loader2.load();
        controllerMainWindow = loader2.getController();
        Stage stage2 = new Stage();
        stage2.setTitle("Client");
        stage2.setScene(new Scene(root));
        stage2.show();
    }

    public static void startMainWindowReg(ControllerRegister controllerRegister) throws IOException {
        FXMLLoader loader2 = new FXMLLoader();
        loader2.setLocation(controllerRegister.getClass().getResource("mainWindow.fxml"));
        Parent root = (Parent) loader2.load();
        controllerMainWindow = loader2.getController();
        Stage stage2 = new Stage();
        stage2.setTitle("Client");
        stage2.setScene(new Scene(root));
        stage2.show();
    }


    public static void refreshList(String message){
        messList.add(message);
        user.setMesssagesList(messList);
        controllerMainWindow.refresh(messList);
    }

    public static void startClient(){
        clientThread.start();
    }

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(this.getClass().getResource("loginWindow.fxml"));
        VBox vBox = loader.load();
        Scene scene = new Scene(vBox);

        ControllerLogin kontroler = loader.getController();
        primaryStage.setScene(scene);
        primaryStage.setTitle("Login");
        primaryStage.show();

    }


    public static void main(String[] args) throws IOException {
        launch(args);
    }
}
