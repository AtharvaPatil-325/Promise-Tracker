package com.promisetracker.config;

import com.promisetracker.customer.Customer;
import com.promisetracker.customer.CustomerRepository;
import com.promisetracker.organization.Organization;
import com.promisetracker.organization.OrganizationRepository;
import com.promisetracker.promise.Promise;
import com.promisetracker.promise.PromisePriority;
import com.promisetracker.promise.PromiseRepository;
import com.promisetracker.promise.PromiseStatus;
import com.promisetracker.user.User;
import com.promisetracker.user.UserRepository;
import com.promisetracker.user.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final PromiseRepository promiseRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        Organization org;
        if (organizationRepository.count() == 0) {
            org = Organization.builder()
                    .name("Acme Sales Corp")
                    .build();
            org = organizationRepository.save(org);
        } else {
            org = organizationRepository.findAll().get(0);
        }

        final Organization targetOrg = org;

        userRepository.findByEmail("vedantpatil0692004@gmail.com").ifPresentOrElse(
                user -> {
                    user.setPasswordHash(passwordEncoder.encode("692004"));
                    user.setEnabled(true);
                    user.setRole(UserRole.OWNER);
                    userRepository.save(user);
                    log.info("Updated password for vedantpatil0692004@gmail.com");
                },
                () -> {
                    User owner = User.builder()
                            .organization(targetOrg)
                            .email("vedantpatil0692004@gmail.com")
                            .passwordHash(passwordEncoder.encode("692004"))
                            .firstName("Vedant")
                            .lastName("Patil")
                            .role(UserRole.OWNER)
                            .enabled(true)
                            .build();
                    userRepository.save(owner);
                    log.info("Created admin user vedantpatil0692004@gmail.com");
                }
        );

        if (userRepository.count() == 1 && customerRepository.count() == 0) {
            log.info("Seeding initial demo data for PromiseTracker...");
            User owner = userRepository.findByEmail("vedantpatil0692004@gmail.com").orElseThrow();

            Customer customer = Customer.builder()
                    .organization(org)
                    .name("Rahul Sharma")
                    .companyName("ABC Pvt Ltd")
                    .email("rahul@abc.com")
                    .phone("+91 9876543210")
                    .notes("Key enterprise customer interested in software licensing.")
                    .build();
            customer = customerRepository.save(customer);

            Promise promise1 = Promise.builder()
                    .organization(org)
                    .customer(customer)
                    .createdBy(owner)
                    .assignedTo(owner)
                    .title("Send quotation by Friday")
                    .description("Prepare customized pricing quotation for software license tier.")
                    .sourceText("I'll send you the quotation by Friday.")
                    .dueDate(LocalDate.now())
                    .priority(PromisePriority.HIGH)
                    .status(PromiseStatus.OPEN)
                    .build();
            promiseRepository.save(promise1);

            Promise promise2 = Promise.builder()
                    .organization(org)
                    .customer(customer)
                    .createdBy(owner)
                    .assignedTo(owner)
                    .title("Schedule product demo with technical team")
                    .description("Coordinate with technical architect for integration presentation.")
                    .sourceText("We will schedule a detailed technical demo next week.")
                    .dueDate(LocalDate.now().plusDays(2))
                    .priority(PromisePriority.MEDIUM)
                    .status(PromiseStatus.IN_PROGRESS)
                    .build();
            promiseRepository.save(promise2);
        }
    }
}
