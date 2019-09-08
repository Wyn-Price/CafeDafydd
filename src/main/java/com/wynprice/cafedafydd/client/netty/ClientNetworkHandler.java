package com.wynprice.cafedafydd.client.netty;

import com.wynprice.cafedafydd.client.utils.AlertUtils;
import com.wynprice.cafedafydd.common.netty.NetworkHandler;
import com.wynprice.cafedafydd.common.netty.packets.packets.clientbound.PacketLoginIncorrect;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

public class ClientNetworkHandler extends NetworkHandler {
    @Override
    protected void handlePacket(Object packet) {
        if(packet instanceof PacketLoginIncorrect) {
            Platform.runLater(() ->
                AlertUtils.showBasicAlert(Alert.AlertType.ERROR, "Invalid Credentials", "Username or Password is incorrect.\nPlease contact a member of staff to change your password", ButtonType.OK));
        }
    }
}
