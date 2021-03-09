package space.redoak.finance.securities;

import java.time.LocalDate;
import java.util.List;
import javax.sql.DataSource;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

/**
 *
 * @author glenn
 */
@Component
public class QuoteLoaderDao {

    
    @AllArgsConstructor
    @Getter
    public static class QuoteDate {
        private final LocalDate quoteDate;
        private final int quoteCount;
    }
    
    
    private final JdbcTemplate jdbcTemplate;

    private static final String QUOTE_DATE_SQL =
            """
            SELECT READ_DTE AS quote_dte
                  ,COUNT(*) AS quote_count
              FROM SM_EOD_QUOTE
              GROUP BY read_dte
              ORDER BY 1 DESC
              LIMIT 4
            """
    ;
    
    
    @Autowired
    public QuoteLoaderDao(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }
    
    
    private final RowMapper<QuoteDate> quoteDateRowMapper = 
            (rs, rowNum) -> new QuoteDate(
                    rs.getDate("quote_dte").toLocalDate(),
                    rs.getInt("quote_count")
            );
    
    public List<QuoteDate> getMostRecentQuoteDates() {        
        return jdbcTemplate.query(QUOTE_DATE_SQL, quoteDateRowMapper);
    }

        
}
