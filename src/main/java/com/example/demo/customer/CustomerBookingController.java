package com.example.demo.customer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.example.demo.auth.Authentication;
import com.example.demo.booking.BookingService;
import com.example.demo.event.Event;
import com.example.demo.event.EventRepository;
import com.example.demo.user.User;

import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/customer")
public class CustomerBookingController {

    @Autowired
    private Authentication authentication;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private BookingService bookingService;

    // Show Confirm Booking Page
    @GetMapping("/book/{id}")
    public String confirmBookingPage(
            @PathVariable("id") Integer eventId,
            HttpServletRequest request,
            Model model,
            @ModelAttribute("error") String error // receive error flash attribute
    ) {

        User customer = authentication.authenticate(request);
        if (customer == null) return "redirect:/login";

        Event event = eventRepository.findById(eventId).orElse(null);
        if (event == null) return "redirect:/customer/dashboard";

        int availableSeats = bookingService.getAvailableSeats(event);

        model.addAttribute("event", event);
        model.addAttribute("availableSeats", availableSeats);
        model.addAttribute("error", error); // pass error to template if exists

        return "customer/confirm-booking";
    }

    // Confirm & Save Booking (with capacity check)
    @PostMapping("/confirm-booking")
    public String confirmBooking(
            @RequestParam("eventId") Integer eventId,
            @RequestParam("ticketCount") int ticketCount,
            HttpServletRequest request,
            Model model
    ) {
        User customer = authentication.authenticate(request);
        if (customer == null) return "redirect:/login";

        Event event = eventRepository.findById(eventId).orElse(null);
        if (event == null) return "redirect:/customer/dashboard";

        int availableSeats = bookingService.getAvailableSeats(event);

        // Check if requested tickets exceed available seats
        if (ticketCount > availableSeats) {
            model.addAttribute("event", event);
            model.addAttribute("availableSeats", availableSeats);
            model.addAttribute("error", "Only " + availableSeats + " tickets are available.");
            return "customer/confirm-booking";
        }

        // Proceed with booking
        bookingService.bookEvent(customer, event, ticketCount);

        // Redirect to dashboard with success message
        model.addAttribute("success", "Booking successful!");
        return "redirect:/customer/dashboard";
    }
}
