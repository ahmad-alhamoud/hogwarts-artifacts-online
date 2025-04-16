package com.ahmad.hogwartsartifactsonline.system.exception;

import com.ahmad.hogwartsartifactsonline.system.Result;
import com.ahmad.hogwartsartifactsonline.system.StatusCode;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class ExceptionHandlerAdvice {


    @ExceptionHandler(ObjectNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    Result handleObjectNotFoundException(ObjectNotFoundException exception) {
        return new Result(false, StatusCode.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    Result handelValidationException(MethodArgumentNotValidException ex) {
        List<ObjectError> errors = ex.getBindingResult().getAllErrors();
        Map<String, String> map = new HashMap<>(errors.size());

        errors.forEach((error) -> {
            String key = ((FieldError) error).getField();
            String val = error.getDefaultMessage();
            map.put(key, val);
        });
        return new Result(false, StatusCode.INVALID_ARGUMENT, "Provided arguments are invalid, see data for details.", map);
    }


    @ExceptionHandler(InsufficientAuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    Result handelInsufficientAuthenticationException(InsufficientAuthenticationException exception) {
        return new Result(false, StatusCode.UNAUTHORIZED, "Login credentials are missing.", exception.getMessage());
    }

    @ExceptionHandler({UsernameNotFoundException.class, BadCredentialsException.class})
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    Result handelAuthenticationException(Exception exception) {
        return new Result(false, StatusCode.UNAUTHORIZED, "username or password is incorrect.", exception.getMessage());
    }

    @ExceptionHandler(AccountStatusException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    Result handelAccountStatusException(AccountStatusException exception) {
        return new Result(false, StatusCode.UNAUTHORIZED, "user account is abnormal", exception.getMessage());
    }

    @ExceptionHandler(InvalidBearerTokenException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    Result handelInvalidBearerTokenException(InvalidBearerTokenException exception) {
        return new Result(false, StatusCode.UNAUTHORIZED, "The access token provided is expired, revoked, or invalid for other reasons.", exception.getMessage());
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    Result handleNoHandlerFoundException(NoHandlerFoundException exception) {
        return new Result(false, StatusCode.NOT_FOUND, "This API endpoint is not found.", exception.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    Result handelAccessDeniedException(AccessDeniedException exception) {
        return new Result(false, StatusCode.FORBIDDEN, "No Permission.", exception.getMessage());
    }


    /*
        Fallback handles any unhandled  exceptions.
    */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    Result handelOtherException(Exception exception) {
        return new Result(false, StatusCode.INTERNAL_SERVER_ERROR, "A server internal error occurs.", exception.getMessage());
    }


}
