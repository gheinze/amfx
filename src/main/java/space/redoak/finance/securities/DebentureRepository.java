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
    @Query(value = "UPDATE sm_debenture_detail SET percentage = :percentage WHERE instrument_id = :instrumentId", nativeQuery = true)
    int updateDebentureSetPercentageForInstrumentId(@Param("instrumentId") Integer instrumentId, @Param("percentage") Float percentage);

    
    @Query(value = "SELECT id FROM sm_instrument WHERE symbol = :symbol", nativeQuery = true)
    Integer findInstrumentIdForSymbol(String symbol);

    @Transactional
    @Modifying
    @Query(value = "UPDATE sm_debenture_detail SET maturity_dte = :maturityDate WHERE instrument_id = :instrumentId", nativeQuery = true)
    int updateDebentureSetMaturityDateForInstrumentId(@Param("instrumentId") Integer instrumentId, @Param("maturityDate") LocalDate maturityDate);
 
    
    @Transactional
    @Modifying
    @Query(value = "UPDATE sm_debenture_detail SET underlying_instrument_id = :underlyingSymbolId WHERE instrument_id = :instrumentId", nativeQuery = true)
    int updateDebentureSetUnderlyingSymbolForInstrumentId(@Param("instrumentId") Integer instrumentId, @Param("underlyingSymbolId") Integer underlyingSymbolId);
    
    @Transactional
    @Modifying
    @Query(value = "UPDATE sm_debenture_detail SET conversion_price = :conversionPrice WHERE instrument_id = :instrumentId", nativeQuery = true)
    int updateDebentureSetConversionPriceForInstrumentId(@Param("instrumentId") Integer instrumentId, @Param("conversionPrice") Float conversionPrice);
    
    @Transactional
    @Modifying
    @Query(value = "UPDATE sm_debenture_detail SET prospectus = :prospectus WHERE instrument_id = :instrumentId", nativeQuery = true)
    int updateDebentureSetProspectusForInstrumentId(@Param("instrumentId") Integer instrumentId, @Param("prospectus") String prospectus);

    @Transactional
    @Modifying
    @Query(value = "UPDATE sm_debenture_detail SET comments = :comments WHERE instrument_id = :instrumentId", nativeQuery = true)
    int updateDebentureSetCommentsForInstrumentId(@Param("instrumentId") Integer instrumentId, @Param("comments") String comments);
    
}
