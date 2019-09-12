package com.wynprice.cafedafydd.client.controllers;

import com.wynprice.cafedafydd.client.CafeDafyddMain;
import com.wynprice.cafedafydd.common.Page;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;

public class UserLoginPage implements BaseController {

    @FXML public Button okayButton;
    @FXML public ListView sessionList;

    @FXML
    public void enterButtonClicked() {
        CafeDafyddMain.getClient().logout();
    }

    @FXML
    public void changePasswordClicked() {
        CafeDafyddMain.showPage(Page.CHANGE_PASSWORD);
    }

    @FXML
    public void addSessionButton(ActionEvent actionEvent) {
    }

    @FXML
    public void removeSessionButton(ActionEvent actionEvent) {

    }
}
