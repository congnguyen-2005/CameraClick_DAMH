package com.cameraclick.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendOrderConfirmation(String toEmail, Long orderId, String receiverName, String amount) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("Xác nhận đơn hàng #" + orderId + " - CameraClick");
            message.setText(
                    "Xin chào " + receiverName + ",\n\n" +
                    "Cảm ơn bạn đã đặt hàng tại CameraClick.\n" +
                    "Mã đơn hàng: #" + orderId + "\n" +
                    "Tổng tiền: " + amount + " VNĐ\n\n" +
                    "Chúng tôi sẽ xử lý và giao hàng trong thời gian sớm nhất.\n\n" +
                    "Trân trọng,\nCameraClick Team"
            );
            mailSender.send(message);
            log.info("Order confirmation email sent to {} for order #{}", toEmail, orderId);
        } catch (Exception e) {
            // Do not fail the notification flow if the mail server (e.g. SMTP) is not configured
            log.warn("Could not send email for order #{}: {}", orderId, e.getMessage());
        }
    }
}
