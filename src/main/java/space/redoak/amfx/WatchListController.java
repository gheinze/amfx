package space.redoak.amfx;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import java.time.LocalDate;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import space.redoak.finance.securities.FinSecService;
import space.redoak.finance.securities.InstrumentFilterEntity;
import space.redoak.util.TableCutAndPaste;

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

    
    @FXML private TableView<Instrument> watchListTable;

    @FXML private TableColumn<Instrument, Void> deleteColumn;
    @FXML private TableColumn<Instrument, String> symbolColumn;
    @FXML private TableColumn<Instrument, String> nameColumn;
    @FXML private TableColumn<Instrument, LocalDate> quoteDateColumn;
    @FXML private TableColumn<Instrument, Float> quoteColumn;
    @FXML private TableColumn<Instrument, Float> strikePriceColumn;
    @FXML private TableColumn<Instrument, String> commentsColumn;

    
    private WatchListTableModel watchListTableModel;
    
    
    @FXML
    public void initialize() {
        
        prepareSecuritySelector();
        prepareWatchListTable();

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
                Instrument instrument = finsecService.getInstrumentDetail(selectedItem.getId());
                finsecService.addToWatchList(instrument);
                watchListTableModel.addInstrument(instrument);
                watchListTable.sort();
            }
            
        });

    }

    
    private void prepareWatchListTable() {
        
        symbolColumn.setCellValueFactory(row -> row.getValue().symbolProperty());
        nameColumn.setCellValueFactory(row -> row.getValue().descriptionProperty());
        quoteDateColumn.setCellValueFactory(row -> row.getValue().readDateProperty());
        quoteColumn.setCellValueFactory(row -> row.getValue().closePriceProperty());
        quoteColumn.setCellFactory((AbstractConvertCellFactory<Instrument, Float>) value -> Formats.Money.nullSafeFormat(value));
        commentsColumn.setCellValueFactory(row -> row.getValue().commentsProperty());

        prepareDeleteColumn();
        
        TableCutAndPaste.installCopyPasteHandler(watchListTable);

        watchListTableModel = new WatchListTableModel(finsecService.getWatchList());
        watchListTable.setItems(watchListTableModel.getWatchList());
        
        watchListTable.getSortOrder().add(symbolColumn);
        
    }
    
    private void prepareDeleteColumn() {
        
        Callback<TableColumn<Instrument, Void>, TableCell<Instrument, Void>> cellFactory
                = new Callback<TableColumn<Instrument, Void>, TableCell<Instrument, Void>>() {
            @Override
            public TableCell<Instrument, Void> call(final TableColumn<Instrument, Void> param) {
                return new DeleteButtonTableCell();
            }
        };

        deleteColumn.setCellFactory(cellFactory);

    }
    
    
    private class DeleteButtonTableCell extends TableCell<Instrument, Void> {
        
        private final Button btn = new Button("", new FontAwesomeIconView(FontAwesomeIcon.TIMES));

        public DeleteButtonTableCell() {
            btn.setOnAction((ActionEvent event) -> {
                Instrument instrument = getTableView().getItems().get(getIndex());
                finsecService.removeFromWatchList(instrument);
                watchListTableModel.removeInstrument(instrument);
                watchListTable.sort();
            });
        }

        @Override
        public void updateItem(Void item, boolean empty) {
            super.updateItem(item, empty);
            setGraphic(empty ? null : btn);
        }
        
    }
    
}
