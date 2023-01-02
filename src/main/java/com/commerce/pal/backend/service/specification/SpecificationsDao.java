package com.commerce.pal.backend.service.specification;


import com.commerce.pal.backend.models.product.Product;
import com.commerce.pal.backend.models.product.categories.ProductCategory;
import com.commerce.pal.backend.models.transaction.AgentFloat;
import com.commerce.pal.backend.service.specification.utils.SearchCriteria;
import com.commerce.pal.backend.service.specification.utils.SpecificationQueryCriteriaConsumer;
import lombok.extern.java.Log;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;

@Log
@Component
@SuppressWarnings("Duplicates")
public class SpecificationsDao {
    @PersistenceContext
    private EntityManager entityManager;

    public List<ProductCategory> getProductCategory(final List<SearchCriteria> params) {
        final CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        final CriteriaQuery<ProductCategory> query = builder.createQuery(ProductCategory.class);
        final Root r = query.from(ProductCategory.class);

        Predicate predicate = builder.conjunction();
        SpecificationQueryCriteriaConsumer searchProductCategory = new SpecificationQueryCriteriaConsumer(predicate, builder, r);
        params.stream().forEach(searchProductCategory);
        predicate = searchProductCategory.getPredicate();
        query.where(predicate);

        return entityManager.createQuery(query).getResultList();
    }


    public List<Product> getProducts(final List<SearchCriteria> params) {
        final CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        final CriteriaQuery<Product> query = builder.createQuery(Product.class);
        final Root r = query.from(Product.class);

        Predicate predicate = builder.conjunction();
        SpecificationQueryCriteriaConsumer searchProduct = new SpecificationQueryCriteriaConsumer(predicate, builder, r);
        params.stream().forEach(searchProduct);
        predicate = searchProduct.getPredicate();
        query.where(predicate);

        return entityManager.createQuery(query).getResultList();
    }



    public List<Product> getOrderHistory(final List<SearchCriteria> params) {
        final CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        final CriteriaQuery<Product> query = builder.createQuery(Product.class);
        final Root r = query.from(Product.class);

        Predicate predicate = builder.conjunction();
        SpecificationQueryCriteriaConsumer searchOrder = new SpecificationQueryCriteriaConsumer(predicate, builder, r);
        params.stream().forEach(searchOrder);
        predicate = searchOrder.getPredicate();
        query.where(predicate);

        return entityManager.createQuery(query).getResultList();
    }

    public List<AgentFloat> getAgentRequest(final List<SearchCriteria> params) {
        final CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        final CriteriaQuery<AgentFloat> query = builder.createQuery(AgentFloat.class);
        final Root r = query.from(AgentFloat.class);

        Predicate predicate = builder.conjunction();
        SpecificationQueryCriteriaConsumer searchRequest = new SpecificationQueryCriteriaConsumer(predicate, builder, r);
        params.stream().forEach(searchRequest);
        predicate = searchRequest.getPredicate();
        query.where(predicate);

        return entityManager.createQuery(query).getResultList();
    }
}
