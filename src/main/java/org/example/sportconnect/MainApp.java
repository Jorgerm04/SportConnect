package org.example.sportconnect;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("login/login-view.fxml"));
        Scene scene = new Scene(loader.load());

        stage.setTitle("Sport Connect - Login");
        stage.setScene(scene);
        stage.setMinWidth(500);
        stage.setMinHeight(700);
        stage.setMaximized(true);
        stage.show();
    }
}
