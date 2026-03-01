package org.example.sportconnect.services;

import org.example.sportconnect.daos.SportDAO;
import org.example.sportconnect.models.Sport;
import java.util.List;

public class SportService {

    private final SportDAO sportDAO = new SportDAO();

    /** Devuelve todos los deportes */
    public List<Sport> findAll() { return sportDAO.findAll(); }

    /** Crea un deporte nuevo. Devuelve false si el nombre está vacío */
    public boolean save(String name) {
        if (name == null || name.isBlank()) return false;
        return sportDAO.save(new Sport(name));
    }

    /** Actualiza el nombre de un deporte */
    public boolean update(Sport sport) { return sportDAO.update(sport); }

    /** Elimina un deporte por id */
    public boolean delete(Long id) { return sportDAO.delete(id); }
}
