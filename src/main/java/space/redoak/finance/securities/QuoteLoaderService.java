package space.redoak.finance.securities;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author glenn
 */
@Service
public class QuoteLoaderService {

    @Autowired QuoteLoaderDao quoteLoaderDao;
    
    
    public List<QuoteLoaderDao.QuoteDate> getMostRecentQuoteDates() {
        return quoteLoaderDao.getMostRecentQuoteDates();
    }
    
}
