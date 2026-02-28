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

    public String getTotalUsersCount() { return String.valueOf(userDAO.countTotalUsers()); }
    public List<User> getAllUsers() { return userDAO.getAllUsers(); }

    public boolean saveUser(String name, String lastName, String email, String phone, String plainPassword, boolean isAdmin) {
        if (userDAO.findByEmail(email) != null) return false;
        User user = new User();
        user.setName(name); user.setLastName(lastName); user.setEmail(email);
        user.setPhone(phone); user.setPassword(BCrypt.hashpw(plainPassword, BCrypt.gensalt()));
        user.setAdmin(isAdmin);
        userDAO.save(user);
        return true;
    }

    public boolean updateUser(User user, String newPassword) {
        if (newPassword != null && !newPassword.isBlank()) {
            user.setPassword(BCrypt.hashpw(newPassword, BCrypt.gensalt()));
        }
        return userDAO.update(user);
    }

    public boolean deleteUser(Long id) { return userDAO.delete(id); }
}