package com.wynprice.cafedafydd.client.utils;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.NamedArg;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Tooltip;
import javafx.util.Duration;
import lombok.extern.log4j.Log4j2;
import org.omg.CORBA.DoubleHolder;

import java.lang.reflect.Field;

@Log4j2
public class FXUtils {

    public static void showBasicAlert(Alert.AlertType alertType, String title, String contentText, ButtonType... buttons) {
        Alert alert = new Alert(alertType, contentText, buttons);
        alert.setTitle(title);
        alert.setHeaderText("");
        alert.showAndWait();
    }

    public static void hackTooltipTimer(Node parent, Tooltip tooltip, int millis) {
        try {
            Field fieldBehavior = tooltip.getClass().getDeclaredField("BEHAVIOR");
            fieldBehavior.setAccessible(true);
            Object objBehavior = fieldBehavior.get(tooltip);

            Field fieldTimer = objBehavior.getClass().getDeclaredField("activationTimer");
            fieldTimer.setAccessible(true);
            Timeline objTimer = (Timeline) fieldTimer.get(objBehavior);

            objTimer.getKeyFrames().clear();
            objTimer.getKeyFrames().add(new KeyFrame(new Duration(millis)));
        } catch (Exception e) {
            log.error("Unable to hack tooltip delay timer to be " + millis + "ms. Timer: " + tooltip, e);
        }

        parent.setOnMouseMoved(m -> {
            tooltip.setX(m.getScreenX() - tooltip.getWidth()/2);
            tooltip.setY(m.getScreenY() - tooltip.getHeight());
        });

    }
}
