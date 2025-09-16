package com.pdnt.restaurant.controller;

import com.pdnt.restaurant.config.VnpayConfig;
import com.pdnt.restaurant.dto.response.PaymentResponse;
import com.pdnt.restaurant.entity.OrderGroup;
import com.pdnt.restaurant.repository.OrderGroupRepository;
import com.pdnt.restaurant.service.CheckoutService;
import com.pdnt.restaurant.service.OrderService;
import com.pdnt.restaurant.service.vnpay.VnpayService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {
    private final OrderGroupRepository orderGroupRepository;

    @GetMapping("/create-payment")
    public ResponseEntity<?> createPayment(@RequestParam("orderId") Long orderId) throws UnsupportedEncodingException {
        // Lấy orderGroup từ DB
        OrderGroup orderGroup = orderGroupRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // totalAmount thường là kiểu double => nhân với 100 để gửi cho VNPAY
        long amount = (long) (orderGroup.getTotalAmount() * 100);

        String vnp_TxnRef = VnpayConfig.getRandomNumber(8);
        String vnp_TmnCode = VnpayConfig.vnp_TmnCode;

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", VnpayConfig.vnp_Version);
        vnp_Params.put("vnp_Command", VnpayConfig.vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_BankCode", "NCB");
        vnp_Params.put("vnp_OrderType", "other");
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang:" + orderGroup.getId());
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_IpAddr", "13.160.92.202");
        vnp_Params.put("vnp_ReturnUrl", "http://localhost:8080/payment/return");

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        for (Iterator<String> itr = fieldNames.iterator(); itr.hasNext();) {
            String fieldName = itr.next();
            String fieldValue = vnp_Params.get(fieldName);
            if (fieldValue != null && fieldValue.length() > 0) {
                hashData.append(fieldName).append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString())).append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }

        String vnp_SecureHash = VnpayConfig.hmacSHA512(VnpayConfig.secretKey, hashData.toString());
        String queryUrl = query.toString() + "&vnp_SecureHash=" + vnp_SecureHash;
        String paymentUrl = VnpayConfig.vnp_PayUrl + "?" + queryUrl;

        PaymentResponse response = new PaymentResponse();
        response.setStatus("OK");
        response.setMassage("Tạo URL thanh toán thành công");
        response.setUrl(paymentUrl);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    @GetMapping("/return")
    public void paymentReturn(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Map<String, String[]> params = request.getParameterMap();

        // Lấy các tham số VNPAY trả về
        String vnp_ResponseCode = request.getParameter("vnp_ResponseCode");
        String vnp_TxnRef = request.getParameter("vnp_TxnRef");
        String vnp_Amount = request.getParameter("vnp_Amount");
        String vnp_OrderInfo = request.getParameter("vnp_OrderInfo");

        String status = "FAILED";
        String orderId = vnp_OrderInfo.replace("Thanh toan don hang:", "");
        if ("00".equals(vnp_ResponseCode)) {
            status = "SUCCESS";
        }

        // Redirect sang front-end kèm query string
        String redirectUrl = "http://localhost:5173/payment-result"
                + "?status=" + status
                + "&orderId=" + orderId
                + "&amount=" + vnp_Amount;

        response.sendRedirect(redirectUrl);
    }

}