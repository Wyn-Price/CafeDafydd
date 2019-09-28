package com.wynprice.cafedafydd.client.controllers;

import com.sun.javafx.tk.Toolkit;
import com.wynprice.cafedafydd.client.CafeDafyddMain;
import com.wynprice.cafedafydd.client.netty.DatabaseRequest;
import com.wynprice.cafedafydd.common.Page;
import com.wynprice.cafedafydd.common.netty.packets.serverbound.PacketTryEditDatabase;
import com.wynprice.cafedafydd.common.utils.DatabaseRecord;
import com.wynprice.cafedafydd.common.utils.FormBuilder;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Font;
import lombok.Value;

import java.util.ArrayList;
import java.util.List;

import static com.wynprice.cafedafydd.common.DatabaseStrings.Users;


public class SearchUsersPage implements BaseController {
    @FXML public Button backButton;
    @FXML public ListView<UserRecord> searchResult;

    @FXML public TextField usernameText;
    @FXML public TextField emailText;
    @FXML public ComboBox<String> permissionComboBox;

    @FXML
    public void backButtonClicked() {
        CafeDafyddMain.back();
    }

    @Override
    public void resync() {
        this.doSearch();
    }

    @FXML
    public void doSearch() {
        //Delay the task, to make sure that the text fields have the right data set.
        //This method is called BEFORE the text fields are updated, thus we need to wait
        Platform.runLater(() -> {
            FormBuilder form = FormBuilder.create();
            if(!this.usernameText.getText().isEmpty()) {
                form.with(Users.USERNAME, this.usernameText.getText());
            }
            if(!this.emailText.getText().isEmpty()) {
                form.with(Users.EMAIL, this.emailText.getText());
            }
            if(this.permissionComboBox.getValue() != null && !this.permissionComboBox.getValue().isEmpty()) {
                form.with(Users.PERMISSION_LEVEL, this.permissionComboBox.getValue());
            }

            if(form.isEmpty()) {
                this.searchResult.getItems().clear();
                return;
            }

            DatabaseRequest.SEARCH_ENTRIES.sendRequest(Users.FILE_NAME,
                records -> Platform.runLater(() -> { //Ensure on Java FX Thread
                    this.searchResult.getItems().clear();
                    for (DatabaseRecord record : records) {
                        this.searchResult.getItems().add(new UserRecord(
                            record.getPrimaryField(),

                            "Username: " + bloat(record.getField(Users.USERNAME)) +
                                "Email: " + bloat(record.getField(Users.EMAIL)) + "    " +
                                "Permissions: " +bloat(record.getField(Users.PERMISSION_LEVEL)) + "    "

                        ));
                    }
                }),
                form.getForm()
            );
        });
    }

    private String bloat(String in) {
        int len = 100;
        Font font = Font.getDefault();

        float inWidth = Toolkit.getToolkit().getFontLoader().computeStringWidth(in, font);

        if(inWidth > len) {
            float ellipsisWidth = Toolkit.getToolkit().getFontLoader().computeStringWidth("...", font);
            while (inWidth > len - ellipsisWidth) {
                in = in.substring(0, in.length()-1);
                inWidth = Toolkit.getToolkit().getFontLoader().computeStringWidth(in, font);
            }
            return in + "...";
        }

        float spaceWidth = Toolkit.getToolkit().getFontLoader().computeStringWidth(" ", font);
        float remainingWidth = len - inWidth;

        StringBuilder out = new StringBuilder(in);

        while(remainingWidth > 0) {
            out.append(" ");
            remainingWidth -= spaceWidth;
        }


        return out.toString();
    }

    @FXML
    public void mouseClicked(MouseEvent mouseEvent) {
        if(mouseEvent.getButton() == MouseButton.PRIMARY && mouseEvent.getClickCount() == 2) {
            UserRecord item = this.searchResult.getSelectionModel().getSelectedItem();
            if(item != null) { //Should always be true
                CafeDafyddMain.showPage(Page.EDIT_USER_PAGE);
                CafeDafyddMain.getController(EditUserPage.class).ifPresent(e -> {
                    e.setId(item.id);
                    e.displayImages(false);
                    e.resync();
                    e.setTitle("Edit User");
                    e.setConsumer((hasUsername, username, hasEmail, email, hasPassword, passwordHash) -> {
                        FormBuilder form = FormBuilder.create()
                            .with(Users.USERNAME, username).when(hasUsername)
                            .with(Users.EMAIL, email).when(hasEmail)
                            .with(Users.PASSWORD_HASH, passwordHash).when(hasPassword);

                        if(!form.isEmpty()) {
                            CafeDafyddMain.getClient().getHandler().sendPacket(new PacketTryEditDatabase(Users.FILE_NAME, item.id, form.getForm()));
                        }
                        CafeDafyddMain.back();
                        CafeDafyddMain.getController(BaseController.class).ifPresent(BaseController::resync);
                    });
                });
            }
        }
    }

    @Value
    private class UserRecord {
        private final int id;
        private final String toString;

        @Override
        public String toString() {
            return this.toString;
        }
    }
}
