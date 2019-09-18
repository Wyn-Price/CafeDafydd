package com.wynprice.cafedafydd.client.controllers;

import com.wynprice.cafedafydd.client.CafeDafyddMain;
import com.wynprice.cafedafydd.client.netty.DatabaseRequest;
import com.wynprice.cafedafydd.client.utils.FXUtils;
import com.wynprice.cafedafydd.common.DatabaseStrings;
import com.wynprice.cafedafydd.common.netty.packets.serverbound.PacketStartSession;
import com.wynprice.cafedafydd.common.utils.DatabaseRecord;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import lombok.Value;

public class CreateSessionPage implements BaseController {

    @FXML
    public ComboBox<Computers> systemsComboBox;

    @Override
    public void resync() {
        this.systemsComboBox.getItems().clear();
        DatabaseRequest.GET_ENTRIES.sendRequest(DatabaseStrings.Computers.FILE_NAME, records -> {
            for (DatabaseRecord record : records) {
                if(record.getField(DatabaseStrings.Computers.SESSION_ID).equals("-1")) {
                    this.systemsComboBox.getItems().add(new Computers(record.getPrimaryField(), "Type: " + record.getField(DatabaseStrings.Computers.OS)));
                }
            }
        });
    }

    @FXML
    public void startButtonClick() {
        if(this.systemsComboBox.getValue() != null) {
            CafeDafyddMain.getClient().getHandler().sendPacket(new PacketStartSession(this.systemsComboBox.getValue().getId()));
        } else {
            FXUtils.showBasicAlert(Alert.AlertType.ERROR, "No system Selected", "You need to select a system to start the session", ButtonType.OK);
        }
    }


    @Value
    private class Computers {
        private final int id;
        private final String computerString;

        @Override
        public String toString() {
            return this.computerString;
        }
    }
}
