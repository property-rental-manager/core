package pl.propertyrentalmanager.test;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.propertyrentalmanager.user.UserEntity;

import java.util.UUID;

public interface TestUserRepository extends JpaRepository<UserEntity, UUID> {
}
