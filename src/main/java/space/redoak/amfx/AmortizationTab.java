package space.redoak.amfx;


import java.io.IOException;
import javafx.scene.control.Tab;
import javafx.scene.layout.Pane;
import space.redoak.App;


/**
 *
 * @author glenn
 */
public class AmortizationTab extends Tab {
    
    private static int tabNumber = 1;
    
    
    public AmortizationTab() throws IOException {
        super("AmSched " + tabNumber++);
//        Pane mortgageAttributePane =  FXMLLoader.load(getClass().getResource("/space/redoak/amfx/loanTerms.fxml"));
        Pane mortgageAttributePane =  (Pane)App.loadFXML("loanTerms");
        this.setContent(mortgageAttributePane);
    }
    
}
