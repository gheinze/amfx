package space.redoak.finance.securities;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import javafx.concurrent.Task;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import space.redoak.util.TextFlowConsole;

/**
 *
 * @author glenn
 */
@Service
@Slf4j
public class QuoteLoaderFacade {

    @Autowired QuoteLoaderDaoService quoteLoaderDaoService;
    @Autowired QuoteLoaderEodScraperService quoteLoaderEodScraperService;
    
    
    public List<QuoteLoaderDaoService.QuoteDate> getMostRecentQuoteDates() {
        return quoteLoaderDaoService.getMostRecentQuoteDates();
    }
    
    
    public Optional<LocalDate> getLatestAvailableEodDate(SecurityExchange exchange) {
        try {
            return Optional.of(quoteLoaderEodScraperService.getDate(exchange));
        } catch (IOException ex) {
            log.error("Failed to find date for EOD data: ", ex);
            return Optional.empty();
        }
    }

    
    public void loadEodData(SecurityExchange securityExchange, TextFlowConsole textFlowConsole, Runnable completionCallback) {
        final Task eodLoadTask = new EodLoadTask(securityExchange, textFlowConsole, completionCallback);
        final Thread taskThread = new Thread(eodLoadTask, "EOD-Load");
        taskThread.start();
    }
    
    
    class EodLoadTask extends Task<Void> {
        
        private final SecurityExchange securityExchange;
        private final TextFlowConsole textFlowConsole;
        private final Runnable completionCallback;
        
        EodLoadTask(SecurityExchange securityExchange, TextFlowConsole textFlowConsole, Runnable completionCallback) {
            this.securityExchange = securityExchange;
            this.textFlowConsole = textFlowConsole;
            this.completionCallback = completionCallback;
        }
        
        @Override protected Void call() throws Exception {
            try {    
                quoteLoaderEodScraperService.extractEodForExchange(securityExchange, textFlowConsole, completionCallback);
            } catch (Throwable t) {
              log.error("EOD loading failure", t);
            }
            return null;
        }
                
    }
    
    
}
