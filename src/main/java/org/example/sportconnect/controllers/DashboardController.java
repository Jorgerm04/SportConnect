package org.example.sportconnect.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
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

    private List<Object> allData = new ArrayList<>(); // Almacena la lista completa actual
    private static final int ROWS_PER_PAGE = 10; // Cuántas filas quieres por página

    private final ReservationService reservationService = new ReservationService();
    private final UserService userService = new UserService();
    private final CourtService courtService = new CourtService();
    private final SportService sportService = new SportService();

    @FXML
    public void initialize() {
        User currentUser = Session.getInstance().getUser();
        if (currentUser == null) return; // Seguridad por si no hay sesión
        lblUserName.setText(currentUser.getName());
        pagination.setPageFactory(this::createPage);
        actualizarEstadisticas();
        cargarTabReservas();
        btnAddAction.setText("Nueva Reserva");
    }

    private void actualizarEstadisticas() {
        lblTotalReservas.setText(String.valueOf(reservationService.getTotalActiveReservationsCount()));
        lblTotalIngresos.setText(reservationService.getFormattedTotalEarnings());
        lblTotalUsuarios.setText(String.valueOf(userService.getTotalUsersCount()));
        lblTotalPistas.setText(String.valueOf(courtService.getTotalCourtsCount()));
    }

    private javafx.scene.Node createPage(int pageIndex) {
        int fromIndex = pageIndex * ROWS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ROWS_PER_PAGE, allData.size());

        if (allData.isEmpty()) {
            genericTable.setItems(javafx.collections.FXCollections.observableArrayList());
        } else {
            genericTable.setItems(javafx.collections.FXCollections.observableArrayList(allData.subList(fromIndex, toIndex)));
        }

        // Devolvemos un nodo vacío porque no queremos reemplazar la tabla,
        // solo actualizar sus items. La tabla ya está en el layout.
        return new Label();
    }

    private void configurarPaginacion(List<?> datos) {
        this.allData = new ArrayList<>(datos);
        int pageCount = (int) Math.ceil((double) allData.size() / ROWS_PER_PAGE);
        pagination.setPageCount(pageCount > 0 ? pageCount : 1);
        pagination.setCurrentPageIndex(0);

        // Refrescar la primera página
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

        // Limpieza total de la tabla antes de cambiar de vista
        genericTable.getItems().clear();
        genericTable.getColumns().clear();

        genericTable.setVisible(true);
        scrollDeportes.setVisible(false);

        if (clickedButton == btnTabReservas) cargarTabReservas();
        else if (clickedButton == btnTabDeportes) {
            genericTable.setVisible(false);
            scrollDeportes.setVisible(true);
            cargarTabDeportes();
        }
        else if (clickedButton == btnTabPistas) cargarTabPistas();
        else if (clickedButton == btnTabUsuarios) cargarTabUsuarios();

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

    private void cargarTabReservas() {
        genericTable.getColumns().clear();
        pagination.setVisible(true);
        // Columna ID
        TableColumn<Object, String> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(d -> {
            if (d.getValue() instanceof Reservation r) return new SimpleStringProperty(String.valueOf(r.getId()));
            return null;
        });

        // Columna Usuario
        TableColumn<Object, String> colUser = new TableColumn<>("Usuario");
        colUser.setCellFactory(column -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow().getItem() == null || !(getTableRow().getItem() instanceof Reservation r)) {
                    setGraphic(null);
                } else {
                    Label name = new Label(r.getUser().getName() + " " + r.getUser().getLastName());
                    Label email = new Label(r.getUser().getEmail());
                    name.getStyleClass().add("table-text-bold");
                    email.getStyleClass().add("table-text-sub");
                    setGraphic(new VBox(2, name, email));
                }
            }
        });

        TableColumn<Object, String> colPista = new TableColumn<>("Pista");
        colPista.setCellValueFactory(d -> {
            if (d.getValue() instanceof Reservation r) return new SimpleStringProperty(r.getCourt().getName());
            return null;
        });

        TableColumn<Object, String> colFecha = new TableColumn<>("Fecha");
        colFecha.setCellValueFactory(d -> {
            if (d.getValue() instanceof Reservation r) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy", new Locale("es", "ES"));
                return new SimpleStringProperty(r.getDate().format(formatter));
            }
            return null;
        });

        TableColumn<Object, String> colHorario = new TableColumn<>("Horario");
        colHorario.setCellValueFactory(d -> {
            if (d.getValue() instanceof Reservation r) return new SimpleStringProperty(r.getStartHour() + " - " + r.getEndHour());
            return null;
        });

        TableColumn<Object, String> colEstado = new TableColumn<>("Estado");
        colEstado.setCellFactory(column -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow().getItem() == null || !(getTableRow().getItem() instanceof Reservation r)) {
                    setGraphic(null);
                } else {
                    Label statusLabel = new Label(r.isCancelled() ? "Cancelada" : "Activa");
                    String color = r.isCancelled() ? "#ef4444" : "#10b981";
                    String bg = r.isCancelled() ? "rgba(239, 68, 68, 0.1)" : "rgba(16, 185, 129, 0.1)";
                    statusLabel.setStyle("-fx-text-fill: " + color + "; -fx-background-color: " + bg + "; -fx-padding: 3 8; -fx-background-radius: 5;");
                    setGraphic(statusLabel);
                }
            }
        });

        genericTable.getColumns().addAll(colId, colUser, colPista, colFecha, colHorario, colEstado);
        configurarPaginacion(reservationService.getAllReservations());
    }

    private void cargarTabDeportes() {
        pagination.setVisible(false);
        tileDeportes.getChildren().clear();
        List<Sport> deportes = sportService.findAllSports();
        for (Sport sport : deportes) {
            VBox card = new VBox();
            card.getStyleClass().add("sport-card");
            card.setPrefSize(200, 120);
            Label nameLabel = new Label(sport.getName().toUpperCase());
            nameLabel.getStyleClass().add("table-text-bold");
            nameLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #2563eb;");
            card.getChildren().add(nameLabel);
            card.setOnMouseEntered(e -> card.setStyle("-fx-border-color: #2563eb; -fx-cursor: hand;"));
            card.setOnMouseExited(e -> card.setStyle("-fx-border-color: #1e293b;"));
            tileDeportes.getChildren().add(card);
        }
    }

    private void cargarTabUsuarios() {
        genericTable.getColumns().clear();
        pagination.setVisible(true);

        TableColumn<Object, String> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(d -> {
            if (d.getValue() instanceof User u) return new SimpleStringProperty(String.valueOf(u.getId()));
            return null;
        });

        TableColumn<Object, String> colNombre = new TableColumn<>("Nombre");
        colNombre.setCellFactory(column -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow().getItem() == null || !(getTableRow().getItem() instanceof User u)) {
                    setText(null);
                } else {
                    setText(u.getName());
                    getStyleClass().add("table-text-bold");
                }
            }
        });

        TableColumn<Object, String> colApellidos = new TableColumn<>("Apellidos");
        colApellidos.setCellValueFactory(d -> d.getValue() instanceof User u ? new SimpleStringProperty(u.getLastName()) : null);

        TableColumn<Object, String> colEmail = new TableColumn<>("Email");
        colEmail.setCellFactory(column -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow().getItem() == null || !(getTableRow().getItem() instanceof User u)) {
                    setText(null);
                } else {
                    setText(u.getEmail());
                    getStyleClass().add("table-text-sub");
                }
            }
        });

        TableColumn<Object, String> colPhone = new TableColumn<>("Teléfono");
        colPhone.setCellValueFactory(d -> {
            if (d.getValue() instanceof User u) return new SimpleStringProperty(u.getPhone() != null ? u.getPhone() : "N/A");
            return null;
        });

        TableColumn<Object, String> colAdmin = new TableColumn<>("Rol");
        colAdmin.setCellFactory(column -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow().getItem() == null || !(getTableRow().getItem() instanceof User u)) {
                    setGraphic(null);
                } else {
                    Label roleLabel = new Label(u.isAdmin() ? "ADMIN" : "CLIENTE");
                    if (u.isAdmin()) roleLabel.setStyle("-fx-text-fill: #2563eb; -fx-font-weight: bold; -fx-background-color: rgba(37, 99, 235, 0.1); -fx-padding: 3 8; -fx-background-radius: 5;");
                    else roleLabel.setStyle("-fx-text-fill: #94a3b8; -fx-padding: 3 8;");
                    setGraphic(roleLabel);
                }
            }
        });

        genericTable.getColumns().addAll(colId, colNombre, colApellidos, colEmail, colPhone, colAdmin);
        configurarPaginacion(userService.getAllUsers());
    }

    private void cargarTabPistas() {
        genericTable.getColumns().clear();
        pagination.setVisible(true);
        TableColumn<Object, String> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(d -> d.getValue() instanceof Court c ? new SimpleStringProperty(String.valueOf(c.getId())) : null);

        TableColumn<Object, String> colPista = new TableColumn<>("Pista");
        colPista.setCellFactory(column -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow().getItem() == null || !(getTableRow().getItem() instanceof Court c)) {
                    setText(null);
                } else {
                    setText(c.getName());
                    getStyleClass().add("table-text-bold");
                }
            }
        });

        TableColumn<Object, String> colDeporte = new TableColumn<>("Deporte");
        colDeporte.setCellFactory(column -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow().getItem() == null || !(getTableRow().getItem() instanceof Court c)) {
                    setText(null);
                } else {
                    setText(c.getSport().getName().toUpperCase());
                    setStyle("-fx-text-fill: #2563eb; -fx-font-weight: bold; -fx-font-size: 12px;");
                }
            }
        });

        TableColumn<Object, String> colPrecio = new TableColumn<>("Precio / Hora");
        colPrecio.setCellValueFactory(d -> d.getValue() instanceof Court c ? new SimpleStringProperty(String.format("%.2f €", c.getPrice())) : null);

        genericTable.getColumns().addAll(colId, colPista, colDeporte, colPrecio);
        configurarPaginacion(courtService.getAllCourts());
    }

    /**
     * Método que se ejecuta al pulsar el botón "Nuevo Registro" (btnAddAction)
     */
    @FXML
    private void handleAdd() {
        // Detectamos qué pestaña está activa mediante el texto del botón o consultando la clase CSS de los tabs
        String currentAction = btnAddAction.getText();

        switch (currentAction) {
            case "Nueva Reserva" -> abrirFormularioReserva();
            case "Nuevo Deporte" -> abrirFormularioDeporte();
            case "Nueva Pista"   -> abrirFormularioPista();
            case "Nuevo Usuario" -> abrirFormularioUsuario();
            default -> System.out.println("Acción no definida");
        }
    }

    private void abrirFormularioReserva() {
        abrirModal("/org/example/sportconnect/reservation-form/reservation-form-view.fxml", "Nueva Reserva");
    }

    private void abrirFormularioDeporte() {
        abrirModal("/org/example/sportconnect/forms/deporte-form.fxml", "Nuevo Deporte");
    }

    private void abrirFormularioPista() {
        abrirModal("/org/example/sportconnect/forms/pista-form.fxml", "Nueva Pista");
    }

    private void abrirFormularioUsuario() {
        abrirModal("/org/example/sportconnect/forms/usuario-form.fxml", "Nuevo Usuario");
    }

    @FXML
    public void handleLogout(javafx.event.ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/sportconnect/login/login-view.fxml"));
            Scene loginScene = new Scene(loader.load());
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(loginScene);
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void abrirModal(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Scene scene = new Scene(loader.load());

            // Crear el nuevo Stage (Ventana)
            Stage stage = new Stage();
            stage.setTitle(title);
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL); // Bloquea la ventana principal
            stage.initOwner(btnAddAction.getScene().getWindow()); // Hace que dependa del Dashboard
            stage.setMinWidth(1000);
            stage.setMinHeight(750);
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.showAndWait(); // Pausa la ejecución hasta que se cierre el modal

            // Al cerrar el modal, podrías refrescar los datos
            actualizarEstadisticas();
            handleTabChange(new javafx.event.ActionEvent(obtenerBotonActivo(), null));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Método auxiliar para saber qué pestaña refrescar
    private Button obtenerBotonActivo() {
        if (btnTabReservas.getStyleClass().contains("active")) return btnTabReservas;
        if (btnTabDeportes.getStyleClass().contains("active")) return btnTabDeportes;
        if (btnTabPistas.getStyleClass().contains("active")) return btnTabPistas;
        return btnTabUsuarios;
    }
}