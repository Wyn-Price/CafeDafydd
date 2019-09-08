package com.wynprice.cafedafydd.client.netty;

import com.wynprice.cafedafydd.client.CafeDafyddMain;
import com.wynprice.cafedafydd.client.utils.FXUtils;
import com.wynprice.cafedafydd.common.netty.NetworkHandler;
import com.wynprice.cafedafydd.common.netty.packets.packets.clientbound.PacketDisplayError;
import com.wynprice.cafedafydd.common.netty.packets.packets.clientbound.PacketDisplayScreen;
import com.wynprice.cafedafydd.common.netty.packets.packets.clientbound.PacketHasDatabaseEntryResult;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

public class ClientNetworkHandler extends NetworkHandler {
    @Override
    protected void handlePacket(Object packet) {
        if(packet instanceof PacketDisplayError) {
            PacketDisplayError error = (PacketDisplayError) packet;
            Platform.runLater(() -> FXUtils.showBasicAlert(Alert.AlertType.ERROR, error.getTitle(), error.getReason(), ButtonType.OK));
        }
        if(packet instanceof PacketDisplayScreen) {
            PacketDisplayScreen screen = (PacketDisplayScreen) packet;
            Platform.runLater(() -> CafeDafyddMain.showPage(screen.getPage()));
        }
        if(packet instanceof PacketHasDatabaseEntryResult) {
            DatabaseCheck.receive(((PacketHasDatabaseEntryResult) packet).result());
        }
    }
}
