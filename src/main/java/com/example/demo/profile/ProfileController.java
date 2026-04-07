package com.example.demo.profile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.demo.auth.Authentication;
import com.example.demo.storage.StorageService;
import com.example.demo.user.User;
import com.example.demo.user.UserRepository;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class ProfileController {

	@Autowired
	private Authentication authentication;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private StorageService storageService;

	@GetMapping("/profile")
	public String getProfilePage(HttpServletRequest request, Model model) {
		// get user from cookie
		// user verification
		// user identification
		User user = authentication.authenticate(request);

		if (user == null) {
			return "redirect:/login";
		}

		model.addAttribute("user", user);
		return "profile.html";
	}

	@GetMapping("/profile/edit")
	public String getProfileEditPage(HttpServletRequest request, Model model) {
		// show profile edit page
		User user = authentication.authenticate(request);
		if (user == null) {
			// TODO: show Not Authorized message
			return "redirect:/login";
		}

		model.addAttribute("user", user);
		return "profile-edit.html";
	}

	@PostMapping("/profile/edit")
	public String updateProfile(ProfileEditForm form, HttpServletRequest request) throws IOException {
		// update profile info
		User user = authentication.authenticate(request);
		if (user == null) {
			// TODO: show Not Authorized message
			return "redirect:/login";
		}

		System.out.println("Address: " + form.getAddress());
		System.out.println("Image: " + form.getProfile().getOriginalFilename());

		// update user's address
		user.setAddress(form.getAddress());

		String oldProfilePicture = user.getProfilePicture();

		// save user's profile picture
		String fileName = storageService.store(form.getProfile());
		user.setProfilePicture(fileName);

		userRepository.save(user);

		// delete old profile picture file if exists
		try {
			Files.delete(Paths.get(StorageService.DIRECTORY, oldProfilePicture));
		} catch (Exception e) {
		}

		return "redirect:/profile";
	}

}