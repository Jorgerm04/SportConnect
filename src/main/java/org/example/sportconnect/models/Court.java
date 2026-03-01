package org.example.sportconnect.models;

import jakarta.persistence.*;

@Entity
@Table(name = "courts")
public class Court {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sport_id", nullable = false)
    private Sport sport;

    @Column(nullable = false)
    private double price;

    public Court() {}

    public Long getId()           { return id; }
    public void setId(Long id)    { this.id = id; }

    public String getName()           { return name; }
    public void setName(String name)  { this.name = name; }

    public Sport getSport()             { return sport; }
    public void setSport(Sport sport)   { this.sport = sport; }

    public double getPrice()            { return price; }
    public void setPrice(double price)  { this.price = price; }
}
