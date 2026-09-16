package org.example.pensionat.customer.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.example.pensionat.booking.BookingStatus;
import org.example.pensionat.booking.model.Booking;
import org.example.pensionat.booking.service.BookingService;
import org.example.pensionat.customer.client.CustomerClient;
import org.example.pensionat.customer.model.*;
import org.example.pensionat.room.model.Room;
import org.example.pensionat.room.service.RoomService;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;

import java.util.List;

@Controller
@RequestMapping("/customers")
public class CustomerController {

    private final BookingService bookingService;
    private final RoomService roomService;
    private final CustomerClient customerClient;
    private final SecurityContextRepository securityContextRepository;

    public CustomerController(BookingService bookingService,
                              RoomService roomService,
                              CustomerClient customerClient,
                              SecurityContextRepository securityContextRepository
    ) {
        this.bookingService = bookingService;
        this.roomService = roomService;
        this.customerClient = customerClient;
        this.securityContextRepository = securityContextRepository;
    }

    @GetMapping
    public String customers(
            Model model,
            RedirectAttributes redirect,
            Authentication authentication) {

        Long customerId = (Long) authentication.getPrincipal();
        System.out.println("Authentication: " + authentication.isAuthenticated());
        System.out.println("Principal: " + authentication.getPrincipal());
        System.out.println("Authorities: " + authentication.getAuthorities());

        CustomerResponse customer = customerClient.getCustomer(customerId);

        List<Booking> currentBookings = bookingService.getBookingByCustomerId(customerId);
        model.addAttribute("bookings", currentBookings);
        model.addAttribute("customer", customer);
        model.addAttribute("activeStatus", BookingStatus.ACTIVE);

        return "customers";
    }

    @GetMapping("/form")
    public String showCustomerForm(
            @RequestParam Long roomId,
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(defaultValue = "false") boolean extraBed,
            Model model
    ) {

        model.addAttribute("roomId", roomId);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("extraBed", extraBed);

        return "customer-form";
    }

    @PostMapping("/edit")
    public String editCustomer(
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam String phoneNumber,
            @RequestParam String password,
            @RequestParam String newPassword,
            RedirectAttributes redirect,
            Authentication authentication,
            HttpServletRequest httpRequest
    ) {
        Long customerId = (Long) authentication.getPrincipal();

        //sätts till true om någon av fälten är fyllda
        boolean changePassword = !password.isBlank() || !newPassword.isBlank();

        UpdateCustomerRequest updateCustomerRequest = new UpdateCustomerRequest(
                firstName,
                lastName,
                phoneNumber,
                newPassword,
                password,
                changePassword
        );

        try {
            CustomerResponse updatedCustomer = customerClient.updateCustomer(customerId, updateCustomerRequest);

            //spara namnet för frontend/enkel error hantering
            httpRequest.getSession().setAttribute("customerFirstName", updatedCustomer.firstName());

            if (changePassword) {
                redirect.addFlashAttribute("message", "Profilen uppdaterad och lösenord ändrat");
            } else {
                redirect.addFlashAttribute("message", "Profilen uppdaterad");
            }

            redirect.addFlashAttribute("color", "success");

        } catch (HttpClientErrorException.BadRequest exception) {
            redirect.addFlashAttribute("message", "Profilen uppdaterades inte. Kontrollera att du har skrivit rätt lösenord.");
            redirect.addFlashAttribute("color", "error");
        }

        return "redirect:/customers/edit";
    }

    @GetMapping("/edit")
    public String showEditCustomer(Authentication authentication, Model model) {
        Long customerId = (Long) authentication.getPrincipal();
        System.out.println("Authentication: " + authentication.isAuthenticated());

        CustomerResponse customer = customerClient.getCustomer(customerId);
        model.addAttribute("customer", customer);

        return "customer-edit";
    }

    @PostMapping
    public String createCustomer(
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam String email,
            @RequestParam String phoneNumber,
            @RequestParam String password,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {

        CreateCustomerRequest request = new CreateCustomerRequest(
                firstName,
                lastName,
                email,
                phoneNumber,
                password
        );

        CustomerResponse customer = customerClient.createCustomer(request);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                customer.id(),
                null,
                List.of()
        );
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        securityContextRepository.saveContext(
                context,
                httpRequest,
                httpResponse
        );

        return "redirect:/customers";
    }

    @PostMapping("/booking")
    public String createCustomerWhileBooking(
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam String email,
            @RequestParam String phoneNumber,
            @RequestParam String password,
            @RequestParam Long roomId,
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam boolean extraBed,
            RedirectAttributes redirect,
            Model model,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse

    ) {
        CreateCustomerRequest request = new CreateCustomerRequest(
                firstName,
                lastName,
                email,
                phoneNumber,
                password
        );
        try {
            CustomerResponse customer = customerClient.createCustomer(request);

            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    customer.id(),
                    null,
                    List.of()
            );

            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);

            securityContextRepository.saveContext(
                    context,
                    httpRequest,
                    httpResponse
            );

            Room room = roomService.getRoomById(roomId);

            int totalPrice = bookingService.getTotalPrice(
                    room,
                    java.time.LocalDate.parse(startDate),
                    java.time.LocalDate.parse(endDate),
                    extraBed
            );

            model.addAttribute("customer", customer);
            model.addAttribute("room", room);
            model.addAttribute("roomId", roomId);
            model.addAttribute("startDate", startDate);
            model.addAttribute("endDate", endDate);
            model.addAttribute("extraBed", extraBed);
            model.addAttribute("totalPrice", totalPrice);

        } catch (HttpClientErrorException.Conflict e) {

            redirect.addAttribute("roomId", roomId);
            redirect.addAttribute("startDate", startDate);
            redirect.addAttribute("endDate", endDate);
            redirect.addAttribute("extraBed", extraBed);
            redirect.addFlashAttribute("loginError", "E-post är kopplat till ett redan existerande konto");

            return "redirect:/customers/form";
        }
        return "booking-form";
    }

    @PostMapping("/login")
    public String login(
            @RequestParam String email,
            @RequestParam String password,
            Model model,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        if (email.isEmpty() || password.isEmpty()) {
            model.addAttribute("title", "Välkommen till Hotellbokning");
            model.addAttribute("subtitle", "Sök lediga rum och boka");
            model.addAttribute("loginError", "Fyll i användarnamn och lösenord");
            return "index";
        }

        LoginRequest request = new LoginRequest(email, password);
        CustomerResponse customer = customerClient.login(request);

        //spara namnet för frontend/enkel error hantering
        httpRequest.getSession().setAttribute("customerFirstName", customer.firstName());

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                customer.id(),
                null,
                List.of()
        );

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        securityContextRepository.saveContext(
                context,
                httpRequest,
                httpResponse
        );

        return "redirect:/customers";
    }

    @PostMapping("/delete")
    public String deleteCustomer(
            Model model,
            Authentication authentication,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse

    ) {
        Long customerId = (Long) authentication.getPrincipal();

        boolean hasActiveBooking = checkIfActiveCustomerHasActiveBookings(customerId);

        if (!hasActiveBooking) {
            customerClient.deleteCustomer(customerId);

            SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
            logoutHandler.logout(
                    httpRequest,
                    httpResponse,
                    authentication
            );
            model.addAttribute("successMessage", "Ditt konto har raderats");
            model.addAttribute("title", "Välkommen till Hotellbokning");
            model.addAttribute("subtitle", "Sök lediga rum och boka");

            return "index";
        } else {
            CustomerResponse customer = customerClient.getCustomer(customerId);
            List<Booking> currentBookings = bookingService.getBookingByCustomerId(customerId);

            model.addAttribute("customer", customer);
            model.addAttribute("bookings", currentBookings);
            model.addAttribute("activeStatus", BookingStatus.ACTIVE);
            model.addAttribute("deleteError", "Du har aktiva bokningar, du kan inte radera ditt konto");

            return "customers";
        }
    }

    public boolean checkIfActiveCustomerHasActiveBookings(Long customerId) {
        return bookingService.checkIfCustomerHasActiveBookings(customerId);
    }
}




