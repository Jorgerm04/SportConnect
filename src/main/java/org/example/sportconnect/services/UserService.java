package org.example.sportconnect.services;

import org.example.sportconnect.daos.UserDAO;
import org.example.sportconnect.models.User;
import org.mindrot.jbcrypt.BCrypt;
import java.util.List;

public class UserService {

    private final UserDAO userDAO = new UserDAO();

    /** Valida credenciales y devuelve el usuario, o null si son incorrectas */
    public User login(String email, String plainPassword) {
        User user = userDAO.findByEmail(email);
        if (user != null && BCrypt.checkpw(plainPassword, user.getPassword())) return user;
        return null;
    }

    /** Devuelve todos los usuarios */
    public List<User> findAll() { return userDAO.findAll(); }

    /** Total de usuarios formateado para el dashboard */
    public String countFormatted() { return String.valueOf(userDAO.count()); }

    /** Crea un usuario nuevo. Devuelve false si el email ya existe */
    public boolean save(String name, String lastName, String email, String phone, String plainPassword, boolean isAdmin) {
        if (userDAO.findByEmail(email) != null) return false;
        User user = new User();
        user.setName(name);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setPhone(phone);
        user.setPassword(BCrypt.hashpw(plainPassword, BCrypt.gensalt()));
        user.setAdmin(isAdmin);
        return userDAO.save(user);
    }

    /** Actualiza datos del usuario. Si newPassword no es null, la actualiza hasheada */
    public boolean update(User user, String newPassword) {
        if (newPassword != null && !newPassword.isBlank())
            user.setPassword(BCrypt.hashpw(newPassword, BCrypt.gensalt()));
        return userDAO.update(user);
    }

    /** Elimina un usuario por id */
    public boolean delete(Long id) { return userDAO.delete(id); }
}
