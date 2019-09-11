package com.wynprice.cafedafydd.client.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class FXUtils {

    public static void showBasicAlert(Alert.AlertType alertType, String title, String contentText, ButtonType... buttons) {
        Alert alert = new Alert(alertType, contentText, buttons);
        alert.setTitle(title);
        alert.setHeaderText("");
        alert.showAndWait();
    }
}
