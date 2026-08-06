package pl.propertyrentalmanager.test;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.propertyrentalmanager.common.exception.ResourceConflictException;
import pl.propertyrentalmanager.common.exception.ResourceNotFoundException;

@RestController
@RequestMapping("/api/test-fixture")
public class TestValidationController {

    public record TestValidationRequest(
            @NotBlank(message = "Email is required")
            @Email(message = "Email must be valid")
            String email,

            @NotBlank(message = "Name is required")
            String name
    ) {}

    @PostMapping("/validate")
    public String validate(@Valid @RequestBody TestValidationRequest request) {
        return "OK";
    }

    @GetMapping("/not-found")
    public void throwNotFound() {
        throw new ResourceNotFoundException("Test resource was not found");
    }

    @GetMapping("/conflict")
    public void throwConflict() {
        throw new ResourceConflictException("Test resource conflict");
    }

    @GetMapping("/unexpected")
    public void throwUnexpected() {
        throw new RuntimeException("Unexpected exception for testing");
    }

    @GetMapping("/param")
    public String requireParam(@RequestParam("id") Long id) {
        return "ID: " + id;
    }
}
