package org.example.sportconnect.utils;

import org.example.sportconnect.models.User;

/** Singleton que guarda el usuario autenticado durante la sesión */
public class Session {

    private static Session instance;
    private User user;

    private Session() {}

    public static Session getInstance() {
        if (instance == null) instance = new Session();
        return instance;
    }

    public User getUser()           { return user; }
    public void setUser(User user)  { this.user = user; }

    /** Cierra la sesión eliminando el usuario actual */
    public void logOut() { user = null; }
}
