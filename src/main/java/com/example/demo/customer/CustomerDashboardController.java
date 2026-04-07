package com.example.demo.customer;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import com.example.demo.auth.Authentication;
import com.example.demo.booking.BookingService;
import com.example.demo.booking.Booking;
import com.example.demo.event.Event;
import com.example.demo.event.EventService;
import com.example.demo.user.User;
import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/customer")
public class CustomerDashboardController {

    @Autowired
    private Authentication authentication;

    @Autowired
    private EventService eventService;

    @Autowired
    private BookingService bookingService;

    @GetMapping("/dashboard")
    public String getDashboard(HttpServletRequest request, Model model) {
        User user = authentication.authenticate(request);
        if (user == null) return "redirect:/login";

        // Only active events
        List<Event> events = eventService.getActiveEvents();
        model.addAttribute("user", user);
        model.addAttribute("events", events);

        return "customer/dashboard";
    }

    @GetMapping("/booking")
    public String getBookings(HttpServletRequest request, Model model) {
        User user = authentication.authenticate(request);
        if (user == null) return "redirect:/login";

        List<Booking> bookings = bookingService.getBookingsByCustomer(user);
        if (bookings == null) bookings = List.of();

        model.addAttribute("bookings", bookings);
        return "customer/booking";
    }
}
