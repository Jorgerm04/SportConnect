package org.example.sportconnect.services;

import org.example.sportconnect.daos.CourtDAO;
import org.example.sportconnect.models.Court;
import org.example.sportconnect.models.Sport;
import java.util.List;

public class CourtService {

    private final CourtDAO courtDAO = new CourtDAO();

    /** Devuelve todas las pistas con su deporte */
    public List<Court> findAll() { return courtDAO.findAll(); }

    /** Total de pistas formateado para el dashboard */
    public String countFormatted() { return String.valueOf(courtDAO.count()); }

    /** Crea una pista nueva */
    public boolean save(String name, Sport sport, double price) {
        Court court = new Court();
        court.setName(name);
        court.setSport(sport);
        court.setPrice(price);
        return courtDAO.save(court);
    }

    /** Actualiza una pista existente */
    public boolean update(Court court) { return courtDAO.update(court); }

    /** Elimina una pista por id */
    public boolean delete(Long id) { return courtDAO.delete(id); }
}
