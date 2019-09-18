package com.wynprice.cafedafydd.client.controllers;

import com.wynprice.cafedafydd.client.netty.DatabaseRequest;
import com.wynprice.cafedafydd.common.utils.DatabaseRecord;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.util.ArrayList;
import java.util.List;

import static com.wynprice.cafedafydd.common.DatabaseStrings.*;


public class SearchUsersPage implements BaseController {
    @FXML public Button backButton;
    @FXML public ListView<String> searchResult;

    @FXML public TextField usernameText;
    @FXML public TextField emailText;
    @FXML public ComboBox<String> permissionComboBox;

    @Override
    public void onLoaded() {

    }

    @FXML
    public void backButtonClicked() {
    }


    @FXML
    public void doSearch() {
        //Delay the task, to make sure that the text fields have the right data set.
        //This method is called BEFORE the text fields are updated, thus we need to wait
        Platform.runLater(() -> {
            List<String> form = new ArrayList<>();
            if(!this.usernameText.getText().isEmpty()) {
                form.add(Users.USERNAME);
                form.add(this.usernameText.getText());
            }
            if(!this.emailText.getText().isEmpty()) {
                form.add(Users.EMAIL);
                form.add(this.emailText.getText());
            }
            if(this.permissionComboBox.getValue() != null && !this.permissionComboBox.getValue().isEmpty()) {
                form.add(Users.PERMISSION_LEVEL);
                form.add(this.permissionComboBox.getValue());
            }

            if(form.isEmpty()) {
                this.searchResult.getItems().clear();
                return;
            }

            DatabaseRequest.SEARCH_ENTRIES.sendRequest(Users.FILE_NAME,
                records -> Platform.runLater(() -> { //Ensure on Java FX Thread
                    this.searchResult.getItems().clear();
                    for (DatabaseRecord record : records) {
                        this.searchResult.getItems().add("Username: " + record.getField(Users.USERNAME) + "Email: " + record.getField(Users.EMAIL) + "Permissions: " + record.getField(Users.PERMISSION_LEVEL));
                    }
                }),
                form.toArray(new String[0])
            );
        });
    }
}
