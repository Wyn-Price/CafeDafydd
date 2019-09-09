package com.wynprice.cafedafydd.client.netty;

import com.wynprice.cafedafydd.client.CafeDafyddMain;
import com.wynprice.cafedafydd.client.utils.FXUtils;
import com.wynprice.cafedafydd.common.netty.NetworkHandler;
import com.wynprice.cafedafydd.common.netty.packets.packets.clientbound.PacketDisplayError;
import com.wynprice.cafedafydd.common.netty.packets.packets.clientbound.PacketDisplayScreen;
import com.wynprice.cafedafydd.common.netty.packets.packets.clientbound.PacketHasDatabaseEntryResult;
import com.wynprice.cafedafydd.common.utils.NetworkConsumer;
import com.wynprice.cafedafydd.common.utils.NetworkHandle;
import com.wynprice.cafedafydd.common.utils.NetworkHandleScanner;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

public class ClientNetworkHandler extends NetworkHandler {

    private static final NetworkConsumer CONSUMER = NetworkHandleScanner.generateConsumer(ClientNetworkHandler.class);

    public ClientNetworkHandler() {
        super(CONSUMER);
    }

    @NetworkHandle
    public void handleDisplayError(PacketDisplayError error) {
        Platform.runLater(() -> FXUtils.showBasicAlert(Alert.AlertType.ERROR, error.getTitle(), error.getReason(), ButtonType.OK));
    }

    @NetworkHandle
    public void handleDisplayScreen(PacketDisplayScreen screen) {
        Platform.runLater(() -> CafeDafyddMain.showPage(screen.getPage()));
    }

    @NetworkHandle
    public void handleDatabaseRequestResult(PacketHasDatabaseEntryResult result) {
        DatabaseCheck.receive(result.result());
    }
}
