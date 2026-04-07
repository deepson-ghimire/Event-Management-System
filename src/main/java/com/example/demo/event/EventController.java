package com.example.demo.event;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.auth.Authentication;
import com.example.demo.user.User;
import com.example.demo.user.UserType;

import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/events")
public class EventController {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private Authentication authentication;

    private static final String UPLOAD_DIR = "uploads/images";

    // ================= HOST =================

    // Add Event Form (host)
    @GetMapping("/host/add")
    public String addEventForm(HttpServletRequest request, Model model) {
        User host = authentication.authenticate(request);
        if (host == null || host.getType() != UserType.HOST || !host.isApproved()) {
            return "redirect:/host/pending";
        }

        model.addAttribute("event", new Event());
        return "host/add-event";
    }

    // Save Event (host)
    @PostMapping("/host/add")
    public String saveEvent(HttpServletRequest request,
                            @ModelAttribute Event event,
                            @RequestParam("imageFile") MultipartFile imageFile) throws IOException {

        User host = authentication.authenticate(request);
        if (host == null || host.getType() != UserType.HOST || !host.isApproved()) {
            return "redirect:/host/pending";
        }

        // Handle image upload
        if (!imageFile.isEmpty()) {
            String filename = System.currentTimeMillis() + "_" + imageFile.getOriginalFilename();
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            Path filePath = uploadPath.resolve(filename);
            imageFile.transferTo(filePath.toFile());

            event.setImageUrl("/uploads/images/" + filename);
        }

        event.setHost(host);
        eventRepository.save(event); // No admin approval needed
        return "redirect:/host/dashboard";
    }

    // Edit Event Form (host)
    @GetMapping("/host/edit/{id}")
    public String editEventForm(@PathVariable Integer id, HttpServletRequest request, Model model) {
        User host = authentication.authenticate(request);
        if (host == null || host.getType() != UserType.HOST || !host.isApproved()) {
            return "redirect:/host/pending";
        }

        Event event = eventRepository.findById(id).orElse(null);
        if (event == null || !event.getHost().getId().equals(host.getId())) {
            return "redirect:/host/dashboard"; // Host can edit only their events
        }

        model.addAttribute("event", event);
        return "host/edit-event";
    }

    // Save Edited Event (host)
    @PostMapping("/host/edit/{id}")
    public String updateEvent(@PathVariable Integer id,
                              HttpServletRequest request,
                              @ModelAttribute Event event,
                              @RequestParam("imageFile") MultipartFile imageFile) throws IOException {

        User host = authentication.authenticate(request);
        if (host == null || host.getType() != UserType.HOST || !host.isApproved()) {
            return "redirect:/host/pending";
        }

        Event existingEvent = eventRepository.findById(id).orElse(null);
        if (existingEvent == null || !existingEvent.getHost().getId().equals(host.getId())) {
            return "redirect:/host/dashboard";
        }

        // Update fields
        existingEvent.setName(event.getName());
        existingEvent.setLocation(event.getLocation());
        existingEvent.setEventStartDateTime(event.getEventStartDateTime());
        existingEvent.setEventEndDateTime(event.getEventEndDateTime());
        existingEvent.setGuestCapacity(event.getGuestCapacity());
        existingEvent.setTicketPrice(event.getTicketPrice());
        existingEvent.setVenue(event.getVenue());

        // Handle new image upload
        if (!imageFile.isEmpty()) {
            String filename = System.currentTimeMillis() + "_" + imageFile.getOriginalFilename();
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            Path filePath = uploadPath.resolve(filename);
            imageFile.transferTo(filePath.toFile());

            existingEvent.setImageUrl("/uploads/images/" + filename);
        }

        eventRepository.save(existingEvent);
        return "redirect:/host/dashboard";
    }

    // Delete Event (host)
    @GetMapping("/host/delete/{id}")
    public String deleteEvent(@PathVariable Integer id, HttpServletRequest request) {
        User host = authentication.authenticate(request);
        if (host == null || host.getType() != UserType.HOST || !host.isApproved()) {
            return "redirect:/host/pending";
        }

        Event event = eventRepository.findById(id).orElse(null);
        if (event != null && event.getHost().getId().equals(host.getId())) {
            eventRepository.delete(event);
        }
        

        return "redirect:/host/dashboard";
    }

    // ================= CUSTOMER =================

    // View all events (customer)
    @GetMapping("/customer")
    public String viewAllEvents(Model model) {
        model.addAttribute("events", eventRepository.findAll());
        return "customer/events";
    }
}
