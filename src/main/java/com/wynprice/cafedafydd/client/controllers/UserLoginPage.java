package com.wynprice.cafedafydd.client.controllers;

import com.wynprice.cafedafydd.client.CafeDafyddMain;
import com.wynprice.cafedafydd.client.netty.DatabaseRequest;
import com.wynprice.cafedafydd.common.Page;
import com.wynprice.cafedafydd.common.utils.DatabaseRecord;
import com.wynprice.cafedafydd.server.utils.DateUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;

import java.util.Date;

import static com.wynprice.cafedafydd.common.DatabaseStrings.Sessions;


public class UserLoginPage implements BaseController {

    @FXML public Button okayButton;
    @FXML public ListView<String> sessionList;

    @Override
    public void onLoaded() {
        DatabaseRequest.GET_ENTRIES.sendRequest(Sessions.FILE_NAME, records -> {
            for (DatabaseRecord record : records) {
                System.out.println(DateUtils.toISO8691(new Date(System.currentTimeMillis())));
                this.sessionList.getItems().add(
                    "ID: " + record.getPrimaryField() + "  " +
                    "PC: " + record.getField(Sessions.COMPUTER_ID) + "  " +
                    "Start: " + DateUtils.fromISO8691(record.getField(Sessions.ISO8601_START)) + "  " +
                    "End: " + DateUtils.fromISO8691(record.getField(Sessions.ISO8601_END))
                );
            }
        }, Sessions.USER_ID, "$$userid");
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
    }

    @FXML
    public void removeSessionButton() {

    }
}
