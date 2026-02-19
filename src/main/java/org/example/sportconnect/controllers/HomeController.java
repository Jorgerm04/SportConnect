package org.example.sportconnect.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.sportconnect.models.Reservation;
import org.example.sportconnect.models.User;
import org.example.sportconnect.services.ReservationService;
import org.example.sportconnect.utils.Session;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class HomeController {

    @FXML private VBox containerProximas;
    @FXML private VBox containerHistorial;
    @FXML private Button btnNuevaReserva;
    @FXML private Label lblUserName;

    private final ReservationService reservationService = new ReservationService();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM", new Locale("es", "ES"));

    @FXML
    public void initialize() {
        cargarDatos();
    }

    private void cargarDatos() {
        containerProximas.getChildren().clear();
        containerHistorial.getChildren().clear();

        // 1. Obtener el usuario actual de la sesión
        User currentUser = Session.getInstance().getUser();
        if (currentUser == null) return; // Seguridad por si no hay sesión
        lblUserName.setText(currentUser.getName());

        List<Reservation> todas = reservationService.getAllReservations();
        LocalDate hoy = LocalDate.now();

        for (Reservation res : todas) {
            // 2. FILTRO: Solo mostramos la reserva si pertenece al usuario logueado
            if (res.getUser().getId() == currentUser.getId()) {

                boolean esPasada = res.getDate().isBefore(hoy) || res.isCancelled();
                HBox tarjeta = crearTarjetaReserva(res, esPasada);

                if (esPasada) {
                    containerHistorial.getChildren().add(tarjeta);
                } else {
                    containerProximas.getChildren().add(tarjeta);
                }
            }
        }
    }

    private HBox crearTarjetaReserva(Reservation res, boolean esHistorial) {
        HBox card = new HBox(0); // Spacing 0 para que el indicador pegue al borde
        card.getStyleClass().add("reservation-card");
        card.setAlignment(Pos.CENTER_LEFT);

        // 1. El Indicador lateral
        Region indicator = new Region();
        indicator.getStyleClass().add(esHistorial ? "indicator-history" : "indicator-active");
        // Hacemos que el indicador crezca verticalmente para llenar la card
        VBox.setVgrow(indicator, Priority.ALWAYS);

        // 2. Contenedor para el contenido (Texto, iconos, botón)
        HBox content = new HBox(25); // Este mantiene el espacio entre elementos
        content.getStyleClass().add("card-content");
        content.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(content, Priority.ALWAYS);

        // --- Bloque de Deporte ---
        VBox infoSport = new VBox(2);
        Label lblSport = new Label(res.getCourt().getSport().getName());
        lblSport.getStyleClass().add("sport-name");
        Label lblCourt = new Label(res.getCourt().getName());
        lblCourt.getStyleClass().add("court-name");
        infoSport.getChildren().addAll(lblSport, lblCourt);
        infoSport.setMinWidth(160);

        // --- Bloque Fecha/Hora (puedes unirlos como en tu última foto) ---
        HBox infoBox = new HBox(15);
        infoBox.setAlignment(Pos.CENTER_LEFT);

        FontIcon iconCal = new FontIcon("far-calendar-alt");
        iconCal.setIconColor(Color.web("#64748b"));
        Label lblData = new Label(res.getDate().format(formatter) + " - " + res.getStartHour());
        lblData.getStyleClass().add("info-text");
        infoBox.getChildren().addAll(iconCal, lblData);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Añadimos todo al contenedor de contenido
        content.getChildren().addAll(infoSport, infoBox, spacer);

        // Botón cancelar
        if (!esHistorial && !res.isCancelled()) {
            Button btnCancel = new Button("Cancelar reserva", new FontIcon("far-times-circle"));
            btnCancel.getStyleClass().add("cancel-link");
            content.getChildren().add(btnCancel);
        }

        // FINALMENTE: Añadimos el indicador y el contenido a la card principal
        card.getChildren().addAll(indicator, content);

        return card;
    }

    @FXML
    private void handleNuevaReserva(ActionEvent event) {
        abrirModal("/org/example/sportconnect/reservation-form/reservation-form-view.fxml", "Nueva Reserva");
    }

    /**
     * Cierra la sesión y vuelve al login
     */
    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/sportconnect/login/login-view.fxml"));
            Scene loginScene = new Scene(loader.load());
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(loginScene);
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void abrirModal(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Scene scene = new Scene(loader.load());

            Stage stage = new Stage();
            stage.setTitle(title);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(btnNuevaReserva.getScene().getWindow());
            stage.setMinWidth(1000);
            stage.setMinHeight(750);
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.showAndWait();

            cargarDatos();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}