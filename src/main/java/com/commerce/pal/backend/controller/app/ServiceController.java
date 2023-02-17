package com.commerce.pal.backend.controller.app;

import com.commerce.pal.backend.common.ResponseCodes;
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
    private final RegionRepository regionRepository;
    private final CountryRepository countryRepository;
    private final AppVersionRepository appVersionRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final PaymentMethodItemRepository paymentMethodItemRepository;

    @Autowired
    public ServiceController(BankRepository bankRepository,
                             CityRepository cityRepository,
                             RegionRepository regionRepository,
                             CountryRepository countryRepository,
                             AppVersionRepository appVersionRepository,
                             PaymentMethodRepository paymentMethodRepository,
                             PaymentMethodItemRepository paymentMethodItemRepository) {
        this.bankRepository = bankRepository;
        this.cityRepository = cityRepository;
        this.regionRepository = regionRepository;
        this.countryRepository = countryRepository;
        this.appVersionRepository = appVersionRepository;
        this.paymentMethodRepository = paymentMethodRepository;
        this.paymentMethodItemRepository = paymentMethodItemRepository;
    }

    @RequestMapping(value = {"/countries"}, method = {RequestMethod.GET}, produces = {"application/json"})
    @ResponseBody
    public ResponseEntity<?> countryList() {
        List<JSONObject> countries = new ArrayList<>();
        countryRepository.findAll().forEach(country -> {
            JSONObject one = new JSONObject();
            one.put("countryCode", country.getCountryCode());
            one.put("country", country.getCountry());
            countries.add(one);
        });
        JSONObject response = new JSONObject();
        response.put("statusCode", ResponseCodes.SUCCESS)
                .put("data", countries)
                .put("statusDescription", "Product Passed")
                .put("statusMessage", "Product Passed");

        return ResponseEntity.status(HttpStatus.OK).body(response.toString());
    }

    @RequestMapping(value = {"/regions"}, method = {RequestMethod.GET}, produces = {"application/json"})
    @ResponseBody
    public ResponseEntity<?> regionList() {
        List<JSONObject> list = new ArrayList<>();
        regionRepository.findAll().forEach(region -> {
            JSONObject one = new JSONObject();
            one.put("regionName", region.getRegionName());
            one.put("regionCode", region.getRegionCode());
            one.put("regionId", region.getId());
            list.add(one);
        });
        JSONObject response = new JSONObject();
        response.put("statusCode", ResponseCodes.SUCCESS)
                .put("data", list)
                .put("statusDescription", "Product Passed")
                .put("statusMessage", "Product Passed");

        return ResponseEntity.status(HttpStatus.OK).body(response.toString());
    }

    @RequestMapping(value = {"/cities"}, method = {RequestMethod.GET}, produces = {"application/json"})
    @ResponseBody
    public ResponseEntity<?> citiesList() {

        List<JSONObject> countries = new ArrayList<>();
        cityRepository.findAll().forEach(country -> {
            JSONObject one = new JSONObject();
            one.put("cityName", country.getCity());
            one.put("cityId", country.getCityId());
            one.put("countryId", country.getCountryId());
            countries.add(one);
        });
        JSONObject response = new JSONObject();
        response.put("statusCode", ResponseCodes.SUCCESS)
                .put("data", countries)
                .put("statusDescription", "Product Passed")
                .put("statusMessage", "Product Passed");

        return ResponseEntity.status(HttpStatus.OK).body(response.toString());
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
        JSONObject response = new JSONObject();
        response.put("statusCode", ResponseCodes.SUCCESS)
                .put("data", banks)
                .put("statusDescription", "Product Passed")
                .put("statusMessage", "Product Passed");

        return ResponseEntity.status(HttpStatus.OK).body(response.toString());
    }

    @RequestMapping(value = {"/app-version"}, method = {RequestMethod.GET}, produces = {"application/json"})
    @ResponseBody
    public ResponseEntity<?> appVersion() {
        JSONObject one = new JSONObject();
        appVersionRepository.findById(1)
                .ifPresent(appVersion -> {
                    one.put("AndroidVersion", appVersion.getAndroidVersion());
                    one.put("IosVersion", appVersion.getIosVersion());
                    one.put("AndroidUpdate", appVersion.getAndroidVersion());
                    one.put("AndroidUpdateType", appVersion.getAndroidUpdateType());
                    one.put("AndroidComment", appVersion.getAndroidComment());
                    one.put("IosUpdate", appVersion.getIosUpdate());
                    one.put("iosUpdateType", appVersion.getIosUpdateType());
                    one.put("IosComment", appVersion.getIosComment());
                    one.put("SessionTimeout", appVersion.getSessionTimeout());
                });

        JSONObject response = new JSONObject();
        response.put("statusCode", ResponseCodes.SUCCESS)
                .put("data", one)
                .put("statusDescription", "Success")
                .put("statusMessage", "Success");

        return ResponseEntity.status(HttpStatus.OK).body(response.toString());
    }

    @RequestMapping(value = {"/payment-method"}, method = {RequestMethod.GET}, produces = {"application/json"})
    @ResponseBody
    public ResponseEntity<?> paymentMethodList(@RequestParam("userType") String userType) {
        List<JSONObject> paymentMethods = new ArrayList<>();
        paymentMethodRepository.findPaymentMethodByStatus(1).forEach(paymentMethod -> {
            if (paymentMethod.getUserType().contains(userType)) {
                JSONObject payMed = new JSONObject();
                payMed.put("name", paymentMethod.getName());
                payMed.put("paymentMethod", paymentMethod.getPaymentMethod());
                payMed.put("iconUrl", paymentMethod.getIconUrl());
                List<JSONObject> items = new ArrayList<>();
                paymentMethodItemRepository.findPaymentMethodItemsByPaymentMethodIdAndStatus(
                        paymentMethod.getId(), 1
                ).forEach(paymentMethodItem -> {
                    if (paymentMethodItem.getUserType().contains(userType)) {
                        JSONObject item = new JSONObject();
                        item.put("paymentMethod", paymentMethod.getPaymentMethod());
                        item.put("name", paymentMethodItem.getName());
                        item.put("paymentType", paymentMethodItem.getPaymentType());
                        item.put("iconUrl", paymentMethodItem.getIconUrl());
                        items.add(item);
                    }
                });
                payMed.put("items", items);
                paymentMethods.add(payMed);
            }
        });
        JSONObject paymentMeds = new JSONObject();
        paymentMeds.put("paymentMethods", paymentMethods);

        JSONObject response = new JSONObject();
        response.put("statusCode", ResponseCodes.SUCCESS)
                .put("data", paymentMeds)
                .put("statusDescription", "Product Passed")
                .put("statusMessage", "Product Passed");

        return ResponseEntity.status(HttpStatus.OK).body(response.toString());
    }


}
