package ee.digit25.detector.domain.person.common;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PersonRepository extends JpaRepository<Person, Long>, JpaSpecificationExecutor<Person> {
    // TODO: lisa meetod personCode alusel otsimiseks
    Optional<Person> findByPersonCode(String personCode);
}
