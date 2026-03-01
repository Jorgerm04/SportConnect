package org.example.sportconnect.services;

import org.example.sportconnect.daos.ReservationDAO;
import org.example.sportconnect.models.Court;
import org.example.sportconnect.models.Reservation;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

public class ReservationService {

    private final ReservationDAO reservationDAO = new ReservationDAO();

    /** Devuelve todas las reservas */
    public List<Reservation> findAll() { return reservationDAO.findAll(); }

    /** Total de reservas activas formateado para el dashboard */
    public String countActiveFormatted() { return String.valueOf(reservationDAO.countActive()); }

    /** Ingresos totales formateados para el dashboard */
    public String earningsFormatted() { return String.format("%.2f€", reservationDAO.sumEarnings()); }

    /** Reservas activas de una pista en una fecha */
    public List<Reservation> findByCourtAndDate(Court court, LocalDate date) {
        if (court == null || date == null) return Collections.emptyList();
        return reservationDAO.findByCourtAndDate(court, date);
    }

    /** Reservas activas de una pista en una fecha, excluyendo una (para edición) */
    public List<Reservation> findByCourtAndDateExcluding(Court court, LocalDate date, Long excludeId) {
        if (court == null || date == null) return Collections.emptyList();
        return reservationDAO.findByCourtAndDateExcluding(court, date, excludeId);
    }

    /** Guarda una nueva reserva */
    public boolean save(Reservation reservation) { return reservationDAO.save(reservation); }

    /** Actualiza una reserva existente */
    public boolean update(Reservation reservation) { return reservationDAO.update(reservation); }

    /** Cancela una reserva */
    public boolean cancel(Long id) { return reservationDAO.cancel(id); }

    /** Reactiva una reserva cancelada */
    public boolean reactivate(Long id) { return reservationDAO.reactivate(id); }
}
