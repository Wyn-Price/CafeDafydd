package com.wynprice.cafedafydd.client.controllers;

import com.wynprice.cafedafydd.client.CafeDafyddMain;
import com.wynprice.cafedafydd.client.utils.FXUtils;
import com.wynprice.cafedafydd.common.netty.packets.serverbound.PacketChangePassword;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;

import static com.wynprice.cafedafydd.common.utils.PasswordUtils.generatePasswordHash;

public class ChangePasswordPage implements BaseController {

    @FXML public PasswordField currentPassword;
    @FXML public PasswordField passwordField;
    @FXML public PasswordField repeatPasswordField;
    @FXML public Label errorMessage;

    @Override
    public void onLoaded() {
        this.passwordField.textProperty().addListener((observable, oldValue, newValue) -> this.checkPasswordsMatch(newValue, this.repeatPasswordField.getText()));
        this.repeatPasswordField.textProperty().addListener((observable, oldValue, newValue) -> this.checkPasswordsMatch(newValue, this.passwordField.getText()));
    }

    private void checkPasswordsMatch(String normal, String repeat) {
        if(!normal.equals(repeat)) {
            this.errorMessage.setText("Passwords to not match!");
        } else {
            this.errorMessage.setText("");
        }
    }

    @FXML
    public void changePasswordButtonClicked() {
        String username = CafeDafyddMain.getClient().getHandler().getCurrentUsername();
        if(username == null) {
            FXUtils.showBasicAlert(Alert.AlertType.INFORMATION, "Internal Error", "Username was null. This happens when the client is not logged in. Something has gone very wrong.", ButtonType.OK);
            return;
        }
        if(!this.passwordField.getText().equals(this.repeatPasswordField.getText())) {
            FXUtils.showBasicAlert(Alert.AlertType.ERROR, "Input Error", "Passwords do not match", ButtonType.OK);
            return;
        }
        CafeDafyddMain.getClient().getHandler().sendPacket(new PacketChangePassword(generatePasswordHash(username, this.currentPassword.getText()), generatePasswordHash(username, this.passwordField.getText())));
    }

    @FXML
    public void goBack() {
        CafeDafyddMain.back();
    }
}
