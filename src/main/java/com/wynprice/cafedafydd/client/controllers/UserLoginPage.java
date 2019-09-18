package com.wynprice.cafedafydd.client.controllers;

import com.sun.istack.internal.Nullable;
import com.sun.javafx.application.PlatformImpl;
import com.wynprice.cafedafydd.client.CafeDafyddMain;
import com.wynprice.cafedafydd.client.netty.DatabaseRequest;
import com.wynprice.cafedafydd.common.Page;
import com.wynprice.cafedafydd.common.netty.packets.serverbound.PacketCanStartSession;
import com.wynprice.cafedafydd.common.netty.packets.serverbound.PacketStopSession;
import com.wynprice.cafedafydd.common.utils.DatabaseRecord;
import com.wynprice.cafedafydd.client.utils.DateUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import lombok.Data;

import java.util.Date;

import static com.wynprice.cafedafydd.common.DatabaseStrings.Sessions;


public class UserLoginPage implements BaseController {

    @FXML public Button okayButton;
    @FXML public ListView<Session> sessionList;
    @FXML public Button deleteButton;

    @Override
    public void onLoaded() {
        this.resync();
        this.sessionList.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            boolean disabled = newValue.intValue() < 0;
            if(newValue.intValue() > 0) {
                Session session = this.sessionList.getItems().get(newValue.intValue());
                disabled |= session.endData != DateUtils.EMPTY_DATE;
            }
            this.deleteButton.setDisable(disabled);
        });
    }

    @Override
    public void resync() {
        this.sessionList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        this.sessionList.getItems().clear();
        DatabaseRequest.GET_ENTRIES.sendRequest(Sessions.FILE_NAME, records -> PlatformImpl.runLater(() -> {
            for (DatabaseRecord record : records) {
                this.sessionList.getItems().add(new Session(
                    record.getPrimaryField(),
                    record.getField(Sessions.COMPUTER_ID),
                    DateUtils.fromISO8691(record.getField(Sessions.ISO8601_START), true),
                    DateUtils.fromISO8691(record.getField(Sessions.ISO8601_END), false)
                ));
            }
        }), Sessions.USER_ID, "$$userid");
    }

    @FXML
    public void enterButtonClicked() {
        CafeDafyddMain.getClient().logout();
    }

    @FXML
    public void changePasswordClicked() {
        CafeDafyddMain.showPage(Page.CHANGE_PASSWORD);
    }

    @FXML
    public void addSessionButton() {
        CafeDafyddMain.getClient().getHandler().sendPacket(new PacketCanStartSession());
    }

    @FXML
    public void removeSessionButton() {
        Session selected = this.sessionList.getSelectionModel().getSelectedItem();
        if(selected != null) {
            CafeDafyddMain.getClient().getHandler().sendPacket(new PacketStopSession(selected.fieldID));
        }
    }

    @Data
    private class Session {
        private final int fieldID;
        private final String computerID;
        private final Date startDate;
        private final Date endData;

    @Override
    public String toString() {
        String state = this.endData == DateUtils.EMPTY_DATE ? "STARTED" : this.endData + " NOTPAID";
        return
            "ID: " + this.fieldID + "  " +
            "PC: " + this.computerID + "  " +
            "Start: " + this.startDate + "  " +
            "State: " + state;
    }
}

}
