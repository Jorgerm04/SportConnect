package org.example.sportconnect.services;

import org.example.sportconnect.daos.ReservationDAO;
import org.example.sportconnect.models.Court;
import org.example.sportconnect.models.Reservation;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

public class ReservationService {
    private final ReservationDAO reservationDAO = new ReservationDAO();

    public String getTotalActiveReservationsCount() { return String.valueOf(reservationDAO.countActiveReservations()); }
    public List<Reservation> getAllReservations() { return reservationDAO.getAllReservations(); }

    public String getFormattedTotalEarnings() {
        Double earnings = reservationDAO.getTotalEarnings();
        if (earnings == null) earnings = 0.0;
        return String.format("%.2f€", earnings);
    }

    public List<Reservation> findByCourtAndDate(Court court, LocalDate date) {
        if (court == null || date == null) return Collections.emptyList();
        return reservationDAO.findByCourtAndDate(court, date);
    }

    public List<Reservation> findByCourtAndDateExcluding(Court court, LocalDate date, Long excludeId) {
        if (court == null || date == null) return Collections.emptyList();
        return reservationDAO.findByCourtAndDateExcluding(court, date, excludeId);
    }

    public boolean makeReservation(Reservation reservation) { return reservationDAO.save(reservation); }

    public boolean updateReservation(Reservation reservation) { return reservationDAO.update(reservation); }

    public boolean cancelReservation(Long id) { return reservationDAO.cancel(id); }
    public boolean reactivateReservation(Long id) { return reservationDAO.reactivate(id); }
}