package com.commerce.pal.backend.common;

import org.json.JSONObject;
import org.springframework.stereotype.Component;

@Component
public class ServiceConstants {

    /*
    User Type Key
     */


    public static final Integer customer = 1;
    public static final Integer client = 2;
    public static final Integer custodian = 3;
    public static final Integer driver = 4;
    /**
     * Booking Statuses
     */
    public static final Integer bookPending = 0;
    public static final Integer bookedPaymentInProgress = 1;
    public static final Integer bookDeclinedPayment = 2;
    public static final Integer bookSuccess = 3;
    public static final Integer bookReturned = 4;
    public static final Integer bookCancelled = 5;
    public static final Integer bookRefunded = 10;

    /**
     * Hand Over Status
     */
    public static final Integer pendingToCustomer = 0;
    public static final Integer clientToCustomer = 1;
    public static final Integer customerAccepted = 2;
    public static final Integer customerToClient = 3;
    public static final Integer clientReceived = 5;

    /**
     * Customer Profile Statuses
     */
    public static final Integer profilePending = 0;
    public static final Integer profileValidated = 1;
    public static final Integer profileConfirmed = 3;
    public static final Integer profileDenied = 5;

    /*
     * Payment Types
     *
     */
    public static String rentalPayment = "RENTAL";
    public static String penaltyPayment = "PENALTY";
    public static String extensionPayment = "EXTENSION";
    public static String bookingPayment = "BOOKING";
    public static String savingsDeposit = "SAVINGS";
    public static String walletTopUp = "WALLET-TOP";
    public static String walletCashOut = "WALLET-CASH-OUT";
    public static String fundTransfer = "FUNDS-TRANSFER";
    /*
     *
     * Transaction Status
     */
    public static final Integer TRANSACTION_INITIAL = 0;
    public static final Integer TRANSACTION_STARTED_PROCESSING = 1;
    public static final Integer TRANSACTION_FAILED = 5;
    public static final Integer TRANSACTION_VALIDATING = 4;
    public static final Integer TRANSACTION_SUCCESS = 3;

    /*
     *
     * ACCOUNT TYPES
     */
    public static final String MAIN_WALLET_ACCOUNT = "MAIN";
    public static final String FIXED_INVESTMENT_ACCOUNT = "FIXED_INVESTMENT";
    public static final String INCOME_ACCOUNT = "INCOME";
    public static final String INVESTMENT_ACCOUNT = "INVESTMENT";

    /*
    DOCUMENT TYPES
     */
    public static final String NATIONAL_ID_FRONT = "NATIONAL_ID_FRONT";
    public static final String NATIONAL_ID_BACK = "NATIONAL_ID_BACK";
    public static final String PASSPORT = "PASSPORT";
    public static final String DRIVING_LICENCE_FRONT = "DRIVING_LICENCE_FRONT";
    public static final String DRIVING_LICENCE_BACK = "DRIVING_LICENCE_BACK";
    public static final String SELFIE_DOCUMENT = "SELFIE_DOCUMENT";
    public static final String TRADE_AGREEMENT = "TRADE_AGREEMENT";


    /*
      SERVICE ROLES
     */
    public static final String CUSTOMER = "1";
    public static final String CLIENT = "2";
    public static final String DRIVER = "3";
    public static final String CUSTODIAN = "4";
    public static final String SUB_CLIENT = "5";

    private static final String roles = "{\"1\":\"ROLE_CUSTOMER\",\"2\":\"ROLE_CLIENT\"}";

    public static final JSONObject ROLES_ARRAY = new JSONObject(roles);

    /*
    EMAIL TYPES
     */
    public static final String CREATE_ACCOUNT_VERIFICATION = "CREATE_ACCOUNT_VERIFICATION";
    public static final String PASSWORD_RESET = "PASSWORD_RESET";
    public static final String SUCCESS_EMAIL_CONFIRM = "SUCCESS_EMAIL_CONFIRM";
    public static final String USER_SUCCESS_LOGIN_DEVICE = "USER_SUCCESS_LOGIN_DEVICE";
    public static final String USER_FAILED_LOGIN_DEVICE = "USER_FAILED_LOGIN_DEVICE";
    public static final String USER_REVIEW_STATUS = "USER_REVIEW_STATUS";
    public static final String CLIENT_CASH_PAYMENT = "CLIENT_CASH_PAYMENT";

    /*
    PAYMENT METHODS
     */
    public static final String MPESA_CHECKOUT = "MPESA-CHECKOUT";
    public static final String MPESA_C2B = "MPESA-CHECKOUT";
    public static final String INTER_VISA_CARD = "INTER-VISA-CARD";
    public static final String PAYPAL_TOPUP = "PAYPAL-TOPUP";
    public static final String PAYPAL_CASHOUT = "PAYPAL-CASHOUT";
}
