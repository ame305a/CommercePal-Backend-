package com.commerce.pal.backend.common;

public class EndPointConstant {

    public static String versioning = "/prime/api/v1";

    public static String[] whitelistEndpoints = {

            /*
            PORTAL
           */
            versioning + "/portal/**",
            versioning + "/portal/app/**",
            versioning + "/app/dashboard/**",

            /*
            DATA ACCESS ENDPOINT
             */
            versioning + "/data/request",
            versioning + "/data/financial/request",
            /*
            OPEN APIS FOR APPS
             */
            versioning + "/app/*",
            versioning + "/customer/order/get-pricing",
            /*
            UPLOADS
             */
            versioning + "/upload/image",
            /*
            REGISTRATION
             */
            versioning + "/auth-user",
            versioning + "/authenticate",
            versioning + "/password-reset",
            versioning + "/confirm-code",
            versioning + "/registration",
            versioning + "/client/registration",

            /*
            CATEGORIES
             */
            "/prime/api/v1/product/**",
            "/images/***",
            "/vehicle/images/**",
            "/document/**",
            versioning + "/upload/get-document/**",

            /*
            USED OPEN SERVICES
             */
            versioning + "/service/**",
            versioning + "/service/sale/**",
    };
}
