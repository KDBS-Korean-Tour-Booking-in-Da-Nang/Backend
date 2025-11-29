package com.example.KDBS.service;

import com.example.KDBS.enums.BookingStatus;
import com.example.KDBS.model.Booking;
import com.example.KDBS.model.BookingGuest;
import com.example.KDBS.model.Tour;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
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

    /**
     * Gửi email xác nhận booking thành công
     */
    @Async
    public void sendBookingConfirmationEmailAsync(Booking booking, Tour tour) {
        try {
            String emailContent = buildBookingConfirmationEmail(booking, tour);
            String subject = "여행 예약 완료 안내 - " + tour.getTourName();

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

        // Format ngày tháng (style Hàn)
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd", Locale.KOREA);
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm", Locale.KOREA);

        // Thay thế các placeholder

        return template
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
                .replace("{totalGuests}", booking.getTotalGuests().toString())
                .replace("{contactName}", booking.getContactName())
                .replace("{contactEmail}", booking.getContactEmail())
                .replace("{contactPhone}", booking.getContactPhone())
                .replace("{contactAddress}", booking.getContactAddress() != null ? booking.getContactAddress() : "Chưa cung cấp");
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
                    .append(" | Loại: ").append(guest.getBookingGuestType().name())
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
    @Async
    public void sendPasswordResetSuccessEmail(String email, String username) {
        try {
            String subject = "비밀번호 변경 완료 안내 - KDBS";
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
            <html lang="ko">
            <head>
              <meta charset="UTF-8" />
              <title>KDBS Account</title>
                <style>
                :root {
                  --blue-soft: #cfe5ff;
                  --blue-card: #e6f1ff;
                  --blue-accent: #5a8dee;
                  --red-soft: #ffe0e8;
                  --red-card: #ffe8f0;
                  --text-main: #1e293b;
                  --text-sub: #64748b;
                  --radius: 22px;
                  --shadow: 0 12px 32px rgba(30, 41, 59, 0.10);
                }

                    body {
                  margin: 0;
                  padding: 0;
                  background: linear-gradient(160deg, var(--blue-soft), var(--red-soft));
                  font-family: -apple-system, BlinkMacSystemFont, 'Noto Sans KR', sans-serif;
                  color: var(--text-main);
                  position: relative;
                  overflow-x: hidden;
                }

                body::before,
                body::after {
                  content: "";
                  position: absolute;
                  border-radius: 50%%;
                  filter: blur(120px);
                  opacity: 0.3;
                }

                body::before {
                  width: 300px;
                  height: 300px;
                  top: -80px;
                  left: -100px;
                  background: var(--blue-soft);
                }

                body::after {
                  width: 280px;
                  height: 280px;
                  bottom: -100px;
                  right: -80px;
                  background: var(--red-soft);
                }

                .wrapper {
                  width: 100%%;
                  padding: 32px 14px;
                  box-sizing: border-box;
                  position: relative;
                  z-index: 1;
                }

                .card {
                  max-width: 620px;
                        margin: 0 auto;
                  padding: 32px 26px 28px;
                  background: linear-gradient(135deg, var(--blue-card), var(--red-card));
                  border-radius: var(--radius);
                  box-shadow: var(--shadow);
                  border: 1px solid rgba(90, 141, 238, 0.12);
                }

                .badge {
                  display: inline-flex;
                  align-items: center;
                  padding: 7px 14px;
                  border-radius: 999px;
                  background: linear-gradient(120deg, var(--blue-soft), var(--red-soft));
                  font-size: 11.5px;
                  font-weight: 600;
                  color: var(--blue-accent);
                  letter-spacing: 0.15em;
                  text-transform: uppercase;
                  margin-bottom: 16px;
                  box-shadow: 0 3px 8px rgba(90, 141, 238, 0.18);
                }

                .badge-dot {
                  width: 7px;
                  height: 7px;
                  border-radius: 50%%;
                  background: var(--blue-accent);
                  margin-right: 6px;
                }

                h1 {
                  font-size: 23px;
                  font-weight: 600;
                  margin: 0 0 12px;
                  line-height: 1.45;
                  color: var(--text-main);
                }

                p {
                  font-size: 15px;
                  line-height: 1.75;
                  margin: 0 0 12px;
                  color: var(--text-main);
                }

                .sub {
                  font-size: 14px;
                  color: var(--text-sub);
                  margin-bottom: 6px;
                }

                .section {
                  margin-top: 20px;
                  padding: 18px 20px;
                  border-radius: 18px;
                  border: 1px solid rgba(100, 116, 139, 0.12);
                  background: #e6f1ff;
                }

                .section-title {
                  font-size: 12.5px;
                  font-weight: 700;
                  color: var(--text-sub);
                  letter-spacing: 0.11em;
                  text-transform: uppercase;
                  margin-bottom: 8px;
                }

                ul {
                  padding-left: 18px;
                  margin: 6px 0 0;
                }

                li {
                  font-size: 14px;
                  color: var(--text-main);
                  margin-bottom: 6px;
                  line-height: 1.6;
                }

                    .footer {
                  margin-top: 28px;
                  font-size: 11px;
                  color: var(--text-sub);
                        text-align: center;
                    }
                </style>
            </head>
            <body>
              <div class="wrapper">
                <div class="card">
                  <div class="badge">
                    <span class="badge-dot"></span>
                    KDBS ACCOUNT
                    </div>
        
                  <h1>비밀번호가 안전하게 변경되었습니다</h1>
                  <p>%s 님, 안녕하세요.</p>
                  <p class="sub">
                    요청하신 대로 KDBS 계정의 비밀번호가 새롭게 설정되었습니다. 이제부터는 새 비밀번호로 로그인해 주세요.
                  </p>

                  <div class="section">
                    <div class="section-title">안전하게 이용하는 방법</div>
                    <ul>
                      <li>비밀번호는 다른 사람과 공유하지 마세요.</li>
                      <li>다른 서비스와 다른, 충분히 길고 복잡한 비밀번호를 사용하는 것이 좋습니다.</li>
                      <li>만약 본인이 요청하지 않았다면, 즉시 KDBS 고객센터로 문의해 주세요.</li>
                            </ul>
                        </div>

                  <p style="margin-top: 22px;">
                    KDBS를 이용해 주셔서 감사합니다.
                  </p>

                  <p>
                    감사합니다.<br>
                    <strong>KDBS 드림</strong>
                  </p>

                    <div class="footer">
                    본 메일은 발신 전용으로 회신이 불가합니다.
                  </div>
                    </div>
                </div>
            </body>
            </html>
            """, username);
    }

    /**
     * Gửi email OTP
     */
    @Async
    public void sendOTPEmail(String email, String otpCode, String purpose) {
        try {
            String subject = "인증번호 (OTP) - KDBS";
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
        String purposeText;
        if ("FORGOT_PASSWORD".equals(purpose)) {
            purposeText = "비밀번호 재설정";
        } else if ("EMAIL_VERIFICATION".equals(purpose)) {
            purposeText = "이메일 인증";
        } else if ("PHONE_VERIFICATION".equals(purpose)) {
            purposeText = "휴대폰 번호 인증";
        } else {
            purposeText = "계정 인증";
        }

        return String.format("""
            <!DOCTYPE html>
            <html lang="ko">
            <head>
              <meta charset="UTF-8" />
              <title>KDBS Security</title>
                <style>
                :root {
                  --blue-soft: #cfe5ff;
                  --blue-card: #e6f1ff;
                  --blue-accent: #5a8dee;
                  --red-soft: #ffe0e8;
                  --red-card: #ffe8f0;
                  --text-main: #1e293b;
                  --text-sub: #64748b;
                  --radius: 22px;
                  --shadow: 0 12px 32px rgba(30, 41, 59, 0.10);
                }

                    body {
                  margin: 0;
                  padding: 0;
                  background: linear-gradient(160deg, var(--blue-soft), var(--red-soft));
                  font-family: -apple-system, BlinkMacSystemFont, 'Noto Sans KR', sans-serif;
                  color: var(--text-main);
                  position: relative;
                  overflow-x: hidden;
                }

                body::before,
                body::after {
                  content: "";
                  position: absolute;
                  border-radius: 50%%;
                  filter: blur(120px);
                  opacity: 0.3;
                }

                body::before {
                  width: 300px;
                  height: 300px;
                  top: -80px;
                  left: -100px;
                  background: var(--blue-soft);
                }

                body::after {
                  width: 280px;
                  height: 280px;
                  bottom: -100px;
                  right: -80px;
                  background: var(--red-soft);
                }

                .wrapper {
                  width: 100%%;
                  padding: 32px 14px;
                  box-sizing: border-box;
                  position: relative;
                  z-index: 1;
                }

                .card {
                  max-width: 620px;
                        margin: 0 auto;
                  padding: 32px 26px 28px;
                  background: linear-gradient(135deg, var(--blue-card), var(--red-card));
                  border-radius: var(--radius);
                  box-shadow: var(--shadow);
                  border: 1px solid rgba(90, 141, 238, 0.12);
                }

                .badge {
                  display: inline-flex;
                  align-items: center;
                  padding: 7px 14px;
                  border-radius: 999px;
                  background: linear-gradient(120deg, var(--blue-soft), var(--red-soft));
                  font-size: 11.5px;
                  font-weight: 600;
                  color: var(--blue-accent);
                  letter-spacing: 0.15em;
                  text-transform: uppercase;
                  margin-bottom: 16px;
                  box-shadow: 0 3px 8px rgba(90, 141, 238, 0.18);
                }

                .badge-dot {
                  width: 7px;
                  height: 7px;
                  border-radius: 50%%;
                  background: var(--blue-accent);
                  margin-right: 6px;
                }

                h1 {
                  font-size: 23px;
                  font-weight: 600;
                  margin: 0 0 12px;
                  line-height: 1.45;
                  color: var(--text-main);
                }

                p {
                  font-size: 15px;
                  line-height: 1.75;
                  margin: 0 0 12px;
                  color: var(--text-main);
                }

                .sub {
                  font-size: 14px;
                  color: var(--text-sub);
                  margin-bottom: 6px;
                }

                .otp-code {
                  margin-top: 20px;
                  margin-bottom: 12px;
                  padding: 20px 24px;
                  border-radius: 18px;
                  background: linear-gradient(135deg, #e6f1ff, #ffe8f0);
                  color: var(--blue-accent);
                  font-size: 28px;
                  letter-spacing: 0.4em;
                  text-align: center;
                  font-weight: 700;
                  border: 1px solid rgba(90, 141, 238, 0.2);
                  box-shadow: 0 4px 12px rgba(90, 141, 238, 0.15);
                }

                .hint {
                  font-size: 13px;
                  color: var(--text-sub);
                  margin-top: 6px;
                        text-align: center;
                    }

                .section {
                  margin-top: 20px;
                  padding: 18px 20px;
                  border-radius: 18px;
                  border: 1px solid rgba(100, 116, 139, 0.12);
                  background: #e6f1ff;
                }

                .section-title {
                  font-size: 12.5px;
                  font-weight: 700;
                  color: var(--text-sub);
                  letter-spacing: 0.11em;
                  text-transform: uppercase;
                  margin-bottom: 8px;
                }

                ul {
                  padding-left: 18px;
                  margin: 6px 0 0;
                }

                li {
                  font-size: 14px;
                  color: var(--text-main);
                  margin-bottom: 6px;
                  line-height: 1.6;
                }

                    .footer {
                  margin-top: 28px;
                  font-size: 11px;
                  color: var(--text-sub);
                        text-align: center;
                    }
                </style>
            </head>
            <body>
              <div class="wrapper">
                <div class="card">
                  <div class="badge">
                    <span class="badge-dot"></span>
                    KDBS SECURITY
                    </div>
           
                  <h1>인증번호 안내</h1>
                  <p>KDBS 계정에서 %s 작업을 진행 중입니다.</p>
                  <p class="sub">
                    아래 인증번호를 입력하여 절차를 완료해 주세요.
                  </p>

                        <div class="otp-code">%s</div>
                  <div class="hint">인증번호는 짧은 시간 동안만 유효하니, 바로 입력해 주세요.</div>

                  <div class="section">
                    <div class="section-title">안내 사항</div>
                    <ul>
                      <li>KDBS 직원을 사칭하더라도, 인증번호는 절대 공유하지 마세요.</li>
                      <li>본인이 요청하지 않은 메일이라면, 이 메일을 무시하셔도 됩니다.</li>
                            </ul>
                        </div>

                  <p style="margin-top: 22px;">
                    KDBS와 함께해 주셔서 감사합니다.
                  </p>

                  <p>
                    감사합니다.<br>
                    <strong>KDBS 드림</strong>
                  </p>

                  <div class="footer">
                    본 메일은 발신 전용으로 회신이 불가합니다.
                  </div>
                </div>
              </div>
            </body>
            </html>
            """, purposeText, otpCode);
    }

    /**
     * Gửi email thông báo thay đổi trạng thái booking
     */
    @Async
    public void sendBookingStatusUpdateEmail(String to,
                                             Booking booking,
                                             Tour tour,
                                             BookingStatus oldStatus,
                                             BookingStatus newStatus,
                                             boolean isCompanyRecipient,
                                             String message) {
        try {
            // Nếu là reject booking, dùng email riêng với nhiều thông tin hơn
            if (newStatus == BookingStatus.BOOKING_REJECTED) {
                sendBookingRejectedEmail(to, booking, tour, isCompanyRecipient, message);
                return;
            }

            String subject;
            if (isCompanyRecipient) {
                subject = String.format("Cập nhật trạng thái booking #%d - %s",
                        booking.getBookingId(), tour.getTourName());
            } else {
                subject = String.format("예약 상태 변경 안내 - %s", tour.getTourName());
            }
            String content = buildBookingStatusUpdateEmail(to, booking, tour, oldStatus, newStatus, isCompanyRecipient);

            sendEmail(to, subject, content);
            log.info("Booking status update email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send booking status update email to: {}", to, e);
        }
    }

    /**
     * Gửi email thông báo reject booking (riêng biệt với nhiều thông tin)
     */
    @Async
    public void sendBookingRejectedEmail(String to,
                                         Booking booking,
                                         Tour tour,
                                         boolean isCompanyRecipient,
                                         String rejectionMessage) {
        try {
            String subject;
            if (isCompanyRecipient) {
                subject = String.format("Booking #%d đã bị từ chối - %s",
                        booking.getBookingId(), tour.getTourName());
            } else {
                subject = String.format("예약 취소 안내 - %s", tour.getTourName());
            }
            String content = buildBookingRejectedEmail(to, booking, tour, isCompanyRecipient, rejectionMessage);

            sendEmail(to, subject, content);
            log.info("Booking rejected email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send booking rejected email to: {}", to, e);
        }
    }

    private String buildBookingStatusUpdateEmail(String to,
                                                 Booking booking,
                                                 Tour tour,
                                                 BookingStatus oldStatus,
                                                 BookingStatus newStatus,
                                                 boolean isCompanyRecipient) {
        String departureTextKo = booking.getDepartureDate() != null
                ? booking.getDepartureDate().format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
                : "미정";
        String departureTextVi = booking.getDepartureDate() != null
                ? booking.getDepartureDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                : "Chưa xác định";

        if (!isCompanyRecipient) {
            // Email cho user - tiếng Hàn
            String statusMessageKo;
            if (newStatus == BookingStatus.BOOKING_SUCCESS) {
                statusMessageKo = "고객님의 여행 예약이 확정되었습니다.";
            } else if (newStatus == BookingStatus.BOOKING_REJECTED) {
                statusMessageKo = "죄송하지만 예약이 취소되었습니다.";
            } else if (newStatus == BookingStatus.WAITING_FOR_UPDATE) {
                statusMessageKo = "예약 진행을 위해 고객님의 추가 정보가 필요합니다.";
            } else {
                statusMessageKo = "예약 상태가 변경되었습니다.";
            }

            return String.format("""
                <!DOCTYPE html>
                <html lang="ko">
                <head>
                  <meta charset="UTF-8" />
                  <title>KDBS Booking</title>
                  <style>
                    :root {
                      --blue-soft: #cfe5ff;
                      --blue-card: #e6f1ff;
                      --blue-accent: #5a8dee;
                      --red-soft: #ffe0e8;
                      --red-card: #ffe8f0;
                      --text-main: #1e293b;
                      --text-sub: #64748b;
                      --radius: 22px;
                      --shadow: 0 12px 32px rgba(30, 41, 59, 0.10);
                    }

                    body {
                      margin: 0;
                      padding: 0;
                      background: linear-gradient(160deg, var(--blue-soft), var(--red-soft));
                      font-family: -apple-system, BlinkMacSystemFont, 'Noto Sans KR', sans-serif;
                      color: var(--text-main);
                      position: relative;
                      overflow-x: hidden;
                    }

                    body::before,
                    body::after {
                      content: "";
                      position: absolute;
                      border-radius: 50%%;
                      filter: blur(120px);
                      opacity: 0.3;
                    }

                    body::before {
                      width: 300px;
                      height: 300px;
                      top: -80px;
                      left: -100px;
                      background: var(--blue-soft);
                    }

                    body::after {
                      width: 280px;
                      height: 280px;
                      bottom: -100px;
                      right: -80px;
                      background: var(--red-soft);
                    }

                    .wrapper {
                      width: 100%%;
                      padding: 32px 14px;
                      box-sizing: border-box;
                      position: relative;
                      z-index: 1;
                    }

                    .card {
                      max-width: 620px;
                      margin: 0 auto;
                      padding: 32px 26px 28px;
                      background: linear-gradient(135deg, var(--blue-card), var(--red-card));
                      border-radius: var(--radius);
                      box-shadow: var(--shadow);
                      border: 1px solid rgba(90, 141, 238, 0.12);
                    }

                    .badge {
                      display: inline-flex;
                      align-items: center;
                      padding: 7px 14px;
                      border-radius: 999px;
                      background: linear-gradient(120deg, var(--blue-soft), var(--red-soft));
                      font-size: 11.5px;
                      font-weight: 600;
                      color: var(--blue-accent);
                      letter-spacing: 0.15em;
                      text-transform: uppercase;
                      margin-bottom: 16px;
                      box-shadow: 0 3px 8px rgba(90, 141, 238, 0.18);
                    }

                    .badge-dot {
                      width: 7px;
                      height: 7px;
                      border-radius: 50%%;
                      background: var(--blue-accent);
                      margin-right: 6px;
                    }

                    h1 {
                      font-size: 23px;
                      font-weight: 600;
                      margin: 0 0 12px;
                      line-height: 1.45;
                      color: var(--text-main);
                    }

                    p {
                      font-size: 15px;
                      line-height: 1.75;
                      margin: 0 0 12px;
                      color: var(--text-main);
                    }

                    .sub {
                      font-size: 14px;
                      color: var(--text-sub);
                      margin-bottom: 6px;
                    }

                    .section {
                      margin-top: 20px;
                      padding: 18px 20px;
                      border-radius: 18px;
                      border: 1px solid rgba(100, 116, 139, 0.12);
                      background: #e6f1ff;
                    }

                    .section-title {
                      font-size: 12.5px;
                      font-weight: 700;
                      color: var(--text-sub);
                      letter-spacing: 0.11em;
                      text-transform: uppercase;
                      margin-bottom: 8px;
                    }

                    .row {
                      display: flex;
                      justify-content: space-between;
                      font-size: 14px;
                      margin-bottom: 6px;
                    }

                    .label {
                      color: var(--text-sub);
                    }

                    .value {
                      font-weight: 600;
                      color: var(--blue-accent);
                    }

                    .footer {
                      margin-top: 28px;
                      font-size: 11px;
                      color: var(--text-sub);
                      text-align: center;
                    }
                  </style>
                </head>
                <body>
                  <div class="wrapper">
                    <div class="card">
                      <div class="badge">
                        <span class="badge-dot"></span>
                        KDBS BOOKING
                      </div>

                      <h1>예약 상태가 변경되었습니다</h1>
                      <p>안녕하세요, 고객님.</p>
                      <p class="sub">%s</p>

                      <div class="section">
                        <div class="section-title">예약 정보</div>
                        <div class="row">
                          <span class="label">투어</span>
                          <span class="value">%s</span>
                        </div>
                        <div class="row">
                          <span class="label">예약 번호</span>
                          <span class="value">#%d</span>
                        </div>
                        <div class="row">
                          <span class="label">출발일</span>
                          <span class="value">%s</span>
                        </div>
                      </div>

                      <p style="margin-top: 22px;">
                        예약과 관련해 궁금한 점이 있으시면 KDBS 고객센터 또는 여행사로 문의해 주세요.
                      </p>

                      <p>
                        감사합니다.<br>
                        <strong>KDBS 드림</strong>
                      </p>

                      <div class="footer">
                        본 메일은 발신 전용으로 회신이 불가합니다.
                      </div>
                    </div>
                  </div>
                </body>
                </html>
                """,
                statusMessageKo,
                tour.getTourName(),
                booking.getBookingId(),
                departureTextKo);
        }

        // Email cho company - tiếng Việt
        String statusMessageVi;
        if (newStatus == BookingStatus.BOOKING_SUCCESS) {
            statusMessageVi = "Booking của khách đã được xác nhận.";
        } else if (newStatus == BookingStatus.BOOKING_REJECTED) {
            statusMessageVi = "Booking của khách đã bị từ chối.";
        } else if (newStatus == BookingStatus.WAITING_FOR_UPDATE) {
            statusMessageVi = "Bạn đã yêu cầu khách cập nhật thêm thông tin cho booking này.";
        } else {
            statusMessageVi = "Trạng thái booking đã được cập nhật.";
        }

        return String.format("""
                <!DOCTYPE html>
                <html lang="vi">
                <head>
                  <meta charset="UTF-8" />
                  <title>KDBS Booking</title>
                  <style>
                    :root {
                      --blue-soft: #cfe5ff;
                      --blue-card: #e6f1ff;
                      --blue-accent: #5a8dee;
                      --red-soft: #ffe0e8;
                      --red-card: #ffe8f0;
                      --text-main: #1e293b;
                      --text-sub: #64748b;
                      --radius: 22px;
                      --shadow: 0 12px 32px rgba(30, 41, 59, 0.10);
                    }

                    body {
                      margin: 0;
                      padding: 0;
                      background: linear-gradient(160deg, var(--blue-soft), var(--red-soft));
                      font-family: -apple-system, BlinkMacSystemFont, 'Noto Sans KR', sans-serif;
                      color: var(--text-main);
                      position: relative;
                      overflow-x: hidden;
                    }

                    body::before,
                    body::after {
                      content: "";
                      position: absolute;
                      border-radius: 50%%;
                      filter: blur(120px);
                      opacity: 0.3;
                    }

                    body::before {
                      width: 300px;
                      height: 300px;
                      top: -80px;
                      left: -100px;
                      background: var(--blue-soft);
                    }

                    body::after {
                      width: 280px;
                      height: 280px;
                      bottom: -100px;
                      right: -80px;
                      background: var(--red-soft);
                    }

                    .wrapper {
                      width: 100%%;
                      padding: 32px 14px;
                      box-sizing: border-box;
                      position: relative;
                      z-index: 1;
                    }

                    .card {
                      max-width: 620px;
                      margin: 0 auto;
                      padding: 32px 26px 28px;
                      background: linear-gradient(135deg, var(--blue-card), var(--red-card));
                      border-radius: var(--radius);
                      box-shadow: var(--shadow);
                      border: 1px solid rgba(90, 141, 238, 0.12);
                    }

                    .badge {
                      display: inline-flex;
                      align-items: center;
                      padding: 7px 14px;
                      border-radius: 999px;
                      background: linear-gradient(120deg, var(--blue-soft), var(--red-soft));
                      font-size: 11.5px;
                      font-weight: 600;
                      color: var(--blue-accent);
                      letter-spacing: 0.15em;
                      text-transform: uppercase;
                      margin-bottom: 16px;
                      box-shadow: 0 3px 8px rgba(90, 141, 238, 0.18);
                    }

                    .badge-dot {
                      width: 7px;
                      height: 7px;
                      border-radius: 50%%;
                      background: var(--blue-accent);
                      margin-right: 6px;
                    }

                    h1 {
                      font-size: 23px;
                      font-weight: 600;
                      margin: 0 0 12px;
                      line-height: 1.45;
                      color: var(--text-main);
                    }

                    p {
                      font-size: 15px;
                      line-height: 1.75;
                      margin: 0 0 12px;
                      color: var(--text-main);
                    }

                    .sub {
                      font-size: 14px;
                      color: var(--text-sub);
                      margin-bottom: 6px;
                    }

                    .section {
                      margin-top: 20px;
                      padding: 18px 20px;
                      border-radius: 18px;
                      border: 1px solid rgba(100, 116, 139, 0.12);
                      background: #e6f1ff;
                    }

                    .section-title {
                      font-size: 12.5px;
                      font-weight: 700;
                      color: var(--text-sub);
                      letter-spacing: 0.11em;
                      text-transform: uppercase;
                      margin-bottom: 8px;
                    }

                    .row {
                      display: flex;
                      justify-content: space-between;
                      font-size: 14px;
                      margin-bottom: 6px;
                    }

                    .label {
                      color: var(--text-sub);
                    }

                    .value {
                      font-weight: 600;
                      color: var(--blue-accent);
                    }

                    .footer {
                      margin-top: 28px;
                      font-size: 11px;
                      color: var(--text-sub);
                      text-align: center;
                    }
                  </style>
                </head>
                <body>
                  <div class="wrapper">
                    <div class="card">
                      <div class="badge">
                        <span class="badge-dot"></span>
                        KDBS BOOKING
                      </div>

                      <h1>Cập nhật trạng thái booking</h1>
                      <p>Xin chào,</p>
                      <p class="sub">%s</p>

                      <div class="section">
                        <div class="section-title">Thông tin chính</div>
                        <div class="row">
                          <span class="label">Tour</span>
                          <span class="value">%s</span>
                        </div>
                        <div class="row">
                          <span class="label">Mã booking</span>
                          <span class="value">#%d</span>
                        </div>
                        <div class="row">
                          <span class="label">Ngày khởi hành dự kiến</span>
                          <span class="value">%s</span>
                        </div>
                    </div>

                      <p style="margin-top: 22px;">
                        Nếu bạn có bất kỳ câu hỏi nào về booking này, hãy liên hệ với đội ngũ hỗ trợ KDBS hoặc kiểm tra chi tiết trên hệ thống.
                      </p>

                      <p>
                        Thân mến,<br>
                        <strong>Đội ngũ KDBS</strong>
                      </p>

                    <div class="footer">
                        Email mang tính thông báo, vui lòng không trả lời trực tiếp.
                      </div>
                    </div>
                </div>
            </body>
            </html>
                """,
                statusMessageVi,
                tour.getTourName(),
                booking.getBookingId(),
                departureTextVi);
    }

    /**
     * Xây dựng email reject booking với nhiều thông tin chi tiết
     */
    private String buildBookingRejectedEmail(String to,
                                             Booking booking,
                                             Tour tour,
                                             boolean isCompanyRecipient,
                                             String rejectionMessage) {
        String departureTextKo = booking.getDepartureDate() != null
                ? booking.getDepartureDate().format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
                : "미정";
        String departureTextVi = booking.getDepartureDate() != null
                ? booking.getDepartureDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                : "Chưa xác định";
        String createdDateKo = booking.getCreatedAt() != null
                ? booking.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm"))
                : "미정";
        String createdDateVi = booking.getCreatedAt() != null
                ? booking.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                : "Chưa xác định";

        if (!isCompanyRecipient) {
            // Email cho user - tiếng Hàn
            String reasonText = (rejectionMessage != null && !rejectionMessage.isBlank())
                    ? rejectionMessage
                    : "예약 조건이 맞지 않아 진행이 어렵습니다. 자세한 사항은 여행사에 문의해 주세요.";

            return String.format("""
                <!DOCTYPE html>
                <html lang="ko">
                <head>
                  <meta charset="UTF-8" />
                  <title>KDBS Booking</title>
                  <style>
                    :root {
                      --blue-soft: #cfe5ff;
                      --blue-card: #e6f1ff;
                      --blue-accent: #5a8dee;
                      --red-soft: #ffe0e8;
                      --red-card: #ffe8f0;
                      --text-main: #1e293b;
                      --text-sub: #64748b;
                      --radius: 22px;
                      --shadow: 0 12px 32px rgba(30, 41, 59, 0.10);
                    }

                    body {
                      margin: 0;
                      padding: 0;
                      background: linear-gradient(160deg, var(--blue-soft), var(--red-soft));
                      font-family: -apple-system, BlinkMacSystemFont, 'Noto Sans KR', sans-serif;
                      color: var(--text-main);
                      position: relative;
                      overflow-x: hidden;
                    }

                    body::before,
                    body::after {
                      content: "";
                      position: absolute;
                      border-radius: 50%%;
                      filter: blur(120px);
                      opacity: 0.3;
                    }

                    body::before {
                      width: 300px;
                      height: 300px;
                      top: -80px;
                      left: -100px;
                      background: var(--blue-soft);
                    }

                    body::after {
                      width: 280px;
                      height: 280px;
                      bottom: -100px;
                      right: -80px;
                      background: var(--red-soft);
                    }

                    .wrapper {
                      width: 100%%;
                      padding: 32px 14px;
                      box-sizing: border-box;
                      position: relative;
                      z-index: 1;
                    }

                    .card {
                      max-width: 620px;
                      margin: 0 auto;
                      padding: 32px 26px 28px;
                      background: linear-gradient(135deg, var(--blue-card), var(--red-card));
                      border-radius: var(--radius);
                      box-shadow: var(--shadow);
                      border: 1px solid rgba(90, 141, 238, 0.12);
                    }

                    .badge {
                      display: inline-flex;
                      align-items: center;
                      padding: 7px 14px;
                      border-radius: 999px;
                      background: linear-gradient(120deg, var(--blue-soft), var(--red-soft));
                      font-size: 11.5px;
                      font-weight: 600;
                      color: var(--blue-accent);
                      letter-spacing: 0.15em;
                      text-transform: uppercase;
                      margin-bottom: 16px;
                      box-shadow: 0 3px 8px rgba(90, 141, 238, 0.18);
                    }

                    .badge-dot {
                      width: 7px;
                      height: 7px;
                      border-radius: 50%%;
                      background: var(--blue-accent);
                      margin-right: 6px;
                    }

                    h1 {
                      font-size: 23px;
                      font-weight: 600;
                      margin: 0 0 12px;
                      line-height: 1.45;
                      color: var(--text-main);
                    }

                    p {
                      font-size: 15px;
                      line-height: 1.75;
                      margin: 0 0 12px;
                      color: var(--text-main);
                    }

                    .sub {
                      font-size: 14px;
                      color: var(--text-sub);
                      margin-bottom: 6px;
                    }

                    .section {
                      margin-top: 20px;
                      padding: 18px 20px;
                      border-radius: 18px;
                      border: 1px solid rgba(100, 116, 139, 0.12);
                    }

                    .section.tour {
                      background: #e6f1ff;
                    }

                    .section.reason {
                      background: #ffe8f0;
                    }

                    .section.contact {
                      background: #d9ecff;
                    }

                    .section.refund {
                      background: #fff4e6;
                    }

                    .section-title {
                      font-size: 12.5px;
                      font-weight: 700;
                      color: var(--text-sub);
                      letter-spacing: 0.11em;
                      text-transform: uppercase;
                      margin-bottom: 8px;
                    }

                    .row {
                      display: flex;
                      justify-content: space-between;
                      font-size: 14px;
                      margin-bottom: 6px;
                    }

                    .label {
                      color: var(--text-sub);
                    }

                    .value {
                      font-weight: 600;
                      color: var(--blue-accent);
                    }

                    .reason-box {
                      padding: 12px 14px;
                      background: rgba(255, 255, 255, 0.7);
                      border-radius: 12px;
                      margin-top: 8px;
                      font-size: 14px;
                      line-height: 1.6;
                      color: var(--text-main);
                    }

                    .footer {
                      margin-top: 28px;
                      font-size: 11px;
                      color: var(--text-sub);
                      text-align: center;
                    }
                  </style>
                </head>
                <body>
                  <div class="wrapper">
                    <div class="card">
                      <div class="badge">
                        <span class="badge-dot"></span>
                        KDBS BOOKING
                      </div>

                      <h1>예약이 취소되었습니다</h1>
                      <p>안녕하세요, 고객님.</p>
                      <p class="sub">
                        죄송합니다. 고객님이 예약하신 투어가 진행되지 못하게 되었습니다.
                      </p>

                      <div class="section tour">
                        <div class="section-title">예약 정보</div>
                        <div class="row">
                          <span class="label">투어</span>
                          <span class="value">%s</span>
                        </div>
                        <div class="row">
                          <span class="label">예약 번호</span>
                          <span class="value">#%d</span>
                        </div>
                        <div class="row">
                          <span class="label">예약일시</span>
                          <span class="value">%s</span>
                        </div>
                        <div class="row">
                          <span class="label">출발일</span>
                          <span class="value">%s</span>
                        </div>
                        <div class="row">
                          <span class="label">인원</span>
                          <span class="value">%d명</span>
                        </div>
                        <div class="row">
                          <span class="label">연락처</span>
                          <span class="value">%s</span>
                        </div>
                      </div>

                      <div class="section reason">
                        <div class="section-title">취소 사유</div>
                        <div class="reason-box">
                          %s
                        </div>
                      </div>

                      <div class="section refund">
                        <div class="section-title">환불 안내</div>
                        <p style="font-size: 14px; margin: 0;">
                          이미 결제하신 금액이 있다면, 환불 절차가 진행됩니다. 환불은 영업일 기준 3-5일 소요될 수 있습니다.
                        </p>
                        <p style="font-size: 14px; margin: 8px 0 0;">
                          환불 관련 문의는 여행사 또는 KDBS 고객센터로 연락해 주세요.
                        </p>
                      </div>

                      <div class="section contact">
                        <div class="section-title">문의 안내</div>
                        <div class="row">
                          <span class="label">KDBS 고객센터</span>
                          <span class="value">support@kdbs.com</span>
                        </div>
                        <div class="row">
                          <span class="label">여행사 문의</span>
                          <span class="value">여행사로 직접 연락</span>
                        </div>
                      </div>

                      <p style="margin-top: 22px;">
                        다른 투어에 관심이 있으시다면, KDBS에서 다양한 여행 상품을 확인해 보세요.
                      </p>

                      <p>
                        불편을 드려 죄송합니다.<br>
                        <strong>KDBS 드림</strong>
                      </p>

                      <div class="footer">
                        본 메일은 발신 전용으로 회신이 불가합니다.
                      </div>
                    </div>
                  </div>
                </body>
                </html>
                """,
                tour.getTourName(),
                booking.getBookingId(),
                createdDateKo,
                departureTextKo,
                booking.getTotalGuests(),
                booking.getContactPhone() != null ? booking.getContactPhone() : "미기재",
                reasonText);
        }

        // Email cho company - tiếng Việt
        String reasonTextVi = (rejectionMessage != null && !rejectionMessage.isBlank())
                ? rejectionMessage
                : "Booking không đáp ứng các điều kiện yêu cầu.";

        return String.format("""
                <!DOCTYPE html>
                <html lang="vi">
                <head>
                  <meta charset="UTF-8" />
                  <title>KDBS Booking</title>
                  <style>
                    :root {
                      --blue-soft: #cfe5ff;
                      --blue-card: #e6f1ff;
                      --blue-accent: #5a8dee;
                      --red-soft: #ffe0e8;
                      --red-card: #ffe8f0;
                      --text-main: #1e293b;
                      --text-sub: #64748b;
                      --radius: 22px;
                      --shadow: 0 12px 32px rgba(30, 41, 59, 0.10);
                    }

                    body {
                      margin: 0;
                      padding: 0;
                      background: linear-gradient(160deg, var(--blue-soft), var(--red-soft));
                      font-family: -apple-system, BlinkMacSystemFont, 'Noto Sans KR', sans-serif;
                      color: var(--text-main);
                      position: relative;
                      overflow-x: hidden;
                    }

                    body::before,
                    body::after {
                      content: "";
                      position: absolute;
                      border-radius: 50%%;
                      filter: blur(120px);
                      opacity: 0.3;
                    }

                    body::before {
                      width: 300px;
                      height: 300px;
                      top: -80px;
                      left: -100px;
                      background: var(--blue-soft);
                    }

                    body::after {
                      width: 280px;
                      height: 280px;
                      bottom: -100px;
                      right: -80px;
                      background: var(--red-soft);
                    }

                    .wrapper {
                      width: 100%%;
                      padding: 32px 14px;
                      box-sizing: border-box;
                      position: relative;
                      z-index: 1;
                    }

                    .card {
                      max-width: 620px;
                      margin: 0 auto;
                      padding: 32px 26px 28px;
                      background: linear-gradient(135deg, var(--blue-card), var(--red-card));
                      border-radius: var(--radius);
                      box-shadow: var(--shadow);
                      border: 1px solid rgba(90, 141, 238, 0.12);
                    }

                    .badge {
                      display: inline-flex;
                      align-items: center;
                      padding: 7px 14px;
                      border-radius: 999px;
                      background: linear-gradient(120deg, var(--blue-soft), var(--red-soft));
                      font-size: 11.5px;
                      font-weight: 600;
                      color: var(--blue-accent);
                      letter-spacing: 0.15em;
                      text-transform: uppercase;
                      margin-bottom: 16px;
                      box-shadow: 0 3px 8px rgba(90, 141, 238, 0.18);
                    }

                    .badge-dot {
                      width: 7px;
                      height: 7px;
                      border-radius: 50%%;
                      background: var(--blue-accent);
                      margin-right: 6px;
                    }

                    h1 {
                      font-size: 23px;
                      font-weight: 600;
                      margin: 0 0 12px;
                      line-height: 1.45;
                      color: var(--text-main);
                    }

                    p {
                      font-size: 15px;
                      line-height: 1.75;
                      margin: 0 0 12px;
                      color: var(--text-main);
                    }

                    .sub {
                      font-size: 14px;
                      color: var(--text-sub);
                      margin-bottom: 6px;
                    }

                    .section {
                      margin-top: 20px;
                      padding: 18px 20px;
                      border-radius: 18px;
                      border: 1px solid rgba(100, 116, 139, 0.12);
                    }

                    .section.tour {
                      background: #e6f1ff;
                    }

                    .section.reason {
                      background: #ffe8f0;
                    }

                    .section.contact {
                      background: #d9ecff;
                    }

                    .section.customer {
                      background: #fff4e6;
                    }

                    .section-title {
                      font-size: 12.5px;
                      font-weight: 700;
                      color: var(--text-sub);
                      letter-spacing: 0.11em;
                      text-transform: uppercase;
                      margin-bottom: 8px;
                    }

                    .row {
                      display: flex;
                      justify-content: space-between;
                      font-size: 14px;
                      margin-bottom: 6px;
                    }

                    .label {
                      color: var(--text-sub);
                    }

                    .value {
                      font-weight: 600;
                      color: var(--blue-accent);
                    }

                    .reason-box {
                      padding: 12px 14px;
                      background: rgba(255, 255, 255, 0.7);
                      border-radius: 12px;
                      margin-top: 8px;
                      font-size: 14px;
                      line-height: 1.6;
                      color: var(--text-main);
                    }

                    .footer {
                      margin-top: 28px;
                      font-size: 11px;
                      color: var(--text-sub);
                      text-align: center;
                    }
                  </style>
                </head>
                <body>
                  <div class="wrapper">
                    <div class="card">
                      <div class="badge">
                        <span class="badge-dot"></span>
                        KDBS BOOKING
                      </div>

                      <h1>Booking đã bị từ chối</h1>
                      <p>Xin chào,</p>
                      <p class="sub">
                        Booking của khách hàng đã được đánh dấu là từ chối. Dưới đây là thông tin chi tiết.
                      </p>

                      <div class="section tour">
                        <div class="section-title">Thông tin booking</div>
                        <div class="row">
                          <span class="label">Tour</span>
                          <span class="value">%s</span>
                        </div>
                        <div class="row">
                          <span class="label">Mã booking</span>
                          <span class="value">#%d</span>
                        </div>
                        <div class="row">
                          <span class="label">Ngày đặt</span>
                          <span class="value">%s</span>
                        </div>
                        <div class="row">
                          <span class="label">Ngày khởi hành</span>
                          <span class="value">%s</span>
                        </div>
                        <div class="row">
                          <span class="label">Số khách</span>
                          <span class="value">%d khách</span>
                        </div>
                      </div>

                      <div class="section customer">
                        <div class="section-title">Thông tin khách hàng</div>
                        <div class="row">
                          <span class="label">Tên liên hệ</span>
                          <span class="value">%s</span>
                        </div>
                        <div class="row">
                          <span class="label">Email</span>
                          <span class="value">%s</span>
                        </div>
                        <div class="row">
                          <span class="label">Số điện thoại</span>
                          <span class="value">%s</span>
                        </div>
                      </div>

                      <div class="section reason">
                        <div class="section-title">Lý do từ chối</div>
                        <div class="reason-box">
                          %s
                        </div>
                      </div>

                      <div class="section contact">
                        <div class="section-title">Hướng dẫn tiếp theo</div>
                        <p style="font-size: 14px; margin: 0;">
                          • Khách hàng đã được thông báo về việc từ chối booking qua email.
                        </p>
                        <p style="font-size: 14px; margin: 8px 0 0;">
                          • Nếu khách hàng đã thanh toán, cần xử lý hoàn tiền theo quy trình của công ty.
                        </p>
                        <p style="font-size: 14px; margin: 8px 0 0;">
                          • Vui lòng kiểm tra và cập nhật trạng thái booking trên hệ thống.
                        </p>
                      </div>

                      <p style="margin-top: 22px;">
                        Nếu có bất kỳ câu hỏi nào, vui lòng liên hệ với đội ngũ hỗ trợ KDBS.
                      </p>

                      <p>
                        Thân mến,<br>
                        <strong>Đội ngũ KDBS</strong>
                      </p>

                      <div class="footer">
                        Email mang tính thông báo, vui lòng không trả lời trực tiếp.
                      </div>
                    </div>
                  </div>
                </body>
                </html>
                """,
                tour.getTourName(),
                booking.getBookingId(),
                createdDateVi,
                departureTextVi,
                booking.getTotalGuests(),
                booking.getContactName(),
                booking.getContactEmail() != null ? booking.getContactEmail() : "Chưa cung cấp",
                booking.getContactPhone() != null ? booking.getContactPhone() : "Chưa cung cấp",
                reasonTextVi);
    }
} 