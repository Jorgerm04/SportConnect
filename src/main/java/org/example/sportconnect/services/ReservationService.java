package org.example.sportconnect.services;

import org.example.sportconnect.daos.ReservationDAO;
import org.example.sportconnect.models.Court;
import org.example.sportconnect.models.Reservation;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

public class ReservationService {

    private final ReservationDAO reservationDAO = new ReservationDAO();

    /** Página de reservas con filtro opcional — consulta paginada en BD */
    public List<Reservation> findPage(int offset, int limit, String filtro) {
        return reservationDAO.findPage(offset, limit, filtro);
    }

    /** Total de reservas que coinciden con el filtro (para calcular páginas) */
    public long count(String filtro) { return reservationDAO.count(filtro); }

    /** Todos los registros con filtro — para seleccionar todo entre páginas */
    public List<Reservation> findAll(String filtro) { return reservationDAO.findAll(filtro); }

    /** Página de reservas con filtro de texto y rango de fechas */
    public List<Reservation> findPage(int offset, int limit, String filtro, LocalDate desde, LocalDate hasta) {
        return reservationDAO.findPage(offset, limit, filtro, desde, hasta);
    }

    /** Cuenta reservas con filtro de texto y rango de fechas */
    public long count(String filtro, LocalDate desde, LocalDate hasta) {
        return reservationDAO.count(filtro, desde, hasta);
    }

    /** Todos los registros con filtro de texto y rango de fechas — para seleccionar todo entre páginas */
    public List<Reservation> findAll(String filtro, LocalDate desde, LocalDate hasta) {
        return reservationDAO.findAll(filtro, desde, hasta);
    }

    /** findAll completo — para exportación CSV */
    public List<Reservation> findAll() { return reservationDAO.findAll(); }

    /** Reservas del usuario actual — para la vista Home */
    public List<Reservation> findByUser(Long userId) {
        if (userId == null) return Collections.emptyList();
        return reservationDAO.findByUser(userId);
    }

    public String countActiveFormatted() { return String.valueOf(reservationDAO.countActive()); }
    public String earningsFormatted()    { return String.format("%.2f€", reservationDAO.sumEarnings()); }

    public List<Reservation> findByCourtAndDate(Court court, LocalDate date) {
        if (court == null || date == null) return Collections.emptyList();
        return reservationDAO.findByCourtAndDate(court, date);
    }

    public List<Reservation> findByCourtAndDateExcluding(Court court, LocalDate date, Long excludeId) {
        if (court == null || date == null) return Collections.emptyList();
        return reservationDAO.findByCourtAndDateExcluding(court, date, excludeId);
    }

    public boolean save(Reservation reservation)   { return reservationDAO.save(reservation); }
    public boolean update(Reservation reservation) { return reservationDAO.update(reservation); }
    public boolean cancel(Long id)                 { return reservationDAO.cancel(id); }
    public boolean reactivate(Long id)             { return reservationDAO.reactivate(id); }
}
