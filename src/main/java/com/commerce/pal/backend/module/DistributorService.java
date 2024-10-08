package com.commerce.pal.backend.module;

import com.commerce.pal.backend.common.ResponseCodes;
import com.commerce.pal.backend.models.user.Distributor;
import com.commerce.pal.backend.models.user.Merchant;
import com.commerce.pal.backend.repo.user.DistributorRepository;
import com.commerce.pal.backend.utils.GlobalMethods;
import lombok.extern.java.Log;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

@Log
@Service
@SuppressWarnings("Duplicates")
public class DistributorService {

    private final GlobalMethods globalMethods;
    private final DistributorRepository distributorRepository;

    @Autowired
    public DistributorService(GlobalMethods globalMethods,
                              DistributorRepository distributorRepository) {
        this.globalMethods = globalMethods;
        this.distributorRepository = distributorRepository;
    }

    public JSONObject uploadDocs(String distributorId, String fileType, String imageFileUrl) {
        JSONObject responseMap = new JSONObject();
        try {
            distributorRepository.findDistributorByDistributorId(Long.valueOf(distributorId))
                    .ifPresentOrElse(distributor -> {
                        responseMap.put("statusCode", ResponseCodes.SUCCESS)
                                .put("statusDescription", "success")
                                .put("statusMessage", "Request Successful");
                        switch (fileType) {
                            case "IdImage":
                                distributor.setIdImage(imageFileUrl);
                                break;
                            case "PhotoImage":
                                distributor.setPhotoImage(imageFileUrl);
                                break;
                            default:
                                responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                                        .put("statusDescription", "The file type does not exist")
                                        .put("statusMessage", "The file type does not exist");
                                break;
                        }
                        distributorRepository.save(distributor);
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

    public JSONObject updateDistributor(String distributorId, JSONObject payload) {
        JSONObject responseMap = new JSONObject();
        try {
            distributorRepository.findDistributorByDistributorId(Long.valueOf(distributorId))
                    .ifPresentOrElse(distributor -> {
                        distributor.setDistributorName(payload.has("distributorName") ? payload.getString("distributorName") : distributor.getDistributorName());
                        distributor.setDistributorType(payload.has("distributorType") ? payload.getString("distributorType") : distributor.getDistributorType());
                        distributor.setDistrict(payload.has("district") ? payload.getString("district") : distributor.getDistrict());
                        distributor.setLocation(payload.has("location") ? payload.getString("location") : distributor.getLocation());
                        distributor.setCountry(payload.has("country") ? payload.getString("country") : distributor.getCountry());
                        distributor.setCity(payload.has("city") ? payload.getString("city") : distributor.getCity());
                        distributor.setIdNumber(payload.has("idNumber") ? payload.getString("idNumber") : distributor.getIdNumber());
                        distributorRepository.save(distributor);
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
            List<JSONObject> distributors = new ArrayList<>();
            distributorRepository.findAll()
                    .forEach(distributor -> {
                        JSONObject payload = getDistributorInfo(distributor.getDistributorId());
                        distributors.add(payload);
                    });
            responseMap.put("statusCode", ResponseCodes.SUCCESS)
                    .put("list", distributors)
                    .put("statusDescription", "success")
                    .put("statusMessage", "Request Successful");
        } catch (Exception e) {
            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", "failed to process request")
                    .put("statusMessage", "internal system error");
        }
        return responseMap;
    }

    public JSONObject getAllUsers1(JSONObject req) {
        Sort.Direction direction = Sort.Direction.ASC;
        if (req.getString("sortDirection").equalsIgnoreCase("desc"))
            direction = Sort.Direction.DESC;

        String sortBy = req.getString("sortBy").isEmpty() ? "distributorName" : req.getString("sortBy");

        Sort sort = Sort.by(direction, sortBy);
        Pageable pageable = PageRequest.of(req.getInt("page"), req.getInt("size"), sort);

        Page<Distributor> distributorPage = distributorRepository.findAll((Root<Distributor> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (!req.getString("fullName").isEmpty()) {
                String[] distributorNameParts = req.getString("fullName").split("\\s+");
                List<Predicate> namePredicates = new ArrayList<>();
                for (String part : distributorNameParts) {
                    namePredicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("distributorName")), "%" + part.toLowerCase() + "%"));
                }
                predicates.add(criteriaBuilder.or(namePredicates.toArray(new Predicate[0])));
            }

            if (req.getInt("status") != -1)
                predicates.add(criteriaBuilder.equal(root.get("status"), req.getInt("status")));

            if (req.getLong("id") != -1)
                predicates.add(criteriaBuilder.equal(root.get("distributorId"), req.getLong("id")));

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        }, pageable);

        List<JSONObject> distributors = new ArrayList<>();
        distributorPage.forEach(distributor -> {
            JSONObject detail = getDistributorInfo(distributor);
            distributors.add(detail);
        });

        return GlobalMethods.buildResponseWithPagination("distributors", distributors, distributorPage);
    }

    public JSONObject getUser(JSONObject req) {
        JSONObject responseMap = new JSONObject();
        try {
            distributorRepository.findDistributorByDistributorId(req.getLong("userId"))
                    .ifPresentOrElse(distributor -> {
                        JSONObject payload = getDistributorInfo(distributor.getDistributorId());
                        responseMap.put("statusCode", ResponseCodes.SUCCESS)
                                .put("data", payload)
                                .put("statusDescription", "success")
                                .put("statusMessage", "Request Successful");
                    }, () -> {
                        responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                                .put("statusDescription", "Merchant does not exist")
                                .put("statusMessage", "Merchant does not exist");
                    });

        } catch (Exception e) {
            responseMap.put("statusCode", ResponseCodes.SYSTEM_ERROR)
                    .put("statusDescription", "failed to process request")
                    .put("statusMessage", "internal system error");
        }
        return responseMap;
    }

    public JSONObject getDistributorInfo(Long distributorId) {
        AtomicReference<JSONObject> payload = new AtomicReference<>(new JSONObject());
        distributorRepository.findDistributorByDistributorId(distributorId)
                .ifPresent(distributor -> {
                    try {
                        payload.get().put("userId", distributor.getDistributorId());
                        payload.get().put("ownerPhoneNumber", distributor.getPhoneNumber());
                        payload.get().put("distributorName", distributor.getDistributorName());
                        payload.get().put("businessPhoneNumber", distributor.getPhoneNumber());
                        payload.get().put("email", distributor.getEmailAddress());
                        payload.get().put("name", distributor.getDistributorName());
                        payload.get().put("distributorType", distributor.getDistributorType());
                        payload.get().put("idImage", distributor.getIdImage());
                        payload.get().put("photoImage", distributor.getPhotoImage());
                        payload.get().put("tillNumber", distributor.getTillNumber());
                        payload.get().put("commissionAccount", distributor.getCommissionAccount());

                        payload.get().put("IdImage", distributor.getIdImage());
                        payload.get().put("PhotoImage", distributor.getPhotoImage());

                        payload.get().put("district", distributor.getDistrict());
                        payload.get().put("location", distributor.getLocation());
                        payload.get().put("idNumber", distributor.getIdNumber());
                        payload.get().put("country", distributor.getCountry());
                        payload.get().put("city", distributor.getCity());
                        payload.get().put("branch", distributor.getBranch());
                        if (distributor.getDistributorType().contains("A")) {
                            payload.get().put("canRegAgent", 1);
                        } else {
                            payload.get().put("canRegAgent", 0);
                        }
                        if (distributor.getDistributorType().contains("M")) {
                            payload.get().put("canRegMerchant", 1);
                        } else {
                            payload.get().put("canRegMerchant", 0);
                        }
                        if (distributor.getDistributorType().contains("B")) {
                            payload.get().put("canRegBusiness", 1);
                        } else {
                            payload.get().put("canRegBusiness", 0);
                        }
                        payload.get().put("Status", distributor.getStatus().toString());
                        payload.get().put("longitude", distributor.getLongitude());
                        payload.get().put("latitude", distributor.getLatitude());
                        JSONObject customer = globalMethods.getMultiUserCustomer(distributor.getEmailAddress());
                        payload.set(globalMethods.mergeJSONObjects(payload.get(), customer));
                    } catch (Exception ex) {
                        log.log(Level.WARNING, ex.getMessage());
                    }
                });
        return payload.get();
    }

    public JSONObject getDistributorInfo(Distributor distributor) {
        AtomicReference<JSONObject> payload = new AtomicReference<>(new JSONObject());
        try {
            payload.get().put("userId", distributor.getDistributorId());
            payload.get().put("ownerPhoneNumber", distributor.getPhoneNumber());
            payload.get().put("distributorName", distributor.getDistributorName());
            payload.get().put("businessPhoneNumber", distributor.getPhoneNumber());
            payload.get().put("email", distributor.getEmailAddress());
            payload.get().put("name", distributor.getDistributorName());
            payload.get().put("distributorType", distributor.getDistributorType());
            payload.get().put("idImage", distributor.getIdImage());
            payload.get().put("photoImage", distributor.getPhotoImage());
            payload.get().put("tillNumber", distributor.getTillNumber());
            payload.get().put("commissionAccount", distributor.getCommissionAccount());

            payload.get().put("IdImage", distributor.getIdImage());
            payload.get().put("PhotoImage", distributor.getPhotoImage());

            payload.get().put("district", distributor.getDistrict());
            payload.get().put("location", distributor.getLocation());
            payload.get().put("idNumber", distributor.getIdNumber());
            payload.get().put("country", distributor.getCountry());
            payload.get().put("city", distributor.getCity());
            payload.get().put("branch", distributor.getBranch());
            if (distributor.getDistributorType().contains("A")) {
                payload.get().put("canRegAgent", 1);
            } else {
                payload.get().put("canRegAgent", 0);
            }
            if (distributor.getDistributorType().contains("M")) {
                payload.get().put("canRegMerchant", 1);
            } else {
                payload.get().put("canRegMerchant", 0);
            }
            if (distributor.getDistributorType().contains("B")) {
                payload.get().put("canRegBusiness", 1);
            } else {
                payload.get().put("canRegBusiness", 0);
            }
            payload.get().put("Status", distributor.getStatus().toString());
            payload.get().put("longitude", distributor.getLongitude());
            payload.get().put("latitude", distributor.getLatitude());
            JSONObject customer = globalMethods.getMultiUserCustomer(distributor.getEmailAddress());
            payload.set(globalMethods.mergeJSONObjects(payload.get(), customer));
        } catch (Exception ex) {
            log.log(Level.WARNING, ex.getMessage());
        }

        return payload.get();
    }

    //Retrieves a paginated list of Distributors with support for sorting, filtering, searching, and date range.
    public JSONObject getAllDistributors(
            int page,
            int size,
            Sort sort,
            Integer status,
            String city,
            String searchKeyword,
            Timestamp startDate,
            Timestamp endDate
    ) {
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Distributor> distributorPage = distributorRepository.findBySearchKeywordAndDateAndStatus(searchKeyword, startDate, endDate, status, city, pageable);

        List<JSONObject> distributors = new ArrayList<>();
        distributorPage.getContent().stream()
                .forEach(distributor -> {
                    JSONObject detail = new JSONObject();

                    detail.put("distributorName", distributor.getDistributorName());
                    detail.put("distributorType", distributor.getDistributorType());
                    detail.put("status", distributor.getStatus());
                    detail.put("createdBy", distributor.getCreatedBy());
                    detail.put("createdDate", distributor.getCreatedDate());
                    detail.put("deactivatedBy", distributor.getDeactivatedBy());
                    detail.put("deactivatedDate", distributor.getDeactivatedDate());
                    detail.put("activatedBy", distributor.getActivatedBy());
                    detail.put("activatedDate", distributor.getActivatedDate());

                    distributors.add(detail);
                });

        JSONObject paginationInfo = new JSONObject();
        paginationInfo.put("pageNumber", distributorPage.getNumber())
                .put("pageSize", distributorPage.getSize())
                .put("totalElements", distributorPage.getTotalElements())
                .put("totalPages", distributorPage.getTotalPages());

        JSONObject data = new JSONObject();
        data.put("distributors", distributors).
                put("paginationInfo", paginationInfo);

        JSONObject response = new JSONObject();
        response.put("statusCode", ResponseCodes.SUCCESS)
                .put("statusDescription", "Distributor Passed")
                .put("statusMessage", "Distributor Passed")
                .put("data", data);

        return response;
    }
}
