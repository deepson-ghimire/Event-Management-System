package com.example.demo.user;

import java.util.List;
import com.example.demo.user.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Service
public class UserService {

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Autowired
    private UserRepository userRepository;

    @Value("${EVENT_MGMS_ADMIN_PASSWORD}")
    private String password;

    // ================= ADMIN AUTO-CREATION =================
    @PostConstruct
    public void createAdminUserIfNotExist() {
        if (userRepository.count() == 0) {
            User admin = new User();
            admin.setFirstName("Admin");
            admin.setEmail("admin@gmail.com");
            admin.setUsername("admin@gmail.com");
            admin.setPassword(passwordEncoder.encode(password));
            admin.setGender(Gender.MALE);
            admin.setType(UserType.ADMIN);
            userRepository.save(admin);
        }
    }

    // ================= COMMON =================
//    public User findByEmail(String email) {
//        return userRepository.findByEmail(email)
//                .orElseThrow(() -> new RuntimeException("User not found"));
//    }

    public User findById(Integer id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // ================= ADMIN =================
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public void deleteUser(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.isDeleted()) {
            return; // already deleted
        }

        user.setDeleted(true);
        user.setSession(null); // 🔥 logout user immediately
        userRepository.save(user);
    }


    
    public User getUserById(Integer id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    
    public List<User> getUsersByType(UserType type) {
        return userRepository.findByTypeAndIsDeletedFalse(type);
    }




    // ================= HOST APPROVAL =================
    public User approveHost(Integer userId) {
        User host = findById(userId);
        host.setApproved(true);
        return userRepository.save(host);
    }

    public User rejectHost(Integer userId) {
        User host = findById(userId);
        host.setApproved(false);
        return userRepository.save(host);
    }

    // ================= REGISTRATION =================
    public User registerUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setApproved(user.getType() != UserType.HOST); // hosts need approval
        return userRepository.save(user);
    }
    
 // ================= REPORT COUNTS =================
    public Integer countAll() {
        return userRepository.countByIsDeletedFalse();
    }

    public Integer countByType(UserType type) {
        return userRepository.countByTypeAndIsDeletedFalse(type);
    }

}
