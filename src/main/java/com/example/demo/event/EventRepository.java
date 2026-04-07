package com.example.demo.event;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.demo.user.User;

public interface EventRepository extends JpaRepository<Event, Integer> {

    List<Event> findByHost(User host);

    List<Event> findByHostId(Integer hostId);

    List<Event> findByIsDeletedFalse();

    List<Event> findByHostAndIsDeletedFalse(User host);

    Integer countByIsDeletedFalse();

    Integer countByIsDeletedTrue();
}
