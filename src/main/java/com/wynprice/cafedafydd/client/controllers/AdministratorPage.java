package com.wynprice.cafedafydd.client.controllers;

import com.wynprice.cafedafydd.client.CafeDafyddMain;
import com.wynprice.cafedafydd.common.Page;
import javafx.fxml.FXML;

public class AdministratorPage implements BaseController {
    @FXML
    public void onLogout() {
        CafeDafyddMain.getClient().logout();
    }

    @FXML
    public void onAddUser() {
        CafeDafyddMain.showPage(Page.ADD_USER_PAGE);
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
