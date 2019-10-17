package com.wynprice.cafedafydd.client.controllers;

import com.wynprice.cafedafydd.client.CafeDafyddMain;
import com.wynprice.cafedafydd.client.netty.DatabaseRequest;
import com.wynprice.cafedafydd.client.utils.FXUtils;
import com.wynprice.cafedafydd.common.FieldDefinitions;
import com.wynprice.cafedafydd.common.FieldDefinitions.Computers;
import com.wynprice.cafedafydd.common.netty.packets.serverbound.PacketStartSession;
import com.wynprice.cafedafydd.common.utils.DatabaseRecord;
import com.wynprice.cafedafydd.common.utils.FormBuilder;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import lombok.RequiredArgsConstructor;
import lombok.Value;

public class CreateSessionPage implements BaseController {

    @FXML public ComboBox<ComputerEntry> systemsComboBox;
    @FXML public ListView<GameEntry> gameListView;

    @Override
    public void onLoaded() {
        this.resync();
        this.systemsComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            this.gameListView.getItems().clear();
            for (Integer computerId : newValue.computerIds) {
                DatabaseRequest.GET_ENTRIES.sendRequest(FieldDefinitions.Games.FILE_NAME, records -> {
                    for (DatabaseRecord record : records) {
                        this.gameListView.getItems().add(new GameEntry(record.get(FieldDefinitions.Games.GAME_NAME), record.get(FieldDefinitions.Games.GAME_RATING)));
                    }
                }, FormBuilder.create().with(FieldDefinitions.ID, computerId).getForm());
            }
        });
    }

    @Override
    public void resync() {
        this.systemsComboBox.getItems().clear();
        DatabaseRequest.GET_ENTRIES.sendRequest(Computers.FILE_NAME, records -> {
            for (DatabaseRecord record : records) {
                if(record.get(Computers.SESSION_ID) == -1) {
                    this.systemsComboBox.getItems().add(new ComputerEntry(record.getPrimaryField(), record.get(Computers.OS), record.get(Computers.PRICE_PER_HOUR), record.get(Computers.INSTALLED_GAMES)));
                }
            }
        });
    }

    @FXML
    public void startButtonClick() {
        if(this.systemsComboBox.getValue() != null) {
            CafeDafyddMain.getClient().getHandler().sendPacket(new PacketStartSession(this.systemsComboBox.getValue().id));
        } else {
            FXUtils.showBasicAlert(Alert.AlertType.ERROR, "No system Selected", "You need to select a system to start the session", ButtonType.OK);
        }
    }

    @FXML
    public void goBack() {
        CafeDafyddMain.back();
    }


    @RequiredArgsConstructor
    private class ComputerEntry {
        private final int id;
        private final String computerString;
        private final float pricePerHour;
        private final Integer[] computerIds;

        @Override
        public String toString() {
            return "Type " + this.computerString + ": £" + this.pricePerHour + "/hour";
        }
    }

    @RequiredArgsConstructor
    private class GameEntry {
        private final String gameName;
        private final String gameRating;

        @Override
        public String toString() {
            return this.gameName + ": " + this.gameRating;
        }
    }
}
