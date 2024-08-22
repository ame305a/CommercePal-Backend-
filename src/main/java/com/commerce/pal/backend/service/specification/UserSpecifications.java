package com.commerce.pal.backend.service.specification;


import com.commerce.pal.backend.models.user.Merchant;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

public class UserSpecifications {

    public static Specification<Merchant> merchantNameContains(String[] keywords) {
        return new Specification<Merchant>() {
            @Override
            public Predicate toPredicate(Root<Merchant> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                for (String keyword : keywords) {
                    predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("merchantName")), "%" + keyword.toLowerCase() + "%"));
                }
                predicates.add(criteriaBuilder.equal(root.get("status"), 1));

                return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
            }
        };
    }
}
