package space.redoak.finance.securities;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;
import lombok.Getter;


/**
 * Data object for storing the json response when querying the AlphaVantage
 * stock quote service for a "Global" quote.
 * i.e. https://www.alphavantage.co/query?function=GLOBAL_QUOTE&symbol=%s&apikey=%s
 * 
 * @author glenn
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AlphaVantageQuoteDao {

    @Getter
    @JsonAlias("Global Quote")
    private GlobalQuote globalQuote;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GlobalQuote  implements QuoteApi {

        @JsonAlias("01. symbol")
        private String symbol;
        @JsonAlias("02. open")
        private BigDecimal open;
        @JsonAlias("03. high")
        private BigDecimal high;
        @JsonAlias("04. low")
        private BigDecimal low;
        @JsonAlias("05. price")
        private BigDecimal price;
        @JsonAlias("06. volume")
        private int volume;
        @JsonAlias("07. latest trading day")
        private String lastTradingDay;
        @JsonAlias("08. previous close")
        private BigDecimal previousClose;
        @JsonAlias("09. change")
        private BigDecimal change;
        @JsonAlias("10. change percent")
        private String changePercent;

        @Override
        public BigDecimal getClosingPrice() {
            return getPrice();
        }

        @Override
        public LocalDate getLocalDate() {
            return LocalDate.parse(getLastTradingDay());
        }

    }

}
