package org.example.sportconnect.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.sportconnect.models.Court;
import org.example.sportconnect.models.Sport;
import org.example.sportconnect.services.CourtService;
import org.example.sportconnect.services.SportService;

public class CourtFormController {

    @FXML private TextField txtNombre;
    @FXML private TextField txtPrecio;
    @FXML private ComboBox<Sport> comboDeporte;
    @FXML private Label lblError;
    @FXML private Button btnGuardar;
    @FXML private Label lblTitulo;

    private final CourtService courtService = new CourtService();
    private final SportService sportService = new SportService();
    private Court pistaAEditar = null;

    @FXML
    public void initialize() {
        comboDeporte.setItems(FXCollections.observableArrayList(sportService.findAllSports()));
        comboDeporte.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Sport item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getName());
            }
        });
        comboDeporte.setButtonCell(comboDeporte.getCellFactory().call(null));
    }

    public void setPistaAEditar(Court court) {
        this.pistaAEditar = court;
        txtNombre.setText(court.getName());
        txtPrecio.setText(String.valueOf(court.getPrice()));
        // Preseleccionar el deporte
        comboDeporte.getItems().stream()
                .filter(s -> s.getId().equals(court.getSport().getId()))
                .findFirst().ifPresent(comboDeporte::setValue);
        lblTitulo.setText("Editar Pista");
        btnGuardar.setText("Guardar Cambios");
    }

    @FXML
    private void handleGuardar() {
        String nombre = txtNombre.getText().trim();
        Sport deporte = comboDeporte.getValue();
        String precioTxt = txtPrecio.getText().trim();

        if (nombre.isEmpty() || deporte == null || precioTxt.isEmpty()) {
            mostrarError("Todos los campos son obligatorios."); return;
        }
        double precio;
        try { precio = Double.parseDouble(precioTxt.replace(",", ".")); }
        catch (NumberFormatException e) { mostrarError("El precio debe ser un número válido."); return; }

        boolean ok;
        if (pistaAEditar != null) {
            pistaAEditar.setName(nombre);
            pistaAEditar.setSport(deporte);
            pistaAEditar.setPrice(precio);
            ok = courtService.updateCourt(pistaAEditar);
        } else {
            ok = courtService.saveCourt(nombre, deporte, precio);
        }

        if (ok) cerrar();
        else mostrarError("Error al guardar la pista.");
    }

    private void mostrarError(String msg) { lblError.setText(msg); lblError.setVisible(true); }
    @FXML private void handleCancelar() { cerrar(); }
    private void cerrar() { ((Stage) btnGuardar.getScene().getWindow()).close(); }
}