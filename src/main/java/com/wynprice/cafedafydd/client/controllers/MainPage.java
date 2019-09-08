package com.wynprice.cafedafydd.client.controllers;

import com.wynprice.cafedafydd.client.CafeDafyddMain;
import com.wynprice.cafedafydd.client.utils.AlertUtils;
import com.wynprice.cafedafydd.common.netty.packets.packets.serverbound.PacketLogin;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import lombok.SneakyThrows;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainPage {

    private static final MessageDigest MD5 = getDigest();

    private static final Matcher USERNAME_MATCHER = Pattern.compile("[a-zA-Z]|_|\\d").matcher("");

    @FXML public TextField usernameField;
    @FXML public PasswordField passwordField;

    @FXML
    public void submitButtonClicked() {
        if(this.usernameField.getText().isEmpty() || this.passwordField.getText().isEmpty()) {
            AlertUtils.showBasicAlert(Alert.AlertType.INFORMATION, "Input Error", "Password or Username fields are empty", ButtonType.OK);
        } else {
            SecureRandom rand = new SecureRandom((this.usernameField.getText() + this.passwordField.getText()).getBytes());
            byte[] abyte = new byte[256];
            rand.nextBytes(abyte);

            CafeDafyddMain.getClient().getHandler().sendPacket(new PacketLogin(this.usernameField.getText(), bytesToHex(MD5.digest(abyte))));
        }
    }


    @FXML
    public void onKeyUsername(KeyEvent keyEvent) {
        if(!USERNAME_MATCHER.reset(String.valueOf(keyEvent.getCharacter())).find()) {
            keyEvent.consume();
        }
    }

    @SneakyThrows(NoSuchAlgorithmException.class)
    private static MessageDigest getDigest() {
        return MessageDigest.getInstance("SHA-256");
    }


    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder();
        for (byte abyte : hash) {
            String hex = Integer.toHexString(0xff & abyte);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
