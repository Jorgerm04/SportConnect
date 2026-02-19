package org.example.sportconnect.services;

import org.example.sportconnect.daos.SportDAO;
import org.example.sportconnect.models.Sport;
import java.util.List;

public class SportService {
    private final SportDAO sportDAO;

    public SportService() {
        this.sportDAO = new SportDAO();
    }

    public List<Sport> findAllSports() {
        return sportDAO.getAllSports();
    }

    /**
     * Devuelve solo los nombres de los deportes (útil para filtros en la UI)
     */
    public List<String> getAllSportNames() {
        return sportDAO.getAllSports().stream()
                .map(Sport::getName)
                .toList();
    }
}