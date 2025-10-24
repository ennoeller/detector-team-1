package ee.digit25.detector.domain.person.common;

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

@Entity
// TODO: lisa indeks personCode veerule, et kiirendada otsinguid
@Table(indexes = @Index(name = "idx_person_code", columnList = "personCode"))
@Getter
@Setter
@ToString
@NoArgsConstructor
public class Person {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // TODO: lisa unikaalsus ja mitte-null tingimus
    @Column(unique = true, nullable = false, length = 50)
    private String personCode;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public Person(String personCode) {
        this.personCode = personCode;
    }
}
