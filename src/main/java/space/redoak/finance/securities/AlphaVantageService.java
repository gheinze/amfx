package space.redoak.finance.securities;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;



/**
 * Utilities for accessing the AlphaVantage stock quote service.
 * 
 * @author glenn
 */
@Service
public class AlphaVantageService {

    // Requires certificate to be registered with jvm.  Example registration:
    // sudo ~/sw/java/jdk1.8.0_74/bin/keytool -importcert -file ./wwwalphavantageco.crt -alias alphavantage -keystore ~/sw/java/jdk1.8.0_74/jre/lib/security/cacerts -storepass changeit

    private static final String ALPHAVANTAGE__GLOBAL_QUOTE_QUERY_TEMPLATE =
            "https://www.alphavantage.co/query?function=GLOBAL_QUOTE&symbol=%s&apikey=%s";

    private final HttpComponentsClientHttpRequestFactory requestFactory;

    @Value("${finsec.stockQuoteService.alphavantage.apiKey}")
    private String alphavantageApiKey;
    
    public AlphaVantageService() {
        CloseableHttpClient httpClient = HttpClients
                .custom()
                .setSSLHostnameVerifier(new NoopHostnameVerifier())
                .build()
                ;
        requestFactory = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setHttpClient(httpClient);
        
    }
    

    /**
     * 
     * @param tmxSymbol Ticker symbol as used on the TMX. Will need to be converted.
     * @return
     * @throws IOException 
     */
    public AlphaVantageQuoteDao.GlobalQuote getQuote(final String tmxSymbol) throws IOException {

        String alphaVantageSymbol = convertTmxSymbolToAlphaVantageSymbol(tmxSymbol);

        String url = String.format(
                ALPHAVANTAGE__GLOBAL_QUOTE_QUERY_TEMPLATE,
                alphaVantageSymbol,
                alphavantageApiKey
        );
        
        System.out.println("url: " + url);
        ResponseEntity<String> response = new RestTemplate(requestFactory)
                .exchange(url, HttpMethod.GET, null, String.class);

        String body = response.getBody();
        System.out.println("body: " + body);
        
        ObjectMapper objectMapper = new ObjectMapper();
        AlphaVantageQuoteDao quote = objectMapper.readValue(body, AlphaVantageQuoteDao.class);

        return quote.getGlobalQuote();
        
    }


    /*
     * First "." is replaced by a "-"
     * Subsequent "." are removed.
     *
     * Ex.:   ARE.DB.B  => ARE-DBB.TO
     *
     */
    private String convertTmxSymbolToAlphaVantageSymbol(final String symbol) {
        String newSymbol = symbol.replaceFirst("\\.", "-").replace(".", "") + ".TO";
        return newSymbol;
    }

}
