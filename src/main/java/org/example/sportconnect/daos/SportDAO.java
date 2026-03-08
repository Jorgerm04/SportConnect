package org.example.sportconnect.daos;

import org.example.sportconnect.models.Sport;
import org.example.sportconnect.utils.HibernateUtil;
import org.hibernate.Session;
import java.util.Collections;
import java.util.List;

public class SportDAO extends GenericDAO<Sport> {

    public SportDAO() { super(Sport.class); }

    /** findAll ordenado por nombre — usado en selectores de formulario */
    @Override
    public List<Sport> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Sport s ORDER BY s.name", Sport.class).list();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}
