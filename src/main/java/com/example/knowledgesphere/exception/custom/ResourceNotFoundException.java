package com.example.knowledgesphere.exception.custom;

public class ResourceNotFoundException extends RuntimeException{

    public ResourceNotFoundException(String message){

        super(message);

    }

}