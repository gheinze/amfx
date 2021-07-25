package space.redoak.finance.securities;

import java.math.BigDecimal;
import java.time.LocalDate;


/**
 *
 * @author glenn
 */
public interface QuoteApi {

    BigDecimal getClosingPrice();
    
    LocalDate getLocalDate();
    
    default boolean isGoodQuote() {
        return getClosingPrice() != null && getLocalDate() != null;
    }
    
    
}
