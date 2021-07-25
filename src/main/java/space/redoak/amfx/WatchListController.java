package space.redoak.amfx;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import java.io.IOException;
import java.time.LocalDate;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
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
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import space.redoak.finance.securities.AlphaVantageQuoteDao;
import space.redoak.finance.securities.FinSecService;
import space.redoak.finance.securities.InstrumentFilterEntity;
import space.redoak.util.EditCell;
import space.redoak.util.MoneyStringConverter;
import space.redoak.util.TableCutAndPaste;

/**
 *
 * @author glenn
 */
@Slf4j
@Component
@Scope("prototype")
public class WatchListController {

    @Autowired
    private FinSecService finsecService;
    
    
    @FXML private TextField filterField;
     
    @FXML private ListView<InstrumentFilterEntity> instrumentListView;
    @FXML private Button updateQuotesButton;

    
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
        
        prepareInstrumentSelector();
        prepareWatchListTable();

    }
    
 
    private void prepareInstrumentSelector() {
        
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

        prepareDeleteColumn();
        prepareStrikePriceColumn();
        prepareCommentsColumn();

        setTableEditable();        
        TableCutAndPaste.installCopyPasteHandler(watchListTable);

        watchListTableModel = new WatchListTableModel(finsecService.getWatchList());
        watchListTable.setItems(watchListTableModel.getWatchList());
        
        watchListTable.getSortOrder().add(symbolColumn);
        
    }
    
    private void prepareDeleteColumn() {
        
        Callback<TableColumn<Instrument, Void>, TableCell<Instrument, Void>> cellFactory
                = (final TableColumn<Instrument, Void> param) -> new DeleteButtonTableCell();

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
  
    private void prepareStrikePriceColumn() {

        strikePriceColumn.setCellValueFactory(row -> row.getValue().strikePriceProperty());

        strikePriceColumn.setCellFactory(
                EditCell.<Instrument, Float>forTableColumn(new MoneyStringConverter())
        );

        strikePriceColumn.setOnEditCommit(event -> {
            
            if (null == event.getNewValue()) {
                return;
            }
            
            final Float newStrikePrice = event.getNewValue();
            
            Instrument instrument = watchListTableModel
                    .getWatchList()
                    .get(event.getTablePosition().getRow())
                    ;

            instrument.setStrikePrice(newStrikePrice);
            
            watchListTable.refresh();
            
            finsecService.updateInstrumentStrikePrice(instrument.getInstrumentId(), newStrikePrice);
            
        });

    }
    
    
    private void prepareCommentsColumn() {
        
        commentsColumn.setCellValueFactory(row -> row.getValue().commentsProperty());
        
        commentsColumn.setCellFactory(EditCell.<Instrument>forTableColumn());

        commentsColumn.setOnEditCommit(event -> {
            
            if (null == event.getNewValue()) {
                return;
            }
            
            String newComments = event.getNewValue();
            
            Instrument instrument = watchListTableModel
                    .getWatchList()
                    .get(event.getTablePosition().getRow())
                    ;

            instrument.setComments(newComments);
            
            watchListTable.refresh();
            
            finsecService.updateInstrumentComments(instrument.getInstrumentId(), newComments);
            
        });

    }


    private void setTableEditable() {
        watchListTable.setEditable(true);
        watchListTable.getSelectionModel().cellSelectionEnabledProperty().set(true);
        // when character or numbers pressed it will start edit in editable
        // fields
        watchListTable.setOnKeyPressed(event -> {
            if (event.getCode().isLetterKey() || event.getCode().isDigitKey()) {
                editFocusedCell();
            } else if (event.getCode() == KeyCode.RIGHT
                    || event.getCode() == KeyCode.TAB) {
                watchListTable.getSelectionModel().selectNext();
                event.consume();
            } else if (event.getCode() == KeyCode.LEFT) {
                // work around due to
                // TableView.getSelectionModel().selectPrevious() due to a bug
                // stopping it from working on
                // the first column in the last row of the table
                selectPrevious();
                event.consume();
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void editFocusedCell() {
        final TablePosition<Instrument, ?> focusedCell = watchListTable
                .focusModelProperty().get().focusedCellProperty().get();
        watchListTable.edit(focusedCell.getRow(), focusedCell.getTableColumn());
    }

    @SuppressWarnings("unchecked")
    private void selectPrevious() {
        if (watchListTable.getSelectionModel().isCellSelectionEnabled()) {
            // in cell selection mode, we have to wrap around, going from
            // right-to-left, and then wrapping to the end of the previous line
            TablePosition<Instrument, ?> pos = watchListTable.getFocusModel()
                    .getFocusedCell();
            if (pos.getColumn() - 1 >= 0) {
                // go to previous row
                watchListTable.getSelectionModel().select(pos.getRow(),
                        getTableColumn(pos.getTableColumn(), -1));
            } else if (pos.getRow() < watchListTable.getItems().size()) {
                // wrap to end of previous row
                watchListTable.getSelectionModel().select(pos.getRow() - 1,
                        watchListTable.getVisibleLeafColumn(
                                watchListTable.getVisibleLeafColumns().size() - 1));
            }
        } else {
            int focusIndex = watchListTable.getFocusModel().getFocusedIndex();
            if (focusIndex == -1) {
                watchListTable.getSelectionModel().select(watchListTable.getItems().size() - 1);
            } else if (focusIndex > 0) {
                watchListTable.getSelectionModel().select(focusIndex - 1);
            }
        }
    }

    private TableColumn<Instrument, ?> getTableColumn(
            final TableColumn<Instrument, ?> column, int offset) {
        int columnIndex = watchListTable.getVisibleLeafIndex(column);
        int newColumnIndex = columnIndex + offset;
        return watchListTable.getVisibleLeafColumn(newColumnIndex);
    }

    
    @FXML
    void handleUpdateQuotes(MouseEvent event) {
        
            FilteredList<Instrument> quoteList = watchListTableModel
                    .getWatchList()
                    .filtered(i -> i.getReadDate().isBefore(LocalDate.now()));

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(12), new EventHandler<ActionEvent>() {
                    private int i = 0;
                    @Override
                    public void handle(ActionEvent event) {
                        lookupQuote(quoteList.get(i));
                        i++;
                    }
                }));
        
        timeline.setCycleCount(quoteList.size() - 1);
        timeline.play();
        
    }
    
    private void lookupQuote(Instrument instrument) {
        
        try {
            
            AlphaVantageQuoteDao.GlobalQuote quote = finsecService.lookupQuote(instrument.getSymbol());
            if (null == quote.getClosingPrice()) {
                log.warn("Failed to retrieve quote for: " + instrument.getSymbol());
                return;
            }
            
            instrument.setClosePrice(quote.getClosingPrice().floatValue());
            instrument.setReadDate(quote.getLocalDate());
            
        } catch (IOException ex) {
            log.warn("Failed to fetch quote for: " + instrument.getSymbol(), ex);
        }
        
    }
    
    
}
