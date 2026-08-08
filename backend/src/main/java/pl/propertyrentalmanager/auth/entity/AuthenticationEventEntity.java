package pl.propertyrentalmanager.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.propertyrentalmanager.user.UserEntity;

import java.time.OffsetDateTime;

@Entity
@Table(name = "authentication_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @Column(name = "email_normalized", length = 320)
    private String emailNormalized;

    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;

    @Column(name = "status", nullable = false, length = 30)
    private String status;

    @Column(name = "failure_reason", length = 100)
    private String failureReason;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 1000)
    private String userAgent;

    @Column(name = "request_id", length = 100)
    private String requestId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}
