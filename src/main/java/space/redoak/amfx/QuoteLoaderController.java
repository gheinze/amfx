package space.redoak.amfx;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.FutureTask;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import space.redoak.finance.securities.QuoteLoaderDaoService;
import space.redoak.finance.securities.QuoteLoaderFacade;
import space.redoak.finance.securities.SecurityExchange;
import space.redoak.util.TextFlowConsole;

/**
 *
 * @author glenn
 */
@Component
public class QuoteLoaderController {

    private static String DATE_FORMAT_WITH_DAY = "%tF [%ta]";
    
    @FXML
    private TextFlow recentLoadMsg;

    @FXML
    private TextFlow currentlyAvailableMsg;
    
    @FXML
    private Button loadEodDataButton;

    @FXML
    private TextFlow loadStatus;
    private TextFlowConsole textFlowConsole;
    
    @FXML
    private ScrollPane consoleScrollPane;
    

    private Optional<LocalDate> lastLoadDate;
    private Optional<LocalDate> currentlyAvailableDate;
    
    
    @Autowired
    private QuoteLoaderFacade quoteLoaderFacade;
    
    
    @FXML
    public void initialize() {
        populateRecentLoadMsg();
        populateCurrentlyAvailableMsg();
        updateLoadButtonState();
        prepareConsole();
    }
    
    
    private void populateRecentLoadMsg() {
        
        List<QuoteLoaderDaoService.QuoteDate> mostRecentQuoteDates = quoteLoaderFacade.getMostRecentQuoteDates();
        
        Optional<QuoteLoaderDaoService.QuoteDate> listLead = mostRecentQuoteDates.stream().findFirst();
        lastLoadDate = listLead.map(q -> q.getQuoteDate());
                
        List<Text> recentQuotes = mostRecentQuoteDates.stream()
                .skip(1L)
                .map(q -> String.format(DATE_FORMAT_WITH_DAY + ":  %,d quotes loaded\n", q.getQuoteDate(), q.getQuoteDate(), q.getQuoteCount()))
                .map(s -> new Text(s))
                .map(t -> {t.setFont(new Font(15)); return t;})
                .collect(Collectors.toList());
        
        ObservableList list = recentLoadMsg.getChildren(); 
        list.clear();

        listLead.ifPresent(q -> {
                Text text = new Text(String.format(DATE_FORMAT_WITH_DAY, q.getQuoteDate(), q.getQuoteDate()));
                text.setFont(new Font(15));
                text.setFill(Color.GREEN);  
                Text text2 = new Text(String.format(":  %,d quotes loaded\n", q.getQuoteCount()));
                text2.setFont(new Font(15));
                list.addAll(text, text2);
        });
        
        list.addAll(recentQuotes);        
    }

    
    private void populateCurrentlyAvailableMsg() {
        currentlyAvailableDate = quoteLoaderFacade.getLatestAvailableEodDate(SecurityExchange.TSX);
        String msg = currentlyAvailableDate.map(d -> String.format(DATE_FORMAT_WITH_DAY, d, d)).orElse("Unknown");
        Text text = new Text(msg);
        text.setFont(new Font(15));
        text.setFill(Color.GREEN);
        ObservableList list = currentlyAvailableMsg.getChildren(); 
        list.clear();
        list.addAll(text);
    }
    
    
    private void updateLoadButtonState() {
        
        try {
            loadEodDataButton.setDisable(
                    lastLoadDate.orElseThrow().equals(currentlyAvailableDate.orElseThrow())
            );
        } catch(NoSuchElementException nse) {
            loadEodDataButton.setDisable(true);
        }
        
    }

    private void prepareConsole() {
        textFlowConsole = new TextFlowConsole(loadStatus);
        // Change scroll position to bottom of page as text is appended
        loadStatus.getChildren().addListener(
                (ListChangeListener<Node>) ((change) -> {
                    loadStatus.layout();
                    consoleScrollPane.layout();
                    consoleScrollPane.setVvalue(1.0f);
                }));
    }

    
    private void fireLoadDone() {
        FutureTask<Void> updateUITask = new FutureTask(
                () -> {
                    populateRecentLoadMsg();
                    populateCurrentlyAvailableMsg();
                    updateLoadButtonState();        
                },
                null
        );

        Platform.runLater(updateUITask);
    }
    
    
    @FXML
    void loadEodData(MouseEvent event) {
        loadEodDataButton.setDisable(true);
        textFlowConsole.clear();
        consoleScrollPane.setVisible(true);
        quoteLoaderFacade.loadEodData(SecurityExchange.TSX, textFlowConsole, () -> {fireLoadDone();});
    }
    
    
}
