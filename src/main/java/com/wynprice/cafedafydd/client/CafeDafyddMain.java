package com.wynprice.cafedafydd.client;

import com.wynprice.cafedafydd.client.controllers.BaseController;
import com.wynprice.cafedafydd.client.netty.CafeDayfddClient;
import com.wynprice.cafedafydd.common.Page;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.Stack;

public class CafeDafyddMain extends Application {

    static {
        System.setProperty("logFilename", "client_" + DateTimeFormatter.ofPattern("uuuu-MM-ddHH-mmss").withZone(ZoneId.of("GMT")).format(Instant.now()));
    }

    //Don't use @Log4j2 here, as we need to set the filename before we reference the logger
    private static final Logger log = LogManager.getLogger(CafeDafyddMain.class);


    /**
     * The current state
     */
    private static Stage stage;

    /**
     * The controller of the current page
     */
    private static BaseController controller;

    /**
     * The stack of pages. Used to navigate with back buttons
     */
    private static Stack<Page> pageHistory = new Stack<>();

    /**
     * The network client associated with this instance
     */
    @Getter private static CafeDayfddClient client;

    public static void main(String[] args) {
        //Create a new client and connect to the server, then launch the gui application
        client = new CafeDayfddClient("localhost");
        launch(CafeDafyddMain.class);
    }


    @Override
    public void start(Stage stage) {
        CafeDafyddMain.stage = stage;
        stage.setTitle("Cafe Dafydd");
        stage.setScene(getScene(Page.LOGIN_PAGE).orElseThrow(IllegalArgumentException::new));
        stage.show();

        stage.onCloseRequestProperty().set(event -> {
            client.close();
            System.exit(0);
        });
    }

    /**
     * Display the specified page
     * @param page the display the page
     */
    public static void showPage(Page page) {
        getScene(page).ifPresent(stage::setScene);
    }

    /**
     * Move back in the page history.
     */
    public static void back() {
        //Pop the top most page history.
        pageHistory.pop();

        //Show and remove the now top-most page. This then gets pushed back at #getScene
        showPage(pageHistory.pop());
    }

    /**
     * Gets the scene for the specified page
     * @param page the page to get the scene from
     * @return an optional of the scene for the {@code page}, or {@link Optional#empty()} if it couldn't be loaded for some reason.
     */
    private static Optional<Scene> getScene(Page page) {
        try {
            FXMLLoader loader = new FXMLLoader(CafeDafyddMain.class.getResource("/pages/" + page.getFileName() + ".fxml"));
            Parent loaded = loader.load();
            pageHistory.push(page);
            controller = loader.getController();
            controller.onLoaded();
            return Optional.of(new Scene(loaded, loaded.prefWidth(500), loaded.prefHeight(275)));
        } catch (IOException e) {
            log.error("Unable to load page " + page + " for file " + page.getFileName(), e);
            return Optional.empty();
        }
    }

    /**
     * Gets an optional of the controller. This will return an optional of the controller is an instance of @{controllerClass}
     * @param controllerClass the controller class to check with. If the current controller is a subclass of this then a non empty optional will be returned
     * @param <T> the controller class type
     * @return a full optional of the controller, or {@link Optional#empty()} if the current controller isn't a instance of {@code T}
     */
    @SuppressWarnings("unchecked")
    public static <T> Optional<T> getController(Class<T> controllerClass) {
        return controllerClass.isInstance(controller) ? Optional.of((T)controller) : Optional.empty();
    }
}
