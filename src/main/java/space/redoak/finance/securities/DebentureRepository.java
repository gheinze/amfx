package space.redoak.finance.securities;

import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;


public interface DebentureRepository extends Repository<DebentureEntity, Long> {

    Page<DebentureEntity> findAll(Pageable pageRequest);
    
    @Transactional
    @Modifying
    @Query(value =
            """
            MERGE INTO sm_debenture_detail dd
              USING ( SELECT id FROM sm_instrument WHERE id = :instrumentId ) i
              ON dd.instrument_id = i.id
              WHEN NOT MATCHED THEN INSERT (instrument_id, percentage) VALUES (i.id, :percentage)
              WHEN MATCHED THEN UPDATE SET percentage = :percentage
            """
            ,nativeQuery = true)
    int updateDebentureDetailSetPercentageForInstrumentId(@Param("instrumentId") Integer instrumentId, @Param("percentage") Float percentage);

    
    @Query(value = "SELECT id FROM sm_instrument WHERE symbol = UPPER(:symbol)", nativeQuery = true)
    Integer findInstrumentIdForSymbol(String symbol);

    @Transactional
    @Modifying
    @Query(value =
            """
            MERGE INTO sm_debenture_detail dd
               USING ( SELECT id FROM sm_instrument WHERE id = :instrumentId ) i
               ON dd.instrument_id = i.id
               WHEN NOT MATCHED THEN INSERT (instrument_id, issue_dte) VALUES (i.id, :issueDate)
               WHEN MATCHED THEN UPDATE SET issue_dte = :issueDate
            """
            ,nativeQuery = true)
    int updateDebentureSetIssuedDateForInstrumentId(@Param("instrumentId") Integer instrumentId, @Param("issueDate") LocalDate issueDate);
 
    
    @Transactional
    @Modifying
    @Query(value =
            """
            MERGE INTO sm_debenture_detail dd
               USING ( SELECT id FROM sm_instrument WHERE id = :instrumentId ) i
               ON dd.instrument_id = i.id
               WHEN NOT MATCHED THEN INSERT (instrument_id, maturity_dte) VALUES (i.id, :maturityDate)
               WHEN MATCHED THEN UPDATE SET maturity_dte = :maturityDate
            """
            ,nativeQuery = true)
    int updateDebentureSetMaturityDateForInstrumentId(@Param("instrumentId") Integer instrumentId, @Param("maturityDate") LocalDate maturityDate);
 
    
    @Transactional
    @Modifying
    @Query(value =
            """
            MERGE INTO sm_debenture_detail dd
              USING ( SELECT id FROM sm_instrument WHERE id = :instrumentId ) i
              ON dd.instrument_id = i.id
              WHEN NOT MATCHED THEN INSERT (instrument_id, underlying_instrument_id) VALUES (i.id, :underlyingInstrumentId)
              WHEN MATCHED THEN UPDATE SET underlying_instrument_id = :underlyingInstrumentId
            """
            ,nativeQuery = true)
    int updateDebentureSetUnderlyingSymbolForInstrumentId(@Param("instrumentId") Integer instrumentId, @Param("underlyingInstrumentId") Integer underlyingInstrumentId);
    
    @Transactional
    @Modifying
    @Query(value =
            """
            MERGE INTO sm_debenture_detail dd
              USING ( SELECT id FROM sm_instrument WHERE id = :instrumentId ) i
              ON dd.instrument_id = i.id
              WHEN NOT MATCHED THEN INSERT (instrument_id, conversion_price) VALUES (i.id, :conversionPrice)
              WHEN MATCHED THEN UPDATE SET conversion_price = :conversionPrice
            """            
            ,nativeQuery = true)
    int updateDebentureSetConversionPriceForInstrumentId(@Param("instrumentId") Integer instrumentId, @Param("conversionPrice") Float conversionPrice);
    
    @Transactional
    @Modifying
    @Query(value =
            """
            MERGE INTO sm_debenture_detail dd
              USING ( SELECT id FROM sm_instrument WHERE id = :instrumentId ) i
              ON dd.instrument_id = i.id
              WHEN NOT MATCHED THEN INSERT (instrument_id, prospectus) VALUES (i.id, :prospectus)
              WHEN MATCHED THEN UPDATE SET prospectus = :prospectus
            """            
            ,nativeQuery = true)
    int updateDebentureSetProspectusForInstrumentId(@Param("instrumentId") Integer instrumentId, @Param("prospectus") String prospectus);

    @Transactional
    @Modifying
    @Query(value =
            """
            MERGE INTO sm_debenture_detail dd
              USING ( SELECT id FROM sm_instrument WHERE id = :instrumentId ) i
              ON dd.instrument_id = i.id
              WHEN NOT MATCHED THEN INSERT (instrument_id, comments) VALUES (i.id, :comments)
              WHEN MATCHED THEN UPDATE SET comments = :comments
            """            
            ,nativeQuery = true)
    int updateDebentureSetCommentsForInstrumentId(@Param("instrumentId") Integer instrumentId, @Param("comments") String comments);
    
}
