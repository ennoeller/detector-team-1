package ee.digit25.detector.domain.transaction.common;

import ee.digit25.detector.domain.person.common.Person_;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TransactionSpecification {

    public static Specification<Transaction> senderEquals(String sender) {
        return (root, query, builder) -> builder.equal(root.get(Transaction_.sender).get(Person_.personCode), sender);
    }

    public static Specification<Transaction> timestampIsAfter(LocalDateTime time) {
        return (root, query, builder) -> builder.greaterThan(root.get(Transaction_.timestamp), time);
    }

    public static Specification<Transaction> sender(String code) {
        return (root, query, builder) -> builder.equal(root.get(Transaction_.SENDER), code);
    }
}
