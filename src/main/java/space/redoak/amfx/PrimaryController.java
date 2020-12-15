package space.redoak.amfx;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.control.TabPane;
import space.redoak.finance.loan.gui.AmortizationTab;

public class PrimaryController {

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
    
    
}
