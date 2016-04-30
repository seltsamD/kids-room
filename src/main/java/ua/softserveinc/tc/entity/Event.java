package ua.softserveinc.tc.entity;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by Nestor on 30.04.2016.
 */

@Entity
@Table(name = "events")
public class Event {
    @Id
    //@GenericGenerator(name = "gen", strategy = "increment")
    //@GeneratedValue(generator = "gen")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id", unique = true, nullable = false)
    private Long id;

    private String name;
    private Date startTime;
    private Date endTime;
    private Integer ageLow;
    private Integer ageHigh;

    @ManyToOne @JoinColumn(name = "room_id")
    private Room room;

    private String description;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Integer getAgeLow() {
        return ageLow;
    }

    public void setAgeLow(Integer ageLow) {
        this.ageLow = ageLow;
    }

    public Integer getAgeHigh() {
        return ageHigh;
    }

    public void setAgeHigh(Integer ageHigh) {
        this.ageHigh = ageHigh;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
       this.room = room;
   }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}