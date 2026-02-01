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
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class PaymentService {

    @Value("${razorpay.key_id}")
    private String razorpayKeyId;

    @Value("${razorpay.key_secret}")
    private String razorpayKeySecret;

    private final SubscriptionRepository subscriptionRepo;

    /**
     * Updated Maps to reflect the new Pricing and specific Duration.
     * Note: Room counts are handled in the SubscriptionService and RoomService
     * based on these Durations.
     */
    private static final Map<Integer, Integer> OWNER_PLANS = Map.of(
            99, 7,      // Trial (Reduced Rooms)
            199, 30,    // 1 Month
            999, 180,   // 6 Months
            1499, 365   // 1 Year
    );

    private static final Map<Integer, Integer> USER_PLANS = Map.of(
            99, 30,
            499, 180,
            899, 365
    );

    public ResponseEntity<?> createOrder(Map<String, String> request) {
        try {
            double amountToPay = Double.parseDouble(request.get("amountToPay"));
            String currency = request.get("currency");

            RazorpayClient client = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
            JSONObject options = new JSONObject();
            options.put("receipt", "order_rcptid_" + System.currentTimeMillis());
            options.put("amount", (int) (amountToPay * 100)); // Razorpay expects Paisa
            options.put("currency", currency);

            Order order = client.orders.create(options);

            return ResponseEntity.ok(Map.of(
                    "orderId", order.get("id"),
                    "razorpayKey", razorpayKeyId
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error creating Razorpay order");
        }
    }

    public ResponseEntity<?> varifyPayment(Map<String, String> req) {
        try {
            // 1. Signature Verification (Security First)
            JSONObject attrs = new JSONObject();
            attrs.put("razorpay_payment_id", req.get("razorpayPaymentId"));
            attrs.put("razorpay_order_id", req.get("razorpayOrderId"));
            attrs.put("razorpay_signature", req.get("razorpaySignature"));
            Utils.verifyPaymentSignature(attrs, razorpayKeySecret);

            String email = req.get("email");
            String role = req.get("role");
            double amount = Double.parseDouble(req.get("amountToPay"));

            // 2. Fetch Existing Subscription for Extension Logic
            Optional<Subscription> old = subscriptionRepo
                    .findTopByEmailAndRoleAndActiveTrueAndEndDateAfterOrderByEndDateDesc(email, role, LocalDateTime.now());

            LocalDateTime base = (old.isPresent() && old.get().getEndDate().isAfter(LocalDateTime.now()))
                    ? old.get().getEndDate()
                    : LocalDateTime.now();

            // 3. Match Duration based on Price
            Integer days = role.equals("ROLE_OWNER")
                    ? OWNER_PLANS.get((int) amount)
                    : USER_PLANS.get((int) amount);

            if (days == null) {
                return ResponseEntity.badRequest().body("Invalid plan amount");
            }

            // 4. Create and Save Subscription Object
            Subscription s = new Subscription();
            s.setEmail(email);
            s.setRole(role);
            s.setAmountPaid(amount); // Setting this fixes the DB null constraint
            s.setRazorpayOrderId(req.get("razorpayOrderId"));
            s.setRazorpayPaymentId(req.get("razorpayPaymentId"));
            s.setPlanCode(role + "_" + days + "D");
            s.setStartDate(base);
            s.setEndDate(base.plusDays(days));
            s.setActive(true);

            subscriptionRepo.save(s);

            return ResponseEntity.ok(Map.of("message", "Subscription successfully activated"));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Invalid payment or verification error");
        }
    }
}