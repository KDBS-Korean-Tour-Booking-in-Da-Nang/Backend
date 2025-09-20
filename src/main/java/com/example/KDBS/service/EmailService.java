package com.example.KDBS.service;

import com.example.KDBS.model.Booking;
import com.example.KDBS.model.BookingGuest;
import com.example.KDBS.model.Tour;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.core.io.ClassPathResource;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    /**
     * Gửi email xác nhận booking thành công
     */
    public void sendBookingConfirmationEmail(Booking booking, Tour tour) {
        try {
            String emailContent = buildBookingConfirmationEmail(booking, tour);
            String subject = "Xác nhận đặt tour thành công - " + tour.getTourName();

            sendEmail(booking.getContactEmail(), subject, emailContent);
            log.info("Booking confirmation email sent successfully to: {}", booking.getContactEmail());

        } catch (Exception e) {
            log.error("Failed to send booking confirmation email to: {}", booking.getContactEmail(), e);
            throw new RuntimeException("Failed to send booking confirmation email", e);
        }
    }

    /**
     * Xây dựng nội dung email booking confirmation
     */
    private String buildBookingConfirmationEmail(Booking booking, Tour tour) throws IOException {
        // Đọc template HTML
        ClassPathResource resource = new ClassPathResource("templates/email/booking-confirmation.html");
        String template = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

        // Format ngày tháng
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.forLanguageTag("vi"));
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.forLanguageTag("vi"));

        // Thay thế các placeholder
        String emailContent = template
                .replace("{bookingId}", booking.getBookingId().toString())
                .replace("{createdAt}", booking.getCreatedAt().format(dateTimeFormatter))
                .replace("{departureDate}", booking.getDepartureDate().format(dateFormatter))
                .replace("{pickupPoint}", booking.getPickupPoint() != null ? booking.getPickupPoint() : "Chưa xác định")
                .replace("{note}", booking.getNote() != null ? booking.getNote() : "Không có ghi chú")
                .replace("{tourName}", tour.getTourName())
                .replace("{tourImage}", tour.getTourImgPath() != null ? tour.getTourImgPath() : "")
                .replace("{tourDescription}", tour.getTourDescription() != null ? tour.getTourDescription() : "Không có mô tả")
                .replace("{tourDuration}", tour.getTourDuration() != null ? tour.getTourDuration() : "Chưa xác định")
                .replace("{tourDestination}", tour.getTourDeparturePoint() != null ? tour.getTourDeparturePoint() : "Chưa xác định")
                .replace("{guestsList}", buildGuestsListHtml(booking.getGuests()))
                .replace("{adultsCount}", booking.getAdultsCount().toString())
                .replace("{childrenCount}", booking.getChildrenCount().toString())
                .replace("{babiesCount}", booking.getBabiesCount().toString())
                .replace("{adultsPrice}", formatPrice(booking.getAdultsCount() * tour.getAdultPrice().longValue()))
                .replace("{childrenPrice}", formatPrice(booking.getChildrenCount() * tour.getChildrenPrice().longValue()))
                .replace("{babiesPrice}", formatPrice(booking.getBabiesCount() * tour.getBabyPrice().longValue()))
                .replace("{totalAmount}", formatPrice(calculateTotalAmount(booking, tour)))
                .replace("{contactName}", booking.getContactName())
                .replace("{contactEmail}", booking.getContactEmail())
                .replace("{contactPhone}", booking.getContactPhone())
                .replace("{contactAddress}", booking.getContactAddress() != null ? booking.getContactAddress() : "Chưa cung cấp");

        return emailContent;
    }

    /**
     * Xây dựng HTML cho danh sách hành khách
     */
    private String buildGuestsListHtml(List<BookingGuest> guests) {
        if (guests == null || guests.isEmpty()) {
            return "<p>Không có thông tin hành khách</p>";
        }

        StringBuilder guestsHtml = new StringBuilder();
        for (BookingGuest guest : guests) {
            guestsHtml.append("<div class=\"guest-card\">")
                    .append("<div class=\"guest-name\">").append(guest.getFullName()).append("</div>")
                    .append("<div class=\"guest-details\">")
                    .append("Ngày sinh: ").append(guest.getBirthDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                    .append(" | Giới tính: ").append(guest.getGender() != null ? guest.getGender().name() : "Chưa xác định")
                    .append(" | Loại: ").append(guest.getGuestType().name())
                    .append(" | CMND/CCCD: ").append(guest.getIdNumber() != null ? guest.getIdNumber() : "Chưa cung cấp")
                    .append(" | Quốc tịch: ").append(guest.getNationality() != null ? guest.getNationality() : "Chưa cung cấp")
                    .append("</div>")
                    .append("</div>");
        }

        return guestsHtml.toString();
    }

    /**
     * Tính tổng số tiền booking
     */
    private long calculateTotalAmount(Booking booking, Tour tour) {
        return (booking.getAdultsCount() * tour.getAdultPrice().longValue()) +
               (booking.getChildrenCount() * tour.getChildrenPrice().longValue()) +
               (booking.getBabiesCount() * tour.getBabyPrice().longValue());
    }

    /**
     * Format giá tiền theo định dạng VNĐ
     */
    private String formatPrice(long price) {
        return String.format("%,d", price);
    }

    /**
     * Gửi email
     */
    private void sendEmail(String to, String subject, String content) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(content, true); // true = HTML content

        mailSender.send(message);
    }

    /**
     * Gửi email thông báo đổi mật khẩu thành công
     * @param email Email người dùng
     * @param username Tên người dùng
     */
    public void sendPasswordResetSuccessEmail(String email, String username) {
        try {
            String subject = "Đổi mật khẩu thành công - KDBS";
            String content = buildPasswordResetSuccessEmail(username);
            
            sendEmail(email, subject, content);
            log.info("Password reset success email sent to: {}", email);

        } catch (Exception e) {
            log.error("Failed to send password reset success email to: {}", email, e);
            throw new RuntimeException("Failed to send password reset success email", e);
        }
    }

    /**
     * Xây dựng nội dung email thông báo đổi mật khẩu thành công
     */
    private String buildPasswordResetSuccessEmail(String username) {
        return String.format("""
            <!DOCTYPE html>
            <html lang="vi">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Đổi mật khẩu thành công</title>
                <style>
                    body {
                        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                        line-height: 1.6;
                        color: #333;
                        max-width: 600px;
                        margin: 0 auto;
                        padding: 20px;
                        background-color: #f4f4f4;
                    }
                    .container {
                        background-color: #ffffff;
                        border-radius: 10px;
                        box-shadow: 0 0 20px rgba(0,0,0,0.1);
                        overflow: hidden;
                    }
                    .header {
                        background: linear-gradient(135deg, #28a745 0%, #20c997 100%);
                        color: white;
                        padding: 30px;
                        text-align: center;
                    }
                    .header h1 {
                        margin: 0;
                        font-size: 28px;
                        font-weight: 300;
                    }
                    .content {
                        padding: 30px;
                    }
                    .success-icon {
                        font-size: 48px;
                        color: #28a745;
                        margin-bottom: 15px;
                    }
                    .footer {
                        background-color: #2c3e50;
                        color: white;
                        padding: 20px;
                        text-align: center;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <div class="success-icon">✓</div>
                        <h1>Đổi Mật Khẩu Thành Công!</h1>
                        <p>Tài khoản của bạn đã được bảo mật</p>
                    </div>
                    
                    <div class="content">
                        <h2>Xin chào %s,</h2>
                        <p>Chúng tôi xin thông báo rằng mật khẩu của bạn đã được thay đổi thành công.</p>
                        
                        <div style="background-color: #d1ecf1; border: 1px solid #bee5eb; border-radius: 8px; padding: 15px; margin: 20px 0;">
                            <h4 style="margin-top: 0; color: #0c5460;">🔒 Thông tin bảo mật</h4>
                            <ul style="margin: 10px 0; padding-left: 20px;">
                                <li>Mật khẩu mới đã được áp dụng cho tài khoản của bạn</li>
                                <li>Bạn có thể đăng nhập với mật khẩu mới ngay bây giờ</li>
                                <li>Nếu bạn không thực hiện thay đổi này, vui lòng liên hệ hỗ trợ ngay</li>
                            </ul>
                        </div>

                        <div style="background-color: #fff3cd; border: 1px solid #ffeaa7; border-radius: 8px; padding: 15px; margin: 20px 0;">
                            <h4 style="margin-top: 0; color: #856404;">⚠️ Lưu ý quan trọng</h4>
                            <ul style="margin: 10px 0; padding-left: 20px;">
                                <li>Không chia sẻ mật khẩu với bất kỳ ai</li>
                                <li>Sử dụng mật khẩu mạnh và khác biệt</li>
                                <li>Thay đổi mật khẩu định kỳ để bảo mật tài khoản</li>
                            </ul>
                        </div>

                        <p>Nếu bạn có bất kỳ câu hỏi nào, vui lòng liên hệ với chúng tôi qua email hỗ trợ.</p>
                        <p>Trân trọng,<br><strong>Đội ngũ KDBS</strong></p>
                    </div>

                    <div class="footer">
                        <p><strong>KDBS Travel Agency</strong></p>
                        <p>📧 Email: info@kdbs.com | 📞 Hotline: 1900-xxxx</p>
                        <p>🏢 Địa chỉ: 123 Đường ABC, Quận XYZ, TP.HCM</p>
                    </div>
                </div>
            </body>
            </html>
            """, username);
    }

    /**
     * Gửi email OTP
     */
    public void sendOTPEmail(String email, String otpCode, String purpose) {
        try {
            String subject = "Mã OTP - KDBS";
            String content = buildOTPEmail(otpCode, purpose);
            
            sendEmail(email, subject, content);
            log.info("OTP email sent successfully to: {}", email);

        } catch (Exception e) {
            log.error("Failed to send OTP email to: {}", email, e);
            throw new RuntimeException("Failed to send OTP email", e);
        }
    }

    /**
     * Xây dựng nội dung email OTP
     */
    private String buildOTPEmail(String otpCode, String purpose) {
        String purposeText = switch (purpose) {
            case "FORGOT_PASSWORD" -> "đặt lại mật khẩu";
            case "EMAIL_VERIFICATION" -> "xác thực email";
            case "PHONE_VERIFICATION" -> "xác thực số điện thoại";
            default -> "xác thực tài khoản";
        };

        return String.format("""
            <!DOCTYPE html>
            <html lang="vi">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Mã OTP - KDBS</title>
                <style>
                    body {
                        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                        line-height: 1.6;
                        color: #333;
                        max-width: 600px;
                        margin: 0 auto;
                        padding: 20px;
                        background-color: #f4f4f4;
                    }
                    .container {
                        background-color: #ffffff;
                        border-radius: 10px;
                        box-shadow: 0 0 20px rgba(0,0,0,0.1);
                        overflow: hidden;
                    }
                    .header {
                        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                        color: white;
                        padding: 30px;
                        text-align: center;
                    }
                    .header h1 {
                        margin: 0;
                        font-size: 28px;
                        font-weight: 300;
                    }
                    .content {
                        padding: 30px;
                    }
                    .otp-code {
                        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                        color: white;
                        font-size: 32px;
                        font-weight: bold;
                        text-align: center;
                        padding: 20px;
                        border-radius: 10px;
                        margin: 20px 0;
                        letter-spacing: 5px;
                        font-family: 'Courier New', monospace;
                    }
                    .footer {
                        background-color: #2c3e50;
                        color: white;
                        padding: 20px;
                        text-align: center;
                    }
                    .warning {
                        background-color: #fff3cd;
                        border: 1px solid #ffeaa7;
                        border-radius: 8px;
                        padding: 15px;
                        margin: 20px 0;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🔐 Mã Xác Thực OTP</h1>
                        <p>Mã bảo mật cho %s</p>
                    </div>
                    
                    <div class="content">
                        <h2>Xin chào,</h2>
                        <p>Bạn đã yêu cầu %s. Vui lòng sử dụng mã OTP bên dưới để hoàn tất quá trình:</p>
                        
                        <div class="otp-code">%s</div>
                        
                        <div class="warning">
                            <h4 style="margin-top: 0; color: #856404;">⚠️ Lưu ý quan trọng</h4>
                            <ul style="margin: 10px 0; padding-left: 20px;">
                                <li>Mã OTP có hiệu lực trong <strong>5 phút</strong></li>
                                <li>Không chia sẻ mã này với bất kỳ ai</li>
                                <li>KDBS sẽ không bao giờ yêu cầu mã OTP qua điện thoại</li>
                                <li>Nếu bạn không yêu cầu mã này, vui lòng bỏ qua email</li>
                            </ul>
                        </div>

                        <p>Nếu bạn có bất kỳ câu hỏi nào, vui lòng liên hệ với chúng tôi qua email hỗ trợ.</p>
                        <p>Trân trọng,<br><strong>Đội ngũ KDBS</strong></p>
                    </div>

                    <div class="footer">
                        <p><strong>KDBS Travel Agency</strong></p>
                        <p>📧 Email: info@kdbs.com | 📞 Hotline: 1900-xxxx</p>
                        <p>🏢 Địa chỉ: 123 Đường ABC, Quận XYZ, TP.HCM</p>
                    </div>
                </div>
            </body>
            </html>
            """, purposeText, purposeText, otpCode);
    }

    /**
     * Gửi email đơn giản (text)
     */
    public void sendSimpleEmail(String to, String subject, String content) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, false); // false = text content

            mailSender.send(message);
            log.info("Simple email sent successfully to: {}", to);

        } catch (MessagingException e) {
            log.error("Failed to send simple email to: {}", to, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }
} 