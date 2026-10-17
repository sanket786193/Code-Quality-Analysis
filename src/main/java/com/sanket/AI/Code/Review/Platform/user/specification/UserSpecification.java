package com.sanket.AI.Code.Review.Platform.user.specification;

import com.sanket.AI.Code.Review.Platform.user.entity.Role;
import com.sanket.AI.Code.Review.Platform.user.entity.User;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class UserSpecification {

    public static Specification<User> hasRole(String roleName) {
        if (roleName == null || roleName.isBlank()) {
            return null;
        }
        return (root, query, criteriaBuilder) -> {
            query.distinct(true);
            Join<User, Role> roleJoin = root.join("roles");
            return criteriaBuilder.equal(roleJoin.get("roleName"), roleName);
        };
    }

    public static Specification<User> isActive(Boolean active) {
        if (active == null) {
            return null;
        }
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("active"), active);
    }

    public static Specification<User> hasEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("email"), email);
    }

    public static Specification<User> hasCompany(String company) {
        if (company == null || company.isBlank()) {
            return null;
        }
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(criteriaBuilder.lower(root.get("company")), "%" + company.toLowerCase() + "%");
    }

    public static Specification<User> hasGithubUsername(String githubUsername) {
        if (githubUsername == null || githubUsername.isBlank()) {
            return null;
        }
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("githubUsername"), githubUsername);
    }

    public static Specification<User> searchByKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        return (root, query, criteriaBuilder) -> {
            String pattern = "%" + keyword.trim().toLowerCase() + "%";
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("firstName")), pattern));
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("lastName")), pattern));
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("username")), pattern));
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), pattern));
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("company")), pattern));
            return criteriaBuilder.or(predicates.toArray(new Predicate[0]));
        };
    }
}
