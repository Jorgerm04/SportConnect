package org.example.sportconnect.daos;

import org.example.sportconnect.models.Sport;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import java.util.List;

public class SportDAO {
    private final SessionFactory factory;

    public SportDAO() {
        this.factory = new Configuration().configure().buildSessionFactory();
    }

    public List<Sport> getAllSports() {
        try (Session session = factory.openSession()) {
            return session.createQuery("FROM Sport", Sport.class).list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Sport getSportById(Long id) {
        try (Session session = factory.openSession()) {
            return session.get(Sport.class, id);
        }
    }
}