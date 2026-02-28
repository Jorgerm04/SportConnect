package org.example.sportconnect.services;

import org.example.sportconnect.daos.SportDAO;
import org.example.sportconnect.models.Sport;
import java.util.List;

public class SportService {
    private final SportDAO sportDAO = new SportDAO();

    public List<Sport> findAllSports() { return sportDAO.getAllSports(); }
    public List<String> getAllSportNames() { return sportDAO.getAllSports().stream().map(Sport::getName).toList(); }

    public boolean saveSport(String name) {
        if (name == null || name.isBlank()) return false;
        return sportDAO.save(new Sport(name));
    }

    public boolean updateSport(Sport sport) { return sportDAO.update(sport); }

    public boolean deleteSport(Long id) { return sportDAO.delete(id); }
}