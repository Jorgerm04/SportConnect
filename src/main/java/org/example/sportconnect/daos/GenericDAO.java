package org.example.sportconnect.daos;

import org.example.sportconnect.utils.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import java.util.List;

public class GenericDAO<T> {
    private final Class<T> type;

    public GenericDAO(Class<T> type) { this.type = type; }

    public void save(T entity) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.persist(entity);
            tx.commit();
        } catch (Exception e) { if (tx != null) tx.rollback(); }
    }
    // Añadir aquí métodos findAll, update y delete según necesites
}
