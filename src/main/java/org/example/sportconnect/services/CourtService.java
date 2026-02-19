package org.example.sportconnect.services;

import org.example.sportconnect.daos.CourtDAO;
import org.example.sportconnect.models.Court;
import java.util.List;

public class CourtService {
    private final CourtDAO courtDAO;

    public CourtService() {
        this.courtDAO = new CourtDAO();
    }

    /**
     * Devuelve el número total de pistas para la card del Dashboard.
     */
    public String getTotalCourtsCount() {
        long count = courtDAO.countTotalCourts();
        return String.valueOf(count);
    }

    public List<Court> getAllCourts() {
        return courtDAO.getAllCourts();
    }
}