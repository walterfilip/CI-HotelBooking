package org.example.pensionat.ui.controller;

import org.example.pensionat.customer.client.CustomerClient;
import org.example.pensionat.customer.model.CustomerResponse;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.client.ResourceAccessException;

@Controller
public class
HomeController {

    private final CustomerClient customerClient;

    public HomeController(CustomerClient customerClient) {
        this.customerClient = customerClient;
    }

    @GetMapping("/")
    public String home(Authentication authentication, Model model) {
        model.addAttribute("title", "Välkommen till Hotellbokning");
        model.addAttribute("subtitle", "Sök lediga rum och boka");

        if (authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {
            Long customerId = (Long) authentication.getPrincipal();
            CustomerResponse customer = customerClient.getCustomer(customerId);
            model.addAttribute("customer", customer);
        }

        return "index";
    }
}