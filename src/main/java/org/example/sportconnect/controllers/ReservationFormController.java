package org.example.sportconnect.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.example.sportconnect.models.*;
import org.example.sportconnect.services.*;
import org.example.sportconnect.utils.Session;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class ReservationFormController {

    @FXML private HBox hboxDeportes;
    @FXML private VBox vboxPistasContenedor;
    @FXML private Label lblFechaActual, lblResumenSport, lblResumenPista, lblResumenFecha, lblResumenHora, lblResumenPrecio;
    @FXML private Button btnConfirmar;
    @FXML private VBox hboxAdminSelector;
    @FXML private ComboBox<User> comboUsuarios;

    private final SportService sportService = new SportService();
    private final CourtService courtService = new CourtService();
    private final UserService userService = new UserService();
    private final ReservationService reservationService = new ReservationService();

    private Button lastSelectedHour = null;
    private Sport deporteActual = null;
    private Court pistaSeleccionada;
    private LocalDate fechaSeleccionada = LocalDate.now();

    private Reservation reservacionAEditar = null;
    private boolean modoEdicion = false;

    private final LocalDate fechaLimite = LocalDate.now().plusDays(15);
    private final DateTimeFormatter formatterLargo = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'de' yyyy", new Locale("es", "ES"));
    private final DateTimeFormatter formatterCorto = DateTimeFormatter.ofPattern("d 'de' MMMM", new Locale("es", "ES"));
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    @FXML
    public void initialize() {
        actualizarInterfazFecha();
        cargarDeportesDesdeDB();
        configurarSelectorAdmin();
    }

    /** Precarga los datos de la reserva a editar */
    public void setReservacionAEditar(Reservation reservacion) {
        this.reservacionAEditar = reservacion;
        this.modoEdicion = true;
        fechaSeleccionada = reservacion.getDate();
        pistaSeleccionada = reservacion.getCourt();
        btnConfirmar.setText("Guardar Cambios");

        if (hboxAdminSelector.isVisible()) {
            comboUsuarios.getItems().stream()
                    .filter(u -> u.getId().equals(reservacion.getUser().getId()))
                    .findFirst()
                    .ifPresent(u -> comboUsuarios.setValue(u));
        }

        actualizarInterfazFecha();

        javafx.application.Platform.runLater(() -> {
            hboxDeportes.getChildren().forEach(node -> {
                if (node instanceof Button btn) {
                    if (btn.getText().equals(reservacion.getCourt().getSport().getName())) {
                        btn.fire();
                    }
                }
            });
        });
    }

    private void configurarSelectorAdmin() {
        User usuarioLogueado = Session.getInstance().getUser();
        boolean isAdmin = usuarioLogueado != null && usuarioLogueado.isAdmin();
        hboxAdminSelector.setVisible(isAdmin);
        hboxAdminSelector.setManaged(isAdmin);

        if (isAdmin) {
            comboUsuarios.setItems(FXCollections.observableArrayList(userService.findAll()));
            comboUsuarios.setPromptText("Seleccionar cliente...");
            comboUsuarios.setCellFactory(lv -> new ListCell<>() {
                @Override protected void updateItem(User item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "" : item.getName() + " " + item.getLastName());
                }
            });
            comboUsuarios.setButtonCell(comboUsuarios.getCellFactory().call(null));
            comboUsuarios.setAccessibleText("Seleccionar el cliente para la reserva");
        }
    }

    private void actualizarInterfazFecha() {
        lblFechaActual.setText(fechaSeleccionada.format(formatterLargo));
        lblResumenFecha.setText(fechaSeleccionada.format(formatterCorto));
        if (deporteActual != null) cargarPistasPorDeporte((int) (long) deporteActual.getId());
    }

    private void cargarDeportesDesdeDB() {
        hboxDeportes.getChildren().clear();
        List<Sport> deportes = sportService.findAll();

        for (Sport sport : deportes) {
            Button btn = new Button(sport.getName());
            btn.getStyleClass().add("tab-button");
            btn.setAccessibleText("Deporte " + sport.getName());

            if (hboxDeportes.getChildren().isEmpty()) {
                seleccionarDeporte(btn, sport);
            }

            btn.setOnAction(e -> seleccionarDeporte(btn, sport));
            hboxDeportes.getChildren().add(btn);
        }
    }

    private void seleccionarDeporte(Button btn, Sport sport) {
        hboxDeportes.getChildren().forEach(n -> n.getStyleClass().remove("active"));
        btn.getStyleClass().add("active");
        deporteActual = sport;
        lblResumenSport.setText(sport.getName());
        cargarPistasPorDeporte((int) (long) sport.getId());
    }

    private void cargarPistasPorDeporte(int sportId) {
        vboxPistasContenedor.getChildren().clear();
        courtService.findAll().stream()
                .filter(c -> c.getSport().getId() == sportId)
                .forEach(this::generarFilaPista);
    }

    private void generarFilaPista(Court pista) {
        HBox fila = new HBox(20);
        fila.setAlignment(Pos.CENTER_LEFT);

        VBox infoPista = new VBox(2);
        infoPista.setMinWidth(120);

        Label lblNombre = new Label(pista.getName());
        lblNombre.getStyleClass().add("court-row-name");

        Label lblPrecio = new Label(String.format("%.2f€/h", pista.getPrice()));
        lblPrecio.getStyleClass().add("court-row-price");

        infoPista.getChildren().addAll(lblNombre, lblPrecio);

        FlowPane flowHoras = new FlowPane(10, 10);
        HBox.setHgrow(flowHoras, Priority.ALWAYS);
        flowHoras.setAccessibleText("Horas disponibles para " + pista.getName());

        List<Reservation> ocupadas = modoEdicion
                ? reservationService.findByCourtAndDateExcluding(pista, fechaSeleccionada, reservacionAEditar.getId())
                : reservationService.findByCourtAndDate(pista, fechaSeleccionada);

        LocalTime hora = LocalTime.of(8, 0);
        while (!hora.isAfter(LocalTime.of(22, 30))) {
            Button btnHora = crearBotonHora(hora, pista, ocupadas);

            if (modoEdicion && reservacionAEditar.getCourt().getId().equals(pista.getId())
                    && reservacionAEditar.getStartHour().equals(hora)
                    && reservacionAEditar.getDate().equals(fechaSeleccionada)) {
                btnHora.getStyleClass().add("hour-selected");
                lastSelectedHour = btnHora;
                pistaSeleccionada = pista;
                lblResumenPista.setText(pista.getName());
                lblResumenHora.setText(hora.format(timeFormatter) + " - " + hora.plusMinutes(90).format(timeFormatter));
                lblResumenPrecio.setText(String.format("%.2f€", pista.getPrice()));
            }

            flowHoras.getChildren().add(btnHora);
            hora = hora.plusMinutes(90);
        }

        fila.getChildren().addAll(infoPista, flowHoras);
        vboxPistasContenedor.getChildren().add(fila);
        vboxPistasContenedor.setSpacing(25);
    }

    private Button crearBotonHora(LocalTime hora, Court pista, List<Reservation> ocupadas) {
        Button btn = new Button(hora.format(timeFormatter));
        boolean estaOcupado = ocupadas.stream().anyMatch(r -> r.getStartHour().equals(hora));
        boolean yaPaso = fechaSeleccionada.equals(LocalDate.now()) && hora.isBefore(LocalTime.now());

        if (estaOcupado || yaPaso) {
            btn.getStyleClass().add("hour-occupied");
            btn.setDisable(true);
            btn.setAccessibleText(hora.format(timeFormatter) + " en " + pista.getName() + ": ocupada");
        } else {
            btn.getStyleClass().add("hour-btn");
            btn.setAccessibleText(hora.format(timeFormatter) + " en " + pista.getName() +
                    ", precio " + String.format("%.0f", pista.getPrice()) + " euros: disponible");
            btn.setOnAction(e -> {
                if (lastSelectedHour != null) lastSelectedHour.getStyleClass().remove("hour-selected");
                btn.getStyleClass().add("hour-selected");
                lastSelectedHour = btn;
                pistaSeleccionada = pista;
                lblResumenPista.setText(pista.getName());
                lblResumenHora.setText(btn.getText() + " - " + hora.plusMinutes(90).format(timeFormatter));
                lblResumenPrecio.setText(String.format("%.2f€", pista.getPrice()));
            });
        }
        return btn;
    }

    @FXML
    private void handleConfirmar() {
        if (lastSelectedHour == null || pistaSeleccionada == null) {
            showAlert(Alert.AlertType.WARNING, "Atención", "Selecciona una pista y hora.");
            return;
        }

        User cliente = hboxAdminSelector.isVisible() ? comboUsuarios.getValue() : Session.getInstance().getUser();
        if (cliente == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Debes seleccionar un cliente.");
            return;
        }

        String mensaje = String.format("%s\n%s\n%s\n%s",
                modoEdicion ? "¿Guardar los cambios en la reserva?" : "¿Confirmar reserva?",
                lblResumenPista.getText(), lblResumenFecha.getText(), lblResumenHora.getText());

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, mensaje, ButtonType.YES, ButtonType.NO);
        confirm.setTitle(modoEdicion ? "Editar Reserva" : "Nueva Reserva");
        confirm.setHeaderText(null);
        applyAlertStyle(confirm);

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                if (modoEdicion) editarReserva(cliente);
                else guardarReserva(cliente);
            }
        });
    }

    private void guardarReserva(User cliente) {
        try {
            Reservation res = new Reservation();
            res.setUser(cliente);
            res.setCourt(pistaSeleccionada);
            res.setDate(fechaSeleccionada);
            res.setStartHour(LocalTime.parse(lastSelectedHour.getText(), timeFormatter));
            res.setEndHour(res.getStartHour().plusMinutes(90));
            res.setCancelled(false);

            if (reservationService.save(res)) {
                showAlert(Alert.AlertType.INFORMATION, "Éxito", "¡Reserva realizada!");
                cerrarVentana();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "No se pudo guardar la reserva.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "No se pudo guardar la reserva.");
        }
    }

    private void editarReserva(User cliente) {
        try {
            reservacionAEditar.setUser(cliente);
            reservacionAEditar.setCourt(pistaSeleccionada);
            reservacionAEditar.setDate(fechaSeleccionada);
            reservacionAEditar.setStartHour(LocalTime.parse(lastSelectedHour.getText(), timeFormatter));
            reservacionAEditar.setEndHour(reservacionAEditar.getStartHour().plusMinutes(90));

            if (reservationService.update(reservacionAEditar)) {
                showAlert(Alert.AlertType.INFORMATION, "Éxito", "¡Reserva actualizada!");
                cerrarVentana();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "No se pudo actualizar la reserva.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "No se pudo actualizar la reserva.");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type, content);
        alert.setTitle(title);
        alert.setHeaderText(null);
        applyAlertStyle(alert);
        alert.showAndWait();
    }

    private void applyAlertStyle(Alert alert) {
        DialogPane dp = alert.getDialogPane();
        var cssResource = getClass().getResource("/org/example/sportconnect/base.css");
        if (cssResource != null) {
            dp.getStylesheets().add(cssResource.toExternalForm());
            dp.getStyleClass().add("custom-alert");
        }
    }

    @FXML private void diaAnterior() {
        if (fechaSeleccionada.isAfter(LocalDate.now())) {
            fechaSeleccionada = fechaSeleccionada.minusDays(1);
            actualizarInterfazFecha();
        }
    }

    @FXML private void diaSiguiente() {
        if (fechaSeleccionada.isBefore(fechaLimite)) {
            fechaSeleccionada = fechaSeleccionada.plusDays(1);
            actualizarInterfazFecha();
        }
    }

    private void cerrarVentana() {
        ((Stage) btnConfirmar.getScene().getWindow()).close();
    }
}