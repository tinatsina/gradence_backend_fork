package com.gradence.ga.controller;

import com.gradence.ga.model.User;
import com.gradence.ga.repository.UserRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserRepository userRepo;

    public UserController(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    @PostMapping
    public User create(@RequestBody User user) {
        return userRepo.save(user);
    }

    @GetMapping
    public List<User> getAll() {
        return userRepo.findAll();
    }
}