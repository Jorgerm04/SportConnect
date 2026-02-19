package org.example.sportconnect.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.sportconnect.models.User;
import org.example.sportconnect.services.UserService;
import org.example.sportconnect.utils.Session;

import java.io.IOException;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;

    private final UserService userService = new UserService();

    @FXML
    public void onLoginClick() {
        User user = userService.login(emailField.getText(), passwordField.getText());

        if (user != null) {
            Session.getInstance().setUser(user);


            String fxml = user.isAdmin() ? "dashboard/dashboard-view.fxml" : "home/home-view.fxml";
            String title = user.isAdmin() ? "SportConnect - Admin" : "SportConnect - Home";

            navigateTo(fxml, title);
        } else {
            messageLabel.setText("Credenciales incorrectas");
        }
    }

    private void navigateTo(String fxmlFile, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/sportconnect/" + fxmlFile));
            Parent root = loader.load();

            Stage stage = (Stage) emailField.getScene().getWindow();

            // Creamos la escena con el tamaño actual del stage para evitar el hueco negro
            Scene scene = new Scene(root, stage.getWidth(), stage.getHeight());

            stage.setTitle(title);
            stage.setScene(scene);

            // Si ya estaba maximizado, lo "refrescamos"
            if (stage.isMaximized()) {
                stage.setMaximized(false);
                stage.setMaximized(true);
            } else {
                stage.setMaximized(true);
            }

            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}