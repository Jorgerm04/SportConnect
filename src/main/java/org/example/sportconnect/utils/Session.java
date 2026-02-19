package org.example.sportconnect.utils;

import org.example.sportconnect.models.User;

public class Session {
    private static Session instance;
    private User user;

    // Constructor privado para evitar que se creen instancias con 'new'
    private Session() {}

    public static Session getInstance() {
        if (instance == null) {
            instance = new Session();
        }
        return instance;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void logOut() {
        user = null;
    }
}