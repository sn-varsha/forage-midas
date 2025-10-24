package com.jpmc.midascore.controller;

import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.foundation.Balance;
import com.jpmc.midascore.repository.UserRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/balance")
public class BalanceController {

    @Autowired
    private UserRecordRepository userRecordRepository;

    @GetMapping
    // 1. Add (value = "userid", required = false)
    public Balance getBalance(@RequestParam(value = "userid", required = false) Long userId) {

        // 2. Add this check for a missing parameter
        if (userId == null) {
            return new Balance(0.0f); // Return 0 if no userid is provided
        }

        // This is your existing logic, which is perfect
        Optional<UserRecord> userOptional = userRecordRepository.findById(userId);

        if (userOptional.isPresent()) {
            UserRecord user = userOptional.get();
            return new Balance(user.getBalance());
        } else {
            return new Balance(0.0f);
        }
    }
}