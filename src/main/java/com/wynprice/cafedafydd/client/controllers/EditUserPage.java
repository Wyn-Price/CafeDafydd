package com.wynprice.cafedafydd.client.controllers;

import com.wynprice.cafedafydd.client.CafeDafyddMain;
import com.wynprice.cafedafydd.client.netty.DatabaseRequest;
import com.wynprice.cafedafydd.common.netty.packets.serverbound.PacketTryEditDatabase;
import com.wynprice.cafedafydd.common.utils.DatabaseRecord;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;

import static com.wynprice.cafedafydd.common.DatabaseStrings.ID;
import static com.wynprice.cafedafydd.common.DatabaseStrings.Users;

public class EditUserPage implements BaseController {
    private int id = -1;

    @FXML public Label errorField;
    @FXML public TextField usernameField;
    @FXML public TextField emailField;

    @FXML
    public void onKeyUsername(KeyEvent keyEvent) {
        if(!"\b".equals(keyEvent.getCharacter()) && !MainPage.USERNAME_MATCHER.reset(keyEvent.getCharacter()).find()) {
            keyEvent.consume();
            this.errorField.setText("Username must only contain characters, numbers and underscores. '" + keyEvent.getCharacter() + "' is not allowed.");
        }
    }

    @Override
    public void resync() {
        if(this.id < 0) {
            return;
        }

        DatabaseRequest.GET_ENTRIES.sendRequest(Users.FILE_NAME, records -> {
            if(records.isEmpty()) {
                throw new IllegalArgumentException("Returned Record Shouldn't be empty. Couldn't find user with id " + this.id);
            }
            DatabaseRecord record = records.get(0);
            this.usernameField.setText(record.getField(Users.USERNAME));
            this.emailField.setText(record.getField(Users.EMAIL));
        }, ID, String.valueOf(this.id));
    }

    @FXML
    public void editUser() {
        CafeDafyddMain.getClient().getHandler().sendPacket(new PacketTryEditDatabase(Users.FILE_NAME, this.id, Users.USERNAME, this.usernameField.getText(), Users.EMAIL, this.emailField.getText()));
        CafeDafyddMain.closeTopPage(this.emailField);
        CafeDafyddMain.getController(BaseController.class).ifPresent(BaseController::resync);
    }

    public void setId(int id) {
        this.id = id;
        this.resync();
    }
}
