package com.wynprice.cafedafydd.client.controllers;

import com.wynprice.cafedafydd.client.CafeDafyddMain;
import com.wynprice.cafedafydd.client.netty.DatabaseRequest;
import com.wynprice.cafedafydd.common.DatabaseStrings;
import com.wynprice.cafedafydd.common.netty.packets.serverbound.PacketEditDatabaseField;
import com.wynprice.cafedafydd.common.netty.packets.serverbound.PacketEditRecordDirect;
import com.wynprice.cafedafydd.common.utils.DatabaseRecord;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.util.Callback;

import java.util.Arrays;

public class DatabaseDirectEditPage implements BaseController {
    private static final Object COLUMN_PROPERTY_KEY = new Object();

    @FXML public TableView<DatabaseRecord> contents;
    @FXML public ComboBox<String> databaseCombobox;
    @FXML public TextField editedField;
    @FXML public Button plusButton;
    @FXML public Button minusButton;

    private ChangeListener<String> editedFieldListener;
    @Override
    public void onLoaded() {
        this.plusButton.setFocusTraversable(false);
        this.minusButton.setFocusTraversable(false);

        //Don't have any buttons with the press enter to move on. Everything on this page should be done manually.
        Platform.runLater(() -> this.plusButton.getScene().getAccelerators().clear());

        this.contents.getSelectionModel().cellSelectionEnabledProperty().set(true);
        this.contents.getSelectionModel().getSelectedCells().addListener((ListChangeListener<TablePosition>) c -> {
            ObservableList<? extends TablePosition<DatabaseRecord, String>> list = (ObservableList<? extends TablePosition<DatabaseRecord, String>>) c.getList();

            if(list.isEmpty() || this.contents.getFocusModel().getFocusedItem() == null) {
                return;
            }

            TablePosition<DatabaseRecord, String> pos = list.get(0);

            if(pos.getColumn() == 0) { //Can't edit ID
                return;
            }
            DatabaseRecord record = this.contents.getItems().get(pos.getRow());
            String field = record.getFields().get(pos.getColumn() - 1);
            if(this.editedFieldListener != null) {
                this.editedField.textProperty().removeListener(this.editedFieldListener);
            }
            String string = record.getField(field).getAsFileString().toString();
            if(!string.equals(this.editedField.getText())) {
                this.editedField.setText(string);
                this.editedField.setDisable(false);
                this.editedField.textProperty().addListener(this.editedFieldListener = (observable, oldValue, newValue) ->
                    CafeDafyddMain.getClient().getHandler().sendPacket(new PacketEditDatabaseField(this.databaseCombobox.valueProperty().get(), record.getPrimaryField(), field, newValue)));
            }
        });


        for (String file : DatabaseStrings.ALL_FILENAMES) {
            this.databaseCombobox.getItems().add(file);
        }
        this.databaseCombobox.valueProperty().addListener((observable, oldValue, newValue) -> Platform.runLater(this::resync));
    }

    @Override
    public void resync() {
        if(this.databaseCombobox.valueProperty().get() != null) {
            DatabaseRequest.GET_ENTRIES.sendRequest(this.databaseCombobox.valueProperty().get(), records -> Platform.runLater(() -> {
                this.contents.getColumns().clear();
                this.contents.getItems().clear();
                boolean setHeaders = false;
                for (DatabaseRecord record : records) {
                    if(!setHeaders) {

                        TableColumn<DatabaseRecord, String> idColumn = new TableColumn<>();
                        idColumn.setPrefWidth(-1);
                        idColumn.setCellValueFactory(param -> new ReadOnlyStringWrapper(String.valueOf(param.getValue().getPrimaryField())));
                        idColumn.setEditable(false);
                        idColumn.setText("ID");
                        this.contents.getColumns().add(idColumn);

                        for (String field : record.getFields()) {
                            TableColumn<DatabaseRecord, String> column = new TableColumn<>();
                            column.getProperties().put(COLUMN_PROPERTY_KEY, field);
                            column.setPrefWidth(-1);
                            column.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().getField(field).getAsFileString().toString()));
                            column.setText(field);
                            column.setEditable(true);

                            this.contents.getColumns().add(column);
                        }
                        setHeaders = true;
                    }

                    this.contents.getItems().add(record);
                }
            }));
        }
    }

    @FXML
    public void backButtonClicked() {
        CafeDafyddMain.back();
    }

    @FXML
    public void addEntryClicked() {
        CafeDafyddMain.getClient().getHandler().sendPacket(new PacketEditRecordDirect(this.databaseCombobox.valueProperty().get(), -1));
    }

    @FXML
    public void removeEntryClicked() {
        CafeDafyddMain.getClient().getHandler().sendPacket(new PacketEditRecordDirect(this.databaseCombobox.valueProperty().get(), this.contents.getSelectionModel().getSelectedItem().getPrimaryField()));
    }

}
