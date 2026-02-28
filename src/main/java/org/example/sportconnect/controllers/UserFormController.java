package org.example.sportconnect.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.example.sportconnect.components.ToggleSwitch;
import org.example.sportconnect.models.User;
import org.example.sportconnect.services.UserService;

public class UserFormController {

    @FXML private TextField txtNombre;
    @FXML private TextField txtApellidos;
    @FXML private TextField txtEmail;
    @FXML private TextField txtTelefono;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblPasswordHint;
    @FXML private Label lblError;
    @FXML private Button btnGuardar;
    @FXML private Label lblTitulo;
    @FXML private HBox toggleContainer;  // contenedor del switch en el FXML

    private final ToggleSwitch toggleAdmin = new ToggleSwitch();
    private final UserService userService = new UserService();
    private User usuarioAEditar = null;

    @FXML
    public void initialize() {
        // Inyectamos el toggle en el contenedor definido en el FXML
        if (toggleContainer != null) {
            toggleContainer.getChildren().add(toggleAdmin);
        }
    }

    public void setUsuarioAEditar(User user) {
        this.usuarioAEditar = user;
        txtNombre.setText(user.getName());
        txtApellidos.setText(user.getLastName());
        txtEmail.setText(user.getEmail());
        txtTelefono.setText(user.getPhone() != null ? user.getPhone() : "");
        toggleAdmin.setSelected(user.isAdmin());
        txtEmail.setDisable(true);
        if (lblPasswordHint != null) lblPasswordHint.setVisible(true);
        if (lblTitulo != null) lblTitulo.setText("Editar Usuario");
        btnGuardar.setText("Guardar Cambios");
    }

    @FXML
    private void handleGuardar() {
        String nombre    = txtNombre.getText().trim();
        String apellidos = txtApellidos.getText().trim();
        String email     = txtEmail.getText().trim();
        String telefono  = txtTelefono.getText().trim();
        String password  = txtPassword.getText();

        if (nombre.isEmpty() || apellidos.isEmpty()) {
            mostrarError("Nombre y apellidos son obligatorios."); return;
        }

        if (usuarioAEditar != null) {
            usuarioAEditar.setName(nombre);
            usuarioAEditar.setLastName(apellidos);
            usuarioAEditar.setPhone(telefono.isEmpty() ? null : telefono);
            usuarioAEditar.setAdmin(toggleAdmin.isSelected());
            boolean ok = userService.updateUser(usuarioAEditar, password.isEmpty() ? null : password);
            if (ok) cerrar(); else mostrarError("Error al actualizar el usuario.");
        } else {
            if (email.isEmpty() || password.isEmpty()) {
                mostrarError("Email y contraseña son obligatorios."); return;
            }
            if (!email.contains("@")) { mostrarError("Email no válido."); return; }
            if (password.length() < 4) { mostrarError("Contraseña mínimo 4 caracteres."); return; }
            boolean ok = userService.saveUser(nombre, apellidos, email,
                    telefono.isEmpty() ? null : telefono, password, toggleAdmin.isSelected());
            if (ok) cerrar(); else mostrarError("Ya existe un usuario con ese email.");
        }
    }

    private void mostrarError(String msg) { lblError.setText(msg); lblError.setVisible(true); }
    @FXML private void handleCancelar() { cerrar(); }
    private void cerrar() { ((Stage) btnGuardar.getScene().getWindow()).close(); }
}