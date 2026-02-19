package org.example.sportconnect;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        //FXMLLoader loader = new FXMLLoader(getClass().getResource("login/login-view.fxml"));
        FXMLLoader loader = new FXMLLoader(getClass().getResource("login/login-view.fxml"));
        Scene scene = new Scene(loader.load());

        stage.setTitle("Sport Connect - Login");
        stage.setScene(scene);

        // --- CONFIGURACIÓN RESPONSIVE Y LÍMITES ---
        stage.setMinWidth(500);  // Evita que se corte el ancho de la tarjeta
        stage.setMinHeight(700); // Evita que se corten los inputs o el botón

        stage.setMaximized(true);
        stage.setFullScreenExitHint("Presiona ESC para salir de pantalla completa");

        stage.show();
    }
}
