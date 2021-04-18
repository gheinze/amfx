package space.redoak.finance.securities;


import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

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
    
    
    @Query(value = """
                   SELECT w.instrument_id, q.symbol, q.descr
                         ,q.read_dte, q.close_price
                         ,q.strike_price, q.comments
                     FROM sm_watch_list w
                     LEFT JOIN sm_quote_vw q ON (q.instrument_id = w.instrument_id)
                   """
            ,nativeQuery = true)
    List<InstrumentEntity> getWatchList();

    
    @Transactional
    @Modifying
    @Query(value =
            """
            MERGE INTO sm_watch_list w
               USING ( SELECT id FROM sm_instrument WHERE id = :instrumentId ) i
               ON w.instrument_id = i.id
               WHEN NOT MATCHED THEN INSERT (instrument_id) VALUES (:instrumentId)
            """
            ,nativeQuery = true)
    int addToWatchList(@Param("instrumentId") int instrumentId);

    
    @Transactional
    @Modifying
    @Query(value =
            """
            DELETE FROM sm_watch_list WHERE instrument_id = :instrumentId
            """
            ,nativeQuery = true)
        int removeFromWatchList(@Param("instrumentId") int instrumentId);


}
