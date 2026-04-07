package com.example.demo.event;

import java.time.LocalDateTime;
import java.util.List;

import com.example.demo.user.User;

import jakarta.persistence.*;

@Entity
@Table(name = "event_tbl")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;
    private String description;
    private String location;

    private LocalDateTime eventStartDateTime;
    private LocalDateTime eventEndDateTime;

    private int guestCapacity;
    private Double ticketPrice;

    private LocalDateTime regStartDateTime;
    private LocalDateTime regEndDateTime;

    private String status;

    @ManyToOne
    @JoinColumn(name = "host_id")
    private User host;

    @Column(nullable = false)
    private String venue;

    @Column(name = "image_url")
    private String imageUrl;
    
    @Column(nullable = false)
    private boolean isDeleted = false;


    public boolean isDeleted() {
		return isDeleted;
	}
	public void setDeleted(boolean isDeleted) {
		this.isDeleted = isDeleted;
	}
	// 🔥 Transient field to store bookings for Thymeleaf
    @Transient
    private List<com.example.demo.booking.Booking> bookings;

    // Getters and Setters

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public LocalDateTime getEventStartDateTime() { return eventStartDateTime; }
    public void setEventStartDateTime(LocalDateTime eventStartDateTime) { this.eventStartDateTime = eventStartDateTime; }

    public LocalDateTime getEventEndDateTime() { return eventEndDateTime; }
    public void setEventEndDateTime(LocalDateTime eventEndDateTime) { this.eventEndDateTime = eventEndDateTime; }

    public int getGuestCapacity() { return guestCapacity; }
    public void setGuestCapacity(int guestCapacity) { this.guestCapacity = guestCapacity; }

    public Double getTicketPrice() { return ticketPrice; }
    public void setTicketPrice(Double ticketPrice) { this.ticketPrice = ticketPrice; }

    public LocalDateTime getRegStartDateTime() { return regStartDateTime; }
    public void setRegStartDateTime(LocalDateTime regStartDateTime) { this.regStartDateTime = regStartDateTime; }

    public LocalDateTime getRegEndDateTime() { return regEndDateTime; }
    public void setRegEndDateTime(LocalDateTime regEndDateTime) { this.regEndDateTime = regEndDateTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public User getHost() { return host; }
    public void setHost(User host) { this.host = host; }

    public String getVenue() { return venue; }
    public void setVenue(String venue) { this.venue = venue; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public List<com.example.demo.booking.Booking> getBookings() { return bookings; }
    public void setBookings(List<com.example.demo.booking.Booking> bookings) { this.bookings = bookings; }
}
