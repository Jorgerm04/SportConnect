package org.example.sportconnect.services;

import org.example.sportconnect.daos.UserDAO;
import org.example.sportconnect.models.User;
import org.mindrot.jbcrypt.BCrypt;

public class UserService {
    private final UserDAO userDAO;

    public UserService() {
        this.userDAO = new UserDAO();
    }

    /**
     * Lógica de inicio de sesión
     * @return El objeto User si es correcto, null si falla
     */
    public User login(String email, String plainPassword) {
        // 1. Buscamos al usuario por email usando el DAO
        User user = userDAO.findByEmail(email);

        // 2. Si existe, comprobamos la contraseña encriptada
        if (user != null) {
            if (BCrypt.checkpw(plainPassword, user.getPassword())) {
                return user; // Login exitoso
            }
        }
        return null; // Credenciales incorrectas
    }

    public String getTotalUsersCount() {
        // Llama al DAO de usuario para obtener el count
        long count = userDAO.countTotalUsers();
        return String.valueOf(count);
    }

    public java.util.List<User> getAllUsers() {
        return userDAO.getAllUsers();
    }
}