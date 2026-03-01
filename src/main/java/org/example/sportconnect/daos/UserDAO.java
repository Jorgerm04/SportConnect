package org.example.sportconnect.daos;

import org.example.sportconnect.models.User;
import org.example.sportconnect.utils.HibernateUtil;
import org.hibernate.Session;

public class UserDAO extends GenericDAO<User> {

    public UserDAO() { super(User.class); }

    /** Busca un usuario por email (login y validación de duplicados) */
    public User findByEmail(String email) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM User WHERE email = :email", User.class)
                    .setParameter("email", email)
                    .uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /** Cuenta el total de usuarios */
    public long count() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("SELECT count(u) FROM User u", Long.class).getSingleResult();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
}
