package com.wynprice.cafedafydd.client.netty;

import com.sun.istack.internal.Nullable;
import com.sun.javafx.application.PlatformImpl;
import com.wynprice.cafedafydd.client.CafeDafyddMain;
import com.wynprice.cafedafydd.client.controllers.BaseController;
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

/**
 * The client network handler. Used to handle the different incoming packets.
 */
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
        //On the javafx thread, display the error to the screen
        Platform.runLater(() -> FXUtils.showBasicAlert(Alert.AlertType.ERROR, error.getTitle(), error.getReason(), ButtonType.OK));
    }

    @NetworkHandle
    public void handleDisplayScreen(PacketDisplayScreen screen) {
        //On the javafx thread, display the specified page
        Platform.runLater(() -> CafeDafyddMain.showPage(screen.getPage()));
    }

    @NetworkHandle
    public void handleDatabaseRequestResult(PacketHasDatabaseEntryResult result) {
        //Handle the received database has entry result
        DatabaseRequest.HAS_ENTRY.receive(result.requestID(), result.result());
    }

    @NetworkHandle
    public void handleRequestEntriesResult(PacketDatabaseEntriesResult packet) {
        //Handle the received database request result
        switch (packet.getType()) {
            case GET:
                DatabaseRequest.GET_ENTRIES.receive(packet.getRequestID(), packet.getRecords());
                break;
            case SEARCH:
                DatabaseRequest.SEARCH_ENTRIES.receive(packet.getRequestID(), packet.getRecords());
                break;
        }
    }

    @NetworkHandle
    public void handleConfirmLogin(PacketConfirmLogin packet) {
        //Set the current username to the confirmed login name.
        this.currentUsername = packet.getUsername();
    }

    @NetworkHandle
    public void handleResync(PacketCauseResync packet) {
        //Get the current controller and resync it.
        PlatformImpl.runLater(() -> CafeDafyddMain.getController(BaseController.class).ifPresent(BaseController::resync));
    }

    @NetworkHandle
    public void handleHeadersResult(PacketBackupHeadersResult packet) {
        DatabaseRequest.BACKUP_HEADERS.receive(packet.getRequestID(), packet.getHeaderList());
    }
}
