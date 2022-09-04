package com.ironinstruction.api.user;

import com.ironinstruction.api.errors.DuplicateEmail;
import com.ironinstruction.api.errors.ResourceNotFound;
import com.ironinstruction.api.requests.CreateUserRequest;
import com.ironinstruction.api.requests.UpdateAthleteRequest;
import com.ironinstruction.api.requests.UpdateUserRequest;
import com.ironinstruction.api.utils.TokenManager;
import com.ironinstruction.api.utils.TokenType;
import com.ironinstruction.api.errors.ErrorResponse;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.context.SecurityContextHolder;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }
	
    @ResponseBody
    @ResponseStatus(value=HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler({NoSuchAlgorithmException.class, InvalidKeySpecException.class})
    public ErrorResponse hashingError() {
        return new ErrorResponse("Error hashing password");
    }

    @ResponseBody
    @ResponseStatus(value=HttpStatus.BAD_REQUEST)
    @ExceptionHandler(DuplicateEmail.class)
    public ErrorResponse duplicateEmail(DuplicateEmail e) {
        return new ErrorResponse("Account with email '" + e.getEmail() + "' already exists");
    }

    // POST /users
    @PostMapping
    public User createUser(@RequestBody CreateUserRequest createUserRequest) throws HttpMessageNotReadableException, NoSuchAlgorithmException, InvalidKeySpecException, DuplicateEmail {
        try {
            return userService.cleanseUser(userService.createUser(createUserRequest.getName(), createUserRequest.getEmail(), createUserRequest.getPassword(), createUserRequest.getUserType()));
        } catch(DuplicateKeyException e) {
            throw new DuplicateEmail(createUserRequest.getEmail());
        }
    }
	
    @GetMapping("/me")
    public User getUser() throws ResourceNotFound {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        return userService.cleanseUser(userService.findByEmail(userEmail));
    }

    @PostMapping("/me")
    public ResponseEntity<User> updateUserInfo(@RequestBody UpdateUserRequest request, HttpServletResponse response) throws ResourceNotFound {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        
        User updatedUser = userService.cleanseUser(userService.updateUserInfoByEmail(userEmail, request.getEmail(), request.getName()));
        
        // send new access token with updated cookie
        Cookie accessTokenCookie = new Cookie("accessToken", TokenManager.generateJWT(request.getEmail() + ";" + updatedUser.getUserType(), TokenType.ACCESS));
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setSecure(true);
        accessTokenCookie.setPath("/");

        response.addCookie(accessTokenCookie);

        return new ResponseEntity<User>(updatedUser, HttpStatus.OK);
    }

    @PostMapping("/me/athlete")
	public Athlete updateAthleteInfo(@RequestBody UpdateAthleteRequest request) throws ResourceNotFound {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        return (Athlete) userService.cleanseUser(userService.updateAthleteInfoByEmail(
            userEmail, 
            request.getWeightClass(),
            request.getWeight(),
            request.getDob(),
            request.getSquatMax(),
            request.getBenchMax(),
            request.getDeadliftMax(),
            request.getHeight()
        ));
    }
}
