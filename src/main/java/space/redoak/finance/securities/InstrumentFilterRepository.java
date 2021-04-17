package space.redoak.finance.securities;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

/**
 *
 * @author glenn
 */
public interface InstrumentFilterRepository extends Repository<InstrumentFilterEntity, Long> {

    @Query(value = """
                   SELECT id, symbol, descr
                     FROM sm_instrument
                     WHERE exchange_id = (SELECT id FROM sm_exchange WHERE symbol = :exchange)
                       AND instrument_status = 'A'
                     ORDER BY symbol
                   """
            ,nativeQuery = true)
    List<InstrumentFilterEntity> getInstrumentsSparse(@Param("exchange") String exchange);


}
