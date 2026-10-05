package com.sanket.AI.Code.Review.Platform.user.service.impl;

import com.sanket.AI.Code.Review.Platform.user.entity.User;
import com.sanket.AI.Code.Review.Platform.user.repository.UserRepository;
import com.sanket.AI.Code.Review.Platform.user.service.UserService;
import com.sanket.AI.Code.Review.Platform.user.specification.UserSpecification;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository USER_REPOSITORY;

    public UserServiceImpl(UserRepository userRepository) {
        USER_REPOSITORY = userRepository;
    }

    @Override
    public List<User> searchUsers() {
        Specification specification = Specification.where(UserSpecification.hasRole("hasRole"))
                .and(UserSpecification.isActive(true));

        return USER_REPOSITORY.findAll(specification);
    }
}
