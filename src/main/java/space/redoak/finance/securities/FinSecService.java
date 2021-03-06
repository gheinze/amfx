package space.redoak.finance.securities;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import space.redoak.amfx.Debenture;
import space.redoak.amfx.Instrument;
import space.redoak.finance.securities.AlphaVantageQuoteDao.GlobalQuote;

/**
 *
 * @author glenn
 */
@Service
public class FinSecService {

    @Autowired
    private DebentureRepository debentureRepo;
    
    @Autowired
    private QuoteRepository quoteRepository;

    @Autowired
    private InstrumentRepository instrumentRepository;

    @Autowired
    private InstrumentFilterRepository instrumentFilterRepository;
    
    @Autowired
    private AlphaVantageService quoteService;
    
    
    @Value("${finsec.debenture.googleDoc}")
    private String googleDebentureDoc;
    
    @Value("${finsec.debenture.googleDocClientCredentials}")
    private String googleDebentureDocClientCredentials;
    
    
    public Page<DebentureEntity> getDebentures(Pageable pageRequest) {
        return debentureRepo.findAll(pageRequest);
    }

    public Integer findInstrumentIdForSymbol(String symbol) {
        return debentureRepo.findInstrumentIdForSymbol(symbol);
    }
    
    public void updateDebentureRate(Integer instrumentId, Float percentage) {
        debentureRepo.updateDebentureDetailSetPercentageForInstrumentId(instrumentId, percentage);
    }
    
    public void updateDebentureIssuedDate(Integer instrumentId, LocalDate issuedDate) {
        debentureRepo.updateDebentureSetIssuedDateForInstrumentId(instrumentId, issuedDate);
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
    
    
    public List<InstrumentFilterEntity> getInstrumentsSparse(String exchange) {
        return instrumentFilterRepository.getInstrumentsSparse(exchange);
    }
    
    public Instrument getInstrumentDetail(Integer id) {
        return new Instrument(instrumentRepository.getInstrumentDetail(id));
    }
    
    public void addToWatchList(Instrument instrument) {
        instrumentRepository.addToWatchList(instrument.getInstrumentId());
    }
            
    public void removeFromWatchList(Instrument instrument) {
        instrumentRepository.removeFromWatchList(instrument.getInstrumentId());
    }
        
    public List<Instrument> getWatchList() {
        return instrumentRepository.getWatchList().stream()
                .map(i -> new Instrument(i))
                .collect(Collectors.toList())
                ;
    }

    
    public void updateInstrumentStrikePrice(int instrumentId, Float strikePrice) {
        instrumentRepository.updateStrikePrice(instrumentId, strikePrice);
    }

    public void updateInstrumentComments(int instrumentId, String comments) {
        instrumentRepository.updateComments(instrumentId, comments);
    }
    
    
    public List<QuoteEntity> getQuotes(Integer instrumentId, LocalDate fromDate) {
        return quoteRepository.getQuotes(instrumentId, fromDate);
    }
 
    
    public GlobalQuote lookupQuote(String symbol) throws IOException {
        return quoteService.getQuote(symbol);        
    }
    
    
    public void publishToGoogleDoc(Stream<Debenture> debentureStream) throws IOException, GeneralSecurityException {
        
        List<List<Object>> range = debentureStream
                .map(d -> new ArrayList<Object>(Arrays.asList(d.toCsv().split("~", -1))))
                .collect(Collectors.toList())
                ;
        
        Sheets sheetsService = SheetsServiceUtil.getSheetsService(googleDebentureDocClientCredentials);

        ValueRange body = new ValueRange().setValues(range);

        UpdateValuesResponse result = sheetsService.spreadsheets().values()
                .update(googleDebentureDoc, "A4", body)
                .setValueInputOption("RAW")
                .execute()
                ;
     
    }

}
