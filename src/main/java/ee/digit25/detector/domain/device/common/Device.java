package ee.digit25.detector.domain.device.common;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@ToString
@NoArgsConstructor
// TODO: lisa indeks mac veerule, et kiirendada otsinguid
@Table(indexes = @Index(name = "idx_device_mac", columnList = "mac"))
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // TODO: lisa unikaalsus ja mitte-null tingimus
    @Column(unique = true, nullable = false)
    private String mac;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public Device(String mac) {
        this.mac = mac;
    }
}
