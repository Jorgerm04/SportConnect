package org.example.sportconnect.daos;

import org.example.sportconnect.models.User;
import org.example.sportconnect.utils.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.query.Query;

public class UserDAO extends GenericDAO<User> {

    public UserDAO() {
        super(User.class);
    }

    public User findByEmail(String email) {
        // Usamos HibernateUtil en lugar de una variable 'factory' local
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<User> query = session.createQuery("FROM User WHERE email = :email", User.class);
            query.setParameter("email", email);
            return query.uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public long countTotalUsers() {
        // Corregido: Usamos HibernateUtil para abrir la sesión
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT count(u) FROM User u";
            return session.createQuery(hql, Long.class).getSingleResult();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public java.util.List<User> getAllUsers() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM User", User.class).list();
        } catch (Exception e) {
            e.printStackTrace();
            return java.util.List.of();
        }
    }
}