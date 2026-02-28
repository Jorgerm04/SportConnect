package org.example.sportconnect.daos;

import org.example.sportconnect.models.Court;
import org.example.sportconnect.models.Reservation;
import org.example.sportconnect.utils.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

public class ReservationDAO extends GenericDAO<Reservation> {


    public ReservationDAO() {
        super(Reservation.class);
    }

    public long countActiveReservations() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "SELECT count(r) FROM Reservation r WHERE r.cancelled = false", Long.class
            ).getSingleResult();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public List<Reservation> getAllReservations() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "SELECT r FROM Reservation r JOIN FETCH r.user JOIN FETCH r.court ORDER BY r.date DESC, r.startHour DESC",
                    Reservation.class
            ).list();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public Reservation findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Reservation.class, id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Double getTotalEarnings() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Double result = session.createQuery(
                    "SELECT sum(r.court.price) FROM Reservation r WHERE r.cancelled = false", Double.class
            ).getSingleResult();
            return result != null ? result : 0.0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0.0;
        }
    }

    public List<Reservation> findByCourtAndDate(Court court, LocalDate date) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Reservation> query = session.createQuery(
                    "FROM Reservation r WHERE r.court.id = :courtId AND r.date = :date AND r.cancelled = false",
                    Reservation.class
            );
            query.setParameter("courtId", court.getId());
            query.setParameter("date", date);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    // Busca reservas ocupadas excluyendo una reserva concreta (para edición)
    public List<Reservation> findByCourtAndDateExcluding(Court court, LocalDate date, Long excludeId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Reservation> query = session.createQuery(
                    "FROM Reservation r WHERE r.court.id = :courtId AND r.date = :date AND r.cancelled = false AND r.id != :excludeId",
                    Reservation.class
            );
            query.setParameter("courtId", court.getId());
            query.setParameter("date", date);
            query.setParameter("excludeId", excludeId);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public boolean cancel(Long id) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            Reservation r = session.get(Reservation.class, id);
            if (r != null) {
                r.setCancelled(true);
                session.merge(r);
            }
            tx.commit();
            return true;
        } catch (Exception e) {
            if (tx != null && tx.getStatus().canRollback()) tx.rollback();
            e.printStackTrace();
            return false;
        }
    }

    public boolean reactivate(Long id) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            Reservation r = session.get(Reservation.class, id);
            if (r != null) {
                r.setCancelled(false);
                session.merge(r);
            }
            tx.commit();
            return true;
        } catch (Exception e) {
            if (tx != null && tx.getStatus().canRollback()) tx.rollback();
            e.printStackTrace();
            return false;
        }
    }
}