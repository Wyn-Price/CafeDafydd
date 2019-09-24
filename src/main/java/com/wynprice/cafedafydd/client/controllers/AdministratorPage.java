package com.wynprice.cafedafydd.client.controllers;

import com.wynprice.cafedafydd.client.CafeDafyddMain;
import com.wynprice.cafedafydd.common.Page;
import com.wynprice.cafedafydd.common.netty.packets.serverbound.PacketCreateUser;
import com.wynprice.cafedafydd.common.utils.PasswordUtils;
import javafx.fxml.FXML;

public class AdministratorPage implements BaseController {
    @FXML
    public void onLogout() {
        CafeDafyddMain.getClient().logout();
    }

    @FXML
    public void onAddUser() {
        CafeDafyddMain.showPage(Page.EDIT_USER_PAGE);
        CafeDafyddMain.getController(EditUserPage.class).ifPresent(e -> {
            e.setTitle("Add User");
            e.setConsumer((hasUsername, username, hasEmail, email, hasPassword, passwordHash) -> {
                if(hasUsername && hasEmail && hasPassword) {
                    CafeDafyddMain.getClient().getHandler().sendPacket(new PacketCreateUser(username, email, passwordHash));
                }
            });
        });
    }

    @FXML
    public void onEditUser() {
        CafeDafyddMain.showPage(Page.SEARCH_USERS_PAGE);
    }

    @FXML
    public void onAddStaffMemeber() {
    }

    @FXML
    public void onEditStaffMember() {
    }
}
