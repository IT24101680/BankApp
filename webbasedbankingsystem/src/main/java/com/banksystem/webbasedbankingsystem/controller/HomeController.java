package com.banksystem.webbasedbankingsystem.controller;

import com.banksystem.webbasedbankingsystem.dto.CustomerRegistrationDTO;
import com.banksystem.webbasedbankingsystem.dto.LoginDTO;
import com.banksystem.webbasedbankingsystem.entity.User;
import com.banksystem.webbasedbankingsystem.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(originPatterns = "*", allowCredentials = "true")
public class HomeController {

    @Autowired
    private UserService userService;

    @GetMapping("/user/info")
    public ResponseEntity<Map<String, Object>> getUserInfo(HttpSession session) {
        User user = (User) session.getAttribute("user");
        Map<String, Object> response = new HashMap<>();

        if (user != null) {
            response.put("authenticated", true);
            response.put("userId", user.getUserId());
            response.put("username", user.getUsername());
            response.put("firstName", user.getFirstName());
            response.put("lastName", user.getLastName());
            response.put("email", user.getEmail());
            response.put("role", user.getRole());
            response.put("accountNumber", user.getAccountNumber());
            response.put("accountBalance", user.getAccountBalance());
            response.put("redirectUrl", getRedirectUrl(user.getRole()));
            return ResponseEntity.ok(response);
        }

        response.put("authenticated", false);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @Valid @RequestBody LoginDTO loginDTO,
            BindingResult bindingResult,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        if (bindingResult.hasErrors()) {
            response.put("success", false);
            response.put("message", "Invalid input data");
            response.put("errors", getValidationErrors(bindingResult));
            return ResponseEntity.badRequest().body(response);
        }

        try {
            User user = userService.loginUser(loginDTO.getUsername(), loginDTO.getPassword());
            session.setAttribute("user", user);

            response.put("success", true);
            response.put("message", "Login successful");
            response.put("user", getUserBasicInfo(user));
            response.put("redirectUrl", getRedirectUrl(user.getRole()));

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(
            @Valid @RequestBody CustomerRegistrationDTO registrationDTO,
            BindingResult bindingResult) {

        Map<String, Object> response = new HashMap<>();

        if (bindingResult.hasErrors()) {
            response.put("success", false);
            response.put("message", "Invalid input data");
            response.put("errors", getValidationErrors(bindingResult));
            return ResponseEntity.badRequest().body(response);
        }

        if (!registrationDTO.getPassword().equals(registrationDTO.getConfirmPassword())) {
            response.put("success", false);
            response.put("message", "Passwords do not match");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            User user = new User();
            user.setFirstName(registrationDTO.getFirstName());
            user.setLastName(registrationDTO.getLastName());
            user.setEmail(registrationDTO.getEmail());
            user.setUsername(registrationDTO.getUsername());
            user.setPassword(registrationDTO.getPassword());
            user.setPhoneNumber(registrationDTO.getPhoneNumber());
            user.setAddress(registrationDTO.getAddress());
            user.setDateOfBirth(registrationDTO.getDateOfBirth());
            user.setRole("CUSTOMER");
            user.setAccountBalance(0.0);

            User savedUser = userService.registerUser(user);

            response.put("success", true);
            response.put("message", "Registration successful! Your account number is: " + savedUser.getAccountNumber());
            response.put("accountNumber", savedUser.getAccountNumber());
            response.put("redirectUrl", "/login.html");

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(HttpSession session) {
        session.invalidate();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Logged out successfully");
        response.put("redirectUrl", "/");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "online");
        response.put("message", "LakDerana Bank API is running");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/check-username")
    public ResponseEntity<Map<String, Object>> checkUsername(@RequestParam String username) {
        Map<String, Object> response = new HashMap<>();
        boolean available = userService.isUsernameAvailable(username);
        response.put("available", available);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/check-email")
    public ResponseEntity<Map<String, Object>> checkEmail(@RequestParam String email) {
        Map<String, Object> response = new HashMap<>();
        boolean available = userService.isEmailAvailable(email);
        response.put("available", available);
        return ResponseEntity.ok(response);
    }

    private String getRedirectUrl(String role) {
        switch (role) {
            case "ADMIN":
                return "/admin/dashboard.html";
            case "EMPLOYEE":
                return "/employee/dashboard.html";
            default:
                return "/customer/dashboard.html";
        }
    }

    private Map<String, Object> getUserBasicInfo(User user) {
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("userId", user.getUserId());
        userInfo.put("username", user.getUsername());
        userInfo.put("firstName", user.getFirstName());
        userInfo.put("lastName", user.getLastName());
        userInfo.put("email", user.getEmail());
        userInfo.put("role", user.getRole());
        userInfo.put("accountNumber", user.getAccountNumber());
        userInfo.put("accountBalance", user.getAccountBalance());
        return userInfo;
    }

    private Map<String, String> getValidationErrors(BindingResult bindingResult) {
        Map<String, String> errors = new HashMap<>();
        bindingResult.getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );
        return errors;
    }
}