package com.wynprice.cafedafydd.server;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class CafeDafyddServerApplication extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Cafe Dafydd Server");
        primaryStage.setScene(new Scene(new GridPane(), 200, 150));
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        CafeDafyddServerMain.close();
        System.exit(0);
    }
}
