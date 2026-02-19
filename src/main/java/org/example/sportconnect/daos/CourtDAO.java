package org.example.sportconnect.daos;

import org.example.sportconnect.models.Court;
import org.example.sportconnect.utils.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import java.util.List;

public class CourtDAO {
    private final SessionFactory factory;

    public CourtDAO() {
        this.factory = new Configuration().configure().buildSessionFactory();
    }

    public long countTotalCourts() {
        try (Session session = factory.openSession()) {
            return session.createQuery("SELECT count(c) FROM Court c", Long.class).getSingleResult();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public List<Court> getAllCourts() {
        // Usamos HibernateUtil para ser consistentes con tus otros DAOs
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // El JOIN FETCH trae el deporte asociado de una vez para evitar errores en la tabla
            return session.createQuery("SELECT c FROM Court c JOIN FETCH c.sport", Court.class).list();
        } catch (Exception e) {
            e.printStackTrace();
            // Es mejor devolver una lista vacía que null para evitar NullPointerException en la UI
            return java.util.Collections.emptyList();
        }
    }
}
