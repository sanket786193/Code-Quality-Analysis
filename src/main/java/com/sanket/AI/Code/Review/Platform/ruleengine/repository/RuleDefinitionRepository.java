package com.sanket.AI.Code.Review.Platform.ruleengine.repository;

import com.sanket.AI.Code.Review.Platform.ruleengine.entity.RuleCategory;
import com.sanket.AI.Code.Review.Platform.ruleengine.entity.RuleDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RuleDefinitionRepository extends JpaRepository<RuleDefinition, Long> {

    Optional<RuleDefinition> findByRuleKey(String ruleKey);

    List<RuleDefinition> findByCategory(RuleCategory category);

    List<RuleDefinition> findByEnabledTrue();

    List<RuleDefinition> findByCategoryAndEnabledTrue(RuleCategory category);

    boolean existsByRuleKey(String ruleKey);
}
