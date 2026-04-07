package com.example.demo.host;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.auth.Authentication;
import com.example.demo.booking.Booking;
import com.example.demo.booking.BookingService;
import com.example.demo.event.Event;
import com.example.demo.event.EventRepository;
import com.example.demo.event.EventService;
import com.example.demo.user.User;
import com.example.demo.user.UserType;

import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/host")
public class HostDashboardController {

    @Autowired
    private Authentication authentication;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private BookingService bookingService;
    
    @Autowired
    private EventService eventService;
    

    private static final String UPLOAD_DIR =
            System.getProperty("user.dir") + "/uploads/images";


    // ================= ACCESS CHECK =================
    private String checkHostAccess(HttpServletRequest request) {
        User host = authentication.authenticate(request);

        if (host == null || host.getType() != UserType.HOST) {
            return "redirect:/login";
        }

        if (!host.isApproved()) {
            return "redirect:/host/pending";
        }

        return null;
    }

    // ================= DASHBOARD =================
    @GetMapping("/dashboard")
    public String dashboard(HttpServletRequest request, Model model) {
        String redirect = checkHostAccess(request);
        if (redirect != null) return redirect;

        User host = authentication.authenticate(request);
        model.addAttribute("events", eventRepository.findByHostAndIsDeletedFalse(host));


        return "host/dashboard";
    }

    // ================= ADD EVENT =================
    @GetMapping("/add-event")
    public String addEventForm(HttpServletRequest request, Model model) {
        String redirect = checkHostAccess(request);
        if (redirect != null) return redirect;

        model.addAttribute("event", new Event());
        return "host/add-event";
    }

    @PostMapping("/add-event")
    public String addEvent(@ModelAttribute Event event,
                           @RequestParam("image") MultipartFile imageFile,
                           HttpServletRequest request,
                           RedirectAttributes redirectAttributes) {

        try {
            User host = authentication.authenticate(request);
            event.setHost(host); // 🔥 VERY IMPORTANT

            if (imageFile != null && !imageFile.isEmpty()) {

                Path uploadPath = Paths.get(UPLOAD_DIR);
                Files.createDirectories(uploadPath); // ✅ creates folder safely

                String fileName = UUID.randomUUID() + "_" + imageFile.getOriginalFilename();
                Path filePath = uploadPath.resolve(fileName);

                Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                event.setImageUrl("/uploads/images/" + fileName);
            }


            eventRepository.save(event);
            redirectAttributes.addFlashAttribute("success", "Event added successfully!");

        } catch (IOException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Failed to save image.");
            return "redirect:/host/add-event";
        }

        return "redirect:/host/dashboard";
    }


    // ================= EDIT EVENT =================
    @GetMapping("/edit-event/{id}")
    public String editEvent(@PathVariable("id") Integer id, HttpServletRequest request, Model model) {
        String redirect = checkHostAccess(request);
        if (redirect != null) return redirect;

        User host = authentication.authenticate(request);
        Optional<Event> optionalEvent = eventRepository.findById(id);

        if (optionalEvent.isEmpty() || !optionalEvent.get().getHost().getId().equals(host.getId())) {
            return "redirect:/host/dashboard";
        }

        model.addAttribute("event", optionalEvent.get());
        return "host/edit-event";
    }

    @PostMapping("/edit-event/{id}")
    public String editEventPost(
            @PathVariable("id") Integer eventId,
            @ModelAttribute Event event,
            @RequestParam("image") MultipartFile imageFile,
            RedirectAttributes redirectAttributes) {

        Event existingEvent = eventRepository.findById(eventId).orElse(null);
        if (existingEvent == null) {
            redirectAttributes.addFlashAttribute("error", "Event not found");
            return "redirect:/host/dashboard";
        }

        // Update fields
        existingEvent.setName(event.getName());
        existingEvent.setLocation(event.getLocation());
        existingEvent.setVenue(event.getVenue());
        existingEvent.setEventStartDateTime(event.getEventStartDateTime());
        existingEvent.setEventEndDateTime(event.getEventEndDateTime());
        existingEvent.setGuestCapacity(event.getGuestCapacity());
        existingEvent.setTicketPrice(event.getTicketPrice());
        existingEvent.setDescription(event.getDescription());

        // Handle image upload
        try {
        	if (imageFile != null && !imageFile.isEmpty()) {

        	    Path uploadPath = Paths.get(UPLOAD_DIR);
        	    Files.createDirectories(uploadPath);

        	    String fileName = UUID.randomUUID() + "_" + imageFile.getOriginalFilename();
        	    Path filePath = uploadPath.resolve(fileName);

        	    Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        	    existingEvent.setImageUrl("/uploads/images/" + fileName);
        	}


        } catch (IOException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Failed to save image.");
            return "redirect:/host/edit-event/" + eventId;
        }

        eventRepository.save(existingEvent);
        redirectAttributes.addFlashAttribute("success", "Event updated successfully!");
        return "redirect:/host/dashboard";
    }

    // ================= DELETE EVENT =================
    @PostMapping("/delete-event/{id}")
    public String deleteEvent(@PathVariable("id") Integer id, HttpServletRequest request) {
        String redirect = checkHostAccess(request);
        if (redirect != null) return redirect;

        User host = authentication.authenticate(request);
        Optional<Event> optionalEvent = eventRepository.findById(id);

        if (optionalEvent.isPresent() && optionalEvent.get().getHost().getId().equals(host.getId())) {
        	Event event = optionalEvent.get();
        	event.setDeleted(true);
        	eventRepository.save(event);

        }

        return "redirect:/host/dashboard";
    }

    // ================= BOOKINGS =================
    @GetMapping("/booking")
    public String hostBookings(HttpServletRequest request, Model model) {
        String redirect = checkHostAccess(request);
        if (redirect != null) return redirect;

        User host = authentication.authenticate(request);
        // ✅ Only fetch active events
        List<Event> hostEvents = eventService.getActiveEventsByHost(host);

        for (Event event : hostEvents) {
            List<Booking> booking = bookingService.getBookingsByEvent(event.getId());
            event.setBookings(booking);
        }

        model.addAttribute("events", hostEvents);
        return "host/booking";
    }


    // ================= PENDING =================
    @GetMapping("/pending")
    public String pending() {
        return "host/pending";
    }
}
