package space.redoak.amfx;

import java.util.List;
import java.util.stream.Collectors;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import space.redoak.finance.securities.QuoteLoaderDao;
import space.redoak.finance.securities.QuoteLoaderService;

/**
 *
 * @author glenn
 */
@Component
public class QuoteLoaderController {

    @FXML
    private TextFlow recentLoadMsg;

    @FXML
    private TextFlow currentlyAvailableMsg;
    
    
    @Autowired
    private QuoteLoaderService quoteLoaderService;
    
    
    @FXML
    public void initialize() {        
        populateRecentLoadMsg();        
    }
    
    
    private void populateRecentLoadMsg() {
        List<QuoteLoaderDao.QuoteDate> mostRecentQuoteDates = quoteLoaderService.getMostRecentQuoteDates();
        
        List<Text> recentQuotes = mostRecentQuoteDates.stream()
                .map(q -> String.format("%tF [%ta]:  %,d quotes loaded\n", q.getQuoteDate(), q.getQuoteDate(), q.getQuoteCount()))
                .map(s -> new Text(s))
                .map(t -> {t.setFont(new Font(15)); return t;})
                .collect(Collectors.toList());
        
        ObservableList list = recentLoadMsg.getChildren(); 
        list.clear();
        list.addAll(recentQuotes);        
    }
    
    
}
