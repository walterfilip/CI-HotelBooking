package org.example.pensionat.error;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.servlet.ModelAndView;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(HttpClientErrorException.Unauthorized.class)
    public ModelAndView handleUnauthorized() {
        log.warn("Customer login failed: unauthorized");

        ModelAndView modelAndView = new ModelAndView("index");

        //setStatus gör ingenting i programmet just nu, det är bara för att displaya korrekt felstatus i typ Postman
        modelAndView.setStatus(HttpStatus.UNAUTHORIZED);
        modelAndView.addObject("title", "Välkommen till Hotellbokning");
        modelAndView.addObject("subtitle", "Sök lediga rum och boka");
        modelAndView.addObject("loginError", "Fel användarnamn eller lösenord");

        return modelAndView;
    }

    @ExceptionHandler(ResourceAccessException.class)
    public ModelAndView handleServiceUnavailable() {
        log.error("Customer service unavaible: connection error");

        ModelAndView modelAndView = new ModelAndView("index");

        modelAndView.setStatus(HttpStatus.SERVICE_UNAVAILABLE);
        modelAndView.addObject("title", "Välkommen till Hotellbokning");
        modelAndView.addObject("subtitle", "Sök lediga rum och boka");
        modelAndView.addObject("errorMessage", "Tjänsten ligger nere för tillfället. Försök igen senare.");

        return modelAndView;
    }

    @ExceptionHandler(NotFoundException.class)
    public ModelAndView handleNotFound(NotFoundException exception) {
        log.info("Requested resource not found");

        ModelAndView modelAndView = new ModelAndView("index");

        modelAndView.setStatus(HttpStatus.NOT_FOUND);
        modelAndView.addObject("title", "Välkommen till Hotellbokning");
        modelAndView.addObject("subtitle", "Sök lediga rum och boka");
        modelAndView.addObject("errorMessage", exception.getMessage());

        return modelAndView;
    }

    @ExceptionHandler(BadRequestException.class)
    public ModelAndView handleBadRequest(BadRequestException exception) {
        log.info("Request rejected: invalid input");

        ModelAndView modelAndView = new ModelAndView("index");

        modelAndView.setStatus(HttpStatus.BAD_REQUEST);
        modelAndView.addObject("title", "Välkommen till Hotellbokning");
        modelAndView.addObject("subtitle", "Sök lediga rum och boka");
        modelAndView.addObject("errorMessage", exception.getMessage());

        return modelAndView;
    }

    @ExceptionHandler(ForbiddenException.class)
    public ModelAndView handleForbidden(ForbiddenException exception) {
        log.warn("Access to booking denied");

        ModelAndView modelAndView = new ModelAndView("index");

        modelAndView.setStatus(HttpStatus.FORBIDDEN);
        modelAndView.addObject("title", "Välkommen till Hotellbokning");
        modelAndView.addObject("subtitle", "Sök lediga rum och boka");
        modelAndView.addObject("errorMessage", exception.getMessage());

        return modelAndView;
    }
}