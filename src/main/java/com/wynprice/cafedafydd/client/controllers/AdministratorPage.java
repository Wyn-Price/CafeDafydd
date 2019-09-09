package com.wynprice.cafedafydd.client.controllers;

import com.wynprice.cafedafydd.client.CafeDafyddMain;
import com.wynprice.cafedafydd.common.Page;
import com.wynprice.cafedafydd.common.netty.packets.packets.serverbound.PacketLogout;
import javafx.fxml.FXML;

public class AdministratorPage implements BaseController {
    @FXML
    public void onLogout() {
        CafeDafyddMain.getClient().getHandler().sendPacket(new PacketLogout());
        CafeDafyddMain.showPage(Page.LOGIN_PAGE);
    }

    @FXML
    public void onAddUser() {
        CafeDafyddMain.showPage(Page.ADD_USER_PAGE);
    }

    @FXML
    public void onAddStaffMemeber() {
    }

    @FXML
    public void onEditUser() {
    }

    @FXML
    public void onEditStaffMember() {
    }
}
