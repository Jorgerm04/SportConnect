package org.example.sportconnect.models;

import jakarta.persistence.*;

@Entity
@Table(name = "sports")
public class Sport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    public Sport() {}
    public Sport(String name) { this.name = name; }

    public Long getId()           { return id; }
    public void setId(Long id)    { this.id = id; }

    public String getName()           { return name; }
    public void setName(String name)  { this.name = name; }

    /** Usado por ComboBox para mostrar el nombre */
    @Override
    public String toString() { return name; }
}
