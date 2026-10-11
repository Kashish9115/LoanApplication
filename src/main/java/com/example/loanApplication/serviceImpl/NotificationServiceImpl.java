package com.example.loanApplication.serviceImpl;

import com.example.loanApplication.dto.NotificationResponseDto;
import com.example.loanApplication.entity.Customer;
import com.example.loanApplication.entity.Notifications;
import com.example.loanApplication.exception.ResourceNotFoundException;
import com.example.loanApplication.repository.CustomerRepository;
import com.example.loanApplication.repository.NotificationsRepo;
import com.example.loanApplication.service.EmailService;
import com.example.loanApplication.service.NotificationService;
import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class NotificationServiceImpl implements NotificationService {

    private  final CustomerRepository customerRepository;
    private  final NotificationsRepo notificationsRepo;
   private  final EmailService emailService;
   private  final ModelMapper modelMapper;
    @Override
    public NotificationResponseDto sendNotification(Integer CustomerId, String message, String emailSubject) {
   Customer customer= customerRepository.findById(CustomerId).orElseThrow(()->new RuntimeException("Customer not found"));

   Notifications notifications = new Notifications();
   notifications.setMessage(message);
   notifications.setCustomer(customer);
   notifications.setIsRead(false);
   notifications.setCreatedAt(LocalDateTime.now());
   if(customer.getEmail()!=null && !customer.getEmail().isEmpty()){
       emailService.sendEmail(customer.getEmail(), emailSubject, message);

   }
 Notifications   saved=  notificationsRepo.save(notifications);

  return   modelMapper.map(saved, NotificationResponseDto.class);
    }

    @Override
    public List<NotificationResponseDto> getCustomerNotifications(Integer customerId) {
        List<Notifications> dashboardAlerts=notificationsRepo.findByCustomer_CustomerIdOrderByCreatedAtDesc(customerId);


     return    dashboardAlerts.stream().map(alert->modelMapper.map(alert, NotificationResponseDto.class)).toList();





    }

    @Override
    public void markNotificationAsRead(Integer notificationId) {


        Notifications notification = notificationsRepo.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found for ID: " + notificationId));

        notification.setIsRead(true);
        notificationsRepo.save(notification);
    }


    }

//    @Override
//    public void sendEmiDueReminders() {
//
//    }



