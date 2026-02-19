package org.example.sportconnect.services;

import org.example.sportconnect.daos.ReservationDAO;
import org.example.sportconnect.models.Reservation;
import java.util.List;

public class ReservationService {

    private final ReservationDAO reservationDAO;

    public ReservationService() {
        this.reservationDAO = new ReservationDAO();
    }

    /**
     * Obtiene el conteo de reservas formateado para el Label.
     */
    public String getTotalActiveReservationsCount() {
        long count = reservationDAO.countActiveReservations();
        return String.valueOf(count);
    }

    /**
     * Obtiene la lista completa de reservas para la tabla.
     */
    public List<Reservation> getAllReservations() {
        return reservationDAO.getAllReservations();
    }

    /**
     * Obtiene los ingresos totales formateados con el símbolo de Euro.
     */
    public String getFormattedTotalEarnings() {
        Double earnings = reservationDAO.getTotalEarnings();
        if (earnings == null) earnings = 0.0;
        return String.format("%.2f€", earnings);
    }

    /**
     * Obtiene las reservas existentes para una pista y fecha dadas.
     */
    public List<Reservation> findByCourtAndDate(org.example.sportconnect.models.Court court, java.time.LocalDate date) {
        if (court == null || date == null) {
            return java.util.Collections.emptyList();
        }
        return reservationDAO.findByCourtAndDate(court, date);
    }

    public boolean makeReservation(Reservation reservation) {
        // Aquí podrías añadir validaciones de última hora
        return reservationDAO.save(reservation);
    }

    // Aquí puedes añadir lógica de negocio, por ejemplo:
    // public boolean canUserReserve(User user) { ... }
}