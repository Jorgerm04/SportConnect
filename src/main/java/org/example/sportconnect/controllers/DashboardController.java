package org.example.sportconnect.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.sportconnect.models.*;
import org.example.sportconnect.services.*;
import org.example.sportconnect.utils.Session;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DashboardController {

    @FXML private Label lblTotalReservas, lblTotalIngresos, lblTotalUsuarios, lblTotalPistas;
    @FXML private TableView<Object> genericTable;
    @FXML private ScrollPane scrollDeportes;
    @FXML private TilePane tileDeportes;
    @FXML private Button btnTabReservas, btnTabDeportes, btnTabPistas, btnTabUsuarios;
    @FXML private Pagination pagination;
    @FXML private Button btnAddAction;
    @FXML private Label lblUserName;

    private List<Object> allData = new ArrayList<>();
    private static final int ROWS_PER_PAGE = 10;

    private final ReservationService reservationService = new ReservationService();
    private final UserService userService = new UserService();
    private final CourtService courtService = new CourtService();
    private final SportService sportService = new SportService();

    @FXML
    public void initialize() {
        User currentUser = Session.getInstance().getUser();
        if (currentUser == null) return;
        lblUserName.setText(currentUser.getName());
        pagination.setPageFactory(this::createPage);
        actualizarEstadisticas();
        cargarTabReservas();
        btnAddAction.setText("Nueva Reserva");
    }

    private void actualizarEstadisticas() {
        lblTotalReservas.setText(reservationService.getTotalActiveReservationsCount());
        lblTotalIngresos.setText(reservationService.getFormattedTotalEarnings());
        lblTotalUsuarios.setText(userService.getTotalUsersCount());
        lblTotalPistas.setText(courtService.getTotalCourtsCount());
    }

    private javafx.scene.Node createPage(int pageIndex) {
        int fromIndex = pageIndex * ROWS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ROWS_PER_PAGE, allData.size());
        if (allData.isEmpty()) {
            genericTable.setItems(javafx.collections.FXCollections.observableArrayList());
        } else {
            genericTable.setItems(javafx.collections.FXCollections.observableArrayList(allData.subList(fromIndex, toIndex)));
        }
        return new Label();
    }

    private void configurarPaginacion(List<?> datos) {
        this.allData = new ArrayList<>(datos);
        int pageCount = (int) Math.ceil((double) allData.size() / ROWS_PER_PAGE);
        pagination.setPageCount(pageCount > 0 ? pageCount : 1);
        pagination.setCurrentPageIndex(0);
        createPage(0);
    }

    @FXML
    private void handleTabChange(javafx.event.ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        btnTabReservas.getStyleClass().remove("active");
        btnTabDeportes.getStyleClass().remove("active");
        btnTabPistas.getStyleClass().remove("active");
        btnTabUsuarios.getStyleClass().remove("active");
        clickedButton.getStyleClass().add("active");

        genericTable.getItems().clear();
        genericTable.getColumns().clear();
        genericTable.setVisible(true);
        scrollDeportes.setVisible(false);

        if (clickedButton == btnTabReservas) {
            btnAddAction.setText("Nueva Reserva");
            cargarTabReservas();
        } else if (clickedButton == btnTabDeportes) {
            btnAddAction.setText("Nuevo Deporte");
            genericTable.setVisible(false);
            scrollDeportes.setVisible(true);
            cargarTabDeportes();
        } else if (clickedButton == btnTabPistas) {
            btnAddAction.setText("Nueva Pista");
            cargarTabPistas();
        } else if (clickedButton == btnTabUsuarios) {
            btnAddAction.setText("Nuevo Usuario");
            cargarTabUsuarios();
        }
    }

    // ─── TAB RESERVAS ─────────────────────────────────────────────────────────

    private void cargarTabReservas() {
        genericTable.getColumns().clear();
        pagination.setVisible(true);

        TableColumn<Object, String> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(d -> d.getValue() instanceof Reservation r
                ? new SimpleStringProperty(String.valueOf(r.getId())) : null);
        colId.setMaxWidth(55); colId.setMinWidth(55);

        TableColumn<Object, String> colUser = new TableColumn<>("Usuario");
        colUser.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || !(getTableRow().getItem() instanceof Reservation r)) { setGraphic(null); return; }
                Label name = new Label(r.getUser().getName() + " " + r.getUser().getLastName());
                Label email = new Label(r.getUser().getEmail());
                name.getStyleClass().add("table-text-bold");
                email.getStyleClass().add("table-text-sub");
                VBox vbox = new VBox(2, name, email);
                vbox.setAlignment(Pos.CENTER_LEFT);
                setGraphic(vbox);
                setAlignment(Pos.CENTER_LEFT);
            }
        });

        TableColumn<Object, String> colPista = new TableColumn<>("Pista");
        colPista.setCellValueFactory(d -> d.getValue() instanceof Reservation r
                ? new SimpleStringProperty(r.getCourt().getName()) : null);

        TableColumn<Object, String> colFecha = new TableColumn<>("Fecha");
        colFecha.setCellValueFactory(d -> {
            if (d.getValue() instanceof Reservation r) {
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM yyyy", new Locale("es", "ES"));
                return new SimpleStringProperty(r.getDate().format(fmt));
            }
            return null;
        });

        TableColumn<Object, String> colHorario = new TableColumn<>("Horario");
        colHorario.setCellValueFactory(d -> d.getValue() instanceof Reservation r
                ? new SimpleStringProperty(r.getStartHour() + " - " + r.getEndHour()) : null);

        TableColumn<Object, String> colEstado = new TableColumn<>("Estado");
        colEstado.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || !(getTableRow().getItem() instanceof Reservation r)) { setGraphic(null); return; }
                Label lbl = new Label(r.isCancelled() ? "Cancelada" : "Activa");
                String color = r.isCancelled() ? "#ef4444" : "#10b981";
                String bg    = r.isCancelled() ? "rgba(239,68,68,0.1)" : "rgba(16,185,129,0.1)";
                lbl.setStyle("-fx-text-fill:" + color + ";-fx-background-color:" + bg + ";-fx-padding:3 8;-fx-background-radius:5;");
                setGraphic(lbl);
                setAlignment(Pos.CENTER_LEFT);
            }
        });
        colEstado.setMinWidth(90); colEstado.setMaxWidth(90);

        TableColumn<Object, Void> colAcciones = new TableColumn<>("Acciones");
        colAcciones.setMinWidth(165); colAcciones.setMaxWidth(165);
        colAcciones.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || !(getTableRow().getItem() instanceof Reservation r)) { setGraphic(null); return; }

                boolean esPasada = r.getDate().isBefore(java.time.LocalDate.now()) ||
                        (r.getDate().isEqual(java.time.LocalDate.now()) &&
                                r.getEndHour().isBefore(java.time.LocalTime.now()));
                boolean bloquearEdicion = r.isCancelled() || esPasada;

                org.kordamp.ikonli.javafx.FontIcon iconEditar = new org.kordamp.ikonli.javafx.FontIcon("fas-pencil-alt");
                iconEditar.setIconColor(javafx.scene.paint.Color.web("#2563eb")); iconEditar.setIconSize(11);
                Button btnEditar = new Button("Editar", iconEditar);
                btnEditar.setStyle(
                        "-fx-background-color:rgba(37,99,235,0.15);-fx-text-fill:#2563eb;" +
                                "-fx-font-size:11px;-fx-font-weight:bold;-fx-background-radius:6;" +
                                "-fx-padding:4 10;-fx-cursor:hand;-fx-graphic-text-gap:5;-fx-content-display:LEFT;");
                btnEditar.setDisable(bloquearEdicion);
                if (bloquearEdicion) btnEditar.setOpacity(0.3);
                btnEditar.setOnAction(e -> abrirEdicionReserva(r));

                boolean cancelada = r.isCancelled();
                org.kordamp.ikonli.javafx.FontIcon iconToggle = new org.kordamp.ikonli.javafx.FontIcon(cancelada ? "fas-redo" : "fas-times");
                iconToggle.setIconColor(javafx.scene.paint.Color.web(cancelada ? "#10b981" : "#ef4444")); iconToggle.setIconSize(11);
                Button btnToggle = new Button(cancelada ? "Reactivar" : "Cancelar", iconToggle);
                btnToggle.setStyle(
                        "-fx-background-color:" + (cancelada ? "rgba(16,185,129,0.15)" : "rgba(239,68,68,0.15)") + ";" +
                                "-fx-text-fill:" + (cancelada ? "#10b981" : "#ef4444") + ";" +
                                "-fx-font-size:11px;-fx-font-weight:bold;-fx-background-radius:6;" +
                                "-fx-padding:4 10;-fx-cursor:hand;-fx-graphic-text-gap:5;-fx-content-display:LEFT;");
                btnToggle.setOnAction(e -> {
                    if (cancelada) confirmarYReactivar(r);
                    else confirmarYCancelar(r);
                });

                HBox box = new HBox(6, btnEditar, btnToggle);
                box.setAlignment(Pos.CENTER_LEFT);
                setGraphic(box);
                setAlignment(Pos.CENTER_LEFT);
            }
        });

        genericTable.getColumns().addAll(colId, colUser, colPista, colFecha, colHorario, colEstado, colAcciones);
        configurarPaginacion(reservationService.getAllReservations());
    }

    private void abrirEdicionReserva(Reservation reservation) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/org/example/sportconnect/reservation-form/reservation-form-view.fxml"));
            Scene scene = new Scene(loader.load());
            ReservationFormController ctrl = loader.getController();
            ctrl.setReservacionAEditar(reservation);

            Stage stage = new Stage();
            stage.setTitle("Editar Reserva #" + reservation.getId());
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.initOwner(btnAddAction.getScene().getWindow());
            stage.setMinWidth(1000); stage.setMinHeight(750);
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.showAndWait();
            refrescarReservas();
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void confirmarYCancelar(Reservation r) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Cancelar reserva");
        alert.setHeaderText("¿Cancelar la reserva #" + r.getId() + "?");
        alert.setContentText("La reserva quedará marcada como cancelada.");
        estilizarAlert(alert);
        alert.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) { reservationService.cancelReservation(r.getId()); refrescarReservas(); }
        });
    }

    private void confirmarYReactivar(Reservation r) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Reactivar reserva");
        alert.setHeaderText("¿Reactivar la reserva #" + r.getId() + "?");
        alert.setContentText("La reserva volverá a estar activa.");
        estilizarAlert(alert);
        alert.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) { reservationService.reactivateReservation(r.getId()); refrescarReservas(); }
        });
    }

    private void refrescarReservas() {
        actualizarEstadisticas();
        genericTable.getItems().clear();
        genericTable.getColumns().clear();
        cargarTabReservas();
    }

    private void estilizarAlert(Alert alert) {
        DialogPane dp = alert.getDialogPane();
        var css = getClass().getResource("/org/example/sportconnect/base.css");
        if (css != null) { dp.getStylesheets().add(css.toExternalForm()); dp.getStyleClass().add("custom-alert"); }
    }

    // ─── TAB DEPORTES ─────────────────────────────────────────────────────────

    private void cargarTabDeportes() {
        pagination.setVisible(false);
        tileDeportes.getChildren().clear();
        List<Sport> deportes = sportService.findAllSports();
        for (Sport sport : deportes) {
            VBox card = new VBox(10);
            card.getStyleClass().add("sport-card");
            card.setPrefSize(200, 130);
            Label nameLabel = new Label(sport.getName().toUpperCase());
            nameLabel.getStyleClass().add("table-text-bold");
            nameLabel.setStyle("-fx-font-size:16px;-fx-text-fill:#2563eb;");

            javafx.scene.layout.HBox btnBox = new javafx.scene.layout.HBox(6);
            btnBox.setAlignment(javafx.geometry.Pos.CENTER);
            org.kordamp.ikonli.javafx.FontIcon icoED = new org.kordamp.ikonli.javafx.FontIcon("fas-pencil-alt");
            icoED.setIconColor(javafx.scene.paint.Color.web("#2563eb")); icoED.setIconSize(11);
            javafx.scene.control.Button btnEditar = new javafx.scene.control.Button("Editar", icoED);
            btnEditar.setStyle("-fx-background-color:rgba(37,99,235,0.2);-fx-text-fill:#2563eb;-fx-font-size:11px;-fx-font-weight:bold;-fx-background-radius:6;-fx-padding:4 10;-fx-cursor:hand;-fx-graphic-text-gap:5;-fx-content-display:LEFT;");
            org.kordamp.ikonli.javafx.FontIcon icoELD = new org.kordamp.ikonli.javafx.FontIcon("fas-trash-alt");
            icoELD.setIconColor(javafx.scene.paint.Color.web("#ef4444")); icoELD.setIconSize(11);
            javafx.scene.control.Button btnEliminar = new javafx.scene.control.Button("Eliminar", icoELD);
            btnEliminar.setStyle("-fx-background-color:rgba(239,68,68,0.15);-fx-text-fill:#ef4444;-fx-font-size:11px;-fx-font-weight:bold;-fx-background-radius:6;-fx-padding:4 10;-fx-cursor:hand;-fx-graphic-text-gap:5;-fx-content-display:LEFT;");

            btnEditar.setOnAction(e -> abrirEdicionDeporte(sport));
            btnEliminar.setOnAction(e -> confirmarEliminarDeporte(sport));
            btnBox.getChildren().addAll(btnEditar, btnEliminar);
            card.getChildren().addAll(nameLabel, btnBox);
            card.setOnMouseEntered(ev -> card.setStyle("-fx-border-color:#2563eb;-fx-cursor:hand;-fx-background-color:rgba(37,99,235,0.05);-fx-background-radius:12;-fx-border-radius:12;-fx-border-width:1;-fx-padding:20;-fx-alignment:CENTER;"));
            card.setOnMouseExited(ev -> card.setStyle(""));
            tileDeportes.getChildren().add(card);
        }
    }

    private void abrirEdicionDeporte(Sport sport) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/org/example/sportconnect/forms/sport-form-view.fxml"));
            javafx.scene.Scene scene = new javafx.scene.Scene(loader.load());
            SportFormController ctrl = loader.getController();
            ctrl.setSportAEditar(sport);
            Stage stage = new Stage();
            stage.setTitle("Editar Deporte");
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.initOwner(btnAddAction.getScene().getWindow());
            stage.setScene(scene);
            stage.showAndWait();
            handleTabChange(new javafx.event.ActionEvent(btnTabDeportes, null));
        } catch (java.io.IOException e) { e.printStackTrace(); }
    }

    private void confirmarEliminarDeporte(Sport sport) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
        alert.setTitle("Eliminar deporte");
        alert.setHeaderText("¿Eliminar " + sport.getName() + "?");
        alert.setContentText("Se eliminará el deporte y todas sus pistas asociadas.");
        estilizarAlert(alert);
        alert.showAndWait().ifPresent(r -> {
            if (r == javafx.scene.control.ButtonType.OK) {
                sportService.deleteSport(sport.getId());
                handleTabChange(new javafx.event.ActionEvent(btnTabDeportes, null));
            }
        });
    }

    // ─── TAB PISTAS ───────────────────────────────────────────────────────────

    private void cargarTabPistas() {
        genericTable.getColumns().clear();
        pagination.setVisible(true);

        TableColumn<Object, String> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(d -> d.getValue() instanceof Court c
                ? new SimpleStringProperty(String.valueOf(c.getId())) : null);
        colId.setMaxWidth(55); colId.setMinWidth(55);

        TableColumn<Object, String> colPista = new TableColumn<>("Pista");
        colPista.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || !(getTableRow().getItem() instanceof Court c)) { setText(null); return; }
                setText(c.getName()); getStyleClass().add("table-text-bold");
            }
        });

        TableColumn<Object, String> colDeporte = new TableColumn<>("Deporte");
        colDeporte.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || !(getTableRow().getItem() instanceof Court c)) { setText(null); return; }
                setText(c.getSport().getName().toUpperCase());
                setStyle("-fx-text-fill:#2563eb;-fx-font-weight:bold;-fx-font-size:12px;");
            }
        });

        TableColumn<Object, String> colPrecio = new TableColumn<>("Precio / Hora");
        colPrecio.setCellValueFactory(d -> d.getValue() instanceof Court c
                ? new SimpleStringProperty(String.format("%.2f €", c.getPrice())) : null);

        TableColumn<Object, Void> colAccionesPista = new TableColumn<>("Acciones");
        colAccionesPista.setMinWidth(165); colAccionesPista.setMaxWidth(165);
        colAccionesPista.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || !(getTableRow().getItem() instanceof Court c)) { setGraphic(null); return; }
                org.kordamp.ikonli.javafx.FontIcon icoEditP = new org.kordamp.ikonli.javafx.FontIcon("fas-pencil-alt");
                icoEditP.setIconColor(javafx.scene.paint.Color.web("#2563eb")); icoEditP.setIconSize(11);
                Button btnEditar = new Button("Editar", icoEditP);
                btnEditar.setStyle("-fx-background-color:rgba(37,99,235,0.15);-fx-text-fill:#2563eb;-fx-font-size:11px;-fx-font-weight:bold;-fx-background-radius:6;-fx-padding:4 10;-fx-cursor:hand;-fx-graphic-text-gap:5;-fx-content-display:LEFT;");
                btnEditar.setOnAction(e -> abrirEdicionPista(c));
                org.kordamp.ikonli.javafx.FontIcon icoDelP = new org.kordamp.ikonli.javafx.FontIcon("fas-trash-alt");
                icoDelP.setIconColor(javafx.scene.paint.Color.web("#ef4444")); icoDelP.setIconSize(11);
                Button btnEliminar = new Button("Eliminar", icoDelP);
                btnEliminar.setStyle("-fx-background-color:rgba(239,68,68,0.15);-fx-text-fill:#ef4444;-fx-font-size:11px;-fx-font-weight:bold;-fx-background-radius:6;-fx-padding:4 10;-fx-cursor:hand;-fx-graphic-text-gap:5;-fx-content-display:LEFT;");
                btnEliminar.setOnAction(e -> confirmarEliminarPista(c));
                HBox box = new HBox(6, btnEditar, btnEliminar);
                box.setAlignment(Pos.CENTER_LEFT);
                setGraphic(box); setAlignment(Pos.CENTER_LEFT);
            }
        });

        genericTable.getColumns().addAll(colId, colPista, colDeporte, colPrecio, colAccionesPista);
        configurarPaginacion(courtService.getAllCourts());
    }

    private void abrirEdicionPista(Court court) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/org/example/sportconnect/forms/court-form-view.fxml"));
            javafx.scene.Scene scene = new javafx.scene.Scene(loader.load());
            CourtFormController ctrl = loader.getController();
            ctrl.setPistaAEditar(court);
            Stage stage = new Stage();
            stage.setTitle("Editar Pista");
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.initOwner(btnAddAction.getScene().getWindow());
            stage.setScene(scene);
            stage.showAndWait();
            handleTabChange(new javafx.event.ActionEvent(btnTabPistas, null));
        } catch (java.io.IOException e) { e.printStackTrace(); }
    }

    private void confirmarEliminarPista(Court court) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
        alert.setTitle("Eliminar pista");
        alert.setHeaderText("¿Eliminar " + court.getName() + "?");
        alert.setContentText("Se eliminará la pista y todas sus reservas asociadas.");
        estilizarAlert(alert);
        alert.showAndWait().ifPresent(r -> {
            if (r == javafx.scene.control.ButtonType.OK) {
                courtService.deleteCourt(court.getId());
                actualizarEstadisticas();
                handleTabChange(new javafx.event.ActionEvent(btnTabPistas, null));
            }
        });
    }

    // ─── TAB USUARIOS ─────────────────────────────────────────────────────────

    private void cargarTabUsuarios() {
        genericTable.getColumns().clear();
        pagination.setVisible(true);

        TableColumn<Object, String> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(d -> d.getValue() instanceof User u
                ? new SimpleStringProperty(String.valueOf(u.getId())) : null);
        colId.setMaxWidth(55); colId.setMinWidth(55);

        TableColumn<Object, String> colNombre = new TableColumn<>("Nombre");
        colNombre.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || !(getTableRow().getItem() instanceof User u)) { setText(null); return; }
                setText(u.getName()); getStyleClass().add("table-text-bold");
            }
        });

        TableColumn<Object, String> colApellidos = new TableColumn<>("Apellidos");
        colApellidos.setCellValueFactory(d -> d.getValue() instanceof User u
                ? new SimpleStringProperty(u.getLastName()) : null);

        TableColumn<Object, String> colEmail = new TableColumn<>("Email");
        colEmail.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || !(getTableRow().getItem() instanceof User u)) { setText(null); return; }
                setText(u.getEmail()); getStyleClass().add("table-text-sub");
            }
        });

        TableColumn<Object, String> colPhone = new TableColumn<>("Teléfono");
        colPhone.setCellValueFactory(d -> d.getValue() instanceof User u
                ? new SimpleStringProperty(u.getPhone() != null ? u.getPhone() : "N/A") : null);

        TableColumn<Object, String> colAdmin = new TableColumn<>("Rol");
        colAdmin.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || !(getTableRow().getItem() instanceof User u)) { setGraphic(null); return; }
                Label lbl = new Label(u.isAdmin() ? "ADMIN" : "CLIENTE");
                lbl.setStyle(u.isAdmin()
                        ? "-fx-text-fill:#2563eb;-fx-font-weight:bold;-fx-background-color:rgba(37,99,235,0.1);-fx-padding:3 8;-fx-background-radius:5;"
                        : "-fx-text-fill:#94a3b8;-fx-padding:3 8;");
                setGraphic(lbl);
            }
        });

        TableColumn<Object, Void> colAccionesUser = new TableColumn<>("Acciones");
        colAccionesUser.setMinWidth(165); colAccionesUser.setMaxWidth(165);
        colAccionesUser.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || !(getTableRow().getItem() instanceof User u)) { setGraphic(null); return; }
                org.kordamp.ikonli.javafx.FontIcon icoEditU = new org.kordamp.ikonli.javafx.FontIcon("fas-pencil-alt");
                icoEditU.setIconColor(javafx.scene.paint.Color.web("#2563eb")); icoEditU.setIconSize(11);
                Button btnEditar = new Button("Editar", icoEditU);
                btnEditar.setStyle("-fx-background-color:rgba(37,99,235,0.15);-fx-text-fill:#2563eb;-fx-font-size:11px;-fx-font-weight:bold;-fx-background-radius:6;-fx-padding:4 10;-fx-cursor:hand;-fx-graphic-text-gap:5;-fx-content-display:LEFT;");
                btnEditar.setOnAction(e -> abrirEdicionUsuario(u));
                org.kordamp.ikonli.javafx.FontIcon icoDelU = new org.kordamp.ikonli.javafx.FontIcon("fas-trash-alt");
                icoDelU.setIconColor(javafx.scene.paint.Color.web("#ef4444")); icoDelU.setIconSize(11);
                Button btnEliminar = new Button("Eliminar", icoDelU);
                btnEliminar.setStyle("-fx-background-color:rgba(239,68,68,0.15);-fx-text-fill:#ef4444;-fx-font-size:11px;-fx-font-weight:bold;-fx-background-radius:6;-fx-padding:4 10;-fx-cursor:hand;-fx-graphic-text-gap:5;-fx-content-display:LEFT;");
                // No permitir eliminar al usuario actual
                User currentUser = org.example.sportconnect.utils.Session.getInstance().getUser();
                if (currentUser != null && currentUser.getId().equals(u.getId())) {
                    btnEliminar.setDisable(true); btnEliminar.setOpacity(0.3);
                    btnEliminar.setStyle(btnEliminar.getStyle() + "-fx-cursor:default;");
                }
                btnEliminar.setOnAction(e -> confirmarEliminarUsuario(u));
                HBox box = new HBox(6, btnEditar, btnEliminar);
                box.setAlignment(Pos.CENTER_LEFT);
                setGraphic(box); setAlignment(Pos.CENTER_LEFT);
            }
        });

        genericTable.getColumns().addAll(colId, colNombre, colApellidos, colEmail, colPhone, colAdmin, colAccionesUser);
        configurarPaginacion(userService.getAllUsers());
    }

    private void abrirEdicionUsuario(User user) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/org/example/sportconnect/forms/user-form-view.fxml"));
            javafx.scene.Scene scene = new javafx.scene.Scene(loader.load());
            UserFormController ctrl = loader.getController();
            ctrl.setUsuarioAEditar(user);
            Stage stage = new Stage();
            stage.setTitle("Editar Usuario");
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.initOwner(btnAddAction.getScene().getWindow());
            stage.setScene(scene);
            stage.showAndWait();
            handleTabChange(new javafx.event.ActionEvent(btnTabUsuarios, null));
        } catch (java.io.IOException e) { e.printStackTrace(); }
    }

    private void confirmarEliminarUsuario(User user) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
        alert.setTitle("Eliminar usuario");
        alert.setHeaderText("¿Eliminar a " + user.getName() + " " + user.getLastName() + "?");
        alert.setContentText("Esta acción no se puede deshacer.");
        estilizarAlert(alert);
        alert.showAndWait().ifPresent(r -> {
            if (r == javafx.scene.control.ButtonType.OK) {
                userService.deleteUser(user.getId());
                actualizarEstadisticas();
                handleTabChange(new javafx.event.ActionEvent(btnTabUsuarios, null));
            }
        });
    }

    // ─── ACCIONES ─────────────────────────────────────────────────────────────

    @FXML
    private void handleAdd() {
        switch (btnAddAction.getText()) {
            case "Nueva Reserva" -> abrirModal("/org/example/sportconnect/reservation-form-view/reservation-form-view.fxml", "Nueva Reserva");
            case "Nuevo Deporte" -> abrirModal("/org/example/sportconnect/forms/sport-form-view.fxml", "Nuevo Deporte");
            case "Nueva Pista"   -> abrirModal("/org/example/sportconnect/forms/court-form-view.fxml", "Nueva Pista");
            case "Nuevo Usuario" -> abrirModal("/org/example/sportconnect/forms/user-form-view.fxml", "Nuevo Usuario");
        }
    }

    @FXML
    public void handleLogout(javafx.event.ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/sportconnect/login/login-view.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void abrirModal(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Scene scene = new Scene(loader.load());
            Stage stage = new Stage();
            stage.setTitle(title);
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.initOwner(btnAddAction.getScene().getWindow());
            stage.setMinWidth(500); stage.setMinHeight(400);
            stage.setScene(scene);
            stage.showAndWait();
            actualizarEstadisticas();
            handleTabChange(new javafx.event.ActionEvent(obtenerBotonActivo(), null));
        } catch (IOException e) { e.printStackTrace(); }
    }

    private Button obtenerBotonActivo() {
        if (btnTabReservas.getStyleClass().contains("active")) return btnTabReservas;
        if (btnTabDeportes.getStyleClass().contains("active")) return btnTabDeportes;
        if (btnTabPistas.getStyleClass().contains("active")) return btnTabPistas;
        return btnTabUsuarios;
    }
}