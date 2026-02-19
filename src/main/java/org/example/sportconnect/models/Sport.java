package org.example.sportconnect.models;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "sports")
public class Sport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    // Relación inversa opcional: Un deporte tiene muchas pistas
    @OneToMany(mappedBy = "sport", cascade = CascadeType.ALL)
    private List<Court> courts;

    public Sport() {}

    public Sport(String name) {
        this.name = name;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<Court> getCourts() { return courts; }
    public void setCourts(List<Court> courts) { this.courts = courts; }
}