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

    private static final String DEBENTURE_TAB_TITLE = "Debentures";
    
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
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);
    }

    
    @FXML
    void openDebentureTab() throws IOException {

        Optional<Tab> findFirst = tabPane.getTabs().stream()
                .filter(t -> t.getText().equals(DEBENTURE_TAB_TITLE))
                .findFirst();
        
        findFirst.ifPresentOrElse(
                t -> tabPane.getSelectionModel().select(t),
                () -> {
                    try {
                        Pane debenturePane = (Pane) App.loadFXML("debenture");
                        Tab debentureTab = new Tab(DEBENTURE_TAB_TITLE);
                        debentureTab.setContent(debenturePane);
                        debentureTab.setClosable(true);
                        tabPane.getTabs().add(debentureTab);
                        tabPane.getSelectionModel().select(debentureTab);
                    } catch (IOException ex) {
                        log.error("Failed to find debenture tab", ex);
                    }
                }
        );

    }
    
}
