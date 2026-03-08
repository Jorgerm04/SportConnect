package org.example.sportconnect.controllers;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Popup;
import javafx.stage.Stage;
import org.example.sportconnect.models.*;
import org.example.sportconnect.services.*;
import org.example.sportconnect.utils.Session;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class DashboardController {

    // ─── FXML ─────────────────────────────────────────────────────────────────

    @FXML private Label lblTotalReservas, lblTotalIngresos, lblTotalUsuarios, lblTotalPistas;
    @FXML private TableView<Object> genericTable;
    @FXML private ScrollPane scrollDeportes;
    @FXML private TilePane tileDeportes;
    @FXML private Button btnTabReservas, btnTabDeportes, btnTabPistas, btnTabUsuarios;
    @FXML private Pagination pagination;
    @FXML private Button btnAddAction;
    @FXML private Label lblUserName;
    @FXML private HBox bulkBar;
    @FXML private Label lblSeleccionados;
    @FXML private Button btnBulkEliminar;
    @FXML private Button btnBulkExportar;
    @FXML private TextField txtBuscar;
    @FXML private Button btnColumnas;
    @FXML private DatePicker dpDesde;
    @FXML private DatePicker dpHasta;
    @FXML private HBox hboxFechas;

    // ─── ESTADO ───────────────────────────────────────────────────────────────

    private static final int ROWS_PER_PAGE = 10;

    private String filtroActual  = "";
    private LocalDate fechaDesde = null;    // solo reservas
    private LocalDate fechaHasta = null;    // solo reservas
    private long totalRegistros  = 0;

    private javafx.beans.value.ChangeListener<LocalDate> listenerDesde;
    private javafx.beans.value.ChangeListener<LocalDate> listenerHasta;
    private Popup columnasPopup;

    // Mapa ID -> objeto para seleccion robusta (sin depender de equals/hashCode en los modelos)
    private final Map<Long, Object> seleccionados = new LinkedHashMap<>();

    // ─── SERVICIOS ────────────────────────────────────────────────────────────

    private final ReservationService reservationService = new ReservationService();
    private final UserService        userService        = new UserService();
    private final CourtService       courtService       = new CourtService();
    private final SportService       sportService       = new SportService();

    // ─── INICIALIZACION ───────────────────────────────────────────────────────

    @FXML
    public void initialize() {
        User currentUser = Session.getInstance().getUser();
        if (currentUser == null) return;

        lblUserName.setText(currentUser.getName());
        pagination.setPageFactory(this::createPage);
        actualizarEstadisticas();

        btnTabReservas.setAccessibleText("Pestana Reservas");
        btnTabDeportes.setAccessibleText("Pestana Deportes");
        btnTabPistas.setAccessibleText("Pestana Pistas");
        btnTabUsuarios.setAccessibleText("Pestana Usuarios");
        btnAddAction.setAccessibleText("Crear nuevo registro");

        bulkBar.setVisible(false);
        bulkBar.setManaged(false);

        configurarBotonIcono(btnBulkEliminar, "fas-trash-alt", "#ef4444", 12, e -> handleBulkEliminar());
        configurarBotonIcono(btnBulkExportar, "fas-file-csv",  "#10b981", 12, e -> handleBulkExportar());
        configurarBotonIcono(btnColumnas,     "fas-columns",   "#94a3b8", 12, e -> toggleColumnasPopup());

        txtBuscar.textProperty().addListener((obs, oldVal, newVal) -> aplicarFiltro(newVal));
        listenerDesde = (obs, o, nv) -> { fechaDesde = nv; recargarPaginacion(); };
        listenerHasta = (obs, o, nv) -> { fechaHasta = nv; recargarPaginacion(); };
        dpDesde.valueProperty().addListener(listenerDesde);
        dpHasta.valueProperty().addListener(listenerHasta);

        cargarTabReservas();
    }

    // ─── HELPERS DE UI ────────────────────────────────────────────────────────

    /** Asigna icono y handler a un boton ya existente (toolbar) */
    private void configurarBotonIcono(Button btn, String icono, String color, int size,
                                      javafx.event.EventHandler<javafx.event.ActionEvent> handler) {
        FontIcon ico = new FontIcon(icono);
        ico.setIconColor(javafx.scene.paint.Color.web(color));
        ico.setIconSize(size);
        btn.setGraphic(ico);
        btn.setOnAction(handler);
    }

    /** Crea un boton con icono para celdas de tabla */
    private Button crearBoton(String texto, String icono, String color,
                              String styleClass, String accessibleText) {
        FontIcon ico = new FontIcon(icono);
        ico.setIconColor(javafx.scene.paint.Color.web(color));
        ico.setIconSize(11);
        Button btn = new Button(texto, ico);
        btn.getStyleClass().add(styleClass);
        btn.setAccessibleText(accessibleText);
        return btn;
    }

    /** Abre un Stage modal. configureController recibe el controlador cargado para preconfigurar datos */
    private <C> void abrirModal(String fxmlPath, String titulo, int minW, int minH,
                                Consumer<C> configureController) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Scene scene = new Scene(loader.load());
            if (configureController != null) configureController.accept(loader.getController());
            Stage stage = new Stage();
            stage.setTitle(titulo);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(btnAddAction.getScene().getWindow());
            stage.setMinWidth(minW);
            stage.setMinHeight(minH);
            stage.setScene(scene);
            stage.showAndWait();
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void estilizarAlert(Alert alert) {
        DialogPane dp = alert.getDialogPane();
        var css = getClass().getResource("/org/example/sportconnect/base.css");
        if (css != null) { dp.getStylesheets().add(css.toExternalForm()); dp.getStyleClass().add("custom-alert"); }
    }

    /** Controla visibilidad y managed de un nodo a la vez */
    private void setVisible(Node node, boolean visible) {
        node.setVisible(visible);
        node.setManaged(visible);
    }

    /**
     * Configura la visibilidad de los controles de la barra de herramientas segun el tab activo.
     * @param eliminar  si se muestra el boton eliminar bulk
     * @param buscar    si se muestra el campo de busqueda
     * @param fechas    si se muestra el filtro de fechas
     * @param columnas  si se muestra el selector de columnas
     */
    private void configurarBarraHerramientas(boolean eliminar, boolean buscar,
                                             boolean fechas,   boolean columnas) {
        pagination.setVisible(true);
        setVisible(btnBulkEliminar, eliminar);
        setVisible(txtBuscar,       buscar);
        setVisible(hboxFechas,      fechas);
        setVisible(btnColumnas,     columnas);
    }

    // ─── SELECTOR DE COLUMNAS ─────────────────────────────────────────────────

    private void toggleColumnasPopup() {
        if (columnasPopup != null && columnasPopup.isShowing()) { columnasPopup.hide(); return; }
        columnasPopup = new Popup();
        columnasPopup.setAutoHide(true);

        VBox contenido = new VBox(4);
        contenido.setPadding(new Insets(12));
        contenido.getStyleClass().add("columnas-popup");
        contenido.setStyle(
                "-fx-background-color: #1e293b;" +
                        "-fx-border-color: #334155;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;"
        );
        btnColumnas.getScene().getStylesheets().forEach(contenido.getStylesheets()::add);

        for (TableColumn<Object, ?> col : genericTable.getColumns()) {
            String nombre = col.getText();
            if (nombre == null || nombre.isBlank() || nombre.equals("Acciones")) continue;
            CheckBox cb = new CheckBox(nombre);
            cb.setSelected(col.isVisible());
            cb.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 13px;");
            cb.setOnAction(e -> col.setVisible(cb.isSelected()));
            contenido.getChildren().add(cb);
        }

        columnasPopup.getContent().add(contenido);
        javafx.geometry.Bounds bounds = btnColumnas.localToScreen(btnColumnas.getBoundsInLocal());
        columnasPopup.show(btnColumnas.getScene().getWindow(), bounds.getMinX(), bounds.getMaxY() + 8);
    }

    // ─── BUSQUEDA Y PAGINACION ────────────────────────────────────────────────

    private void aplicarFiltro(String texto) {
        filtroActual = (texto == null) ? "" : texto.strip();
        seleccionados.clear();
        recargarPaginacion();
    }

    /**
     * Recalcula totalRegistros y pageCount desde la BD con el filtro actual
     * y resetea el paginador a la pagina 0.
     */
    private void recargarPaginacion() {
        Button activo = obtenerBotonActivo();
        if (activo == btnTabDeportes) return; // Deportes no usa paginacion

        totalRegistros = contarConFiltro(activo);
        int pageCount = (int) Math.ceil((double) totalRegistros / ROWS_PER_PAGE);
        pagination.setPageCount(pageCount > 0 ? pageCount : 1);
        pagination.setCurrentPageIndex(0);
        createPage(0);
        actualizarBulkBar();
    }

    private long contarConFiltro(Button tab) {
        if (tab == btnTabReservas) return reservationService.count(filtroActual, fechaDesde, fechaHasta);
        if (tab == btnTabPistas)   return courtService.count(filtroActual);
        if (tab == btnTabUsuarios) return userService.count(filtroActual);
        return 0;
    }

    private Node createPage(int pageIndex) {
        int offset = pageIndex * ROWS_PER_PAGE;
        List<Object> pagina = obtenerPagina(obtenerBotonActivo(), offset, ROWS_PER_PAGE);
        genericTable.setItems(javafx.collections.FXCollections.observableArrayList(pagina));
        return new Label();
    }

    private List<Object> obtenerPagina(Button tab, int offset, int limit) {
        if (tab == btnTabReservas)
            return new ArrayList<>(reservationService.findPage(offset, limit, filtroActual, fechaDesde, fechaHasta));
        if (tab == btnTabPistas)
            return new ArrayList<>(courtService.findPage(offset, limit, filtroActual));
        if (tab == btnTabUsuarios)
            return new ArrayList<>(userService.findPage(offset, limit, filtroActual));
        return Collections.emptyList();
    }

    private void configurarPaginacion() {
        filtroActual = "";
        fechaDesde   = null;
        fechaHasta   = null;
        txtBuscar.clear();

        // Limpiar datepickers sin disparar los listeners
        dpDesde.valueProperty().removeListener(listenerDesde);
        dpHasta.valueProperty().removeListener(listenerHasta);
        dpDesde.setValue(null);
        dpHasta.setValue(null);
        dpDesde.valueProperty().addListener(listenerDesde);
        dpHasta.valueProperty().addListener(listenerHasta);

        if (obtenerBotonActivo() == btnTabReservas) {
            fechaDesde = LocalDate.now();
            dpDesde.setValue(fechaDesde);
        }
        seleccionados.clear();
        recargarPaginacion();
    }

    private void inyectarBotonesNavegacion() {
        Node controlBox = pagination.lookup(".control-box");
        if (!(controlBox instanceof HBox box)) return;

        box.getChildren().removeIf(n -> n instanceof Button b && b.getStyleClass().contains("page-nav-btn"));

        estilizarBotonPaginacion(pagination.lookup(".left-arrow-button"),  "fas-angle-left");
        estilizarBotonPaginacion(pagination.lookup(".right-arrow-button"), "fas-angle-right");

        Button btnFirst = crearBotonNavegacion("fas-angle-double-left",  "Ir a la primera pagina",
                e -> pagination.setCurrentPageIndex(0));
        Button btnLast  = crearBotonNavegacion("fas-angle-double-right", "Ir a la ultima pagina",
                e -> pagination.setCurrentPageIndex(pagination.getPageCount() - 1));

        box.getChildren().add(0, btnFirst);
        box.getChildren().add(btnLast);
    }

    private void estilizarBotonPaginacion(Node node, String icono) {
        if (!(node instanceof Button btn)) return;
        FontIcon ico = new FontIcon(icono);
        ico.setIconColor(javafx.scene.paint.Color.web("#94a3b8"));
        ico.setIconSize(13);
        btn.setGraphic(ico);
        btn.setText("");
        btn.getStyleClass().add("page-nav-btn");
    }

    private Button crearBotonNavegacion(String icono, String accessibleText,
                                        javafx.event.EventHandler<javafx.event.ActionEvent> handler) {
        FontIcon ico = new FontIcon(icono);
        ico.setIconColor(javafx.scene.paint.Color.web("#94a3b8"));
        ico.setIconSize(13);
        Button btn = new Button();
        btn.setGraphic(ico);
        btn.getStyleClass().add("page-nav-btn");
        btn.setAccessibleText(accessibleText);
        btn.setOnAction(handler);
        return btn;
    }

    // ─── ESTADISTICAS ─────────────────────────────────────────────────────────

    private void actualizarEstadisticas() {
        lblTotalReservas.setText(reservationService.countActiveFormatted());
        lblTotalIngresos.setText(reservationService.earningsFormatted());
        lblTotalUsuarios.setText(userService.countFormatted());
        lblTotalPistas.setText(courtService.countFormatted());
    }

    // ─── CAMBIO DE TAB ────────────────────────────────────────────────────────

    @FXML
    private void handleTabChange(javafx.event.ActionEvent event) {
        Button clickedButton = (Button) event.getSource();

        List.of(btnTabReservas, btnTabDeportes, btnTabPistas, btnTabUsuarios)
                .forEach(b -> b.getStyleClass().remove("active"));
        clickedButton.getStyleClass().add("active");

        if (columnasPopup != null && columnasPopup.isShowing()) columnasPopup.hide();
        genericTable.getItems().clear();
        genericTable.getColumns().clear();
        genericTable.setVisible(true);
        scrollDeportes.setVisible(false);
        seleccionados.clear();
        actualizarBulkBar();

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

    private Button obtenerBotonActivo() {
        if (btnTabReservas.getStyleClass().contains("active")) return btnTabReservas;
        if (btnTabDeportes.getStyleClass().contains("active")) return btnTabDeportes;
        if (btnTabPistas.getStyleClass().contains("active"))   return btnTabPistas;
        return btnTabUsuarios;
    }

    // ─── ACCION PRINCIPAL (boton +) ───────────────────────────────────────────

    @FXML
    private void handleAdd() {
        Button activo = obtenerBotonActivo();
        if (activo == btnTabReservas) {
            abrirModal("/org/example/sportconnect/reservation-form/reservation-form-view.fxml",
                    "Nueva Reserva", 1050, 750, null);
        } else if (activo == btnTabDeportes) {
            abrirModal("/org/example/sportconnect/forms/sport-form-view.fxml",
                    "Nuevo Deporte", 500, 400, null);
        } else if (activo == btnTabPistas) {
            abrirModal("/org/example/sportconnect/forms/court-form-view.fxml",
                    "Nueva Pista", 500, 400, null);
        } else {
            abrirModal("/org/example/sportconnect/forms/user-form-view.fxml",
                    "Nuevo Usuario", 500, 400, null);
        }
        actualizarEstadisticas();
        handleTabChange(new javafx.event.ActionEvent(activo, null));
    }

    @FXML
    public void handleLogout(javafx.event.ActionEvent event) {
        try {
            Session.getInstance().logOut();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/sportconnect/login/login-view.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    // ─── BULK ACTIONS ─────────────────────────────────────────────────────────

    private void actualizarBulkBar() {
        int n = seleccionados.size();
        bulkBar.setVisible(n > 0);
        bulkBar.setManaged(n > 0);
        lblSeleccionados.setText(n + " seleccionado" + (n == 1 ? "" : "s"));
    }

    private Long getEntityId(Object item) {
        if (item instanceof Reservation r) return r.getId();
        if (item instanceof Court c)       return c.getId();
        if (item instanceof User u)        return u.getId();
        if (item instanceof Sport s)       return s.getId();
        return null;
    }

    private List<Object> obtenerTodos(Button tab) {
        if (tab == btnTabReservas) return new ArrayList<>(reservationService.findAll(filtroActual, fechaDesde, fechaHasta));
        if (tab == btnTabPistas)   return new ArrayList<>(courtService.findAll(filtroActual));
        if (tab == btnTabUsuarios) return new ArrayList<>(userService.findAll(filtroActual));
        return Collections.emptyList();
    }

    private TableColumn<Object, Void> crearColumnaCheckboxConHeader(Predicate<Object> filtroTipo) {
        TableColumn<Object, Void> col = new TableColumn<>();
        col.setMinWidth(36); col.setMaxWidth(36); col.setResizable(false); col.setSortable(false);

        CheckBox cbHeader = new CheckBox();
        cbHeader.setOnAction(e -> {
            seleccionados.clear();
            if (cbHeader.isSelected()) {
                obtenerTodos(obtenerBotonActivo()).forEach(o -> {
                    Long id = getEntityId(o);
                    if (id != null) seleccionados.put(id, o);
                });
            }
            actualizarBulkBar();
            genericTable.refresh();
        });
        col.setGraphic(cbHeader);

        col.setCellFactory(c -> new TableCell<>() {
            private final CheckBox cb = new CheckBox();
            {
                cb.setOnAction(ev -> {
                    Object rowItem = getTableRow().getItem();
                    if (rowItem == null) return;
                    Long id = getEntityId(rowItem);
                    if (id == null) return;
                    if (cb.isSelected()) seleccionados.put(id, rowItem);
                    else                 seleccionados.remove(id);
                    cbHeader.setSelected(seleccionados.size() == totalRegistros);
                    actualizarBulkBar();
                });
            }

            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                Object rowItem = getTableRow().getItem();
                if (empty || rowItem == null || !filtroTipo.test(rowItem)) { setGraphic(null); return; }
                cb.setSelected(seleccionados.containsKey(getEntityId(rowItem)));
                setGraphic(cb);
            }
        });
        return col;
    }

    private void handleBulkEliminar() {
        if (seleccionados.isEmpty()) return;
        int n = seleccionados.size();
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Eliminar seleccionados");
        alert.setHeaderText("Eliminar " + n + " registro" + (n == 1 ? "" : "s") + "?");
        alert.setContentText("Esta accion no se puede deshacer.");
        estilizarAlert(alert);
        alert.showAndWait().ifPresent(res -> {
            if (res != ButtonType.OK) return;
            User currentUser = Session.getInstance().getUser();
            for (Object item : new ArrayList<>(seleccionados.values())) {
                if (item instanceof Court c) {
                    courtService.delete(c.getId());
                } else if (item instanceof User u) {
                    if (currentUser == null || !currentUser.getId().equals(u.getId()))
                        userService.delete(u.getId());
                } else if (item instanceof Sport s) {
                    sportService.delete(s.getId());
                }
            }
            seleccionados.clear();
            actualizarEstadisticas();
            handleTabChange(new javafx.event.ActionEvent(obtenerBotonActivo(), null));
        });
    }

    private void handleBulkExportar() {
        if (seleccionados.isEmpty()) return;
        javafx.stage.FileChooser fc = new javafx.stage.FileChooser();
        fc.setTitle("Guardar CSV");
        fc.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("CSV", "*.csv"));
        fc.setInitialFileName("exportacion.csv");
        File file = fc.showSaveDialog(btnAddAction.getScene().getWindow());
        if (file == null) return;

        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            Object first = seleccionados.values().iterator().next();
            if      (first instanceof Reservation) exportarReservasCSV(pw);
            else if (first instanceof Court)       exportarPistasCSV(pw);
            else if (first instanceof User)        exportarUsuariosCSV(pw);
            else if (first instanceof Sport)       exportarDeportesCSV(pw);

            Alert ok = new Alert(Alert.AlertType.INFORMATION);
            ok.setTitle("Exportacion completada");
            ok.setHeaderText(null);
            ok.setContentText("Se exportaron " + seleccionados.size() + " registros a:\n" + file.getAbsolutePath());
            estilizarAlert(ok);
            ok.showAndWait();
        } catch (IOException ex) { ex.printStackTrace(); }
    }

    private void exportarReservasCSV(PrintWriter pw) {
        pw.println("ID,Usuario,Email,Pista,Fecha,Inicio,Fin,Estado");
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        for (Object item : seleccionados.values()) {
            if (!(item instanceof Reservation r)) continue;
            pw.printf("%d,%s,%s,%s,%s,%s,%s,%s%n",
                    r.getId(),
                    csvEscape(r.getUser().getName() + " " + r.getUser().getLastName()),
                    csvEscape(r.getUser().getEmail()),
                    csvEscape(r.getCourt().getName()),
                    r.getDate().format(fmt),
                    r.getStartHour(), r.getEndHour(),
                    r.isCancelled() ? "Cancelada" : "Activa");
        }
    }

    private void exportarPistasCSV(PrintWriter pw) {
        pw.println("ID,Nombre,Deporte,Precio/Hora");
        for (Object item : seleccionados.values()) {
            if (!(item instanceof Court c)) continue;
            pw.printf("%d,%s,%s,%.2f%n",
                    c.getId(), csvEscape(c.getName()),
                    csvEscape(c.getSport().getName()), c.getPrice());
        }
    }

    private void exportarUsuariosCSV(PrintWriter pw) {
        pw.println("ID,Nombre,Apellidos,Email,Telefono,Rol");
        for (Object item : seleccionados.values()) {
            if (!(item instanceof User u)) continue;
            pw.printf("%d,%s,%s,%s,%s,%s%n",
                    u.getId(), csvEscape(u.getName()), csvEscape(u.getLastName()),
                    csvEscape(u.getEmail()),
                    u.getPhone() != null ? csvEscape(u.getPhone()) : "",
                    u.isAdmin() ? "Admin" : "Cliente");
        }
    }

    private void exportarDeportesCSV(PrintWriter pw) {
        pw.println("ID,Nombre");
        for (Object item : seleccionados.values()) {
            if (!(item instanceof Sport s)) continue;
            pw.printf("%d,%s%n", s.getId(), csvEscape(s.getName()));
        }
    }

    private String csvEscape(String val) {
        if (val == null) return "";
        if (val.contains(",") || val.contains("\"") || val.contains("\n"))
            return "\"" + val.replace("\"", "\"\"") + "\"";
        return val;
    }

    // ─── HELPERS DE COLUMNAS GENERICAS ────────────────────────────────────────

    /** Columna de texto simple a partir de un extractor de valor */
    private TableColumn<Object, String> crearColumnaTexto(String titulo, Function<Object, String> extractor) {
        TableColumn<Object, String> col = new TableColumn<>(titulo);
        col.setCellValueFactory(d -> {
            String val = extractor.apply(d.getValue());
            return val != null ? new SimpleStringProperty(val) : null;
        });
        return col;
    }

    /** Columna ID oculta por defecto */
    private TableColumn<Object, String> crearColumnaIdOculta(Function<Object, Long> extractor) {
        TableColumn<Object, String> col = crearColumnaTexto("ID",
                o -> { Long id = extractor.apply(o); return id != null ? String.valueOf(id) : null; });
        col.setMinWidth(55); col.setMaxWidth(55);
        col.setVisible(false);
        return col;
    }

    // ─── TAB RESERVAS ─────────────────────────────────────────────────────────

    private void cargarTabReservas() {
        configurarBarraHerramientas(false, true, true, true);
        txtBuscar.setPromptText("Buscar por nombre o email");

        genericTable.getColumns().addAll(
                crearColumnaCheckboxConHeader(o -> o instanceof Reservation),
                crearColumnaIdOculta(o -> o instanceof Reservation r ? r.getId() : null),
                crearColumnaUsuarioReserva(),
                crearColumnaTexto("Pista",    o -> o instanceof Reservation r ? r.getCourt().getName() : null),
                crearColumnaFechaReserva(),
                crearColumnaTexto("Horario",  o -> o instanceof Reservation r ? r.getStartHour() + " - " + r.getEndHour() : null),
                crearColumnaEstadoReserva(),
                crearColumnaAccionesReserva()
        );
        configurarPaginacion();
        Platform.runLater(this::inyectarBotonesNavegacion);
    }

    private TableColumn<Object, String> crearColumnaUsuarioReserva() {
        TableColumn<Object, String> col = new TableColumn<>("Usuario");
        col.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || !(getTableRow().getItem() instanceof Reservation r)) { setGraphic(null); return; }
                Label name  = new Label(r.getUser().getName() + " " + r.getUser().getLastName());
                Label email = new Label(r.getUser().getEmail());
                name.getStyleClass().add("table-text-bold");
                email.getStyleClass().add("table-text-sub");
                VBox vbox = new VBox(2, name, email);
                vbox.setAlignment(Pos.CENTER_LEFT);
                setGraphic(vbox); setAlignment(Pos.CENTER_LEFT);
            }
        });
        return col;
    }

    private TableColumn<Object, String> crearColumnaFechaReserva() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM yyyy", new Locale("es", "ES"));
        return crearColumnaTexto("Fecha", o -> o instanceof Reservation r ? r.getDate().format(fmt) : null);
    }

    private TableColumn<Object, String> crearColumnaEstadoReserva() {
        TableColumn<Object, String> col = new TableColumn<>("Estado");
        col.setMinWidth(90); col.setMaxWidth(90);
        col.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || !(getTableRow().getItem() instanceof Reservation r)) { setGraphic(null); return; }
                Label lbl = new Label(r.isCancelled() ? "Cancelada" : "Activa");
                lbl.getStyleClass().add(r.isCancelled() ? "badge-cancelled" : "badge-active");
                lbl.setAccessibleText(r.isCancelled() ? "Reserva cancelada" : "Reserva activa");
                setGraphic(lbl); setAlignment(Pos.CENTER_LEFT);
            }
        });
        return col;
    }

    private TableColumn<Object, Void> crearColumnaAccionesReserva() {
        TableColumn<Object, Void> col = new TableColumn<>("Acciones");
        col.setMinWidth(165); col.setMaxWidth(165);
        col.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || !(getTableRow().getItem() instanceof Reservation r)) { setGraphic(null); return; }

                boolean esPasada = r.getDate().isBefore(LocalDate.now()) ||
                        (r.getDate().isEqual(LocalDate.now()) && r.getEndHour().isBefore(java.time.LocalTime.now()));
                boolean bloquearEdicion = r.isCancelled() || esPasada;

                Button btnEditar = crearBoton("Editar", "fas-pencil-alt", "#2563eb", "btn-edit",
                        "Editar reserva numero " + r.getId());
                btnEditar.setDisable(bloquearEdicion);
                if (bloquearEdicion) btnEditar.setOpacity(0.3);
                btnEditar.setOnAction(e -> abrirEdicionReserva(r));

                boolean cancelada = r.isCancelled();
                Button btnToggle = crearBoton(
                        cancelada ? "Reactivar" : "Cancelar",
                        cancelada ? "fas-redo"  : "fas-times",
                        cancelada ? "#10b981"   : "#ef4444",
                        cancelada ? "btn-reactivate" : "btn-cancel",
                        cancelada ? "Reactivar reserva numero " + r.getId()
                                : "Cancelar reserva numero "  + r.getId());
                btnToggle.setOnAction(e -> { if (cancelada) confirmarYReactivar(r); else confirmarYCancelar(r); });

                HBox box = new HBox(6, btnEditar, btnToggle);
                box.setAlignment(Pos.CENTER_LEFT);
                setGraphic(box); setAlignment(Pos.CENTER_LEFT);
            }
        });
        return col;
    }

    private void abrirEdicionReserva(Reservation reservation) {
        abrirModal(
                "/org/example/sportconnect/reservation-form/reservation-form-view.fxml",
                "Editar Reserva #" + reservation.getId(), 1050, 750,
                (ReservationFormController ctrl) -> ctrl.setReservacionAEditar(reservation));
        refrescarReservas();
    }

    private void confirmarYCancelar(Reservation r) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Cancelar reserva");
        alert.setHeaderText("Cancelar la reserva #" + r.getId() + "?");
        alert.setContentText("La reserva quedara marcada como cancelada.");
        estilizarAlert(alert);
        alert.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) { reservationService.cancel(r.getId()); refrescarReservas(); }
        });
    }

    private void confirmarYReactivar(Reservation r) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Reactivar reserva");
        alert.setHeaderText("Reactivar la reserva #" + r.getId() + "?");
        alert.setContentText("La reserva volvera a estar activa.");
        estilizarAlert(alert);
        alert.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) { reservationService.reactivate(r.getId()); refrescarReservas(); }
        });
    }

    private void refrescarReservas() {
        actualizarEstadisticas();
        genericTable.getItems().clear();
        genericTable.getColumns().clear();
        cargarTabReservas();
    }

    // ─── TAB DEPORTES ─────────────────────────────────────────────────────────

    private void cargarTabDeportes() {
        pagination.setVisible(false);
        tileDeportes.getChildren().clear();
        configurarBarraHerramientas(false, false, false, false);
        sportService.findAll().forEach(s -> tileDeportes.getChildren().add(crearTarjetaDeporte(s)));
    }

    private VBox crearTarjetaDeporte(Sport sport) {
        VBox card = new VBox(8);
        card.getStyleClass().add("sport-card");
        card.setPrefSize(200, 130);

        Label nameLabel = new Label(sport.getName().toUpperCase());
        nameLabel.getStyleClass().add("sport-card-name");
        nameLabel.setAccessibleText("Deporte: " + sport.getName());

        Button btnEditar   = crearBoton("Editar",   "fas-pencil-alt", "#2563eb", "btn-edit",   "Editar deporte "   + sport.getName());
        Button btnEliminar = crearBoton("Eliminar", "fas-trash-alt",  "#ef4444", "btn-delete", "Eliminar deporte " + sport.getName());
        btnEditar.setOnAction(e -> abrirEdicionDeporte(sport));
        btnEliminar.setOnAction(e -> confirmarEliminarDeporte(sport));

        HBox btnBox = new HBox(6, btnEditar, btnEliminar);
        btnBox.setAlignment(Pos.CENTER);
        card.getChildren().addAll(nameLabel, btnBox);
        return card;
    }

    private void abrirEdicionDeporte(Sport sport) {
        abrirModal("/org/example/sportconnect/forms/sport-form-view.fxml",
                "Editar Deporte", 500, 400,
                (SportFormController ctrl) -> ctrl.setSportAEditar(sport));
        handleTabChange(new javafx.event.ActionEvent(btnTabDeportes, null));
    }

    private void confirmarEliminarDeporte(Sport sport) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Eliminar deporte");
        alert.setHeaderText("Eliminar " + sport.getName() + "?");
        alert.setContentText("Se eliminara el deporte y todas sus pistas asociadas.");
        estilizarAlert(alert);
        alert.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                sportService.delete(sport.getId());
                handleTabChange(new javafx.event.ActionEvent(btnTabDeportes, null));
            }
        });
    }

    // ─── TAB PISTAS ───────────────────────────────────────────────────────────

    private void cargarTabPistas() {
        configurarBarraHerramientas(true, true, false, true);
        txtBuscar.setPromptText("Buscar por nombre de pista");

        genericTable.getColumns().addAll(
                crearColumnaCheckboxConHeader(o -> o instanceof Court),
                crearColumnaIdOculta(o -> o instanceof Court c ? c.getId() : null),
                crearColumnaNombrePista(),
                crearColumnaDeportePista(),
                crearColumnaTexto("Precio / Hora", o -> o instanceof Court c ? String.format("%.2f \u20ac", c.getPrice()) : null),
                crearColumnaAccionesPista()
        );
        configurarPaginacion();
        Platform.runLater(this::inyectarBotonesNavegacion);
    }

    private TableColumn<Object, String> crearColumnaNombrePista() {
        TableColumn<Object, String> col = new TableColumn<>("Pista");
        col.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || !(getTableRow().getItem() instanceof Court c)) { setText(null); return; }
                setText(c.getName()); getStyleClass().add("table-text-bold");
            }
        });
        return col;
    }

    private TableColumn<Object, String> crearColumnaDeportePista() {
        TableColumn<Object, String> col = new TableColumn<>("Deporte");
        col.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || !(getTableRow().getItem() instanceof Court c)) { setText(null); return; }
                setText(c.getSport().getName().toUpperCase());
                getStyleClass().add("court-name-cell");
            }
        });
        return col;
    }

    private TableColumn<Object, Void> crearColumnaAccionesPista() {
        TableColumn<Object, Void> col = new TableColumn<>("Acciones");
        col.setMinWidth(165); col.setMaxWidth(165);
        col.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || !(getTableRow().getItem() instanceof Court c)) { setGraphic(null); return; }

                Button btnEditar   = crearBoton("Editar",   "fas-pencil-alt", "#2563eb", "btn-edit",   "Editar pista "   + c.getName());
                Button btnEliminar = crearBoton("Eliminar", "fas-trash-alt",  "#ef4444", "btn-delete", "Eliminar pista " + c.getName());
                btnEditar.setOnAction(e -> abrirEdicionPista(c));
                btnEliminar.setOnAction(e -> confirmarEliminarPista(c));

                HBox box = new HBox(6, btnEditar, btnEliminar);
                box.setAlignment(Pos.CENTER_LEFT);
                setGraphic(box); setAlignment(Pos.CENTER_LEFT);
            }
        });
        return col;
    }

    private void abrirEdicionPista(Court court) {
        abrirModal("/org/example/sportconnect/forms/court-form-view.fxml",
                "Editar Pista", 500, 400,
                (CourtFormController ctrl) -> ctrl.setPistaAEditar(court));
        handleTabChange(new javafx.event.ActionEvent(btnTabPistas, null));
    }

    private void confirmarEliminarPista(Court court) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Eliminar pista");
        alert.setHeaderText("Eliminar " + court.getName() + "?");
        alert.setContentText("Se eliminara la pista y todas sus reservas asociadas.");
        estilizarAlert(alert);
        alert.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                courtService.delete(court.getId());
                actualizarEstadisticas();
                handleTabChange(new javafx.event.ActionEvent(btnTabPistas, null));
            }
        });
    }

    // ─── TAB USUARIOS ─────────────────────────────────────────────────────────

    private void cargarTabUsuarios() {
        configurarBarraHerramientas(true, true, false, true);
        txtBuscar.setPromptText("Buscar por nombre, apellido, email o telefono");

        genericTable.getColumns().addAll(
                crearColumnaCheckboxConHeader(o -> o instanceof User),
                crearColumnaIdOculta(o -> o instanceof User u ? u.getId() : null),
                crearColumnaNombreUsuario(),
                crearColumnaTexto("Apellidos",  o -> o instanceof User u ? u.getLastName() : null),
                crearColumnaEmailUsuario(),
                crearColumnaTexto("Telefono",   o -> o instanceof User u ? (u.getPhone() != null ? u.getPhone() : "N/A") : null),
                crearColumnaRolUsuario(),
                crearColumnaAccionesUsuario()
        );
        configurarPaginacion();
        Platform.runLater(this::inyectarBotonesNavegacion);
    }

    private TableColumn<Object, String> crearColumnaNombreUsuario() {
        TableColumn<Object, String> col = new TableColumn<>("Nombre");
        col.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || !(getTableRow().getItem() instanceof User u)) { setText(null); return; }
                setText(u.getName()); getStyleClass().add("table-text-bold");
            }
        });
        return col;
    }

    private TableColumn<Object, String> crearColumnaEmailUsuario() {
        TableColumn<Object, String> col = new TableColumn<>("Email");
        col.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || !(getTableRow().getItem() instanceof User u)) { setText(null); return; }
                setText(u.getEmail()); getStyleClass().add("table-text-sub");
            }
        });
        return col;
    }

    private TableColumn<Object, String> crearColumnaRolUsuario() {
        TableColumn<Object, String> col = new TableColumn<>("Rol");
        col.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || !(getTableRow().getItem() instanceof User u)) { setGraphic(null); return; }
                Label lbl = new Label(u.isAdmin() ? "ADMIN" : "CLIENTE");
                lbl.getStyleClass().add(u.isAdmin() ? "badge-admin" : "badge-client");
                lbl.setAccessibleText(u.isAdmin() ? "Rol administrador" : "Rol cliente");
                setGraphic(lbl);
            }
        });
        return col;
    }

    private TableColumn<Object, Void> crearColumnaAccionesUsuario() {
        TableColumn<Object, Void> col = new TableColumn<>("Acciones");
        col.setMinWidth(165); col.setMaxWidth(165);
        col.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || !(getTableRow().getItem() instanceof User u)) { setGraphic(null); return; }

                Button btnEditar   = crearBoton("Editar",   "fas-pencil-alt", "#2563eb", "btn-edit",   "Editar usuario "   + u.getName() + " " + u.getLastName());
                Button btnEliminar = crearBoton("Eliminar", "fas-trash-alt",  "#ef4444", "btn-delete", "Eliminar usuario " + u.getName() + " " + u.getLastName());

                User currentUser = Session.getInstance().getUser();
                if (currentUser != null && currentUser.getId().equals(u.getId())) {
                    btnEliminar.setDisable(true);
                    btnEliminar.setOpacity(0.3);
                    btnEliminar.setAccessibleText("No puedes eliminar tu propia cuenta");
                }
                btnEditar.setOnAction(e -> abrirEdicionUsuario(u));
                btnEliminar.setOnAction(e -> confirmarEliminarUsuario(u));

                HBox box = new HBox(6, btnEditar, btnEliminar);
                box.setAlignment(Pos.CENTER_LEFT);
                setGraphic(box); setAlignment(Pos.CENTER_LEFT);
            }
        });
        return col;
    }

    private void abrirEdicionUsuario(User user) {
        abrirModal("/org/example/sportconnect/forms/user-form-view.fxml",
                "Editar Usuario", 500, 400,
                (UserFormController ctrl) -> ctrl.setUsuarioAEditar(user));
        handleTabChange(new javafx.event.ActionEvent(btnTabUsuarios, null));
    }

    private void confirmarEliminarUsuario(User user) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Eliminar usuario");
        alert.setHeaderText("Eliminar a " + user.getName() + " " + user.getLastName() + "?");
        alert.setContentText("Esta accion no se puede deshacer.");
        estilizarAlert(alert);
        alert.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                userService.delete(user.getId());
                actualizarEstadisticas();
                handleTabChange(new javafx.event.ActionEvent(btnTabUsuarios, null));
            }
        });
    }
}