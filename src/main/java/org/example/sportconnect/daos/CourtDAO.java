package org.example.sportconnect.daos;

import org.example.sportconnect.models.Court;
import org.example.sportconnect.models.User;
import org.example.sportconnect.utils.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import java.util.Collections;
import java.util.List;

public class CourtDAO extends GenericDAO<Court> {

    public CourtDAO() {
        super(Court.class);
    }

    public long countTotalCourts() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("SELECT count(c) FROM Court c", Long.class).getSingleResult();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public List<Court> getAllCourts() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("SELECT c FROM Court c JOIN FETCH c.sport", Court.class).list();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public Court findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Court.class, id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}