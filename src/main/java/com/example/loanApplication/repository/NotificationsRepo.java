package com.example.loanApplication.repository;

import com.example.loanApplication.entity.Notifications;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationsRepo extends JpaRepository<Notifications, Integer> {




    List<Notifications> findByCustomer_CustomerIdAndIsReadFalseOrderByCreatedAtDesc(
            Integer customerId
    );

}
