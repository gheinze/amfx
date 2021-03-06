package space.redoak.finance.securities;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 *
 * @author glenn
 */
public interface QuoteRepository extends JpaRepository<QuoteEntity, Long> {

    @Query(value = """
                   SELECT *
                     FROM sm_eod_quote
                     WHERE instrument_id = :instrumentId
                       AND read_dte >= :fromDate
                     ORDER BY read_dte
                   """
            ,nativeQuery = true)
    List<QuoteEntity> getQuotes(@Param("instrumentId") Integer instrumentId, @Param("fromDate") LocalDate fromDate);
    
}
