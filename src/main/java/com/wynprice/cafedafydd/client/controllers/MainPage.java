package com.wynprice.cafedafydd.client.controllers;

import com.wynprice.cafedafydd.client.CafeDafyddMain;
import com.wynprice.cafedafydd.client.utils.FXUtils;
import com.wynprice.cafedafydd.common.netty.packets.packets.serverbound.PacketLogin;
import com.wynprice.cafedafydd.common.utils.PasswordUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainPage implements BaseController{

    public static final Matcher USERNAME_MATCHER = Pattern.compile("[a-zA-Z]|_|\\d").matcher("");

    @FXML public TextField usernameField;
    @FXML public PasswordField passwordField;

    @FXML
    public void submitButtonClicked() {
        if(this.usernameField.getText().isEmpty() || this.passwordField.getText().isEmpty()) {
            FXUtils.showBasicAlert(Alert.AlertType.INFORMATION, "Input Error", "Password or Username fields are empty", ButtonType.OK);
        } else {
            CafeDafyddMain.getClient().getHandler().sendPacket(new PacketLogin(this.usernameField.getText(), PasswordUtils.genetatePasswordHash(this.usernameField.getText(), this.passwordField.getText())));
        }
    }


    @FXML
    public void onKeyUsername(KeyEvent keyEvent) {
        if(!USERNAME_MATCHER.reset(String.valueOf(keyEvent.getCharacter())).find()) {
            keyEvent.consume();
        }
    }





}
