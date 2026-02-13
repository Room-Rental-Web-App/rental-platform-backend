package com.web.room.service;

import com.web.room.model.Subscription;
import com.web.room.repository.SubscriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class SubscriptionScheduler {

    @Autowired
    private SubscriptionRepository subscriptionRepo;

    @Autowired
    private EmailService emailService;

    // Har raat 12 baje chalega
    @Scheduled(cron = "0 0 0 * * *")
    public void handleExpiredSubscriptions() {
        List<Subscription> expired = subscriptionRepo.findByActiveTrueAndEndDateBefore(LocalDateTime.now());

        for (Subscription s : expired) {
            s.setActive(false);
            subscriptionRepo.save(s);


            emailService.sendEmailWithInvoice(s.getEmail(), "Plan Expired", "Your premium plan has expired. Renew now to continue services.", null);
        }
    }
}