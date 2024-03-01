package mate.academy.carsharing.repository.role;

import java.util.Optional;
import mate.academy.carsharing.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(Role.RoleName roleName);
}
