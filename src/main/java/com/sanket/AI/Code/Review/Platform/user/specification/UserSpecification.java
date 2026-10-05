package com.sanket.AI.Code.Review.Platform.user.specification;

import com.sanket.AI.Code.Review.Platform.user.entity.Role;
import com.sanket.AI.Code.Review.Platform.user.entity.User;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecification {

    public static Specification<User> hasRole(String roleName) {

        return (root, query, criteriaBuilder) -> {

            Join<User, Role> roleJoin = root.join("roles");

            return criteriaBuilder.equal(roleJoin.get("roleName"), roleName);
        };
    }

    public static Specification<User> isActive(Boolean active){
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("active"), active);
    }
    public static Specification<User> hasEmail(String email){
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("email"), email);
    }

    public static Specification<User> hashasCompany(String hasCompany){
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("email"), hasCompany);
    }
    public static Specification<User> hasGithubUsername(String hasGithubUsername){
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("email"), hasGithubUsername);
    }
}
