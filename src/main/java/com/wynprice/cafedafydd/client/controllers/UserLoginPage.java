package com.wynprice.cafedafydd.client.controllers;

import com.wynprice.cafedafydd.client.CafeDafyddMain;
import com.wynprice.cafedafydd.common.Page;
import com.wynprice.cafedafydd.common.netty.packets.packets.serverbound.PacketLogout;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class UserLoginPage implements BaseController {

    @FXML public Button okayButton;

    @FXML
    public void enterButtonClicked() {
        CafeDafyddMain.getClient().logout();
    }

    @FXML
    public void changePasswordClicked() {
        CafeDafyddMain.showPage(Page.CHANGE_PASSWORD);
    }
}
