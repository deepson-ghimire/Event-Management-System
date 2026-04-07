package com.example.demo.user;

import java.util.List;

//import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Integer>{

	boolean existsByEmail(String email);

	User findByEmail(String email);
//	Optional<User> findByEmail(String email);

	User findBySession(String value);
	
	List<User> findByType(UserType type);
	 
	List<User> findByTypeAndIsDeletedFalse(UserType type);
	
	Integer countByTypeAndIsDeletedFalse(UserType type);

    Integer countByIsDeletedFalse();

} 