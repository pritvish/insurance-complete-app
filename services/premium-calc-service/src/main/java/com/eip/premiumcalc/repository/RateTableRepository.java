package com.eip.premiumcalc.repository;

import com.eip.premiumcalc.domain.RateTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RateTableRepository extends JpaRepository<RateTable, UUID> {

    Optional<RateTable> findByTableCode(String tableCode);

    List<RateTable> findByLineOfBusinessAndStateCodeAndIsActiveTrue(String lob, String stateCode);

    Optional<RateTable> findTopByLineOfBusinessAndStateCodeAndIsActiveTrueAndEffectiveDateLessThanEqualOrderByEffectiveDateDesc(
            String lob, String stateCode, LocalDate date);
}
