1/ Luồng
Yêu cầu đặt lại mật khẩu -> Người dùng nhập email
Gửi OTP -> Hệ thống gửi mã OTP 6 ký tự qua email
Xác thực OTP -> Người dùng nhập mã OTP
Đặt lại mật khẩu -> Người dùng nhập mật khẩu mới
Hoàn tất -> Gửi email xác nhận thành công

2/ API endpoint

API yêu cau dat lai mat khau
POST /api/auth/forgot-password/request
{
    "email": "user@example.com"
}
Response:

{
    "code": 1000,
    "message": "OTP sent successfully to your email",
    "result": null
}


API xac thuc OTP
POST /api/auth/forgot-password/verify-otp?email=user@example.com&otpCode=123456


Response:
{
    "code": 1000,
    "message": "OTP is valid",
    "result": true
}

API Dat lai mat khau
POST /api/auth/forgot-password/reset

{
    "email": "user@example.com",
    "otpCode": "123456",
    "newPassword": "newpassword123"
}

{
    "code": 1000,
    "message": "Password reset successfully",
    "result": null
}


Mã OTP có hiệu lực trong 5 phút
Tối đa 3 lần yêu cầu OTP trong 1 giờ
OTP chỉ sử dụng được 1 lần

Email phải tồn tại trong hệ thống
User không được bị ban
Mật khẩu mới phải có ít nhất 8 ký tự


CREATE TABLE OTP (
    otp_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    otp_code VARCHAR(6) NOT NULL,
    created_at DATETIME NOT NULL,
    expires_at DATETIME NOT NULL,
    is_used BOOLEAN DEFAULT FALSE,
    purpose VARCHAR(50) NOT NULL
);

1004: Email không tồn tại
1011: OTP không hợp lệ
1012: User bị ban
1013: Quá nhiều yêu cầu OTP
1014: OTP đã hết hạn
1015: Đặt lại mật khẩu thất bại


