package org.example.sportconnect.daos;

import org.example.sportconnect.models.Court;
import org.example.sportconnect.utils.HibernateUtil;
import org.hibernate.Session;
import java.util.Collections;
import java.util.List;

public class CourtDAO extends GenericDAO<Court> {

    public CourtDAO() { super(Court.class); }

    /** Cuenta pistas (con filtro opcional por nombre) */
    @Override
    public long count(String filtro) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            if (filtro == null || filtro.isBlank()) {
                return session.createQuery(
                        "SELECT count(c) FROM Court c", Long.class
                ).getSingleResult();
            }
            return session.createQuery(
                    "SELECT count(c) FROM Court c WHERE lower(c.name) LIKE :q", Long.class
            ).setParameter("q", "%" + filtro.toLowerCase() + "%").getSingleResult();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public long count() { return count(null); }

    /** Página de pistas con JOIN FETCH del deporte, filtro opcional por nombre */
    @Override
    public List<Court> findPage(int offset, int limit, String filtro) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            if (filtro == null || filtro.isBlank()) {
                return session.createQuery(
                        "SELECT c FROM Court c JOIN FETCH c.sport ORDER BY c.name", Court.class
                ).setFirstResult(offset).setMaxResults(limit).list();
            }
            return session.createQuery(
                            "SELECT c FROM Court c JOIN FETCH c.sport " +
                                    "WHERE lower(c.name) LIKE :q ORDER BY c.name", Court.class
                    ).setParameter("q", "%" + filtro.toLowerCase() + "%")
                    .setFirstResult(offset).setMaxResults(limit).list();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    @Override
    public List<Court> findPage(int offset, int limit) {
        return findPage(offset, limit, null);
    }

    /** Todos los registros que coinciden con el filtro — para seleccionar todo entre páginas */
    public List<Court> findAll(String filtro) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            if (filtro == null || filtro.isBlank()) return findAll();
            return session.createQuery(
                            "SELECT c FROM Court c JOIN FETCH c.sport " +
                                    "WHERE lower(c.name) LIKE :q ORDER BY c.name", Court.class)
                    .setParameter("q", "%" + filtro.toLowerCase() + "%").list();
        } catch (Exception e) { e.printStackTrace(); return Collections.emptyList(); }
    }

    /** findAll con JOIN FETCH — usado sólo para exportación CSV y selectores de formulario */
    @Override
    public List<Court> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "SELECT c FROM Court c JOIN FETCH c.sport", Court.class).list();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}