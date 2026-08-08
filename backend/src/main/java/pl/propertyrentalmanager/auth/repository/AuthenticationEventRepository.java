package pl.propertyrentalmanager.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.propertyrentalmanager.auth.entity.AuthenticationEventEntity;

public interface AuthenticationEventRepository extends JpaRepository<AuthenticationEventEntity, Long> {
}
