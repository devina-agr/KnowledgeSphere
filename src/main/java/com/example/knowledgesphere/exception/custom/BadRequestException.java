package com.example.knowledgesphere.exception.custom;

public class BadRequestException extends RuntimeException{

    public BadRequestException(String message){

        super(message);

    }

}