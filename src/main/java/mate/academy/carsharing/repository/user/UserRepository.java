package mate.academy.carsharing.repository.user;

import java.util.Optional;
import mate.academy.carsharing.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface UserRepository extends JpaRepository<User, Long>,
        JpaSpecificationExecutor<User> {
    Optional<User> findByEmail(String email);
}
