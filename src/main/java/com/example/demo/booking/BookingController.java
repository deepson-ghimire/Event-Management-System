package com.example.demo.booking;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.demo.user.User;
import com.example.demo.auth.Authentication;
import com.example.demo.event.Event;
import com.example.demo.event.EventRepository;

@Controller
@RequestMapping("/booking")
public class BookingController {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private Authentication authentication;

    // ================= CUSTOMER =================

    // Book an event
    @PostMapping("/add/{eventId}")
    public String bookEvent(@PathVariable Integer eventId) {

        Event event = eventRepository.findById(eventId).orElse(null);
        User customer = authentication.getLoggedInUser(null);

        if (event != null && customer != null) {
            Booking booking = new Booking();
            booking.setEvent(event);
            booking.setCustomer(customer);
            booking.setBookingDateTime(LocalDateTime.now());
            bookingRepository.save(booking);
        }

        return "redirect:/booking/my";
    }

    // View my bookings
    @GetMapping("/my")
    public String myBookings(Model model) {

        User customer = authentication.getLoggedInUser(null);
        List<Booking> bookings = bookingRepository.findByCustomer(customer);

        model.addAttribute("bookings", bookings);
        return "customer/bookings";
    }

    // ================= ADMIN =================

    // View all bookings (admin)
    @GetMapping("/admin")
    public String viewAllBookings(Model model) {
        model.addAttribute("bookings", bookingRepository.findAll());
        return "admin/bookings";
    }
}
