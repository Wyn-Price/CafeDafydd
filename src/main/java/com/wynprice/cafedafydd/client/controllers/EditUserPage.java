package com.wynprice.cafedafydd.client.controllers;

import com.wynprice.cafedafydd.client.CafeDafyddMain;
import com.wynprice.cafedafydd.client.netty.DatabaseRequest;
import com.wynprice.cafedafydd.client.utils.Images;
import com.wynprice.cafedafydd.common.utils.DatabaseRecord;
import com.wynprice.cafedafydd.common.utils.FormBuilder;
import com.wynprice.cafedafydd.common.utils.PasswordUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import lombok.extern.log4j.Log4j2;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.wynprice.cafedafydd.common.FieldDefinitions.ID;
import static com.wynprice.cafedafydd.common.FieldDefinitions.Users;

@Log4j2
public class EditUserPage implements BaseController {
    private int id = -1;
    private ClickConsumer consumer;

    //https://emailregex.com/
    private static final Matcher EMAIL_MATCHER = Pattern.compile("(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])").matcher("");

    @FXML public Label mainText;
    @FXML public TextField usernameField;
    @FXML public PasswordField passwordField;
    @FXML public PasswordField repeatPasswordField;
    @FXML public TextField emailField;

    @FXML public ImageView usernameImage;
    @FXML public ImageView passwordImage;
    @FXML public ImageView emailImage;

    @FXML public Label errorField;

    @Override
    public void onLoaded() {
        this.resync();
        this.usernameField.textProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue.length() < 5) {
                this.errorField.setText("Username must be at least 5 characters.");
                this.usernameImage.setImage(Images.RED_CROSS.getImage());
            } else {
                DatabaseRequest.HAS_ENTRY.sendRequest(Users.FILE_NAME, result -> {
                    this.usernameImage.setImage(getImage(!result));
                    if(result) {
                        this.errorField.setText("Username is taken");
                    } else {
                        this.errorField.setText("");
                    }
                }, FormBuilder.create().with(Users.USERNAME, newValue).without(ID, this.id));
            }
        });

        this.emailField.textProperty().addListener((observable, oldValue, newValue) -> {

            if(!EMAIL_MATCHER.reset(newValue).find()) {
                this.errorField.setText("Invalid email");
                this.emailImage.setImage(Images.RED_CROSS.getImage());
            } else {
                DatabaseRequest.HAS_ENTRY.sendRequest(Users.FILE_NAME, result -> {
                    this.emailImage.setImage(getImage(!result));
                    if(result) {
                        this.errorField.setText("Email is already registered to an account. Contact a member of staff for a password reset");
                    } else {
                        this.errorField.setText("");
                    }
                }, FormBuilder.create().with(Users.EMAIL, newValue).without(ID, this.id));
            }
        });

        this.passwordField.textProperty().addListener((observable, oldValue, newValue) -> {
            boolean largeEnough = newValue.length() >= 8;
            boolean matched = newValue.equals(this.repeatPasswordField.getText());
            if(!largeEnough) {
                this.errorField.setText("Password much be at least 8 characters long.");
            } else if(!matched) {
                this.errorField.setText("Passwords do not match");
            } else {
                this.errorField.setText("");
            }
            this.passwordImage.setImage(getImage(largeEnough && matched));

        });

        this.repeatPasswordField.textProperty().addListener((observable, oldValue, newValue) -> {
            boolean matched = newValue.equals(this.passwordField.getText());
            if(!matched) {
                this.errorField.setText("Passwords do not match");
            } else {
                this.errorField.setText("");
            }
            this.passwordImage.setImage(getImage(matched));
        });
    }

    @Override
    public void resync() {
        if(this.id < 0) {
            return;
        }
        DatabaseRequest.GET_ENTRIES.sendRequest(Users.FILE_NAME, records -> {
            if(records.isEmpty()) {
                throw new IllegalArgumentException("Returned Record Shouldn't be empty. Couldn't find user with id " + this.id);
            }
            DatabaseRecord record = records.get(0);
            this.usernameField.setText(record.get(Users.USERNAME));
            this.emailField.setText(record.get(Users.EMAIL));
        }, FormBuilder.create().with(ID, this.id));
    }

    @FXML
    public void onKeyUsername(KeyEvent keyEvent) {
        if(!"\b".equals(keyEvent.getCharacter()) && !MainPage.USERNAME_MATCHER.reset(keyEvent.getCharacter()).find()) {
            keyEvent.consume();
            this.errorField.setText("Username must only contain characters, numbers and underscores. '" + keyEvent.getCharacter() + "' is not allowed.");
        }
    }

    @FXML
    public void createUser() {
        this.consumer.consume(
            this.greenTick(this.usernameImage), this.usernameField.getText(),
            this.greenTick(this.emailImage), this.emailField.getText(),
            this.greenTick(this.passwordImage), PasswordUtils.generatePasswordHash(this.usernameField.getText(), this.passwordField.getText())
        );
    }

    private boolean greenTick(ImageView view) {
        return view.getImage() == Images.GREEN_TICK.getImage();
    }

    private Image getImage(boolean tick) {
        return (tick ? Images.GREEN_TICK : Images.RED_CROSS).getImage();
    }

    public void setId(int id) {
        this.id = id;
    }

    public void displayImages(boolean displayImages) {
        this.usernameImage.setVisible(displayImages);
        this.emailImage.setVisible(displayImages);
        this.passwordImage.setVisible(displayImages);
    }

    public void setConsumer(ClickConsumer consumer) {
        this.consumer = consumer;
    }

    public void setTitle(String title) {
        this.mainText.setText(title);
    }

    @FXML
    public void goBack() {
        CafeDafyddMain.back();
    }

    public interface ClickConsumer {
        void consume(boolean hasUsername, String username, boolean hasEmail, String email, boolean hasPassword, String passwordHash);
    }
}
