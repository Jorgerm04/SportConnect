package org.example.sportconnect.models;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String last_name;

    @Column(unique = true, nullable = false)
    private String email;

    private String phone;
    private String password;
    private boolean is_admin;

    public User() {}

    public Long getId()             { return id; }
    public void setId(Long id)      { this.id = id; }

    public String getName()             { return name; }
    public void setName(String name)    { this.name = name; }

    public String getLastName()                 { return last_name; }
    public void setLastName(String lastName)    { this.last_name = lastName; }

    public String getEmail()            { return email; }
    public void setEmail(String email)  { this.email = email; }

    public String getPhone()            { return phone; }
    public void setPhone(String phone)  { this.phone = phone; }

    public String getPassword()                 { return password; }
    public void setPassword(String password)    { this.password = password; }

    public boolean isAdmin()            { return is_admin; }
    public void setAdmin(boolean admin) { this.is_admin = admin; }

    /** Usado por ComboBox para mostrar nombre completo */
    @Override
    public String toString() { return name + " " + last_name; }
}
