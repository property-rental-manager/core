package pl.propertyrentalmanager;

import org.springframework.boot.SpringApplication;

public class TestPropertyRentalManagerBackendApplication {

	public static void main(String[] args) {
		SpringApplication.from(PropertyRentalManagerBackendApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
