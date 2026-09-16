package org.example.pensionat.room.controller;

import org.example.pensionat.customer.client.CustomerClient;
import org.example.pensionat.customer.model.CustomerResponse;
import org.example.pensionat.room.model.Room;
import org.example.pensionat.room.service.RoomService;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;


import org.example.pensionat.room.RoomType;
import org.springframework.ui.Model;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.bind.annotation.SessionAttribute;

import java.util.List;
import java.time.LocalDate;

@Controller
@RequestMapping("/rooms")
public class RoomController {

    private final RoomService roomService;
    private final CustomerClient customerClient;

    public RoomController(RoomService roomService, CustomerClient customerClient) {
        this.roomService = roomService;
        this.customerClient = customerClient;

    }

    @GetMapping
    public String rooms(Model model) {
        model.addAttribute("rooms", roomService.getAllRooms());

        return "rooms";
    }

    @GetMapping("/search")
    public String searchRooms(
            Authentication authentication,
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam RoomType roomType,
            Model model) {

        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {
            Long customerId = (Long) authentication.getPrincipal();
            CustomerResponse customer = customerClient.getCustomer(customerId);
            model.addAttribute("customer", customer);

        }

        if (startDate.isBlank() || endDate.isBlank()) {
            model.addAttribute("errorMessage", "Du måste välja datum för både incheckning och utcheckning!");
            model.addAttribute("title", "Välkommen till Hotellbokning");
            model.addAttribute("subtitle", "Sök lediga rum och boka");
            return "index";
        }

        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        LocalDate today = LocalDate.now();

        if (end.isBefore(start)) {
            model.addAttribute("errorMessage", "Utcheckningsdatum måste vara efter incheckningsdatum!");
            model.addAttribute("title", "Välkommen till Hotellbokning");
            model.addAttribute("subtitle", "Sök lediga rum och boka");

            return "index";
        }

        if (start.isBefore(today) || end.isBefore(today)) {
            model.addAttribute("errorMessage", "Du kan inte välja datum bakåt i tiden!");
            model.addAttribute("title", "Välkommen till Hotellbokning");
            model.addAttribute("subtitle", "Sök lediga rum och boka");

            return "index";
        }

        List<Room> availableRooms = roomService.getAvailableRooms(
                roomType,
                LocalDate.parse(startDate),
                LocalDate.parse(endDate)
        );

        model.addAttribute("rooms", availableRooms);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("roomType", roomType);

        return "rooms";
    }
}

