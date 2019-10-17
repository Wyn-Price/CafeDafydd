package com.wynprice.cafedafydd.client.controllers;

import com.wynprice.cafedafydd.client.CafeDafyddMain;
import com.wynprice.cafedafydd.client.netty.DatabaseRequest;
import com.wynprice.cafedafydd.common.BackupHeader;
import com.wynprice.cafedafydd.common.FieldDefinitions;
import com.wynprice.cafedafydd.common.netty.packets.serverbound.PacketRevertDatabase;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import lombok.Value;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class ViewBackupsPage implements BaseController {

    private final DateFormat format = new SimpleDateFormat();

    @FXML public ListView<BackupHeaderDelegate> headersList;
    @FXML public ComboBox<String> databaseComboBox;
    @FXML public TextArea backupEntryText;

    @Override
    public void onLoaded() {
        this.databaseComboBox.getItems().addAll(FieldDefinitions.NAME_TO_SCHEMA.keySet());
        this.databaseComboBox.valueProperty().addListener((observable, oldValue, newValue) -> this.sendHeaderRequest(newValue));
        this.headersList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                if(newValue != null) {
                    DatabaseRequest.BACKUP_ENTRIES.sendRequest(this.databaseComboBox.valueProperty().get(), list ->
                        backupEntryText.setText(String.join("\n", list)), newValue.getHeader().getId());
                }
            }
        );
    }

    @Override
    public void resync() {
        this.backupEntryText.setText("");
        String selectedItem = this.databaseComboBox.getSelectionModel().getSelectedItem();
        if(selectedItem != null) {
            this.sendHeaderRequest(selectedItem);
        }
    }

    private void sendHeaderRequest(String dbName) {
        DatabaseRequest.BACKUP_HEADERS.sendRequest(dbName, headers -> {
            this.headersList.getItems().clear();
            for (BackupHeader header : headers) {
                this.headersList.getItems().add(new BackupHeaderDelegate(header));
            }
        }, null);
    }

    @FXML
    public void backButtonClicked() {
        CafeDafyddMain.back();
    }

    @FXML
    public void revertButtonClicked() {
        String database = this.databaseComboBox.valueProperty().get();
        BackupHeaderDelegate header = this.headersList.getSelectionModel().getSelectedItem();
        if(database != null && header != null) {
            CafeDafyddMain.getClient().getHandler().sendPacket(new PacketRevertDatabase(database, header.header.getId()));
        }
    }

    @Value
    private class BackupHeaderDelegate {
        private final BackupHeader header;
        @Override
        public String toString() {
            return "Backup at: " + format.format(this.header.getBackupTime());
        }
    }
}
