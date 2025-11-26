package de.kleinanzeigen.app.searchprofile;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SearchProfileRepository extends JpaRepository<SearchProfile, Long> {

    Optional<SearchProfile> findByName(String name);
}
