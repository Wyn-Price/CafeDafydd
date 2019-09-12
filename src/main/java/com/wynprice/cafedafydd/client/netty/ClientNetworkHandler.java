package com.wynprice.cafedafydd.client.netty;

import com.sun.istack.internal.Nullable;
import com.wynprice.cafedafydd.client.CafeDafyddMain;
import com.wynprice.cafedafydd.client.utils.FXUtils;
import com.wynprice.cafedafydd.common.netty.NetworkHandler;
import com.wynprice.cafedafydd.common.netty.packets.clientbound.*;
import com.wynprice.cafedafydd.common.utils.NetworkConsumer;
import com.wynprice.cafedafydd.common.utils.NetworkHandle;
import com.wynprice.cafedafydd.common.utils.NetworkHandleScanner;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import lombok.Getter;
import lombok.Setter;

public class ClientNetworkHandler extends NetworkHandler {

    private static final NetworkConsumer CONSUMER = NetworkHandleScanner.generateConsumer(ClientNetworkHandler.class);

    @Nullable
    @Setter
    @Getter
    private String currentUsername = null;

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
        DatabaseRequest.HAS_ENTRY.receive(result.requestID(), result.result());
    }

    @NetworkHandle
    public void handleDatabaseRequestEntriesResult(PacketDatabaseEntriesResult packet) {
        DatabaseRequest.GET_ENTRIES.receive(packet.getRequestID(), packet.getRecords());
    }

    @NetworkHandle
    public void handleConfirmLogin(PacketConfirmLogin packet) {
        this.currentUsername = packet.getUsername();
    }
}
