package org.example.pensionat.room.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.example.pensionat.room.RoomType;

@Entity
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull(message = "Rumstyp måste anges")
    @Enumerated(EnumType.STRING)
    private RoomType roomType;

    @NotBlank(message = "Rumsnummer måste anges")
    private String roomNr;

    @NotBlank(message = "Rumsbeskrivning måste anges")
    private String description;

    @Min(value = 1, message = "Pris per natt måste vara större än 0")
    private int price;

    public Room() {
    }

    public Room(RoomType roomType, String roomNr, String description, int price) {
        this.roomType = roomType;
        this.roomNr = roomNr;
        this.description = description;
        this.price = price;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public RoomType getRoomType() {
        return roomType;
    }

    public String getRoomNr() {
        return roomNr;
    }

    public String getDescription() {
        return description;
    }

    public int getPrice() {
        return price;
    }
}


