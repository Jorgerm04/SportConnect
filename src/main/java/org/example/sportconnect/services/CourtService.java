package org.example.sportconnect.services;

import org.example.sportconnect.daos.CourtDAO;
import org.example.sportconnect.models.Court;
import org.example.sportconnect.models.Sport;
import java.util.List;

public class CourtService {
    private final CourtDAO courtDAO = new CourtDAO();

    public String getTotalCourtsCount() { return String.valueOf(courtDAO.countTotalCourts()); }
    public List<Court> getAllCourts() { return courtDAO.getAllCourts(); }


    public boolean saveCourt(String name, Sport sport, double price) {
        Court court = new Court();
        court.setName(name); court.setSport(sport); court.setPrice(price);
        return courtDAO.save(court);
    }

    public boolean updateCourt(Court court) { return courtDAO.update(court); }

    public boolean deleteCourt(Long id) { return courtDAO.delete(id); }
}