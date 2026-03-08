package org.example.sportconnect.daos;

import org.example.sportconnect.utils.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import java.util.Collections;
import java.util.List;

/** DAO base con operaciones CRUD comunes para todas las entidades */
public class GenericDAO<T> {

    private final Class<T> type;

    public GenericDAO(Class<T> type) { this.type = type; }

    public boolean save(T entity) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.persist(entity);
            tx.commit();
            return true;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
            return false;
        }
    }

    public boolean update(T entity) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.merge(entity);
            tx.commit();
            return true;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(Long id) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            T entity = session.get(type, id);
            if (entity != null) session.remove(entity);
            tx.commit();
            return true;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
            return false;
        }
    }

    public T findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(type, id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<T> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM " + type.getSimpleName(), type).list();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    /**
     * Devuelve una página con LIMIT/OFFSET en BD.
     * Las subclases deben sobrescribir para añadir JOIN FETCH u ORDER BY propios.
     */
    public List<T> findPage(int offset, int limit) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM " + type.getSimpleName(), type)
                    .setFirstResult(offset)
                    .setMaxResults(limit)
                    .list();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    /**
     * Devuelve una página filtrada por texto. Por defecto ignora el filtro.
     * Las subclases deben sobrescribir para implementar búsqueda real en BD.
     */
    public List<T> findPage(int offset, int limit, String filtro) {
        return findPage(offset, limit);
    }

    public long count() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "SELECT count(e) FROM " + type.getSimpleName() + " e", Long.class
            ).getSingleResult();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Cuenta los registros que coinciden con el filtro.
     * Por defecto ignora el filtro. Las subclases deben sobrescribir.
     */
    public long count(String filtro) {
        return count();
    }
}
