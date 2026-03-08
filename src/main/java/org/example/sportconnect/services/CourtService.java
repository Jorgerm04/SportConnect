package org.example.sportconnect.services;

import org.example.sportconnect.daos.CourtDAO;
import org.example.sportconnect.models.Court;
import org.example.sportconnect.models.Sport;
import java.util.List;

public class CourtService {

    private final CourtDAO courtDAO = new CourtDAO();

    /** Página de pistas con filtro opcional — consulta paginada en BD */
    public List<Court> findPage(int offset, int limit, String filtro) {
        return courtDAO.findPage(offset, limit, filtro);
    }

    /** Total de pistas que coinciden con el filtro (para calcular páginas) */
    public long count(String filtro) { return courtDAO.count(filtro); }

    /** Todos los registros con filtro — para seleccionar todo entre páginas */
    public List<Court> findAll(String filtro) { return courtDAO.findAll(filtro); }

    /** findAll completo — sólo para exportación CSV y selectores de formulario */
    public List<Court> findAll() { return courtDAO.findAll(); }

    public String countFormatted() { return String.valueOf(courtDAO.count()); }

    public boolean save(String name, Sport sport, double price) {
        Court court = new Court();
        court.setName(name);
        court.setSport(sport);
        court.setPrice(price);
        return courtDAO.save(court);
    }

    public boolean update(Court court) { return courtDAO.update(court); }
    public boolean delete(Long id)     { return courtDAO.delete(id); }
}