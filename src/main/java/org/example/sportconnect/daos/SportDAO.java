package org.example.sportconnect.daos;

import org.example.sportconnect.models.Sport;
import org.example.sportconnect.utils.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import java.util.Collections;
import java.util.List;

public class SportDAO extends GenericDAO<Sport> {

    public SportDAO() {
        super(Sport.class);
    }

    public List<Sport> getAllSports() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Sport", Sport.class).list();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public Sport getSportById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Sport.class, id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}