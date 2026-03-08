package org.example.sportconnect.services;

import org.example.sportconnect.daos.UserDAO;
import org.example.sportconnect.models.User;
import org.mindrot.jbcrypt.BCrypt;
import java.util.List;

public class UserService {

    private final UserDAO userDAO = new UserDAO();

    public User login(String email, String plainPassword) {
        User user = userDAO.findByEmail(email);
        if (user != null && BCrypt.checkpw(plainPassword, user.getPassword())) return user;
        return null;
    }

    /** Página de usuarios con filtro opcional — consulta paginada en BD */
    public List<User> findPage(int offset, int limit, String filtro) {
        return userDAO.findPage(offset, limit, filtro);
    }

    /** Total de usuarios que coinciden con el filtro (para calcular páginas) */
    public long count(String filtro) { return userDAO.count(filtro); }

    /** Todos los registros con filtro — para seleccionar todo entre páginas */
    public List<User> findAll(String filtro) { return userDAO.findAll(filtro); }

    /** findAll completo — sólo para exportación CSV */
    public List<User> findAll() { return userDAO.findAll(); }

    public String countFormatted() { return String.valueOf(userDAO.count()); }

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

    public boolean update(User user, String newPassword) {
        if (newPassword != null && !newPassword.isBlank())
            user.setPassword(BCrypt.hashpw(newPassword, BCrypt.gensalt()));
        return userDAO.update(user);
    }

    public boolean delete(Long id) { return userDAO.delete(id); }
}