package org.example.sportconnect.daos;

import org.example.sportconnect.models.Sport;

/** Hereda save/update/delete/findById/findAll de GenericDAO */
public class SportDAO extends GenericDAO<Sport> {

    public SportDAO() { super(Sport.class); }
}
