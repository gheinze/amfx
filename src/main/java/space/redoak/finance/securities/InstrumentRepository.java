package space.redoak.finance.securities;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

/**
 *
 * @author glenn
 */
public interface InstrumentRepository extends Repository<InstrumentEntity, Long> {

    @Query(value = """
                   SELECT instrument_id, symbol, descr
                         ,read_dte, close_price
                         ,strike_price, comments
                     FROM sm_quote_vw
                     WHERE instrument_status = 'A'
                       and instrument_id = :instrumentId
                   """
            ,nativeQuery = true)
    InstrumentEntity getInstrumentDetail(@Param("instrumentId") int instrumentId);
    
}
