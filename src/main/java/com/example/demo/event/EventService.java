package com.example.demo.event;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.user.User;

@Service
public class EventService {

    @Autowired
    private EventRepository eventRepository;

    // Add event by host
    public Event addEvent(Event event, User host) {
        event.setHost(host);
        event.setDeleted(false); // ✅ default is active
        return eventRepository.save(event);
    }

    // Update event
    public Event updateEvent(Event event) {
        return eventRepository.save(event);
    }

    // Get events by host (all)
    public List<Event> getEventsByHost(User host) {
        return eventRepository.findByHost(host);
    }

    // Get only active events for host
    public List<Event> getActiveEventsByHost(User host) {
        return eventRepository.findByHostAndIsDeletedFalse(host);
    }

    // Get active events for customers
    public List<Event> getActiveEvents() {
        return eventRepository.findByIsDeletedFalse();
    }

    // Soft delete event
    public void softDeleteEvent(Integer eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        event.setDeleted(true);
        eventRepository.save(event);
    }

    // Get event by ID
    public Event getEventById(Integer eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));
    }
}
