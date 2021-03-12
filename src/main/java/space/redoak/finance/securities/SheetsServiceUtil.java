package space.redoak.finance.securities;

import java.io.IOException;
import java.security.GeneralSecurityException;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;

/**
 * Setup service for programmatically accessing Google Sheets (spreadsheet).
 * 
 * Copied from: https://www.baeldung.com/google-sheets-java-client
 *
 */
public class SheetsServiceUtil {

    private static final String APPLICATION_NAME = "Google Sheets Debenture";

    public static Sheets getSheetsService(String clientCredentials) throws IOException, GeneralSecurityException {
        
        Credential credential = GoogleAuthorizeUtil.authorize(clientCredentials);
        
        return new Sheets.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JacksonFactory.getDefaultInstance(),
                credential
        )
                .setApplicationName(APPLICATION_NAME)
                .build()
                ;
        
    }

}