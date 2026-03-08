package org.example.sportconnect.services;

import org.example.sportconnect.daos.SportDAO;
import org.example.sportconnect.models.Sport;
import java.util.List;

public class SportService {

    private final SportDAO sportDAO = new SportDAO();

    public List<Sport> findAll() { return sportDAO.findAll(); }

    /** Devuelve false si el nombre está vacío */
    public boolean save(String name) {
        if (name == null || name.isBlank()) return false;
        return sportDAO.save(new Sport(name));
    }

    public boolean update(Sport sport) { return sportDAO.update(sport); }

    public boolean delete(Long id) { return sportDAO.delete(id); }
}
