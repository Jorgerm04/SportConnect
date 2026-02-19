package org.example.sportconnect.daos;

import org.example.sportconnect.models.Reservation;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;

import java.util.List;

public class ReservationDAO {

    private final SessionFactory factory;

    public ReservationDAO() {
        // Inicializa la SessionFactory (Asegúrate de tener tu hibernate.cfg.xml configurado)
        this.factory = new Configuration().configure().buildSessionFactory();
    }

    /**
     * Obtiene el total de reservas que NO han sido canceladas.
     * Útil para el Label lblTotalReservas del Dashboard.
     */
    public long countActiveReservations() {
        try (Session session = factory.openSession()) {
            String hql = "SELECT count(r) FROM Reservation r WHERE r.cancelled = false";
            Query<Long> query = session.createQuery(hql, Long.class);
            return query.getSingleResult();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Obtiene todas las reservas para llenar la TableView.
     */
    public List<Reservation> getAllReservations() {
        try (Session session = factory.openSession()) {
            return session.createQuery("from Reservation", Reservation.class).list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Ejemplo de suma de ingresos (asumiendo que Court tiene un precio)
     * Si no tienes campo precio, este es un ejemplo de cómo se haría.
     */
    public Double getTotalEarnings() {
        try (Session session = factory.openSession()) {
            // HQL uniendo con la tabla Court para sumar precios
            String hql = "SELECT sum(r.court.price) FROM Reservation r WHERE r.cancelled = false";
            return session.createQuery(hql, Double.class).getSingleResult();
        } catch (Exception e) {
            return 0.0;
        }
    }
    /**
     * Busca reservas para una pista y fecha específicas que no estén canceladas.
     */
    public List<Reservation> findByCourtAndDate(org.example.sportconnect.models.Court court, java.time.LocalDate date) {
        try (Session session = factory.openSession()) {
            String hql = "FROM Reservation r WHERE r.court.id = :courtId " +
                    "AND r.date = :date " +
                    "AND r.cancelled = false";

            Query<Reservation> query = session.createQuery(hql, Reservation.class);
            query.setParameter("courtId", court.getId());
            query.setParameter("date", date);

            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return java.util.Collections.emptyList();
        }
    }

    public boolean save(Reservation reservation) {
        Session session = null;
        Transaction transaction = null;
        try {
            session = factory.openSession();
            transaction = session.beginTransaction();

            session.persist(reservation); // Usar persist es preferible en Hibernate 6

            transaction.commit();
            return true;
        } catch (Exception e) {
            // Solo intentamos rollback si la transacción está activa y la sesión abierta
            if (transaction != null && transaction.getStatus().canRollback()) {
                transaction.rollback();
            }
            e.printStackTrace();
            return false;
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }
}
