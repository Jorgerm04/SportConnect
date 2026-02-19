package org.example.sportconnect.models;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String last_name;
    private String email;
    private String phone;
    private String password;
    private boolean is_admin;

//    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//    private List<Reservation> reservations;

    // Constructor vacío obligatorio para Hibernate
    public User() {
    }

    public User(Long id, String name, String last_name, String email, String phone, String password, boolean is_admin) {
        this.id = id;
        this.name = name;
        this.last_name = last_name;
        this.email = email;
        this.phone = phone;
        this.password = password;
        this.is_admin = is_admin;
    }

    // --- GETTERS Y SETTERS ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLastName() { return last_name; }
    public void setLastName(String last_name) { this.last_name = last_name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public boolean isAdmin() { return is_admin; }
    public void setAdmin(boolean admin) { is_admin = admin; }

//    public List<Reservation> getReservations() { return reservations; }
//    public void setReservations(List<Reservation> reservations) { this.reservations = reservations; }
}