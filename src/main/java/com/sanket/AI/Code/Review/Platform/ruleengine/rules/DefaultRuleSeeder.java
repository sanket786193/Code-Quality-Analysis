package com.sanket.AI.Code.Review.Platform.ruleengine.rules;

import com.sanket.AI.Code.Review.Platform.ruleengine.entity.RuleCategory;
import com.sanket.AI.Code.Review.Platform.ruleengine.entity.RuleDefinition;
import com.sanket.AI.Code.Review.Platform.ruleengine.entity.RuleSeverity;
import com.sanket.AI.Code.Review.Platform.ruleengine.repository.RuleDefinitionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DefaultRuleSeeder implements CommandLineRunner {

    private final RuleDefinitionRepository ruleRepository;

    @Override
    public void run(String... args) {
        if (ruleRepository.count() > 0) {
            log.info("Rule database already initialized with {} rules.", ruleRepository.count());
            return;
        }

        log.info("Pre-populating enterprise rule engine catalog covering all 20 categories...");
        List<RuleDefinition> rules = new ArrayList<>();

        // 1. Exception Handling
        rules.add(RuleDefinition.builder()
                .ruleKey("EXC-001")
                .name("Avoid Generic Catch Blocks")
                .category(RuleCategory.EXCEPTION_HANDLING)
                .severity(RuleSeverity.HIGH)
                .description("Catching generic 'Exception' or 'Throwable' obscures unexpected runtime issues.")
                .regexPattern("catch\\s*\\(\\s*(Exception|Throwable)\\s+[a-zA-Z0-9_]+\\s*\\)")
                .remediation("Catch specific exceptions like IOException, IllegalArgumentException, etc.")
                .enabled(true)
                .build());

        // 2. Null Safety
        rules.add(RuleDefinition.builder()
                .ruleKey("NUL-001")
                .name("Avoid Unsafe Optional.get() Call")
                .category(RuleCategory.NULL_SAFETY)
                .severity(RuleSeverity.MEDIUM)
                .description("Calling Optional.get() without isPresent() or orElseThrow() can lead to NoSuchElementException.")
                .regexPattern("\\b[a-zA-Z0-9_]+\\.get\\(\\)")
                .remediation("Use .orElseThrow(), .orElse(), or .ifPresent() instead of .get().")
                .enabled(true)
                .build());

        // 3. Transaction Rules
        rules.add(RuleDefinition.builder()
                .ruleKey("TXN-001")
                .name("Avoid @Transactional on Non-Public Methods")
                .category(RuleCategory.TRANSACTION_RULES)
                .severity(RuleSeverity.CRITICAL)
                .description("Spring CGLIB proxies do not apply @Transactional on private/protected methods.")
                .regexPattern("@Transactional[\\s\\S]*?\\bprivate\\b")
                .remediation("Annotate only public service methods with @Transactional.")
                .enabled(true)
                .build());

        // 4. Validation Rules
        rules.add(RuleDefinition.builder()
                .ruleKey("VAL-001")
                .name("Enforce @Valid on Controller @RequestBody")
                .category(RuleCategory.VALIDATION_RULES)
                .severity(RuleSeverity.HIGH)
                .description("Request bodies must be validated with @Valid to enforce DTO constraints.")
                .regexPattern("@RequestBody\\s+(?!@Valid\\s+)[a-zA-Z0-9_<>]+")
                .remediation("Prefix the request DTO argument with @Valid: (@Valid @RequestBody MyDto dto).")
                .enabled(true)
                .build());

        // 5. API Design
        rules.add(RuleDefinition.builder()
                .ruleKey("API-001")
                .name("Do Not Expose JPA Entities in Controller Responses")
                .category(RuleCategory.API_DESIGN)
                .severity(RuleSeverity.HIGH)
                .description("Returning JPA entities directly couples database schemas with API contracts and leaks data.")
                .regexPattern("ResponseEntity<[a-zA-Z0-9_]*(User|Project|ReviewJob|Role)>")
                .remediation("Return dedicated response DTOs (e.g., UserResponse, ProjectResponse).")
                .enabled(true)
                .build());

        // 6. Logging Rules
        rules.add(RuleDefinition.builder()
                .ruleKey("LOG-001")
                .name("Avoid System.out and PrintStackTrace")
                .category(RuleCategory.LOGGING_RULES)
                .severity(RuleSeverity.MEDIUM)
                .description("System.out.print* and printStackTrace() bypass enterprise loggers and lack log levels.")
                .regexPattern("(System\\.out\\.print|\\.printStackTrace\\(\\))")
                .remediation("Use an SLF4J logger instance (e.g., log.info(), log.error()).")
                .enabled(true)
                .build());

        // 7. Security Rules
        rules.add(RuleDefinition.builder()
                .ruleKey("SEC-001")
                .name("Detect Hardcoded Credentials or Secrets")
                .category(RuleCategory.SECURITY_RULES)
                .severity(RuleSeverity.CRITICAL)
                .description("Hardcoded passwords or tokens in source code expose systems to credential leaks.")
                .regexPattern("(?i)(password|secret|apikey|token)\\s*=\\s*\"[a-zA-Z0-9_!@#$%^&*()+=]{6,}\"")
                .remediation("Externalize secrets using environment variables or Spring Cloud Config/Vault.")
                .enabled(true)
                .build());

        // 8. Authentication and Authorization Rules
        rules.add(RuleDefinition.builder()
                .ruleKey("AUT-001")
                .name("Enforce Role-Based Access on Sensitive Mutation Endpoints")
                .category(RuleCategory.AUTH_RULES)
                .severity(RuleSeverity.HIGH)
                .description("Destructive endpoints (@DeleteMapping, @PutMapping) must define @PreAuthorize checks.")
                .regexPattern("@(DeleteMapping|PutMapping|PatchMapping)[\\s\\S]*?public(?!.*@PreAuthorize)")
                .remediation("Add @PreAuthorize(\"hasRole('ADMIN')\") or appropriate permission checks.")
                .enabled(true)
                .build());

        // 9. Database Rules
        rules.add(RuleDefinition.builder()
                .ruleKey("DB-001")
                .name("Avoid Wildcard SELECT in Native Queries")
                .category(RuleCategory.DATABASE_RULES)
                .severity(RuleSeverity.MEDIUM)
                .description("Using 'SELECT *' in native queries causes unnecessary I/O and breaks schema refactorings.")
                .regexPattern("(?i)SELECT\\s+\\*\\s+FROM")
                .remediation("Specify explicit column projections in query statements.")
                .enabled(true)
                .build());

        // 10. Performance Rules
        rules.add(RuleDefinition.builder()
                .ruleKey("PRF-001")
                .name("Avoid Inefficient String Concatenation in Loops")
                .category(RuleCategory.PERFORMANCE_RULES)
                .severity(RuleSeverity.LOW)
                .description("Repeated string concatenation inside loops allocates many intermediate objects.")
                .regexPattern("for\\s*\\([\\s\\S]*?\\)\\s*\\{[\\s\\S]*?\\+=")
                .remediation("Use StringBuilder for repeated concatenations in loop bodies.")
                .enabled(true)
                .build());

        // 11. Code Style
        rules.add(RuleDefinition.builder()
                .ruleKey("STY-001")
                .name("Maintain Consistent CamelCase Method Naming")
                .category(RuleCategory.CODE_STYLE)
                .severity(RuleSeverity.LOW)
                .description("Java method names must follow lowerCamelCase convention.")
                .regexPattern("public\\s+[a-zA-Z0-9_<>]+\\s+[A-Z][a-zA-Z0-9_]+\\s*\\(")
                .remediation("Rename method to begin with lowercase letter.")
                .enabled(true)
                .build());

        // 12. Complexity Rules
        rules.add(RuleDefinition.builder()
                .ruleKey("CPX-001")
                .name("Avoid Deeply Nested If Conditions")
                .category(RuleCategory.COMPLEXITY_RULES)
                .severity(RuleSeverity.MEDIUM)
                .description("Deep nested branching increases cognitive complexity and cyclomatic burden.")
                .regexPattern("if\\s*\\([\\s\\S]*?if\\s*\\([\\s\\S]*?if\\s*\\([\\s\\S]*?if")
                .remediation("Use early return guard clauses or decompose into smaller helper methods.")
                .enabled(true)
                .build());

        // 13. Architecture Rules
        rules.add(RuleDefinition.builder()
                .ruleKey("ARC-001")
                .name("Prevent Controller Calling Repositories Directly")
                .category(RuleCategory.ARCHITECTURE_RULES)
                .severity(RuleSeverity.HIGH)
                .description("Controllers must communicate through Service layer abstractions, not Repositories directly.")
                .regexPattern("class\\s+[a-zA-Z0-9_]+Controller[\\s\\S]*?private\\s+final\\s+[a-zA-Z0-9_]+Repository")
                .remediation("Inject the Service interface instead of the Repository in Controller classes.")
                .enabled(true)
                .build());

        // 14. DTO and Mapping Rules
        rules.add(RuleDefinition.builder()
                .ruleKey("MAP-001")
                .name("Enforce Builder or Mapper Usage on Complex DTOs")
                .category(RuleCategory.DTO_MAPPING_RULES)
                .severity(RuleSeverity.LOW)
                .description("Manual repeated setter chains create boilerplate and error-prone mapping code.")
                .regexPattern("(\\w+\\.set\\w+\\(.*?\\);\\s*){6,}")
                .remediation("Use MapStruct or Lombok @Builder for object transformations.")
                .enabled(true)
                .build());

        // 15. Concurrency Rules
        rules.add(RuleDefinition.builder()
                .ruleKey("CON-001")
                .name("Avoid Unmanaged Raw Thread Instantiation")
                .category(RuleCategory.CONCURRENCY_RULES)
                .severity(RuleSeverity.HIGH)
                .description("Creating 'new Thread()' directly bypasses Spring-managed thread pools and task executors.")
                .regexPattern("new\\s+Thread\\(")
                .remediation("Use Spring TaskExecutor or @Async managed executors.")
                .enabled(true)
                .build());

        // 16. Testing Rules
        rules.add(RuleDefinition.builder()
                .ruleKey("TST-001")
                .name("Test Methods Must Contain Assertions")
                .category(RuleCategory.TESTING_RULES)
                .severity(RuleSeverity.MEDIUM)
                .description("Tests that do not verify outcomes provide false confidence.")
                .regexPattern("@Test[\\s\\S]*?void\\s+[a-zA-Z0-9_]+\\(\\)[\\s\\S]*?\\{(?!.*assert)")
                .remediation("Add Assertions (e.g. assertEquals, assertThat, verify).")
                .enabled(true)
                .build());

        // 17. Documentation Rules
        rules.add(RuleDefinition.builder()
                .ruleKey("DOC-001")
                .name("Public REST Endpoints Must Define OpenAPI Summary")
                .category(RuleCategory.DOCUMENTATION_RULES)
                .severity(RuleSeverity.LOW)
                .description("REST endpoints should feature @Operation annotations for API documentation.")
                .regexPattern("@(GetMapping|PostMapping|PutMapping|DeleteMapping)(?!.*@Operation)")
                .remediation("Add @Operation(summary = \"...\") on endpoint handler methods.")
                .enabled(true)
                .build());

        // 18. Dependency Rules
        rules.add(RuleDefinition.builder()
                .ruleKey("DEP-001")
                .name("Avoid Deprecated javax.* Packages")
                .category(RuleCategory.DEPENDENCY_RULES)
                .severity(RuleSeverity.HIGH)
                .description("Spring Boot 3+ / Java 21 projects use jakarta.* namespace instead of legacy javax.*.")
                .regexPattern("import\\s+javax\\.(persistence|validation|servlet)\\.")
                .remediation("Migrate import statement to jakarta.* namespace.")
                .enabled(true)
                .build());

        // 19. Configuration Rules
        rules.add(RuleDefinition.builder()
                .ruleKey("CFG-001")
                .name("Avoid Hardcoded Localhost In Production Code")
                .category(RuleCategory.CONFIGURATION_RULES)
                .severity(RuleSeverity.MEDIUM)
                .description("Hardcoded 'http://localhost' will fail in containerized cloud environments.")
                .regexPattern("\"http://localhost:[0-9]+\"")
                .remediation("Extract host and port configurations into application.yml properties.")
                .enabled(true)
                .build());

        // 20. Business Rules
        rules.add(RuleDefinition.builder()
                .ruleKey("BIZ-001")
                .name("Enforce Audit Timestamps and Active Flag on Entities")
                .category(RuleCategory.BUSINESS_RULES)
                .severity(RuleSeverity.MEDIUM)
                .description("Platform entities must support audit timestamps (createdAt) and lifecycle control.")
                .regexPattern("@Entity[\\s\\S]*?class(?!.*createdAt)")
                .remediation("Add createdAt, updatedAt, and @PrePersist lifecycle hooks to entity classes.")
                .enabled(true)
                .build());

        ruleRepository.saveAll(rules);
        log.info("Successfully seeded {} default rules covering all 20 governance categories.", rules.size());
    }
}
