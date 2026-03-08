package org.example.sportconnect.daos;

import org.example.sportconnect.models.User;
import org.example.sportconnect.utils.HibernateUtil;
import org.hibernate.Session;
import java.util.Collections;
import java.util.List;

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

    /** Cuenta usuarios (con filtro opcional por nombre, apellido, email o teléfono) */
    @Override
    public long count(String filtro) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            if (filtro == null || filtro.isBlank()) {
                return session.createQuery(
                        "SELECT count(u) FROM User u", Long.class
                ).getSingleResult();
            }
            String q = "%" + filtro.toLowerCase() + "%";
            return session.createQuery(
                    "SELECT count(u) FROM User u WHERE lower(u.name) LIKE :q " +
                            "OR lower(u.last_name) LIKE :q OR lower(u.email) LIKE :q " +
                            "OR lower(u.phone) LIKE :q", Long.class
            ).setParameter("q", q).getSingleResult();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public long count() { return count(null); }

    /** Página de usuarios con filtro opcional */
    @Override
    public List<User> findPage(int offset, int limit, String filtro) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            if (filtro == null || filtro.isBlank()) {
                return session.createQuery(
                        "FROM User u ORDER BY u.last_name, u.name", User.class
                ).setFirstResult(offset).setMaxResults(limit).list();
            }
            String q = "%" + filtro.toLowerCase() + "%";
            return session.createQuery(
                    "FROM User u WHERE lower(u.name) LIKE :q OR lower(u.last_name) LIKE :q " +
                            "OR lower(u.email) LIKE :q OR lower(u.phone) LIKE :q " +
                            "ORDER BY u.last_name, u.name", User.class
            ).setParameter("q", q).setFirstResult(offset).setMaxResults(limit).list();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    /** Todos los registros que coinciden con el filtro — para seleccionar todo entre páginas */
    public List<User> findAll(String filtro) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            if (filtro == null || filtro.isBlank()) return findAll();
            String q = "%" + filtro.toLowerCase() + "%";
            return session.createQuery(
                            "FROM User u WHERE lower(u.name) LIKE :q OR lower(u.last_name) LIKE :q " +
                                    "OR lower(u.email) LIKE :q OR lower(u.phone) LIKE :q " +
                                    "ORDER BY u.last_name, u.name", User.class)
                    .setParameter("q", q).list();
        } catch (Exception e) { e.printStackTrace(); return Collections.emptyList(); }
    }

    @Override
    public List<User> findPage(int offset, int limit) {
        return findPage(offset, limit, null);
    }
}