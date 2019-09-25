package com.wynprice.cafedafydd.client.utils;

import javafx.scene.image.Image;
import lombok.Getter;

/**
 * A class containing the
 */
public enum Images {
    GREEN_TICK("green_tick"),
    RED_CROSS("red_cross");

    @Getter private final Image image;

    Images(String fileName) {
        this.image = new Image(Images.class.getResourceAsStream("/images/" + fileName + ".png"));
    }

}
