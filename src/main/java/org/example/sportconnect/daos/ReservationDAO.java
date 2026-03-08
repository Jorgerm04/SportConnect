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

    /** Cuenta reservas con filtro opcional por nombre, apellido o email del usuario */
    @Override
    public long count(String filtro) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            if (filtro == null || filtro.isBlank()) {
                return session.createQuery(
                        "SELECT count(r) FROM Reservation r", Long.class
                ).getSingleResult();
            }
            String q = "%" + filtro.toLowerCase() + "%";
            return session.createQuery(
                    "SELECT count(r) FROM Reservation r JOIN r.user u " +
                            "WHERE lower(u.name) LIKE :q OR lower(u.lastName) LIKE :q OR lower(u.email) LIKE :q",
                    Long.class
            ).setParameter("q", q).getSingleResult();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public long count() { return count(null); }

    /**
     * Página de reservas con JOIN FETCH, ordenadas por fecha asc.
     * Filtro opcional por nombre, apellido o email del usuario.
     */
    @Override
    public List<Reservation> findPage(int offset, int limit, String filtro) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql;
            if (filtro == null || filtro.isBlank()) {
                hql = "SELECT r FROM Reservation r JOIN FETCH r.user JOIN FETCH r.court " +
                        "ORDER BY r.date ASC, r.startHour ASC";
                return session.createQuery(hql, Reservation.class)
                        .setFirstResult(offset)
                        .setMaxResults(limit)
                        .list();
            }
            String q = "%" + filtro.toLowerCase() + "%";
            hql = "SELECT r FROM Reservation r JOIN FETCH r.user u JOIN FETCH r.court " +
                    "WHERE lower(u.name) LIKE :q OR lower(u.lastName) LIKE :q OR lower(u.email) LIKE :q " +
                    "ORDER BY r.date ASC, r.startHour ASC";
            return session.createQuery(hql, Reservation.class)
                    .setParameter("q", q)
                    .setFirstResult(offset)
                    .setMaxResults(limit)
                    .list();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    @Override
    public List<Reservation> findPage(int offset, int limit) {
        return findPage(offset, limit, null);
    }

    /** Todos los registros con filtro — para seleccionar todo entre páginas */
    public List<Reservation> findAll(String filtro) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            if (filtro == null || filtro.isBlank()) return findAll();
            String q = "%" + filtro.toLowerCase() + "%";
            return session.createQuery(
                            "SELECT r FROM Reservation r JOIN FETCH r.user u JOIN FETCH r.court " +
                                    "WHERE lower(u.name) LIKE :q OR lower(u.lastName) LIKE :q OR lower(u.email) LIKE :q " +
                                    "ORDER BY r.date ASC, r.startHour ASC", Reservation.class)
                    .setParameter("q", q).list();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    /** findAll completo con JOIN FETCH — usado para exportación CSV */
    @Override
    public List<Reservation> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "SELECT r FROM Reservation r JOIN FETCH r.user JOIN FETCH r.court " +
                            "ORDER BY r.date ASC, r.startHour ASC", Reservation.class
            ).list();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    /** Reservas activas de un usuario — usado en la vista Home */
    public List<Reservation> findByUser(Long userId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "SELECT r FROM Reservation r JOIN FETCH r.court c JOIN FETCH c.sport " +
                                    "WHERE r.user.id = :userId ORDER BY r.date ASC, r.startHour ASC",
                            Reservation.class)
                    .setParameter("userId", userId)
                    .list();
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

    /**
     * Igual que findByCourtAndDate pero excluyendo una reserva concreta.
     * Necesario en modo edición para no bloquear el slot de la propia reserva.
     */
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

    public boolean cancel(Long id)     { return setCancelled(id, true);  }
    public boolean reactivate(Long id) { return setCancelled(id, false); }

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

    // ─── FILTROS CON RANGO DE FECHAS ──────────────────────────────────────────

    /**
     * Construye la cláusula WHERE combinando filtro de texto y/o rango de fechas.
     * Se usa como base para count y findPage con fechas.
     */
    private String whereFiltro(boolean conTexto, boolean conFecha) {
        StringBuilder sb = new StringBuilder("WHERE 1=1 ");
        if (conTexto) sb.append("AND (lower(u.name) LIKE :q OR lower(u.lastName) LIKE :q OR lower(u.email) LIKE :q) ");
        if (conFecha) {
            sb.append("AND (:desde IS NULL OR r.date >= :desde) ");
            sb.append("AND (:hasta IS NULL OR r.date <= :hasta) ");
        }
        return sb.toString();
    }

    /** Cuenta reservas con filtro de texto y/o rango de fechas */
    public long count(String filtro, LocalDate desde, LocalDate hasta) {
        boolean conFecha = desde != null || hasta != null;
        boolean conTexto = filtro != null && !filtro.isBlank();
        if (!conTexto && !conFecha) return count(null);

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT count(r) FROM Reservation r JOIN r.user u " + whereFiltro(conTexto, conFecha);
            var query = session.createQuery(hql, Long.class);
            if (conTexto) query.setParameter("q", "%" + filtro.toLowerCase() + "%");
            if (conFecha) { query.setParameter("desde", desde); query.setParameter("hasta", hasta); }
            return query.getSingleResult();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /** Página de reservas con filtro de texto y/o rango de fechas */
    public List<Reservation> findPage(int offset, int limit, String filtro, LocalDate desde, LocalDate hasta) {
        boolean conFecha = desde != null || hasta != null;
        boolean conTexto = filtro != null && !filtro.isBlank();
        if (!conTexto && !conFecha) return findPage(offset, limit, null);

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT r FROM Reservation r JOIN FETCH r.user u JOIN FETCH r.court "
                    + whereFiltro(conTexto, conFecha)
                    + "ORDER BY r.date ASC, r.startHour ASC";
            var query = session.createQuery(hql, Reservation.class);
            if (conTexto) query.setParameter("q", "%" + filtro.toLowerCase() + "%");
            if (conFecha) { query.setParameter("desde", desde); query.setParameter("hasta", hasta); }
            return query.setFirstResult(offset).setMaxResults(limit).list();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    /** Todos los registros con filtro de texto y/o rango de fechas — para seleccionar todo entre páginas */
    public List<Reservation> findAll(String filtro, LocalDate desde, LocalDate hasta) {
        boolean conFecha = desde != null || hasta != null;
        boolean conTexto = filtro != null && !filtro.isBlank();
        if (!conTexto && !conFecha) return findAll(filtro);

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT r FROM Reservation r JOIN FETCH r.user u JOIN FETCH r.court "
                    + whereFiltro(conTexto, conFecha)
                    + "ORDER BY r.date ASC, r.startHour ASC";
            var query = session.createQuery(hql, Reservation.class);
            if (conTexto) query.setParameter("q", "%" + filtro.toLowerCase() + "%");
            if (conFecha) { query.setParameter("desde", desde); query.setParameter("hasta", hasta); }
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}
