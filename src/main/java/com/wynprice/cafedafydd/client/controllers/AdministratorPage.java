package com.wynprice.cafedafydd.client.controllers;

import com.wynprice.cafedafydd.client.CafeDafyddMain;
import com.wynprice.cafedafydd.common.Page;
import com.wynprice.cafedafydd.common.netty.packets.serverbound.PacketCreateUser;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import org.apache.logging.log4j.util.TriConsumer;

/**
 * The controller for the page {@code administrator_page.fxml}
 */
public class AdministratorPage implements BaseController {
    @FXML
    public void onLogout() {
        CafeDafyddMain.getClient().logout();
    }

    @FXML
    public void onEditUser() {
        CafeDafyddMain.showPage(Page.SEARCH_USERS_PAGE);
    }

    @FXML
    public void onAddUser() {
        this.displayEditUser("Add User", (byte) 0);
    }

    @FXML
    public void onAddStaffMemeber() {
        this.displayEditUser("Add Staff Member", (byte) 1);

    }

    @FXML
    public void onAddAdministrator() {
        this.displayEditUser("Add Staff Member", (byte) 2);
    }

    private void displayEditUser(String title, byte permissionLevel) {
        //Show the edit user page
        CafeDafyddMain.showPage(Page.EDIT_USER_PAGE);
        CafeDafyddMain.getController(EditUserPage.class).ifPresent(e -> {
            //Set the controllers handler to have the title and when the submit button is clicked send the packet `PacketCreateUser`
            e.setTitle(title);
            e.setConsumer((hasUsername, username, hasEmail, email, hasPassword, passwordHash) -> {
                if(hasUsername && hasEmail && hasPassword) {
                    CafeDafyddMain.getClient().getHandler().sendPacket(new PacketCreateUser(username, email, passwordHash, permissionLevel));
                }
            });
        });
    }

    @FXML
    public void onSearchSessions() {
        CafeDafyddMain.showPage(Page.SEARCH_SESSION);
    }
}
