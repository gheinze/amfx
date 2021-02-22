package space.redoak.finance.securities;

import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 *
 * @author glenn
 */
@Service
public class FinSecService {

    @Autowired
    private DebentureRepository debentureRepo;

    
    public Page<DebentureEntity> getDebentures(Pageable pageRequest) {
        return debentureRepo.findAll(pageRequest);
    }

    public Integer findInstrumentIdForSymbol(String symbol) {
        return debentureRepo.findInstrumentIdForSymbol(symbol);
    }
    
    public void updateDebentureRate(Integer instrumentId, Float percentage) {
        debentureRepo.updateDebentureDetailSetPercentageForInstrumentId(instrumentId, percentage);
    }
    
    public void updateDebentureMaturityDate(Integer instrumentId, LocalDate maturityDate) {
        debentureRepo.updateDebentureSetMaturityDateForInstrumentId(instrumentId, maturityDate);
    }
    
    public int updateDebentureUnderlyingSymbol(Integer instrumentId, Integer underlyingInstrumentId) {
        return debentureRepo.updateDebentureSetUnderlyingSymbolForInstrumentId(instrumentId, underlyingInstrumentId);
    }
    
    public void updateDebentureConversionPrice(Integer instrumentId, Float conversionPrice) {
        debentureRepo.updateDebentureSetConversionPriceForInstrumentId(instrumentId, conversionPrice);
    }
    
    public void updateDebentureProspectus(Integer instrumentId, String prospectus) {
        debentureRepo.updateDebentureSetProspectusForInstrumentId(instrumentId, prospectus);
    }
    
    public void updateDebentureComments(Integer instrumentId, String comments) {
        debentureRepo.updateDebentureSetCommentsForInstrumentId(instrumentId, comments);        
    }
    
}
