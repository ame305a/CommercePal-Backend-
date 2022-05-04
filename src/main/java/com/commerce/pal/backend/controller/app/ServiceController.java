package com.commerce.pal.backend.controller.app;

import com.commerce.pal.backend.repo.setting.*;
import lombok.extern.java.Log;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;


@Log
@CrossOrigin(origins = {"*"}, maxAge = 3600L)
@RestController
@RequestMapping({"/prime/api/v1/service"})
@SuppressWarnings("Duplicates")
public class ServiceController {
    private final BankRepository bankRepository;
    private final CityRepository cityRepository;
    private final CountryRepository countryRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final PaymentMethodItemRepository paymentMethodItemRepository;

    @Autowired
    public ServiceController(BankRepository bankRepository,
                             CityRepository cityRepository,
                             CountryRepository countryRepository,
                             PaymentMethodRepository paymentMethodRepository,
                             PaymentMethodItemRepository paymentMethodItemRepository) {
        this.bankRepository = bankRepository;
        this.cityRepository = cityRepository;
        this.countryRepository = countryRepository;
        this.paymentMethodRepository = paymentMethodRepository;
        this.paymentMethodItemRepository = paymentMethodItemRepository;
    }

    @RequestMapping(value = {"/countries"}, method = {RequestMethod.GET}, produces = {"application/json"})
    @ResponseBody
    public ResponseEntity<?> countryList() {
        List<JSONObject> countries = new ArrayList<>();
        countryRepository.findAll().forEach(country -> {
            JSONObject one = new JSONObject();
            one.put(String.valueOf(country.getCountryCode()), country.getCountry());
            countries.add(one);
        });
        return ResponseEntity.status(HttpStatus.OK).body(countries.toString());
    }

    @RequestMapping(value = {"/cities"}, method = {RequestMethod.GET}, produces = {"application/json"})
    @ResponseBody
    public ResponseEntity<?> citiesList() {
        List<JSONObject> countries = new ArrayList<>();
        cityRepository.findAll().forEach(country -> {
            JSONObject one = new JSONObject();
            one.put(String.valueOf(country.getCityId()), country.getCity());
            countries.add(one);
        });
        return ResponseEntity.status(HttpStatus.OK).body(countries.toString());
    }

    @RequestMapping(value = {"/banks"}, method = {RequestMethod.GET}, produces = {"application/json"})
    @ResponseBody
    public ResponseEntity<?> bankList() {
        List<JSONObject> banks = new ArrayList<>();
        bankRepository.findAll().forEach(bank -> {
            JSONObject one = new JSONObject();
            one.put("name", bank.getBankName());
            one.put("code", bank.getBankCode());
            banks.add(one);
        });
        return ResponseEntity.status(HttpStatus.OK).body(banks.toString());
    }

    @RequestMapping(value = {"/payment-method"}, method = {RequestMethod.GET}, produces = {"application/json"})
    @ResponseBody
    public ResponseEntity<?> paymentMethodList() {
        List<JSONObject> paymentMethods = new ArrayList<>();
        paymentMethodRepository.findPaymentMethodByStatus(1).forEach(paymentMethod -> {
            JSONObject payMed = new JSONObject();
            payMed.put("name", paymentMethod.getName());
            payMed.put("paymentMethod", paymentMethod.getPaymentMethod());
            payMed.put("iconUrl", paymentMethod.getIconUrl());
            List<JSONObject> items = new ArrayList<>();
            paymentMethodItemRepository.findPaymentMethodItemsByPaymentMethodIdAndStatus(
                    paymentMethod.getId(), 1
            ).forEach(paymentMethodItem -> {
                JSONObject item = new JSONObject();
                item.put("paymentMethod", paymentMethod.getPaymentMethod());
                item.put("name", paymentMethodItem.getName());
                item.put("paymentType", paymentMethodItem.getPaymentType());
                item.put("iconUrl", paymentMethodItem.getIconUrl());
                items.add(item);
            });
            payMed.put("items", items);
            paymentMethods.add(payMed);
        });
        JSONObject paymentMeds = new JSONObject();
        paymentMeds.put("paymentMethods", paymentMethods);
        return ResponseEntity.status(HttpStatus.OK).body(paymentMeds.toString());
    }


}
