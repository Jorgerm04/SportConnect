package org.example.sportconnect.daos;

import org.example.sportconnect.models.Court;
import org.example.sportconnect.utils.HibernateUtil;
import org.hibernate.Session;
import java.util.Collections;
import java.util.List;

public class CourtDAO extends GenericDAO<Court> {

    public CourtDAO() { super(Court.class); }

    /** Cuenta el total de pistas */
    public long count() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("SELECT count(c) FROM Court c", Long.class).getSingleResult();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /** Devuelve todas las pistas con el deporte ya cargado (evita lazy loading) */
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
