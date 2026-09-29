package org.example.pensionat.room.service;
import org.example.pensionat.error.NotFoundException;
import org.example.pensionat.room.model.Room;
import org.example.pensionat.room.repository.RoomRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.OPTIONAL;
import static org.mockito.Mockito.*;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;



@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

    @Mock
    private RoomRepository roomRepository;

    @InjectMocks
    private RoomService roomService;


    @Test
    void getAllRooms() {

        Room room1 = new Room();
        Room room2 = new Room();

        when(roomRepository.findAll()).thenReturn(List.of(room1, room2));
        List<Room> result = roomService.getAllRooms();
        assertEquals(2, result.size());
        assertEquals(List.of(room1, room2), result);
        verify(roomRepository).findAll();
    }

    @Test
    void getRoomById() {
        Room room = new Room();

        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));

        Room result = roomService.getRoomById(1L);
        assertEquals(room, result);
        verify(roomRepository).findById(1L);
    }

    @Test
    void getRoomByIdThrowException() {
        when(roomRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> roomService.getRoomById(1L));
        verify(roomRepository).findById(1L);
    }
}