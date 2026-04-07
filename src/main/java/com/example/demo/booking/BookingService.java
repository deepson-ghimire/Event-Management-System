package com.example.demo.booking;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.event.Event;
import com.example.demo.user.User;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    public int getAvailableSeats(Event event) {
        int capacity = event.getGuestCapacity();
        int booked = bookingRepository.totalBookedTickets(event.getId());
        return Math.max(capacity - booked, 0);
    }

    public Booking bookEvent(User customer, Event event, int ticketCount) {

        int available = getAvailableSeats(event);

        if (available <= 0) {
            throw new RuntimeException("No seats available for this event");
        }

        if (ticketCount > available) {
            throw new RuntimeException("Only " + available + " seats available");
        }

        Booking booking = new Booking();
        booking.setCustomer(customer);
        booking.setEvent(event);
        booking.setBookingDateTime(LocalDateTime.now());
        booking.setTicketCount(ticketCount);
        booking.setTotalAmount(event.getTicketPrice() * ticketCount);
        booking.setPaymentStatus("Pending");

        return bookingRepository.save(booking);
    }

    public List<Booking> getBookingsByCustomer(User customer) {
        return bookingRepository.findByCustomer(customer);
    }

    public List<Booking> getBookingsForHost(User host) {
        return bookingRepository.findByEvent_Host(host);
    }

    public List<Booking> getBookingsByEvent(Integer eventId) {
        return bookingRepository.findByEventId(eventId);
    }
}

