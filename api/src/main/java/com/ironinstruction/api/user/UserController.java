package com.ironinstruction.api.user;

import com.ironinstruction.api.errors.DuplicateEmail;
import com.ironinstruction.api.errors.ResourceNotFound;
import com.ironinstruction.api.requests.CreateUserRequest;
import com.ironinstruction.api.errors.ErrorResponse;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.NoSuchElementException;

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
    @ExceptionHandler(ResourceNotFound.class)
    public ErrorResponse noAccount(ResourceNotFound e) {
        return new ErrorResponse("No account with email '" + e.getResourceId() + "' found" );
    }

    @ResponseBody
    @ResponseStatus(value=HttpStatus.BAD_REQUEST)
    @ExceptionHandler(DuplicateEmail.class)
    public ErrorResponse duplicateEmail(DuplicateEmail e) {
        return new ErrorResponse("Account with email '" + e.getEmail() + "' already exists");
    }

    // POST /users
    @PostMapping
    public User createUser(@RequestBody CreateUserRequest createUserRequest) throws NoSuchAlgorithmException, InvalidKeySpecException, DuplicateEmail {
        try {
            return userService.createUser(createUserRequest.getName(), createUserRequest.getEmail(), createUserRequest.getPassword(), createUserRequest.getUserType());
        } catch(DuplicateKeyException e) {
            throw new DuplicateEmail(createUserRequest.getEmail());
        }
    }

    @GetMapping("/{email}")
    public User getUser(@PathVariable String email) {
        return userService.findUserByEmail(email);
    }
}
