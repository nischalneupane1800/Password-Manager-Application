package com.example.passwordKeepr.passwordKeeprTest.Users.service;
import com.example.passwordKeepr.passwordKeeprTest.Users.entity.User;
import com.example.passwordKeepr.passwordKeeprTest.Users.repository.UsersRepository;
import net.bytebuddy.utility.RandomString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

/* https://stackoverflow.com/questions/32801008/how-to-find-out-if-an-email-already-exist-with-jpa-spring-and-sending-some-error
 * https://mkyong.com/regular-expressions/how-to-validate-password-with-regular-expression/
 * regex to validate a 8-20 character password with at least one digit, one lowercase
 * letter, one uppercase letter, one special character with no white spaces */
@Service
public class RegisterService {

    private static final String PASSWORD_REGEX = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#&()–[{}]:;',?/*~$^+=<>]).{8,20}$";
    private final UsersRepository usersRepository;

    @Autowired
    private JavaMailSender mailSender;
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(PASSWORD_REGEX);
    private boolean duplicateFound = false;
    private PasswordEncoder passwordEncoder;

    @Autowired
    public RegisterService(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    public <lookupRequestObject> String registerUser(Map<String, Object> lookupRequestObject, HttpServletRequest request) throws MessagingException, UnsupportedEncodingException {
        String password = (String) lookupRequestObject.get("password");
        String confirmPassword = (String) lookupRequestObject.get("passwordConfirm");

        List<String> blacklistedValues = Arrays.asList(
                "Passw0rd",
                "Password123",
                "password",
                "Password!",
                "123456",
                "1234567",
                "12345678",
                "123456789",
                "12345678910",
                "qwerty"
        );

        // blacklist users inputted password
        String email = (String) lookupRequestObject.get("email");
        for (int i = 0; i < blacklistedValues.size(); i++) {
            if (password.equals(blacklistedValues.get(i)) == true) {
                this.duplicateFound = true;
                break;
            }
        }

        if (email == "") {
            throw new IllegalStateException("Must provide email!");
        } else if (password == "") {
            throw new IllegalStateException("Must provide password!");
        } else if (confirmPassword == "") {
            throw new IllegalStateException("Must provide password confirmation!");
        } else if (password.equals(confirmPassword) == false) {
            throw new IllegalStateException("Password and password confirmation do not match!");
        } else if (duplicateFound == true) {
            throw new IllegalStateException("You can't use that password! It is too common.");
        }

        User usersFromDb = usersRepository.findByEmail(email);

//        if (usersFromDb != null) {
//            throw new IllegalStateException("User already registered!");
//        } else {
            return this.passwordVerifier(password, email, request);
//        }
    }

    /* https://www.techiedelight.com/validate-password-java/
     * https://mkyong.com/regular-expressions/how-to-validate-password-with-regular-expression/ */
    private String passwordVerifier(String password, String email, HttpServletRequest request) throws MessagingException, UnsupportedEncodingException {
        if (PASSWORD_PATTERN.matcher(password).matches()) {
            return this.commitNewUser(email, password, request);
        } else {
            throw new IllegalStateException("Password does not meet complexity requirement!");
        }
    }

    private String commitNewUser(String email, String password, HttpServletRequest request) throws MessagingException, UnsupportedEncodingException {
        try {
            String uuid = UUID.randomUUID().toString();
            String randomVerificationCode = RandomString.make(64);
            String emailPassword = email + password;
            this.passwordEncoder = new BCryptPasswordEncoder();
            String encodedPassword = this.passwordEncoder.encode(emailPassword);
            User newUser = new User(0, email, encodedPassword, uuid, false, randomVerificationCode, null, 0, false, null, null);
            sendVerificationEmail(newUser, request);
            usersRepository.save(newUser);
            return "User successfully registered! Check your email to verify your new account!";
        } catch (IllegalStateException ex) {
            throw new IllegalStateException("Uh oh, Issue encountered in database! Contact Admin");
        }
    }

    private void sendVerificationEmail(User newUser, HttpServletRequest request) throws UnsupportedEncodingException, MessagingException {
//        String siteUrl = "http:/localhost:3000";
//        String verifyUrl = siteUrl + "/verify" + newUser.getVerificationCode();
//        String subject = "Please verify your registration";
//        String senderName = "Password Manager Application";
//        String mailContent = "<p>Dear " + newUser.getEmail() + ", </p>";
//        mailContent += "<p>Please click the link below to verify your registration and access Password Manager's features!</p>";
//        mailContent += "<h3><a =\"href=" + verifyUrl + "\">VERIFY</a></h3>";
//        mailContent += "<p>Thank you,<br> The Password Manager team</p>";
//
//        MimeMessage message = mailSender.createMimeMessage();
//        MimeMessageHelper helper = new MimeMessageHelper(message);
//
//        helper.setFrom("nischalapplication@gmail.com", senderName);
//        helper.setTo(newUser.getEmail());
//        helper.setSubject(subject);
//        helper.setText(mailContent, true);
//
//        mailSender.send(message);

        String siteUrl = "http://localhost:3000";
        String verifyUrl = siteUrl + "/verify?code=" + newUser.getVerificationCode();
        String subject = "Please verify your registration";
        String senderName = "Password Manager Application";
        String mailContent = "<p>Dear " + newUser.getEmail() + ", </p>";
        mailContent += "<p>Please click the link below to verify your registration and access Password Manager's features!</p>";
        mailContent += "<h3><a href=\"" + verifyUrl + "\">VERIFY</a></h3>";
        mailContent += "<p>Thank you,<br> The Password Manager Team</p>";

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        helper.setFrom("noreply@passwordmanager.com", senderName);
        helper.setTo(newUser.getEmail());
        helper.setSubject(subject);
        helper.setText(mailContent, true);

        try {
            mailSender.send(message);
        } catch (MailException e) {
            e.printStackTrace();
        }
    }
}