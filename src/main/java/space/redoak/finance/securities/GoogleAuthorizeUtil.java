package space.redoak.finance.securities;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.SheetsScopes;
import java.awt.Desktop;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import space.redoak.App;


/**
 * Perform the oauth workflow with Google to create a credential object for
 * accessing Google apis.
 * 
 * Copied from: https://www.baeldung.com/google-sheets-java-client
 */
public class GoogleAuthorizeUtil {

   
//   private static final File CREDENTIAL_FILE_LOCATION = new File(
//           System.getProperty("user.home") + File.separator + ".amfx/google/googleDocDebentureCredential"
//   );


    public static Credential authorize(String clientCredentials) throws IOException, GeneralSecurityException {

//        InputStream in = GoogleAuthorizeUtil.class.getResourceAsStream("/google-sheets-debenture-client-secret.json");
//        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JacksonFactory.getDefaultInstance(), new InputStreamReader(in));

        // load the client credentials
        InputStream is = new ByteArrayInputStream( clientCredentials.getBytes( Charset.defaultCharset() ) );
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JacksonFactory.getDefaultInstance(), new InputStreamReader(is));

        List<String> scopes = Arrays.asList(SheetsScopes.SPREADSHEETS);

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JacksonFactory.getDefaultInstance(),
                clientSecrets,
                scopes
//        ).setDataStoreFactory(new com.google.api.client.util.store.FileDataStoreFactory(CREDENTIAL_FILE_LOCATION))
        ).setDataStoreFactory(new com.google.api.client.util.store.MemoryDataStoreFactory())
                .setAccessType("offline")
                .build()
                ;
                  
        // "authorize" can prompt for user login (Google OAuth)
        // provide a browser to present the Google logon page
        // setup a listener to capture the token response
        // If the browser isn't there, the console will log a message:
        //
        // Please open the following address in your browser:
        //   https://accounts.google.com/o/oauth2/auth?access_type=offline&client_id=641320086503-a875k3l6ojfvsd3omdjrnv9u9avjab0q.apps.googleusercontent.com&redirect_uri=http://localhost:38763/Callback&response_type=code&scope=https://www.googleapis.com/auth/spreadsheets
                
        Credential credential = new AuthorizationCodeInstalledApp(
                flow,
                new LocalServerReceiver(),
                (url) -> { App.hostServices.showDocument(url); }
        ).authorize("user");

        return credential;
    }

}
