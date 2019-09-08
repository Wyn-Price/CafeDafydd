package com.wynprice.cafedafydd.client.utils;

import javafx.beans.NamedArg;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

public class AlertUtils {

    public static void showBasicAlert(Alert.AlertType alertType, String title, String contentText, ButtonType... buttons) {
        Alert alert = new Alert(alertType, contentText, buttons);
        alert.setTitle(title);
        alert.setHeaderText("");
        alert.showAndWait();
    }
}
