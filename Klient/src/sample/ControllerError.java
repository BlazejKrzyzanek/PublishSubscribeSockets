package sample;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class ControllerError {
    @FXML
    private Text errorText = new Text();
    @FXML
    private Button okButton = new Button();

    public void onActionOk(){
        Stage stage = (Stage) okButton.getScene().getWindow();
        stage.close();
    }

    public void initializeData(String message){
        errorText.setText(message);
    }
}
