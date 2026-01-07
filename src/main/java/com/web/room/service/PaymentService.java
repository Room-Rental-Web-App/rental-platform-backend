package com.web.room.service;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.Utils;
import com.web.room.model.Subscription;
import com.web.room.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;


@RequiredArgsConstructor
@Service
public class PaymentService {

    @Value("${razorpay.key_id}")
    private String razorpayKeyId;

    @Value("${razorpay.key_secret}")
    private String razorpayKeySecret;


    private final SubscriptionRepository subscriptionRepo;


    public ResponseEntity<?> createOrder(Map<String, String> request) {
        double amountToPay = Double.parseDouble (request.get ("amountToPay"));
        String currency = request.get ("currency");
        System.out.println ("Creating Razorpay order for amount: " + amountToPay + " and currency: " + currency
        );
        try {
            RazorpayClient client = new RazorpayClient (razorpayKeyId, razorpayKeySecret);
            JSONObject options = new JSONObject ();
            options.put ("receipt", "order_rcptid_" + System.currentTimeMillis ());
            options.put ("amount", amountToPay * 100);
            options.put ("currency", currency);
            Order order = client.orders.create (options);
            String orderId = order.get ("id");

            return ResponseEntity.ok (Map.of (
                    "orderId", orderId,
                    "razorpayKey", razorpayKeyId
            ));
        } catch (Exception e) {
            e.printStackTrace ();
            return ResponseEntity.badRequest ().body ("Error creating Razorpay order");
        }
    }

    public ResponseEntity<?> varifyPayment(Map<String,String> req) {

        System.out.println ("Verifying payment for order: " + req.get("razorpayOrderId") + " and payment: " + req.get("razorpayPaymentId")
        );

        System.out.println("Server time: " + LocalDateTime.now());

        try {
            JSONObject attrs = new JSONObject();
            attrs.put("razorpay_payment_id", req.get("razorpayPaymentId"));
            attrs.put("razorpay_order_id", req.get("razorpayOrderId"));
            attrs.put("razorpay_signature", req.get("razorpaySignature"));
            Utils.verifyPaymentSignature(attrs, razorpayKeySecret);

            String email = req.get("email");
            String role  = req.get("role");
            int amount   = Integer.parseInt(req.get("amountToPay"));

            Subscription s = new Subscription();
            s.setEmail(email);
            s.setRole(role);
            s.setStartDate(LocalDateTime.now());
            s.setActive(true);

            if(role.equals("ROLE_USER") && amount == 99) {
                s.setPlanCode("USER_BASIC");
                s.setEndDate(LocalDateTime.now().plusDays(30));
            }
            else if(role.equals("ROLE_OWNER") && amount == 199) {
                s.setPlanCode("OWNER_TRIAL");
                s.setEndDate(LocalDateTime.now().plusDays(7));
            }
            else if(role.equals("ROLE_OWNER") && amount == 599) {
                s.setPlanCode("OWNER_FEATURED");
                s.setEndDate(LocalDateTime.now().plusDays(30));
            }

            subscriptionRepo.save(s);
            return ResponseEntity.ok("Subscription Activated");

        } catch(Exception e) {
            return ResponseEntity.badRequest().body("Invalid payment");
        }
    }
}
