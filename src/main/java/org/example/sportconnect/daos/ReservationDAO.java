package org.example.sportconnect.daos;

import org.example.sportconnect.models.Court;
import org.example.sportconnect.models.Reservation;
import org.example.sportconnect.utils.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

public class ReservationDAO extends GenericDAO<Reservation> {

    public ReservationDAO() { super(Reservation.class); }

    /** Cuenta reservas no canceladas */
    public long countActive() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "SELECT count(r) FROM Reservation r WHERE r.cancelled = false", Long.class
            ).getSingleResult();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /** Devuelve todas las reservas con usuario y pista cargados, ordenadas por fecha desc */
    @Override
    public List<Reservation> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "SELECT r FROM Reservation r JOIN FETCH r.user JOIN FETCH r.court " +
                            "ORDER BY r.date DESC, r.startHour DESC", Reservation.class
            ).list();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    /** Suma los ingresos de reservas activas */
    public double sumEarnings() {
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

    /** Reservas activas de una pista en una fecha */
    public List<Reservation> findByCourtAndDate(Court court, LocalDate date) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "FROM Reservation r WHERE r.court.id = :courtId AND r.date = :date AND r.cancelled = false",
                            Reservation.class)
                    .setParameter("courtId", court.getId())
                    .setParameter("date", date)
                    .list();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    /** Igual que findByCourtAndDate pero excluyendo una reserva (para modo edición) */
    public List<Reservation> findByCourtAndDateExcluding(Court court, LocalDate date, Long excludeId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "FROM Reservation r WHERE r.court.id = :courtId AND r.date = :date " +
                                    "AND r.cancelled = false AND r.id != :excludeId", Reservation.class)
                    .setParameter("courtId", court.getId())
                    .setParameter("date", date)
                    .setParameter("excludeId", excludeId)
                    .list();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    /** Cancela una reserva */
    public boolean cancel(Long id) { return setCancelled(id, true); }

    /** Reactiva una reserva cancelada */
    public boolean reactivate(Long id) { return setCancelled(id, false); }

    /** Cambia el flag cancelled de una reserva */
    private boolean setCancelled(Long id, boolean cancelled) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            Reservation r = session.get(Reservation.class, id);
            if (r != null) { r.setCancelled(cancelled); session.merge(r); }
            tx.commit();
            return true;
        } catch (Exception e) {
            if (tx != null && tx.getStatus().canRollback()) tx.rollback();
            e.printStackTrace();
            return false;
        }
    }
}
