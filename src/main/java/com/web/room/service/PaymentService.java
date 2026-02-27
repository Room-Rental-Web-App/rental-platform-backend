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
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
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
    private final EmailService emailService;     // Inject Email Service
    private final InvoiceService invoiceService; // Inject Invoice Service

    private static final Map<Integer, Integer> OWNER_PLANS = Map.of(
            1, 7, 49, 30, 149, 180, 999, 365
    );

    private static final Map<Integer, Integer> USER_PLANS = Map.of(
            1, 30, 29, 180, 199, 365
    );

    public ResponseEntity<?> createOrder(Map<String, String> request) {
        try {
            double amountToPay = Double.parseDouble(request.get("amountToPay"));
            String currency = request.get("currency");
            RazorpayClient client = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
            JSONObject options = new JSONObject();
            options.put("receipt", "order_rcptid_" + System.currentTimeMillis());
            options.put("amount", (int) (amountToPay * 100));
            options.put("currency", currency);
            Order order = client.orders.create(options);
            return ResponseEntity.ok(Map.of("orderId", order.get("id"), "razorpayKey", razorpayKeyId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error creating Razorpay order");
        }
    }

    public ResponseEntity<?> varifyPayment(Map<String, String> req) {
        try {
            // 1. Signature Verification
            JSONObject attrs = new JSONObject();
            attrs.put("razorpay_payment_id", req.get("razorpayPaymentId"));
            attrs.put("razorpay_order_id", req.get("razorpayOrderId"));
            attrs.put("razorpay_signature", req.get("razorpaySignature"));
            Utils.verifyPaymentSignature(attrs, razorpayKeySecret);

            String email = req.get("email");
            String role = req.get("role");
            double amount = Double.parseDouble(req.get("amountToPay"));

            // 2. Logic for Extension
            Optional<Subscription> old = subscriptionRepo.findTopByEmailAndRoleAndActiveTrueAndEndDateAfterOrderByEndDateDesc(email, role, LocalDateTime.now());
            LocalDateTime base = (old.isPresent()) ? old.get().getEndDate() : LocalDateTime.now();

            Integer days = role.equals("ROLE_OWNER") ? OWNER_PLANS.get((int) amount) : USER_PLANS.get((int) amount);
            if (days == null) return ResponseEntity.badRequest().body("Invalid plan amount");

            // 3. Save Subscription
            Subscription s = new Subscription();
            s.setEmail(email);
            s.setRole(role);
            s.setAmountPaid(amount);
            s.setRazorpayOrderId(req.get("razorpayOrderId"));
            s.setRazorpayPaymentId(req.get("razorpayPaymentId"));
            s.setPlanCode(role + "_" + days + "D");
            s.setStartDate(base);
            s.setEndDate(base.plusDays(days));
            s.setActive(true);
            subscriptionRepo.save(s);

            // 4. GENERATE INVOICE & SEND EMAIL (New Logic)
            try {
                Map<String, Object> invoiceData = new HashMap<>();
                invoiceData.put("orderId", s.getRazorpayOrderId());
                invoiceData.put("date", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
                invoiceData.put("planName", s.getPlanCode());
                invoiceData.put("amount", s.getAmountPaid());
                invoiceData.put("startDate", s.getStartDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
                invoiceData.put("endDate", s.getEndDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));

                byte[] pdfInvoice = invoiceService.generateInvoicePdf(invoiceData);

                String subject = "Premium Activated - RoomsDekho";
                String body = "Hi, Your premium subscription is now active until " + invoiceData.get("endDate") + ". Please find your invoice attached.";

                emailService.sendEmailWithInvoice(email, subject, body, pdfInvoice);
            } catch (Exception mailError) {
                System.out.println("Payment Success, but Email Failed: " + mailError.getMessage());
            }

            return ResponseEntity.ok(Map.of("message", "Subscription activated and invoice sent!"));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Verification failed");
        }
    }
}