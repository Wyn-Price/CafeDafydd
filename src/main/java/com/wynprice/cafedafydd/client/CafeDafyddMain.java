package com.wynprice.cafedafydd.client;

import com.wynprice.cafedafydd.client.controllers.BaseController;
import com.wynprice.cafedafydd.client.netty.CafeDayfddClient;
import com.wynprice.cafedafydd.common.Page;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.util.Optional;

@Log4j2
public class CafeDafyddMain extends Application {

    private static Stage stage;
    private static BaseController controller;

    @Getter private static CafeDayfddClient client;

    public static void main(String[] args) {
        client = new CafeDayfddClient("localhost");
        launch(CafeDafyddMain.class);
    }

    @Override
    public void start(Stage stage) {
        CafeDafyddMain.stage = stage;
        stage.setTitle("Cafe Dafydd");
        stage.setScene(new Scene(getRoot(Page.LOGIN_PAGE).orElseThrow(IllegalArgumentException::new), 500, 275));
        stage.show();

        stage.onCloseRequestProperty().set(event -> {
            client.close();
            System.exit(0);
        });
    }

    public static void showPage(Page page) {
        getRoot(page).ifPresent(root -> stage.setScene(new Scene(root, root.prefWidth(500), root.prefHeight(275))));
    }

    public static void displayNewPage(Page page, String title) {
        getRoot(page).ifPresent(root -> {
                Scene scene = new Scene(root, 500, 275);
                Stage stage = new Stage();

                stage.initModality(Modality.WINDOW_MODAL);
                stage.initOwner(CafeDafyddMain.stage);

                stage.setTitle(title);
                stage.setScene(scene);
                stage.show();
            }
        );
    }

    private static Optional<Parent> getRoot(Page page) {
        try {
            FXMLLoader loader = new FXMLLoader(CafeDafyddMain.class.getResource("/pages/" + page.getFileName() + ".fxml"));
            Parent loaded = loader.load();
            controller = loader.getController();
            controller.onLoaded();
            return Optional.of(loaded);
        } catch (IOException e) {
            log.error("Unable to load page " + page + " for file " + page.getFileName(), e);
            return Optional.empty();
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> Optional<T> getController(Class<T> controllerClass) {
        return controllerClass.isInstance(controller) ? Optional.of((T)controller) : Optional.empty();
    }
}
