package space.redoak.amfx;

import java.io.IOException;
import java.util.Optional;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.Pane;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import space.redoak.App;
import space.redoak.finance.securities.FinSecService;

@Slf4j
@Component
public class AppController {

    private enum SingletonTabs {
        
        Debenture("Debentures", "debenture"),
        EodLoad("Load EOD Quotes", "eodLoad");
        
        private final String title;
        private final String fxmlFileName;
        
        private SingletonTabs(final String title, final String fxmlFileName) {
            this.title = title;
            this.fxmlFileName = fxmlFileName;
        }
        
        public String getTitle() {return title;}
        public String getFxmlFileName() { return fxmlFileName;}
        
    }
    
    
    
    @Autowired FinSecService finSecService;
    
    @FXML
    private TabPane tabPane;
    
    
    @FXML
    private void openMtgCalcTab() throws IOException {
        //App.setRoot("secondary");
        int numTabs = tabPane.getTabs().size();
        AmortizationTab tab = new AmortizationTab();
        tabPane.getTabs().add(tab);
        tabPane.getSelectionModel().select(tab);
    }

    
    @FXML
    void openDebentureTab() throws IOException {
        openSingletonTab(SingletonTabs.Debenture);
    }

    
    @FXML
    void openEodTab () throws IOException {
        openSingletonTab(SingletonTabs.EodLoad);
    }
 
    
    private Optional<Tab> findTab(final String tabName) {
        return tabPane.getTabs().stream()
                .filter(t -> t.getText().equals(tabName))
                .findFirst()
                ;
    }
    
    void openSingletonTab(final SingletonTabs tabDescriptor) throws IOException {
        
        findTab(tabDescriptor.getTitle()).ifPresentOrElse(
                
                t -> tabPane.getSelectionModel().select(t),
                
                () -> {
                    try {
                        Pane pane = (Pane) App.loadFXML(tabDescriptor.getFxmlFileName());
                        Tab tab = new Tab(tabDescriptor.getTitle());
                        tab.setContent(pane);
                        tab.setClosable(true);
                        tabPane.getTabs().add(tab);
                        tabPane.getSelectionModel().select(tab);
                    } catch (IOException ex) {
                        log.error("Failed to find tab: " + tabDescriptor.getTitle(), ex);
                    }
                }
        );

    }
 
    
}
