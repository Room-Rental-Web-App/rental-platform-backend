package com.web.room.service;

import com.web.room.model.Subscription;
import com.web.room.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class PaymentService {

    @Value("${cashfree.app_id}")
    private String appId;

    @Value("${cashfree.secret_key}")
    private String secretKey;

    @Value("${cashfree.env}")
    private String env;

    private final SubscriptionRepository subscriptionRepo;
    private final EmailService emailService;
    private final InvoiceService invoiceService;
    private final RestTemplate restTemplate = new RestTemplate();

    private String getBaseUrl() {
        return "SANDBOX".equalsIgnoreCase(env)
                ? "https://sandbox.cashfree.com/pg/orders"
                : "https://api.cashfree.com/pg/orders";
    }

    private static final Map<Integer, Integer> OWNER_PLANS =
            Map.of(1, 7, 49, 30, 149, 180, 999, 365);

    private static final Map<Integer, Integer> USER_PLANS =
            Map.of(1, 30, 29, 180, 199, 365);

    /**
     * ✅ STEP 1: CREATE ORDER
     */
    public ResponseEntity<?> createOrder(Map<String, String> request) {
        try {
            double amountToPay = Double.parseDouble(request.get("amountToPay"));
            String customerEmail = request.get("email");
            String customerPhone = request.getOrDefault("phone", "9999999999");

            JSONObject orderRequest = new JSONObject();
            orderRequest.put("order_id", "ORD_" + System.currentTimeMillis());
            orderRequest.put("order_amount", amountToPay);
            orderRequest.put("order_currency", "INR");

            // 🔥 FIX: Dynamic return_url (IMPORTANT)
            JSONObject orderMeta = new JSONObject();

            if ("SANDBOX".equalsIgnoreCase(env)) {
                orderMeta.put("return_url",
                        "http://localhost:3000/payment-status?order_id={order_id}");
            } else {
                orderMeta.put("return_url",
                        "https://www.roomsdekho.in/payment-status?order_id={order_id}");
            }

            orderRequest.put("order_meta", orderMeta);

            JSONObject customerDetails = new JSONObject();
            customerDetails.put("customer_id",
                    customerEmail.replaceAll("[^a-zA-Z0-9]", ""));
            customerDetails.put("customer_email", customerEmail);
            customerDetails.put("customer_phone", customerPhone);

            orderRequest.put("customer_details", customerDetails);

            HttpHeaders headers = new HttpHeaders();
            headers.set("x-api-version", "2023-08-01");
            headers.set("x-client-id", appId.trim());
            headers.set("x-client-secret", secretKey.trim());
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity =
                    new HttpEntity<>(orderRequest.toString(), headers);

            ResponseEntity<Map> response =
                    restTemplate.postForEntity(getBaseUrl(), entity, Map.class);

            Map<String, Object> body = response.getBody();

            return ResponseEntity.ok(Map.of(
                    "orderId", body.get("order_id"),
                    "paymentSessionId", body.get("payment_session_id")
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Cashfree Order Creation Failed: " + e.getMessage());
        }
    }

    /**
     * ✅ STEP 2: VERIFY PAYMENT
     */
    public ResponseEntity<?> varifyPayment(Map<String, String> req) {
        try {
            String orderId = req.get("orderId");

            HttpHeaders headers = new HttpHeaders();
            headers.set("x-api-version", "2023-08-01");
            headers.set("x-client-id", appId.trim());
            headers.set("x-client-secret", secretKey.trim());

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    getBaseUrl() + "/" + orderId,
                    HttpMethod.GET,
                    entity,
                    Map.class
            );

            Map<String, Object> body = response.getBody();
            String status = (String) body.get("order_status");

            if (!"PAID".equalsIgnoreCase(status)) {
                return ResponseEntity.badRequest()
                        .body("Payment failed or pending: " + status);
            }

            // ✅ Subscription logic
            String email = req.get("email");
            String role = req.get("role");
            double amount = Double.parseDouble(req.get("amountToPay"));

            Optional<Subscription> old =
                    subscriptionRepo.findTopByEmailAndRoleAndActiveTrueAndEndDateAfterOrderByEndDateDesc(
                            email, role, LocalDateTime.now());

            LocalDateTime base =
                    old.map(Subscription::getEndDate).orElse(LocalDateTime.now());

            Integer days = role.equals("ROLE_OWNER")
                    ? OWNER_PLANS.get((int) amount)
                    : USER_PLANS.get((int) amount);

            Subscription s = new Subscription();
            s.setEmail(email);
            s.setRole(role);
            s.setAmountPaid(amount);
            s.setCashfreeOrderId(orderId);
            s.setPlanCode(role + "_" + days + "D");
            s.setStartDate(base);
            s.setEndDate(base.plusDays(days));
            s.setActive(true);

            subscriptionRepo.save(s);

            try {
                generateAndSendInvoice(s);
            } catch (Exception ex) {
                System.out.println("Invoice error: " + ex.getMessage());
            }

            return ResponseEntity.ok(Map.of("message", "Success"));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Verification Error: " + e.getMessage());
        }
    }

    private void generateAndSendInvoice(Subscription s) throws Exception {
        Map<String, Object> invoiceData = new HashMap<>();

        invoiceData.put("orderId", s.getCashfreeOrderId());
        invoiceData.put("date",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
        invoiceData.put("planName", s.getPlanCode());
        invoiceData.put("amount", s.getAmountPaid());
        invoiceData.put("startDate",
                s.getStartDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
        invoiceData.put("endDate",
                s.getEndDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));

        byte[] pdf = invoiceService.generateInvoicePdf(invoiceData);

        emailService.sendEmailWithInvoice(
                s.getEmail(),
                "Premium Activated - RoomsDekho",
                "Your subscription is active till " + invoiceData.get("endDate"),
                pdf
        );
    }
}