package com.wynprice.cafedafydd.client.controllers;

import com.wynprice.cafedafydd.client.CafeDafyddMain;
import com.wynprice.cafedafydd.client.controllers.data.Session;
import com.wynprice.cafedafydd.client.netty.DatabaseRequest;
import com.wynprice.cafedafydd.common.netty.packets.serverbound.PacketTryEditDatabase;
import com.wynprice.cafedafydd.common.utils.DatabaseRecord;
import com.wynprice.cafedafydd.common.utils.DateUtils;
import com.wynprice.cafedafydd.common.utils.FormBuilder;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import static com.wynprice.cafedafydd.common.FieldDefinitions.*;

public class SearchSessionsPage implements BaseController {
    @FXML public Button backButton;
    @FXML public ListView<Session> searchResult;
    @FXML public TextField usernameText;
    @FXML public Button payingButton;
    @FXML public ComboBox<String> paidStateBox;

    @Override
    public void onLoaded() {
        this.paidStateBox.getSelectionModel().select(0);

        this.searchResult.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue.intValue() >= 0 && this.paidStateBox.getSelectionModel().getSelectedIndex() != 1) {
                this.payingButton.setDisable(false);
                this.payingButton.setText(this.searchResult.getItems().get(newValue.intValue()).isHasPaid() ? "Set unpaid" : "Set Paid");
            } else {
                this.payingButton.setDisable(true);
            }
        });
    }

    @FXML
    public void backButtonClicked() {
        CafeDafyddMain.back();
    }

    @FXML
    public void doSearch() {
        this.resync();
    }

    @Override
    public void resync() {
        //Delay the task, to make sure that the text fields have the right data set.
        //This method is called BEFORE the text fields are updated, thus we need to wait
        Platform.runLater(() -> {
            FormBuilder form = FormBuilder.create();

            if(!this.usernameText.getText().isEmpty()) {
                form.withInline(Sessions.USER_ID, Users.FILE_NAME, ID, FormBuilder.create().with(Users.USERNAME, this.usernameText.getText()).getForm());
            }
            if(this.paidStateBox.getSelectionModel().getSelectedIndex() > 0) {
                // 0 -> null
                // 1 -> Active
                // 2 -> Paid
                // 3 -> Not Paid

                switch (this.paidStateBox.getSelectionModel().getSelectedIndex()) {
                    case 1:
                        form.with(Sessions.ISO8601_END, DateUtils.EMPTY_DATE);
                        break;
                    case 2:
                        form.with(Sessions.PAID, true);
                        break;
                    case 3:
                        form.with(Sessions.PAID, false);
                        break;
                }
            }

            if(form.isEmpty()) {
                this.searchResult.getItems().clear();
            } else {
                DatabaseRequest.GET_ENTRIES.sendRequest(Sessions.FILE_NAME, records -> {
                    this.searchResult.getItems().clear();
                    for (DatabaseRecord record : records) {
                        this.searchResult.getItems().add(Session.fromRecord(record));
                    }
                }, form);
            }
        });
    }

    @FXML
    public void onPayingButton() {
        Session item = this.searchResult.getSelectionModel().getSelectedItem();
        if(item != null) {
            if(item.isHasPaid()) {
                //Set unpaid
                CafeDafyddMain.getClient().getHandler().sendPacket(new PacketTryEditDatabase(Sessions.FILE_NAME, item.getFieldID(), FormBuilder.create().with(Sessions.PAID, false).getForm()));
            } else {
                //Set paid
                CafeDafyddMain.getClient().getHandler().sendPacket(new PacketTryEditDatabase(Sessions.FILE_NAME, item.getFieldID(), FormBuilder.create().with(Sessions.PAID, true).getForm()));
            }
        }
    }

}
