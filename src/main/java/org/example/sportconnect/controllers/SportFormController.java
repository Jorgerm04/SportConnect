package org.example.sportconnect.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.sportconnect.models.Sport;
import org.example.sportconnect.services.SportService;

public class SportFormController {

    @FXML private TextField txtNombre;
    @FXML private Label lblError;
    @FXML private Button btnGuardar;
    @FXML private Label lblTitulo;

    private final SportService sportService = new SportService();
    private Sport deporteAEditar = null;

    public void setSportAEditar(Sport sport) {
        this.deporteAEditar = sport;
        txtNombre.setText(sport.getName());
        lblTitulo.setText("Editar Deporte");
        btnGuardar.setText("Guardar Cambios");
    }

    @FXML
    private void handleGuardar() {
        String nombre = txtNombre.getText().trim();
        if (nombre.isEmpty()) { mostrarError("El nombre no puede estar vacío."); return; }

        boolean ok;
        if (deporteAEditar != null) {
            deporteAEditar.setName(nombre);
            ok = sportService.update(deporteAEditar);
        } else {
            ok = sportService.save(nombre);
        }

        if (ok) cerrar();
        else mostrarError("Error al guardar el deporte.");
    }

    private void mostrarError(String msg) { lblError.setText(msg); lblError.setVisible(true); }
    @FXML private void handleCancelar() { cerrar(); }
    private void cerrar() { ((Stage) btnGuardar.getScene().getWindow()).close(); }
}