package com.example.demo.booking;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.user.User;

public interface BookingRepository extends JpaRepository<Booking, Integer> {

    // Customer bookings
    List<Booking> findByCustomer(User customer);

    // Host bookings
    List<Booking> findByEvent_Host(User host);

    List<Booking> findByEventId(Integer eventId);

    // Total tickets sold for an event
    @Query("SELECT COALESCE(SUM(b.ticketCount),0) FROM Booking b WHERE b.event.id = :eventId")
    int totalBookedTickets(@Param("eventId") Integer eventId);

    // Total tickets sold overall
    @Query("SELECT COALESCE(SUM(b.ticketCount),0) FROM Booking b")
    Integer getTotalTicketsSold();

    // Total revenue (only paid bookings)
    @Query("SELECT COALESCE(SUM(b.totalAmount),0) FROM Booking b WHERE b.paymentStatus='Paid'")
    Double getTotalRevenue();

    // Count bookings by payment status
    Integer countByPaymentStatus(String paymentStatus);

    // =================== Admin Reports Methods ===================

    // 1️⃣ Top events by tickets sold
    @Query("SELECT b.event.id AS eventId, " +
           "b.event.name AS name, " +
           "SUM(b.ticketCount) AS ticketsSold, " +
           "SUM(b.totalAmount) AS revenue " +
           "FROM Booking b " +
           "GROUP BY b.event.id, b.event.name " +
           "ORDER BY SUM(b.ticketCount) DESC")
    List<Map<String, Object>> findTopEvents();



    // 2️⃣ Top customers by bookings count
    @Query("SELECT b.customer.id AS customerId, b.customer.firstName AS name, COUNT(b.id) AS bookings, SUM(b.totalAmount) AS spent " +
           "FROM Booking b GROUP BY b.customer.id, b.customer.firstName ORDER BY COUNT(b.id) DESC")
    List<Map<String, Object>> findTopCustomers();

    // 3️⃣ Daily revenue for last 7 days
    @Query("SELECT FUNCTION('DATE', b.bookingDateTime) AS bookingDate, SUM(b.totalAmount) AS revenue " +
           "FROM Booking b WHERE b.bookingDateTime >= :startDate AND b.paymentStatus='Paid' " +
           "GROUP BY FUNCTION('DATE', b.bookingDateTime) ORDER BY bookingDate ASC")
    List<Map<String, Object>> getDailyRevenue(@Param("startDate") LocalDateTime startDate);

    // 4️⃣ Booking counts by status
    @Query("SELECT b.paymentStatus AS status, COUNT(b.id) AS count FROM Booking b GROUP BY b.paymentStatus")
    List<Map<String, Object>> getBookingStatusCounts();
}
