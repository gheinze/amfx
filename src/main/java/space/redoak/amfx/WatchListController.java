package space.redoak.amfx;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import space.redoak.finance.securities.FinSecService;
import space.redoak.finance.securities.InstrumentEntity;
import space.redoak.finance.securities.InstrumentFilterEntity;

/**
 *
 * @author glenn
 */
@Component
@Scope("prototype")
public class WatchListController {

    @Autowired
    private FinSecService finsecService;
    
    
    @FXML private TextField filterField;
     
    @FXML private ListView<InstrumentFilterEntity> instrumentListView;

    
    @FXML private TableView<?> watchListTable;

    @FXML private TableColumn<?, ?> symbolColumn;
    @FXML private TableColumn<?, ?> nameColumn;
    @FXML private TableColumn<?, ?> quoteDateColumn;
    @FXML private TableColumn<?, ?> quoteColumn;
    @FXML private TableColumn<?, ?> strikePriceColumn;
    @FXML private TableColumn<?, ?> noteColumn;

    
    @FXML
    public void initialize() {
        
        prepareSecuritySelector();

    }
    
 
    private void prepareSecuritySelector() {
        
        ObservableList<InstrumentFilterEntity> data = FXCollections.observableArrayList();
        data.addAll(finsecService.getInstrumentsSparse("TSX"));
        
        FilteredList<InstrumentFilterEntity> filteredData = new FilteredList<>(data, s -> true);
        
        instrumentListView.setItems(filteredData);

        filterField.textProperty().addListener(obs -> {
            
            final String filter = filterField.getText();
            
            if (null == filter || filter.isEmpty()) {
                filteredData.setPredicate(s -> true);
                
            } else {
                
                filteredData.setPredicate(s -> {
                    
                    if (filter.length() <= 3) {
                        return s.getSymbol().startsWith(filter.toUpperCase());
                    }
                    
                    return s.getSymbol().startsWith(filter.toUpperCase()) ||
                            s.getDescr().toUpperCase().contains(filter.toUpperCase());
                    
                });
            }
            
        });


        instrumentListView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            
            @Override
            public void handle(MouseEvent event) {
                
                if (event.getButton() != MouseButton.PRIMARY || event.getClickCount() != 2) {
                    return;
                }

                InstrumentFilterEntity selectedItem = instrumentListView.getSelectionModel().getSelectedItem();
                System.out.println("Selection: " + selectedItem);
                InstrumentEntity instrumentDetail = finsecService.getInstrumentDetail(selectedItem.getId());
                System.out.println(instrumentDetail);
                    //watchListModel.add(selectedItem);
            }
            
        });

    }

    
}
