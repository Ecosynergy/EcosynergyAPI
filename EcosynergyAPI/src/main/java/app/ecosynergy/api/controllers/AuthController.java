package app.ecosynergy.api.controllers;

import app.ecosynergy.api.data.vo.v1.UserVO;
import app.ecosynergy.api.data.vo.v1.security.AccountCredentialsVO;
import app.ecosynergy.api.services.AuthService;
import app.ecosynergy.api.services.EmailService;
import app.ecosynergy.api.util.ConfirmationCodeGenerator;
import app.ecosynergy.api.util.MediaType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Authentication Endpoint")
@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService service;
    private final EmailService emailService;

    @Autowired
    public AuthController(AuthService service, EmailService emailService) {
        this.service = service;
        this.emailService = emailService;
    }

    private static boolean checkIfParamsIsNotNull(AccountCredentialsVO data) {
        return data == null || data.getIdentifier() == null || data.getIdentifier().isBlank() ||
                data.getPassword() == null || data.getPassword().isBlank();
    }

    private static boolean checkIfParamsIsNotNull(String username, String refreshToken) {
        return refreshToken == null || refreshToken.isBlank() || username == null || username.isBlank();
    }

    private static boolean checkIfParamsIsNotNull(UserVO data) {
        return data.getUserName() == null || data.getUserName().isBlank() ||
                data.getPassword() == null || data.getPassword().isBlank();
    }

    @Operation(summary = "Sign in", description = "Authenticate a user and return a token", tags = {"Authentication Endpoint"})
    @PostMapping(value = "/signin",
            produces = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_YML},
            consumes = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_YML}
    )
    public ResponseEntity<?> signIn(@RequestBody AccountCredentialsVO data) {
        if (checkIfParamsIsNotNull(data))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid client request");

        var token = service.signIn(data);

        if (token == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid client request");
        else
            return token;
    }

    @Operation(summary = "Sign up", description = "Create a new user account", tags = {"Authentication Endpoint"})
    @PostMapping(value = "/signup",
            produces = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_YML},
            consumes = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_YML}
    )
    public ResponseEntity<?> signUp(@RequestBody UserVO data) throws MessagingException {
        if (checkIfParamsIsNotNull(data))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid client request");

        UserVO userVO = service.signUp(data);

        return ResponseEntity.ok(userVO);
    }

    @Operation(summary = "Refresh token", description = "Refresh an existing token for a user")
    @PutMapping(value = "/refresh/{username}",
            produces = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_YML}
    )
    public ResponseEntity<?> refreshToken(@PathVariable("username") String username, @RequestHeader("Authorization") String refreshToken) {
        if (checkIfParamsIsNotNull(username, refreshToken))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid client request");

        var token = service.refreshToken(username, refreshToken);

        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid client request");
        } else {
            return token;
        }
    }

    @PostMapping(value = "/signup/send-confirmation-code")
    public ResponseEntity<String> sendConfirmationCode(@RequestParam String email, @RequestParam String name) throws MessagingException {
        String confirmationCode = ConfirmationCodeGenerator.generateCode();

        emailService.sendConfirmationEmail(email, name, confirmationCode);

        return ResponseEntity.ok(confirmationCode);
    }

    @PostMapping(value = "/forgot-password/send-confirmation-code")
    public ResponseEntity<String> sendConfirmationCodeForgotPassword(@RequestParam String email) throws MessagingException {
        String confirmationCode = service.sendConfirmationCode(email);

        return ResponseEntity.ok(confirmationCode);
    }

    @PostMapping(value = "forgot-password")
    public ResponseEntity<UserVO> forgotPassword(@RequestBody UserVO user) {
        UserVO userVO = service.forgotPassword(user);

        return ResponseEntity.ok(userVO);
    }
}
