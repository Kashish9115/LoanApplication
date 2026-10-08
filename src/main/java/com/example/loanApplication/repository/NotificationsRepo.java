package com.example.loanApplication.repository;

import com.example.loanApplication.entity.Notifications;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationsRepo extends JpaRepository<Notifications, Integer> {
}
