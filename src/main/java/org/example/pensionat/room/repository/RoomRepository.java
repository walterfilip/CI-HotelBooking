package org.example.pensionat.room.repository;

import org.example.pensionat.room.model.Room;
import org.example.pensionat.room.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long> {
    List<Room> findByRoomType(RoomType roomtype);

    Room findById(long id);
}
