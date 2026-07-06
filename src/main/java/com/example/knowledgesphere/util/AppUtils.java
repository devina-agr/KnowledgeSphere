package com.example.knowledgesphere.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public final class AppUtils {

    private AppUtils(){}

    public static String uuid(){

        return UUID.randomUUID().toString();

    }

    public static String now(){

        return LocalDateTime.now()

                .format(

                        DateTimeFormatter

                                .ofPattern(

                                        "dd-MM-yyyy HH:mm:ss"

                                )

                );

    }

}