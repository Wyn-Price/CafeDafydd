package com.wynprice.cafedafydd.client;

import com.wynprice.cafedafydd.client.netty.CafeDayfddClient;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.util.Optional;

@Log4j2
public class CafeDafyddMain extends Application {

    @Getter private static CafeDayfddClient client;

    public static void main(String[] args) {
        client = new CafeDayfddClient("localhost");
        launch(CafeDafyddMain.class);
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("Cafe Dafydd");
        stage.setScene(new Scene(getRoot(Page.MAIN_PAGE).orElseThrow(IllegalArgumentException::new), 500, 275));
        stage.show();

        stage.onCloseRequestProperty().set(event -> client.close());
    }

    public static void showPage(Stage stage, Page page) {
        getRoot(page).ifPresent(stage.getScene()::setRoot);
    }

    private static Optional<Parent> getRoot(Page page) {
        try {
            return Optional.ofNullable(new FXMLLoader(CafeDafyddMain.class.getResource("/pages/" + page.getFileName() + ".fxml")).load());
        } catch (IOException e) {
            log.error("Unable to load page " + page + " for file " + page.getFileName(), e);
            return Optional.empty();
        }
    }
}
