package com.example.demo.admin;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.example.demo.auth.Authentication;
import com.example.demo.booking.BookingRepository;
import com.example.demo.event.EventRepository;
import com.example.demo.event.EventService;
import com.example.demo.user.User;
import com.example.demo.user.UserService;
import com.example.demo.user.UserType;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class AdminDashboardController {

    @Autowired
    private Authentication authentication;

    @Autowired
    private UserService userService;

    @Autowired
    private EventService eventService;
    
    @Autowired
    private EventRepository eventRepository;
    
    @Autowired
    private BookingRepository bookingRepository;

    // ================= DASHBOARD =================
    @GetMapping("/admin/dashboard")
    public String getDashboard(HttpServletRequest request, Model model) {

        User user = authentication.authenticate(request);

        if (user == null || user.getType() != UserType.ADMIN) {
            return "redirect:/login";
        }

        model.addAttribute("user", user);
        return "admin/dashboard";
    }

    // ================= MANAGE USERS =================
    @GetMapping("/admin/users")
    public String manageUsers(HttpServletRequest request, Model model) {

        User admin = authentication.authenticate(request);

        if (admin == null || admin.getType() != UserType.ADMIN) {
            return "redirect:/login";
        }

        model.addAttribute("hosts", userService.getUsersByType(UserType.HOST));
        model.addAttribute("users", userService.getUsersByType(UserType.CUSTOMER));

        return "admin/users";
    }


    // ================= DELETE USER =================
    @GetMapping("/admin/delete-user/{id}")
    public String deleteUser(
            @PathVariable("id") Integer id,
            HttpServletRequest request,
            Model model) {

        User admin = authentication.authenticate(request);

        if (admin == null || admin.getType() != UserType.ADMIN) {
            return "redirect:/login";
        }

        User userToDelete = userService.getUserById(id);

        // ❌ Prevent deleting ADMIN
        if (userToDelete.getType() == UserType.ADMIN) {
            return "redirect:/admin/users?error=admin";
        }

        userService.deleteUser(id);
        return "redirect:/admin/users?success=deleted";
    }
  
    

    // ================= MANAGE EVENTS =================
    @GetMapping("/admin/events")
    public String manageEvents(HttpServletRequest request, Model model) {

        User user = authentication.authenticate(request);

        if (user == null || user.getType() != UserType.ADMIN) {
            return "redirect:/login";
        }

        model.addAttribute("events", eventRepository.findByIsDeletedFalse());

        return "admin/events";
    }

    // ================= DELETE EVENT =================
    @GetMapping("/admin/delete-event/{id}")
    public String deleteEvent(@PathVariable("id") Integer id, HttpServletRequest request) {

        User user = authentication.authenticate(request);

        if (user == null || user.getType() != UserType.ADMIN) {
            return "redirect:/login";
        }

        eventService.softDeleteEvent(id);
        return "redirect:/admin/events";
    }

    // ================= APPROVE HOST =================
    @GetMapping("/admin/approve/{id}")
    public String approveHost(@PathVariable("id") Integer id, HttpServletRequest request) {

        User user = authentication.authenticate(request);

        if (user == null || user.getType() != UserType.ADMIN) {
            return "redirect:/login";
        }

        userService.approveHost(id);
        return "redirect:/admin/users";
    }
    

    // ================= REPORTS =================
    @GetMapping("/admin/reports")
    public String reports(HttpServletRequest request, Model model) {
        User admin = authentication.authenticate(request);
        if (admin == null || admin.getType() != UserType.ADMIN) return "redirect:/login";

        // User stats
        model.addAttribute("totalUsers", userService.countAll());
        model.addAttribute("totalHosts", userService.countByType(UserType.HOST));
        model.addAttribute("totalCustomers", userService.countByType(UserType.CUSTOMER));

        // Event stats
        model.addAttribute("totalEvents", eventRepository.count());
        model.addAttribute("activeEvents", eventRepository.countByIsDeletedFalse());
        model.addAttribute("deletedEvents", eventRepository.countByIsDeletedTrue());

        // Booking stats
        model.addAttribute("totalBookings", bookingRepository.count());
        model.addAttribute("totalTicketsSold", bookingRepository.getTotalTicketsSold());
        model.addAttribute("totalRevenue", bookingRepository.getTotalRevenue());

        // Top events & customers
        model.addAttribute("topEvents", bookingRepository.findTopEvents());
        model.addAttribute("topCustomers", bookingRepository.findTopCustomers());

        // Daily revenue for last 7 days
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        List<Map<String, Object>> dailyRevenue = bookingRepository.getDailyRevenue(sevenDaysAgo);
        model.addAttribute("dailyRevenue", dailyRevenue);

        // Bookings by status
        List<Map<String, Object>> bookingsByStatus = bookingRepository.getBookingStatusCounts();
        model.addAttribute("bookingsByStatus", bookingsByStatus);

        return "admin/reports";
    }
}
