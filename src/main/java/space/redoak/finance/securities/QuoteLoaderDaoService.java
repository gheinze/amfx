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
import space.redoak.util.TextFlowConsole;

/**
 *
 * @author glenn
 */
@Component
public class QuoteLoaderDaoService {

    
    private final JdbcTemplate jdbcTemplate;

    
    @Autowired
    public QuoteLoaderDaoService(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }
    
    

    // ---------------------------------------------------------------
    
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
    
    @AllArgsConstructor
    @Getter
    public static class QuoteDate {
        private final LocalDate quoteDate;
        private final int quoteCount;
    }
        
    private final RowMapper<QuoteDate> quoteDateRowMapper = 
            (rs, rowNum) -> new QuoteDate(
                    rs.getDate("quote_dte").toLocalDate(),
                    rs.getInt("quote_count")
            );
    
    public List<QuoteDate> getMostRecentQuoteDates() {        
        return jdbcTemplate.query(QUOTE_DATE_SQL, quoteDateRowMapper);
    }



    // ---------------------------------------------------------------

    private static final String TRUNCATE_STAGING_TABLE = "TRUNCATE TABLE sm_eod_quote_staging";
        
    private static final String LOAD_STAGING_TABLE_TEMPLATE =
            """
            INSERT INTO sm_eod_quote_staging
              SELECT *
                FROM csvread('%s', 'symbol~security~close~volume', 'charset=UTF-8 fieldSeparator=~')
            """
            ;

    // Define newly encountered securities
    private static final String LOAD_INSTRUMENTS_TEMPLATE =
            """
            INSERT INTO sm_instrument(exchange_id, symbol, descr, create_dte, instrument_status, instrument_type)
              SELECT 1, symbol, security_descr, '%s', 'A'
                    ,CASE WHEN symbol LIKE '%%.DB%%' THEN 'D' WHEN symbol LIKE '%%.P%%.%%' THEN 'P' END
                FROM sm_eod_quote_staging
                WHERE (1, symbol) NOT IN (SELECT 1, symbol FROM sm_instrument)
                ORDER BY symbol
            """
            ;
    
    // Insert end of day quotes if they don't already exist
    private static final String LOAD_QUOTE_TEMPLATE = 
            """
            MERGE INTO sm_eod_quote q
              USING ( SELECT s.id, '%s' AS read_dte, REPLACE(e.close_price, ',') AS close_price, REPLACE(e.volume_traded, ',') AS volume_traded
                        FROM sm_eod_quote_staging e
                        JOIN sm_instrument s ON (s.exchange_id = 1 AND s.symbol = e.symbol)
                    ) f
              ON q.instrument_id = f.id AND q.read_dte = f.read_dte
              WHEN NOT MATCHED THEN
                INSERT (instrument_id, read_dte, close_price, volume_traded)
                  VALUES (f.id, f.read_dte, f.close_price, f.volume_traded)
            """
            ;


    // Mark missing securities as inactive
    private static final String INACTIVE_SECURITIES =
            """
            UPDATE sm_instrument  i
              SET instrument_status = 'I'
              WHERE exchange_id = 1
                AND instrument_status <> 'I'
                AND NOT EXISTS ( SELECT 1 FROM sm_eod_quote_staging s WHERE s.symbol = i.symbol)
            """
            ;

        
    public void loadH2Db(String csvFile, String eodDate, TextFlowConsole textFlowConsole) {

        jdbcTemplate.execute(TRUNCATE_STAGING_TABLE);
        textFlowConsole.println("Tuncated staging table sm_eod_quote_staging");
            
        var count = jdbcTemplate.update(String.format(LOAD_STAGING_TABLE_TEMPLATE, csvFile));
        textFlowConsole.println(String.format("%d records loaded into staging table sm_eod_quote_staging", count));
            
        count = jdbcTemplate.update(String.format(LOAD_INSTRUMENTS_TEMPLATE, eodDate));
        textFlowConsole.println(String.format("%d new instruments loaded into sm_instrument", count));
            
        count = jdbcTemplate.update(String.format(LOAD_QUOTE_TEMPLATE, eodDate));
        textFlowConsole.println(String.format("%d quotes merged into sm_eod_quote", count));
            
        count = jdbcTemplate.update(INACTIVE_SECURITIES);
        textFlowConsole.println(String.format("%d securities marked as inactive in sm_instrument\n", count));                 
        
    }
        
}
