package com.wynprice.cafedafydd.client;

import com.wynprice.cafedafydd.client.controllers.BaseController;
import com.wynprice.cafedafydd.client.netty.CafeDayfddClient;
import com.wynprice.cafedafydd.common.Page;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.util.Optional;
import java.util.Stack;

@Log4j2
public class CafeDafyddMain extends Application {

    private static Stage stage;
    private static Stack<BaseController> controller = new Stack<>();
    private static Stack<Page> pageHistory = new Stack<>();

    @Getter private static CafeDayfddClient client;

    public static void main(String[] args) {
        client = new CafeDayfddClient("localhost");
        launch(CafeDafyddMain.class);
    }

    @Override
    public void start(Stage stage) {
        CafeDafyddMain.stage = stage;
        stage.setTitle("Cafe Dafydd");
        stage.setScene(getScene(Page.LOGIN_PAGE, true).orElseThrow(IllegalArgumentException::new));
        stage.show();

        stage.onCloseRequestProperty().set(event -> {
            client.close();
            System.exit(0);
        });
    }

    public static void closeTopPage(Node node) {
        node.getScene().getWindow().hide();
        controller.pop();
    }

    public static void showPage(Page page) {
        getScene(page, false).ifPresent(stage::setScene);
    }

    public static void back() {
        pageHistory.pop();
        showPage(pageHistory.peek());
    }

    public static void displayNewPage(Page page, String title) {
        getScene(page, true).ifPresent(scene -> {
                Stage stage = new Stage();

                stage.initModality(Modality.WINDOW_MODAL);
                stage.initOwner(CafeDafyddMain.stage);

                stage.setTitle(title);
                stage.setScene(scene);
                stage.show();
            }
        );
    }

    private static Optional<Scene> getScene(Page page, boolean newPage) {
        try {
            FXMLLoader loader = new FXMLLoader(CafeDafyddMain.class.getResource("/pages/" + page.getFileName() + ".fxml"));
            Parent loaded = loader.load();
            if(!newPage) {
                controller.pop();
            }
            pageHistory.push(page);
            controller.push(loader.getController());
            controller.peek().onLoaded();
            return Optional.of(new Scene(loaded, loaded.prefWidth(500), loaded.prefHeight(275)));
        } catch (IOException e) {
            log.error("Unable to load page " + page + " for file " + page.getFileName(), e);
            return Optional.empty();
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> Optional<T> getController(Class<T> controllerClass) {
        return controllerClass.isInstance(controller.peek()) ? Optional.of((T)controller.peek()) : Optional.empty();
    }
}
