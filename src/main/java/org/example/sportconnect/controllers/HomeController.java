package org.example.sportconnect.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
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

        User currentUser = Session.getInstance().getUser();
        if (currentUser == null) return;
        lblUserName.setText(currentUser.getName());

        // Consulta filtrada por usuario directamente en BD
        List<Reservation> reservas = reservationService.findByUser(currentUser.getId());
        LocalDate hoy = LocalDate.now();

        for (Reservation res : reservas) {
            boolean esPasada = res.getDate().isBefore(hoy) ||
                    (res.getDate().isEqual(hoy) && res.getEndHour().isBefore(java.time.LocalTime.now())) ||
                    res.isCancelled();
            HBox tarjeta = crearTarjetaReserva(res, esPasada);
            if (esPasada) containerHistorial.getChildren().add(tarjeta);
            else containerProximas.getChildren().add(tarjeta);
        }

        if (containerProximas.getChildren().isEmpty()) {
            Label lbl = new Label("No tienes reservas próximas");
            lbl.getStyleClass().add("lbl-empty");
            containerProximas.getChildren().add(lbl);
        }
        if (containerHistorial.getChildren().isEmpty()) {
            Label lbl = new Label("No hay reservas en el historial");
            lbl.getStyleClass().add("lbl-empty");
            containerHistorial.getChildren().add(lbl);
        }
    }

    private HBox crearTarjetaReserva(Reservation res, boolean esHistorial) {
        HBox card = new HBox(0);
        card.getStyleClass().add("reservation-card");
        card.setAlignment(Pos.CENTER_LEFT);

        Region indicator = new Region();
        indicator.getStyleClass().add(res.isCancelled() ? "indicator-cancelled" : esHistorial ? "indicator-history" : "indicator-active");
        VBox.setVgrow(indicator, Priority.ALWAYS);

        HBox content = new HBox(25);
        content.getStyleClass().add("card-content");
        content.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(content, Priority.ALWAYS);

        VBox infoSport = new VBox(2);
        Label lblSport = new Label(res.getCourt().getSport().getName());
        lblSport.getStyleClass().add("sport-name");
        Label lblCourt = new Label(res.getCourt().getName());
        lblCourt.getStyleClass().add("court-name");
        infoSport.getChildren().addAll(lblSport, lblCourt);
        infoSport.setMinWidth(160);

        HBox infoBox = new HBox(15);
        infoBox.setAlignment(Pos.CENTER_LEFT);
        FontIcon iconCal = new FontIcon("far-calendar-alt");
        iconCal.setIconColor(Color.web("#64748b"));
        iconCal.setAccessibleText("Fecha");
        Label lblData = new Label(res.getDate().format(formatter) + "  ·  " + res.getStartHour() + " - " + res.getEndHour());
        lblData.getStyleClass().add("info-text");
        infoBox.getChildren().addAll(iconCal, lblData);

        if (res.isCancelled()) {
            Label lblCancelada = new Label("Cancelada");
            lblCancelada.getStyleClass().add("badge-cancelled-card");
            lblCancelada.setAccessibleText("Reserva cancelada");
            infoBox.getChildren().add(lblCancelada);
        }

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        content.getChildren().addAll(infoSport, infoBox, spacer);

        if (!esHistorial && !res.isCancelled()) {
            FontIcon icoCancel = new FontIcon("fas-times");
            icoCancel.setIconColor(Color.web("#ef4444")); icoCancel.setIconSize(11);
            Button btnCancel = new Button("Cancelar", icoCancel);
            btnCancel.getStyleClass().add("btn-cancel");
            btnCancel.setAccessibleText("Cancelar reserva en " + res.getCourt().getName() +
                    ", " + res.getDate().format(formatter));
            btnCancel.setOnAction(e -> confirmarCancelacion(res));
            content.getChildren().add(btnCancel);
        }

        card.getChildren().addAll(indicator, content);
        return card;
    }

    private void confirmarCancelacion(Reservation res) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Cancelar reserva");
        alert.setHeaderText("¿Cancelar esta reserva?");
        alert.setContentText(res.getCourt().getSport().getName() + " · " +
                res.getCourt().getName() + "\n" +
                res.getDate().format(DateTimeFormatter.ofPattern("dd MMMM yyyy", new Locale("es", "ES"))) +
                "  ·  " + res.getStartHour() + " - " + res.getEndHour());

        DialogPane dp = alert.getDialogPane();
        var css = getClass().getResource("/org/example/sportconnect/base.css");
        if (css != null) { dp.getStylesheets().add(css.toExternalForm()); dp.getStyleClass().add("custom-alert"); }

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                reservationService.cancel(res.getId());
                cargarDatos();
            }
        });
    }

    @FXML
    private void handleNuevaReserva(ActionEvent event) {
        abrirModal("/org/example/sportconnect/reservation-form/reservation-form-view.fxml", "Nueva Reserva");
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            Session.getInstance().logOut();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/sportconnect/login/login-view.fxml"));
            Scene loginScene = new Scene(loader.load());
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(loginScene);
            stage.centerOnScreen();
        } catch (IOException e) { e.printStackTrace(); }
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
        } catch (IOException e) { e.printStackTrace(); }
    }
}
