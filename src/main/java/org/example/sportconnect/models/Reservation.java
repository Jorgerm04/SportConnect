package org.example.sportconnect.models;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "reservations")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate date;

    @Column(name = "start_hour", nullable = false)
    private LocalTime startHour;

    @Column(name = "end_hour", nullable = false)
    private LocalTime endHour;

    private boolean cancelled = false;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "court_id", nullable = false)
    private Court court;

    public Reservation() {}

    public Long getId()             { return id; }
    public void setId(Long id)      { this.id = id; }

    public LocalDate getDate()              { return date; }
    public void setDate(LocalDate date)     { this.date = date; }

    public LocalTime getStartHour()                 { return startHour; }
    public void setStartHour(LocalTime startHour)   { this.startHour = startHour; }

    public LocalTime getEndHour()               { return endHour; }
    public void setEndHour(LocalTime endHour)   { this.endHour = endHour; }

    public boolean isCancelled()                { return cancelled; }
    public void setCancelled(boolean cancelled) { this.cancelled = cancelled; }

    public User getUser()           { return user; }
    public void setUser(User user)  { this.user = user; }

    public Court getCourt()             { return court; }
    public void setCourt(Court court)   { this.court = court; }
}