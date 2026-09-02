package com.example.knowledgesphere.exception.handler;

import com.example.knowledgesphere.exception.custom.*;
import com.example.knowledgesphere.exception.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)

    public ResponseEntity<ErrorResponse> handleNotFound(

            ResourceNotFoundException ex,

            HttpServletRequest request

    ){

        return build(

                HttpStatus.NOT_FOUND,

                ex.getMessage(),

                request,

                null

        );

    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(

            UnauthorizedException ex,

            HttpServletRequest request

    ) {

        return build(

                HttpStatus.UNAUTHORIZED,

                ex.getMessage(),

                request,

                null

        );

    }

    @ExceptionHandler(BadRequestException.class)

    public ResponseEntity<ErrorResponse> handleBadRequest(

            BadRequestException ex,

            HttpServletRequest request

    ){

        return build(

                HttpStatus.BAD_REQUEST,

                ex.getMessage(),

                request,

                null

        );

    }

    @ExceptionHandler(ConflictException.class)

    public ResponseEntity<ErrorResponse> handleConflict(

            ConflictException ex,

            HttpServletRequest request

    ){

        return build(

                HttpStatus.CONFLICT,

                ex.getMessage(),

                request,

                null

        );

    }

    @ExceptionHandler(ForbiddenException.class)

    public ResponseEntity<ErrorResponse> handleForbidden(

            ForbiddenException ex,

            HttpServletRequest request

    ){

        return build(

                HttpStatus.FORBIDDEN,

                ex.getMessage(),

                request,

                null

        );

    }

    @ExceptionHandler(DocumentProcessingException.class)

    public ResponseEntity<ErrorResponse> handleDocument(

            DocumentProcessingException ex,

            HttpServletRequest request

    ){

        return build(

                HttpStatus.INTERNAL_SERVER_ERROR,

                ex.getMessage(),

                request,

                null

        );

    }

    @ExceptionHandler(AIException.class)

    public ResponseEntity<ErrorResponse> handleAI(

            AIException ex,

            HttpServletRequest request

    ){

        return build(

                HttpStatus.INTERNAL_SERVER_ERROR,

                ex.getMessage(),

                request,

                null

        );

    }

    @ExceptionHandler(MethodArgumentNotValidException.class)

    public ResponseEntity<ErrorResponse> validation(

            MethodArgumentNotValidException ex,

            HttpServletRequest request

    ){

        List<String> errors =

                ex.getBindingResult()

                        .getFieldErrors()

                        .stream()

                        .map(FieldError::getDefaultMessage)

                        .toList();

        return build(

                HttpStatus.BAD_REQUEST,

                "Validation Failed",

                request,

                errors

        );

    }

    @ExceptionHandler(Exception.class)

    public ResponseEntity<ErrorResponse> handleException(

            Exception ex,

            HttpServletRequest request

    ){

        return build(

                HttpStatus.INTERNAL_SERVER_ERROR,

                ex.getMessage(),

                request,

                null

        );

    }

    private ResponseEntity<ErrorResponse> build(

            HttpStatus status,

            String message,

            HttpServletRequest request,

            List<String> errors

    ){

        ErrorResponse response =

                ErrorResponse.builder()

                        .status(status.value())

                        .error(status.getReasonPhrase())

                        .message(message)

                        .path(request.getRequestURI())

                        .timestamp(LocalDateTime.now())

                        .validationErrors(errors)

                        .build();

        return ResponseEntity

                .status(status)

                .body(response);

    }

}