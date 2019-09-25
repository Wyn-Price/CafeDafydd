package com.wynprice.cafedafydd.client.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import lombok.extern.log4j.Log4j2;

/**
 * A util class for javafx
 */
public class FXUtils {

    /**
     * Displays a basic alert with an error.
     * @param alertType The alert type to display
     * @param title The title of the alert
     * @param contentText the text of the alert
     * @param buttons the buttons to display with the alert
     * @see Alert#Alert(Alert.AlertType, String, ButtonType...)
     */
    public static void showBasicAlert(Alert.AlertType alertType, String title, String contentText, ButtonType... buttons) {
        Alert alert = new Alert(alertType, contentText, buttons);
        alert.setTitle(title);
        alert.setHeaderText("");
        alert.showAndWait();
    }
}
