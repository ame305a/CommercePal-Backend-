package com.commerce.pal.backend.module.users;

import com.commerce.pal.backend.common.ResponseCodes;
import com.commerce.pal.backend.models.product.Product;
import com.commerce.pal.backend.models.user.Merchant;
import com.commerce.pal.backend.module.product.ProductService;
import com.commerce.pal.backend.repo.product.ProductRepository;
import com.commerce.pal.backend.repo.user.MerchantRepository;
import com.commerce.pal.backend.service.specification.UserSpecifications;
import com.commerce.pal.backend.utils.GlobalMethods;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.json.JSONObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

@Log
@Service
@SuppressWarnings("Duplicates")
@RequiredArgsConstructor
public class MerchantService {
    private final GlobalMethods globalMethods;
    private final ProductService productService;
    private final MerchantRepository merchantRepository;
    private final ProductRepository productRepository;


    public JSONObject uploadDocs(String merchantId, String fileType, String imageFileUrl) {
        JSONObject responseMap = new JSONObject();
        try {
            merchantRepository.findMerchantByMerchantId(Long.valueOf(merchantId)).ifPresentOrElse(merchant -> {
                responseMap.put("statusCode", ResponseCodes.SUCCESS)
                        .put("statusDescription", "success")
                        .put("statusMessage", "Request Successful");

                switch (fileType) {
                    case "ShopImage":
                        merchant.setShopImage(imageFileUrl);
                        break;
                    case "CommercialCertImage":
                        merchant.setCommercialCertImage(imageFileUrl);
                        break;
                    case "TillNumberImage":
                        merchant.setTillNumberImage(imageFileUrl);
                        break;
                    case "OwnerPhoto":
                        merchant.setOwnerPhoto(imageFileUrl);
                        break;
                    case "BusinessRegistrationPhoto":
                        merchant.setBusinessRegistrationPhoto(imageFileUrl);
                        break;
                    case "TaxPhoto":
                        merchant.setTaxPhoto(imageFileUrl);
                        break;

                    default:
                        responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                                .put("statusDescription", "The file type does not exist")
                                .put("statusMessage", "The file type does not exist");
                        break;
                }
                merchantRepository.save(merchant);
            }, () -> {
                responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                        .put("statusDescription", "The User does not exist")
                        .put("statusMessage", "The User does not exist");
            });
        } catch (Exception e) {
            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", "failed to process request")
                    .put("statusMessage", "internal system error");
        }
        return responseMap;
    }

    public JSONObject updateMerchant(String merchantId, JSONObject payload) {
        JSONObject responseMap = new JSONObject();
        try {
            merchantRepository.findMerchantByMerchantId(Long.valueOf(merchantId)).ifPresentOrElse(merchant -> {
                merchant.setOwnerPhoneNumber(payload.has("ownerPhoneNumber") ? payload.getString("ownerPhoneNumber") : merchant.getOwnerPhoneNumber());
                merchant.setMerchantName(payload.has("merchantName") ? payload.getString("merchantName") : merchant.getMerchantName());
                merchant.setBusinessType(payload.has("businessType") ? payload.getString("businessType") : merchant.getBusinessType());
                merchant.setBusinessCategory(payload.has("businessCategory") ? payload.getString("businessCategory") : merchant.getBusinessCategory());
                merchant.setBusinessLicense(payload.has("businessLicense") ? payload.getString("businessLicense") : merchant.getBusinessLicense());
                merchant.setCommercialCertNo(payload.has("commercialCertNo") ? payload.getString("commercialCertNo") : merchant.getCommercialCertNo());
                merchant.setTaxNumber(payload.has("taxNumber") ? payload.getString("taxNumber") : merchant.getTaxNumber());
                merchant.setTillNumber(payload.has("tillNumber") ? payload.getString("tillNumber") : merchant.getTillNumber());
                merchant.setBankCode(payload.has("bankCode") ? payload.getString("bankCode") : merchant.getBankCode());
                merchant.setBankAccountNumber(payload.has("bankAccountNumber") ? payload.getString("bankAccountNumber") : merchant.getBankAccountNumber());
                merchant.setBranch(payload.has("branch") ? payload.getString("branch") : merchant.getBranch());
                merchant.setLanguage(payload.has("language") ? payload.getString("language") : merchant.getLanguage());
                merchant.setCountry(payload.has("country") ? payload.getString("country") : merchant.getCountry());
                merchant.setCity(payload.has("city") ? payload.getInt("city") : merchant.getCity());
                merchant.setLongitude(payload.has("longitude") ? payload.getString("longitude") : merchant.getLongitude());
                merchant.setLatitude(payload.has("latitude") ? payload.getString("latitude") : merchant.getLatitude());
                merchant.setLocation(payload.has("location") ? payload.getString("location") : merchant.getLocation());
                merchant.setDistrict(payload.has("district") ? payload.getString("district") : merchant.getDistrict());
                merchantRepository.save(merchant);

                responseMap.put("statusCode", ResponseCodes.SUCCESS)
                        .put("statusDescription", "success")
                        .put("statusMessage", "Request Successful");

            }, () -> {
                responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                        .put("statusDescription", "The User does not exist")
                        .put("statusMessage", "The User does not exist");
            });
        } catch (Exception e) {
            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", "failed to process request")
                    .put("statusMessage", "internal system error");
        }
        return responseMap;
    }

    public JSONObject getUsers(JSONObject req) {
        JSONObject responseMap = new JSONObject();
        try {
            List<JSONObject> merchants = new ArrayList<>();
            merchantRepository.findMerchantsByOwnerIdAndOwnerTypeOrderByCreatedDateDesc(req.getInt("ownerId"), req.getString("ownerType")).forEach(merchant -> {
                JSONObject payload = getMerchantInfo(merchant.getMerchantId());
                merchants.add(payload);
            });
            responseMap.put("statusCode", ResponseCodes.SUCCESS).put("list", merchants).put("statusDescription", "success").put("statusMessage", "Request Successful");
        } catch (Exception e) {
            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR).put("statusDescription", "failed to process request").put("statusMessage", "internal system error");
        }
        return responseMap;
    }

    public JSONObject getAllUsers(JSONObject req) {
        List<JSONObject> merchants = new ArrayList<>();
        merchantRepository.findAll().forEach(merchant -> {
            JSONObject payload = getMerchantInfo(merchant);
            merchants.add(payload);
        });

        return new JSONObject().put("statusCode", ResponseCodes.SUCCESS)
                .put("list", merchants).put("statusDescription", "success")
                .put("statusMessage", "Request Successful");
    }

    public JSONObject getAllUsers1(JSONObject req) {
        Sort.Direction direction = Sort.Direction.ASC;
        if (req.getString("sortDirection").equalsIgnoreCase("desc"))
            direction = Sort.Direction.DESC;

        String sortBy = req.getString("sortBy").isEmpty() ? "merchantName" : req.getString("sortBy");

        Sort sort = Sort.by(direction, sortBy);
        Pageable pageable = PageRequest.of(req.getInt("page"), req.getInt("size"), sort);

        Page<Merchant> merchantPage = merchantRepository.findAll((Root<Merchant> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (!req.getString("fullName").isEmpty()) {
                String[] merchantNameParts = req.getString("fullName").split("\\s+");
                List<Predicate> namePredicates = new ArrayList<>();
                for (String part : merchantNameParts) {
                    namePredicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("merchantName")), "%" + part.toLowerCase() + "%"));
                }
                predicates.add(criteriaBuilder.or(namePredicates.toArray(new Predicate[0])));
            }

            if (req.getInt("status") != -1)
                predicates.add(criteriaBuilder.equal(root.get("status"), req.getInt("status")));

            if (req.getLong("id") != -1)
                predicates.add(criteriaBuilder.equal(root.get("merchantId"), req.getLong("id")));

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        }, pageable);

        List<JSONObject> merchants = new ArrayList<>();
        merchantPage.forEach(merchant -> {
            JSONObject detail = getMerchantInfo(merchant);
            merchants.add(detail);
        });

        return GlobalMethods.buildResponseWithPagination("Merchant", merchants, merchantPage);
    }


    public JSONObject getUser(JSONObject req) {
        JSONObject responseMap = new JSONObject();
        try {
            merchantRepository.findMerchantByOwnerIdAndOwnerTypeAndMerchantId(req.getInt("ownerId"), req.getString("ownerType"), req.getLong("userId")).ifPresentOrElse(merchant -> {
                JSONObject payload = getMerchantInfo(merchant.getMerchantId());
                responseMap.put("statusCode", ResponseCodes.SUCCESS).put("data", payload).put("statusDescription", "success").put("statusMessage", "Request Successful");
            }, () -> {
                responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR).put("statusDescription", "Merchant does not exist").put("statusMessage", "Merchant does not exist");
            });

        } catch (Exception e) {
            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR).put("statusDescription", "failed to process request").put("statusMessage", "internal system error");
        }
        return responseMap;
    }

    public Integer countUser(JSONObject payload) {
        Integer count = 0;
        count = merchantRepository.countByOwnerIdAndOwnerType(payload.getInt("ownerId"), payload.getString("ownerType"));
        return count;
    }

    public JSONObject getMerchantInfo(Long merchantId) {
        AtomicReference<JSONObject> payload = new AtomicReference<>(new JSONObject());
        merchantRepository.findMerchantByMerchantId(merchantId)
                .ifPresent(merchant -> {
                    try {
                        payload.get().put("userId", merchant.getMerchantId());
                        payload.get().put("ownerPhoneNumber", merchant.getOwnerPhoneNumber());
                        payload.get().put("ownerType", merchant.getOwnerType());
                        payload.get().put("email", merchant.getEmailAddress());
                        payload.get().put("name", merchant.getMerchantName());
                        payload.get().put("businessType", merchant.getBusinessType());
                        payload.get().put("businessCategory", merchant.getBusinessCategory());
                        payload.get().put("businessLicense", merchant.getBusinessLicense());
                        payload.get().put("commercialCertNo", merchant.getCommercialCertNo());
                        payload.get().put("tillNumber", merchant.getTillNumber());
                        payload.get().put("commissionAccount", merchant.getCommissionAccount());
                        payload.get().put("taxNumber", merchant.getTaxNumber());
                        payload.get().put("bankCode", merchant.getBankCode());
                        payload.get().put("bankAccountNumber", merchant.getBankAccountNumber());
                        payload.get().put("branch", merchant.getBranch());
                        payload.get().put("language", merchant.getLanguage());
                        payload.get().put("country", merchant.getCountry());
                        payload.get().put("city", merchant.getCity());
                        payload.get().put("longitude", merchant.getLongitude());
                        payload.get().put("latitude", merchant.getLatitude());
                        payload.get().put("ownerPhoto", merchant.getOwnerPhoto());
                        payload.get().put("businessRegistrationPhoto", merchant.getBusinessRegistrationPhoto());
                        payload.get().put("taxPhoto", merchant.getTaxPhoto());

                        payload.get().put("OwnerPhoto", merchant.getOwnerPhoto());
                        payload.get().put("BusinessRegistrationPhoto", merchant.getBusinessRegistrationPhoto());
                        payload.get().put("TaxPhoto", merchant.getTaxPhoto());
                        payload.get().put("TillNumberImage", merchant.getTillNumberImage());
                        payload.get().put("CommercialCertImage", merchant.getCommercialCertImage());
                        payload.get().put("ShopImage", merchant.getShopImage());


                        payload.get().put("Status", merchant.getStatus().toString());
                        payload.get().put("termOfService", merchant.getTermsOfServiceStatus());
                        JSONObject customer = globalMethods.getMultiUserCustomer(merchant.getEmailAddress());
                        payload.set(globalMethods.mergeJSONObjects(payload.get(), customer));
                    } catch (Exception ex) {
                        log.log(Level.WARNING, ex.getMessage());
                    }
                });
        return payload.get();
    }


    public JSONObject getMerchantInfo(Merchant merchant) {
        AtomicReference<JSONObject> payload = new AtomicReference<>(new JSONObject());
        try {
            payload.get().put("userId", merchant.getMerchantId());
            payload.get().put("ownerPhoneNumber", merchant.getOwnerPhoneNumber());
            payload.get().put("ownerType", merchant.getOwnerType());
            payload.get().put("email", merchant.getEmailAddress());
            payload.get().put("name", merchant.getMerchantName().trim());
            payload.get().put("businessType", merchant.getBusinessType());
            payload.get().put("businessCategory", merchant.getBusinessCategory());
            payload.get().put("businessLicense", merchant.getBusinessLicense());
            payload.get().put("commercialCertNo", merchant.getCommercialCertNo());
            payload.get().put("tillNumber", merchant.getTillNumber());
            payload.get().put("commissionAccount", merchant.getCommissionAccount());
            payload.get().put("taxNumber", merchant.getTaxNumber());
            payload.get().put("bankCode", merchant.getBankCode());
            payload.get().put("bankAccountNumber", merchant.getBankAccountNumber());
            payload.get().put("branch", merchant.getBranch());
            payload.get().put("language", merchant.getLanguage());
            payload.get().put("country", merchant.getCountry());
            payload.get().put("city", merchant.getCity());
            payload.get().put("longitude", merchant.getLongitude());
            payload.get().put("latitude", merchant.getLatitude());
            payload.get().put("ownerPhoto", merchant.getOwnerPhoto());
            payload.get().put("businessRegistrationPhoto", merchant.getBusinessRegistrationPhoto());
            payload.get().put("taxPhoto", merchant.getTaxPhoto());
            payload.get().put("OwnerPhoto", merchant.getOwnerPhoto());
            payload.get().put("BusinessRegistrationPhoto", merchant.getBusinessRegistrationPhoto());
            payload.get().put("TaxPhoto", merchant.getTaxPhoto());
            payload.get().put("TillNumberImage", merchant.getTillNumberImage());
            payload.get().put("CommercialCertImage", merchant.getCommercialCertImage());
            payload.get().put("ShopImage", merchant.getShopImage());
            payload.get().put("Status", merchant.getStatus().toString());
            payload.get().put("termOfService", merchant.getTermsOfServiceStatus());
            JSONObject customer = globalMethods.getMultiUserCustomer(merchant.getEmailAddress());
            payload.set(globalMethods.mergeJSONObjects(payload.get(), customer));
        } catch (Exception ex) {
            log.log(Level.WARNING, ex.getMessage());
        }

        return payload.get();
    }

    public JSONObject getMerchantAddressInfo(Long merchantId) {
        JSONObject payload = new JSONObject();
        merchantRepository.findMerchantByMerchantId(merchantId).ifPresent(merchant -> {
            payload.put("phoneNumber", merchant.getOwnerPhoneNumber());
            payload.put("email", merchant.getEmailAddress());
            payload.put("name", merchant.getMerchantName());
            payload.put("country", merchant.getCountry());
            payload.put("city", merchant.getCity());
            payload.put("cityName", globalMethods.cityName(Long.valueOf(merchant.getCity())));
            payload.put("regionId", merchant.getRegionId());
            payload.put("serviceCodeId", merchant.getServiceCodeId());
            payload.put("physicalAddress", merchant.getPhysicalAddress());
            payload.put("latitude", merchant.getLatitude());
            payload.put("longitude", merchant.getLongitude());
        });
        return payload;
    }

    public JSONObject merchantStatus(JSONObject request) {
        JSONObject responseMap = new JSONObject();
        JSONObject updateRes = productService.enableDisableAccount(request);
        if (updateRes.getString("Status").equals("00")) {
            responseMap.put("statusCode", ResponseCodes.SUCCESS).put("statusDescription", "success").put("statusMessage", "Request Successful");
        } else {
            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR).put("statusDescription", "failed to process request").put("statusMessage", "internal system error");
        }
        return responseMap;
    }


    //Retrieves a paginated list of Merchants with support for sorting, filtering, searching, and date range.
    public JSONObject getAllMerchants(int page, int size, Sort sort, Integer status, Integer city, Integer regionId, String searchKeyword, Timestamp startDate, Timestamp endDate) {
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Merchant> merchantPage = merchantRepository.findByFilterAndDateAndStatus(searchKeyword, startDate, endDate, status, city, regionId, pageable);

        List<JSONObject> merchants = new ArrayList<>();
        merchantPage.getContent().forEach(merchant -> {
            JSONObject detail = new JSONObject();
            String regionName = merchant.getRegionId() != null ? globalMethods.regionName(merchant.getRegionId()) : "";

            detail.put("OwnerType", merchant.getOwnerType() != null ? merchant.getOwnerType() : "");
            detail.put("merchantName", merchant.getMerchantName() != null ? merchant.getMerchantName() : "");
            detail.put("businessType", merchant.getBusinessType() != null ? merchant.getBusinessType() : "");
            detail.put("businessCategory", merchant.getBusinessCategory() != null ? merchant.getBusinessCategory() : "");
            detail.put("status", merchant.getStatus() != null ? merchant.getStatus() : "");
            detail.put("region", regionName);
            detail.put("cityName", globalMethods.cityName(Long.valueOf(merchant.getCity())));
            detail.put("location", merchant.getLocation() != null ? merchant.getLocation() : "");
            detail.put("registeredBy", merchant.getRegisteredBy() != null ? merchant.getRegisteredBy() : "");
//                    detail.put("registeredDate", merchant.getRegisteredDate() != null ? merchant.getRegisteredDate() : "");
            detail.put("registeredDate", merchant.getRegisteredDate() != null ? merchant.getRegisteredDate() : merchant.getCreatedDate());
            detail.put("authorizedBy", merchant.getAuthorizedBy() != null ? merchant.getAuthorizedBy() : "");
            detail.put("deactivatedBy", merchant.getDeactivatedBy() != null ? merchant.getDeactivatedBy() : "");
            detail.put("deactivatedDate", merchant.getDeactivatedDate() != null ? merchant.getDeactivatedDate() : "");
            detail.put("activatedBy", merchant.getActivatedBy() != null ? merchant.getActivatedBy() : "");
            detail.put("activatedDate", merchant.getActivatedDate() != null ? merchant.getActivatedDate() : "");

            merchants.add(detail);
        });

        JSONObject paginationInfo = new JSONObject();
        paginationInfo.put("pageNumber", merchantPage.getNumber()).put("pageSize", merchantPage.getSize()).put("totalElements", merchantPage.getTotalElements()).put("totalPages", merchantPage.getTotalPages());

        JSONObject data = new JSONObject();
        data.put("merchants", merchants).put("paginationInfo", paginationInfo);

        JSONObject response = new JSONObject();
        response.put("statusCode", ResponseCodes.SUCCESS).put("statusDescription", "Merchant Passed").put("statusMessage", "Merchant Passed").put("data", data);

        return response;
    }

    public JSONObject getAllMerchants(Sort sort) {
        List<Merchant> merchantList = merchantRepository.findAll(sort);

        List<JSONObject> merchants = new ArrayList<>();
        merchantList.forEach(merchant -> {
            JSONObject detail = new JSONObject();

            detail.put("merchantId", merchant.getMerchantId());
            detail.put("merchantName", merchant.getMerchantName());
            detail.put("status", merchant.getStatus());

            merchants.add(detail);
        });

        JSONObject data = new JSONObject();
        data.put("merchants", merchants);

        JSONObject response = new JSONObject();
        response.put("statusCode", ResponseCodes.SUCCESS).put("statusDescription", "Merchant Passed").put("statusMessage", "Merchant Passed").put("data", data);

        return response;
    }

    public JSONObject searchMerchants(String name, int page, int size) {
        String[] words = name.trim().split("\\s+");
        Specification<Merchant> spec = UserSpecifications.merchantNameContains(words);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.asc("merchantName")));
        Page<Merchant> merchantPage = merchantRepository.findAll(spec, pageable);

        List<JSONObject> merchants = new ArrayList<>();
        merchantPage.getContent().forEach(merchant -> {
            if (merchant.getMerchantName() != null) {
                merchants.add(createMerchantJson(merchant));
            }
        });

        JSONObject paginationInfo = new JSONObject();
        paginationInfo.put("pageNumber", merchantPage.getNumber()).put("pageSize", merchantPage.getSize()).put("totalElements", merchantPage.getTotalElements()).put("totalPages", merchantPage.getTotalPages());

        JSONObject data = new JSONObject();
        data.put("data", merchants).put("paginationInfo", paginationInfo);

        JSONObject response = new JSONObject();
        response.put("statusCode", ResponseCodes.SUCCESS).put("statusDescription", "Success").put("statusMessage", "Success").put("data", data);

        return response;
    }

    public JSONObject getMerchantAboutWithProducts(Long merchantId) {
        JSONObject response = new JSONObject();

        Optional<Merchant> optionalMerchant = merchantRepository.findById(merchantId);
        if (optionalMerchant.isEmpty())
            return response.put("statusCode", ResponseCodes.RECORD_NOT_FOUND).put("statusDescription", "Not found").put("statusMessage", "Not found");

        Merchant merchant = optionalMerchant.get();

        JSONObject merchantAbout = createMerchantJson(merchant);

        List<Product> productList = productRepository.findByOwnerTypeAndMerchantIdAndStatus("MERCHANT", merchantId, 1);
        List<JSONObject> products = new ArrayList<>();
        productList.forEach(pro -> {
            JSONObject detail = productService.getProductListDetailsAlready(pro);
            products.add(detail);
        });

        JSONObject data = new JSONObject();
        data.put("merchantAbout", merchantAbout)
                .put("products", products);

        response.put("statusCode", ResponseCodes.SUCCESS)
                .put("statusDescription", "Success")
                .put("statusMessage", "Success")
                .put("data", data);

        return response;
    }


    private JSONObject createMerchantJson(Merchant merchant) {
        JSONObject merchantAbout = new JSONObject();
        String regionName = merchant.getRegionId() != null ? globalMethods.regionName(merchant.getRegionId()) : "";
        merchantAbout.put("merchantId", merchant.getMerchantId());
        merchantAbout.put("merchantName", merchant.getMerchantName());
        merchantAbout.put("Country", merchant.getCountry().equalsIgnoreCase("ET") ? "Ethiopia" : merchant.getCountry());
        merchantAbout.put("region", regionName);
        merchantAbout.put("cityName", globalMethods.cityName(Long.valueOf(merchant.getCity())));
        merchantAbout.put("location", merchant.getLocation() != null ? merchant.getLocation() : "");
        merchantAbout.put("physicalAddress", merchant.getPhysicalAddress() != null ? merchant.getPhysicalAddress() : "");
        merchantAbout.put("businessPhoneNumber", merchant.getBusinessPhoneNumber());
        merchantAbout.put("shopImage", merchant.getShopImage() != null ? merchant.getShopImage() : "");

        return merchantAbout;
    }

}


