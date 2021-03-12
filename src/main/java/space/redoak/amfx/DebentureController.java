package space.redoak.amfx;

import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.JFXToggleButton;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Control;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.util.StringConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import space.redoak.finance.securities.DebentureEntity;
import space.redoak.finance.securities.QuoteEntity;
import space.redoak.finance.securities.FinSecService;
import space.redoak.finance.securities.QuoteChart;
import space.redoak.util.TableCutAndPaste;


/**
 *
 * @author glenn
 */
@Component
@Slf4j
public class DebentureController {

    
    private final StringConverter<Float> interestStringConverter = new StringConverter<>() {
        @Override
        public String toString​(Float object) {
            return Formats.Interest.nullSafeFormat(object);
        }

        @Override
        public Float fromString​(String string) {
            try {
                return Float.valueOf(string);
            } catch (Exception ex) {
                return null;
            }
        }
    };

    
    private final StringConverter<Float> moneyStringConverter = new StringConverter<>() {
        @Override
        public String toString​(Float object) {
            return Formats.Money.nullSafeFormat(object);
        }

        @Override
        public Float fromString​(String string) {
            try {
                return Float.valueOf(string);
            } catch (Exception ex) {
                return null;
            }
        }
    };

    
    private final StringConverter<Object> hyperlinkStringConverter = new StringConverter<>() {
        @Override
        public String toString​(Object object) {
            if (null == object) return "";
            if (object instanceof String) {
                return (String)object;
            }
            return ((Hyperlink)object).textProperty().getValue();
        }

        @Override
        public Object fromString​(String string) {
            return Debenture.createHyperlinkOrText(string);
        }
    };

    
    @FXML private TableView<Debenture> debentureTable;

    @FXML private TableColumn<Debenture, String> symbolColumn;
    @FXML private TableColumn<Debenture, String> descriptionColumn;
    @FXML private TableColumn<Debenture, Float> parRateColumn;
    @FXML private TableColumn<Debenture, Float> effectiveRateColumn;
    @FXML private TableColumn<Debenture, LocalDate> maturityColumn;
    @FXML private TableColumn<Debenture, Float> closeColumn;
    @FXML private TableColumn<Debenture, Integer> volumeColumn;
    @FXML private TableColumn<Debenture, LocalDate> dateColumn;
    @FXML private TableColumn<Debenture, String> underlyingSymbolColumn;
    @FXML private TableColumn<Debenture, Float> underlyingClosePriceColumn;
    @FXML private TableColumn<Debenture, LocalDate> underlyingReadDateColumn;
    @FXML private TableColumn<Debenture, Float> conversionPriceColumn;
    @FXML private TableColumn<Debenture, Float> conversionRateColumn;
    @FXML private TableColumn<Debenture, Float> convertedColumn;
    @FXML private TableColumn<Debenture, Object> prospectusColumn;
    @FXML private TableColumn<Debenture, String> commentsColumn;
    
    @FXML private JFXToggleButton detailToggle;
    @FXML private AnchorPane detailPane;
    @FXML private JFXTextField detailInterestRate;
    @FXML private Label detailSymbolLabel;
    @FXML private JFXDatePicker detailMaturityDate;
    @FXML private JFXTextField detailUnderlyingSymbol;
    @FXML private JFXTextField detailConversionPrice;
    @FXML private JFXTextField detailProspectus;
    @FXML private JFXTextField detailComments;
        
        
    private DebentureTableModel debentureTableModel;

    private String cachedPercentage;
    private LocalDate cahcedMaturityDate;
    private String cachedUnderlyingSymbol;
    private String cachedConversionPrice;
    private String cachedProspectus;
    private String cachedComments;
    
    @FXML
    private StackPane chartStackPane;
    private QuoteChart detailQuoteChart;
    
    @Autowired FinSecService finSecService;
    
    
    @FXML
    public void initialize() {        
        prepareDebentureTable();
        populateDebentureTable();
        prepareToolBar();
        prepareDetailPane();
        detailPane.setVisible(false);
        detailPane.setManaged(false);
    }
    
    
    public void prepareDebentureTable() {

        symbolColumn.setCellValueFactory(row -> row.getValue().symbolProperty());
        descriptionColumn.setCellValueFactory(row -> row.getValue().descriptionProperty());

        parRateColumn.setCellValueFactory(row -> row.getValue().percentageProperty());
        parRateColumn.setCellFactory((AbstractConvertCellFactory<Debenture, Float>) value -> Formats.Interest.nullSafeFormat(value));
        
        effectiveRateColumn.setCellValueFactory(row -> row.getValue().effectiveRateProperty());
        effectiveRateColumn.setCellFactory((AbstractConvertCellFactory<Debenture, Float>) value -> Formats.Interest.nullSafeFormat(value));
        
        maturityColumn.setCellValueFactory(row -> row.getValue().maturityDateProperty());
        
        closeColumn.setCellValueFactory(row -> row.getValue().closePriceProperty());
        closeColumn.setCellFactory((AbstractConvertCellFactory<Debenture, Float>) value -> Formats.Money.nullSafeFormat(value));

        volumeColumn.setCellValueFactory(row -> row.getValue().volumeProperty());
        volumeColumn.setCellFactory((AbstractConvertCellFactory<Debenture, Integer>) value -> Formats.Integer.nullSafeFormat(value));

        dateColumn.setCellValueFactory(row -> row.getValue().readDateProperty());
        
        underlyingSymbolColumn.setCellValueFactory(row -> row.getValue().underlyingSymbolProperty());
        
        underlyingClosePriceColumn.setCellValueFactory(row -> row.getValue().underlyingClosePriceProperty());
        underlyingClosePriceColumn.setCellFactory((AbstractConvertCellFactory<Debenture, Float>) value -> Formats.Money.nullSafeFormat(value));
        
        underlyingReadDateColumn.setCellValueFactory(row -> row.getValue().underlyingReadDateProperty());

        conversionPriceColumn.setCellValueFactory(row -> row.getValue().conversionPriceProperty());
        conversionPriceColumn.setCellFactory((AbstractConvertCellFactory<Debenture, Float>) value -> Formats.Money.nullSafeFormat(value));

        conversionRateColumn.setCellValueFactory(row -> row.getValue().conversionRateProperty());
        conversionRateColumn.setCellFactory((AbstractConvertCellFactory<Debenture, Float>) value -> Formats.Interest.nullSafeFormat(value));

        convertedColumn.setCellValueFactory(row -> row.getValue().convertedProperty());
        convertedColumn.setCellFactory((AbstractConvertCellFactory<Debenture, Float>) value -> Formats.Money.nullSafeFormat(value));

        prospectusColumn.setCellValueFactory(row -> row.getValue().prospectusProperty());
        
        commentsColumn.setCellValueFactory(row -> row.getValue().commentsProperty());
        
        commentsColumn.setCellFactory(tc -> {
            TableCell<Debenture, String> cell = new TableCell<>();
            Text text = new Text();
            cell.setGraphic(text);
            cell.setPrefHeight(Control.USE_COMPUTED_SIZE);
            text.wrappingWidthProperty().bind(commentsColumn.widthProperty());
            text.textProperty().bind(cell.itemProperty());
            return cell;
        });
        

        debentureTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        //debentureTable.getSelectionModel().setCellSelectionEnabled(true);
        TableCutAndPaste.installCopyPasteHandler(debentureTable);
        
    }


    private final ChangeListener<Debenture> debentureChangeListener = (ObservableValue<? extends Debenture> obs, Debenture oldDebenture, Debenture newDebenture) -> {

        // Remove detail pane bindings from the previously selected debenture that was in the table
        if (oldDebenture != null) {
            detailSymbolLabel.textProperty().unbindBidirectional(oldDebenture.symbolProperty());
            detailInterestRate.textProperty().unbindBidirectional(oldDebenture.percentageProperty());
            detailMaturityDate.valueProperty().unbindBidirectional(oldDebenture.maturityDateProperty());
            detailUnderlyingSymbol.textProperty().unbindBidirectional(oldDebenture.underlyingSymbolProperty());
            detailConversionPrice.textProperty().unbindBidirectional(oldDebenture.conversionPriceProperty());
            detailProspectus.textProperty().unbindBidirectional(oldDebenture.prospectusProperty());
            detailComments.textProperty().unbindBidirectional(oldDebenture.commentsProperty());
            chartStackPane.getChildren().remove(detailQuoteChart);
            
            
        }

        // If there is no debenture selected, nothing should go into the detail pane
        if (newDebenture == null) {
            detailInterestRate.clear();
            detailMaturityDate.setValue(LocalDate.now());
            detailUnderlyingSymbol.clear();
            detailConversionPrice.clear();
            detailProspectus.clear();
            detailComments.clear();
            chartStackPane.getChildren().remove(detailQuoteChart);
           
        // Bind the detail pane to the selected debenture in the table
        } else {
            Bindings.bindBidirectional(detailSymbolLabel.textProperty(), newDebenture.symbolProperty());
            Bindings.bindBidirectional(detailInterestRate.textProperty(), newDebenture.percentageProperty(), interestStringConverter);
            Bindings.bindBidirectional(detailMaturityDate.valueProperty(), newDebenture.maturityDateProperty());
            Bindings.bindBidirectional(detailUnderlyingSymbol.textProperty(), newDebenture.underlyingSymbolProperty());
            Bindings.bindBidirectional(detailConversionPrice.textProperty(), newDebenture.conversionPriceProperty(), moneyStringConverter);
            Bindings.bindBidirectional(detailProspectus.textProperty(), newDebenture.prospectusProperty(), hyperlinkStringConverter);
            Bindings.bindBidirectional(detailComments.textProperty(), newDebenture.commentsProperty());

            // Cache values in order to determine if the field becomes modified
            cachedPercentage = interestStringConverter.toString(newDebenture.percentageProperty().getValue());
            cahcedMaturityDate = newDebenture.maturityDateProperty().getValue();
            cachedUnderlyingSymbol = newDebenture.underlyingSymbolProperty().getValue();
            cachedConversionPrice = moneyStringConverter.toString(newDebenture.conversionPriceProperty().getValue());
            cachedProspectus = hyperlinkStringConverter.toString(newDebenture.prospectusProperty().getValue());
            cachedComments = newDebenture.commentsProperty().getValue();
            
            List<QuoteEntity> quotes = finSecService.getQuotes(newDebenture.getInstrumentId(), LocalDate.of(2020, 1, 1));
            detailQuoteChart = new QuoteChart(quotes);
            chartStackPane.getChildren().add(detailQuoteChart);
        }
        
    };


    private void populateDebentureTable() {
    
        Page<DebentureEntity> debenturePage = finSecService.getDebentures(Pageable.unpaged());
        List<Debenture> debentureList = debenturePage.getContent().stream()
                .map(d -> new Debenture(d))
                .collect(Collectors.toList())
                ;
        
        debentureTableModel = new DebentureTableModel(debentureList);
        debentureTableModel.currentDebentureProperty().addListener(debentureChangeListener);
        debentureTable.setItems(debentureTableModel.getDebentureList());
        
        //debentureTable.setVisible(true);   
    }
    
    private void prepareToolBar() {
        detailToggle.disableProperty().bind(
                Bindings.createBooleanBinding(() -> {
                    return debentureTable.getSelectionModel().isEmpty();
                },
                debentureTable.getSelectionModel().selectedIndexProperty()
        ));
    }
    
    
    private void prepareDetailPane() {
        
        detailInterestRate.textProperty().addListener(
                (var observable, var oldValue, var newValue) -> {
                    if (newValue.isBlank()) {
                        detailInterestRate.setText("");
                    } else if (!Formats.Interest.getPattern().matcher(newValue).matches()) {
                        detailInterestRate.setText(oldValue);
                    }
                }
        );
        
        
        detailInterestRate.focusedProperty().addListener(
                (obs, oldVal, newVal) -> {
                    // new focus value "false" implies focus lost
                    if (!newVal) {
                        Float percentage = debentureTableModel.currentDebentureProperty().getValue().getPercentage();
                        String precentageString = interestStringConverter.toString(percentage);
                        if (!precentageString.equals(cachedPercentage)) {
                            Integer instrumentId = debentureTableModel.currentDebentureProperty().getValue().getInstrumentId();
                            System.out.println("Write to db id: " + instrumentId + " value: "+ percentage);
                            finSecService.updateDebentureRate(instrumentId, percentage);
                            cachedPercentage = precentageString;
                        }
                    }
                }
        );
                
        detailMaturityDate.focusedProperty().addListener(
                (obs, oldVal, newVal) -> {
                    if (!newVal) {
                        LocalDate maturityDate = debentureTableModel.currentDebentureProperty().getValue().getMaturityDate();
                        if (null != maturityDate && !maturityDate.equals(cahcedMaturityDate)) {
                            Integer instrumentId = debentureTableModel.currentDebentureProperty().getValue().getInstrumentId();
                            System.out.println("Write to db id: " + instrumentId + " maturity: "+ maturityDate);
                            finSecService.updateDebentureMaturityDate(instrumentId, maturityDate);
                            cahcedMaturityDate = maturityDate;
                        }
                    }
                }
        );

        detailUnderlyingSymbol.focusedProperty().addListener(
                (obs, oldVal, newVal) -> {
                    if (!newVal) {
                        String underlyingSymbol = debentureTableModel.currentDebentureProperty().getValue().getUnderlyingSymbol();
                        if (null != underlyingSymbol && !underlyingSymbol.equals(cachedUnderlyingSymbol)) {
                            underlyingSymbol = underlyingSymbol.toUpperCase();
                            debentureTableModel.currentDebentureProperty().getValue().setUnderlyingSymbol(underlyingSymbol);
                            Integer instrumentId = debentureTableModel.currentDebentureProperty().getValue().getInstrumentId();
                            System.out.println("Write to db id: " + instrumentId + " underlyingSymbol: "+ underlyingSymbol);
                            Integer underlyingSymbolId = finSecService.findInstrumentIdForSymbol(underlyingSymbol);
                            if (null == underlyingSymbolId) {
                                System.out.println("Failed to find underlying symbol: " + underlyingSymbol);
                                debentureTableModel.currentDebentureProperty().getValue().setUnderlyingSymbol(cachedUnderlyingSymbol);
                            } else {
                                finSecService.updateDebentureUnderlyingSymbol(instrumentId, underlyingSymbolId);
                                cachedUnderlyingSymbol = underlyingSymbol;
                            }
                        }
                    }
                }
        );

        detailConversionPrice.focusedProperty().addListener(
                (obs, oldVal, newVal) -> {
                    if (!newVal) {
                        Float conversionPrice = debentureTableModel.currentDebentureProperty().getValue().getConversionPrice();
                        String conversionPriceString = moneyStringConverter.toString(conversionPrice);
                        if (!conversionPriceString.equals(cachedConversionPrice)) {
                            Integer instrumentId = debentureTableModel.currentDebentureProperty().getValue().getInstrumentId();
                            System.out.println("Write to db id: " + instrumentId + " conversionPrice: "+ conversionPrice);
                            finSecService.updateDebentureConversionPrice(instrumentId, conversionPrice);
                            cachedConversionPrice = conversionPriceString;
                        }
                    }
                
                }
        );

        detailProspectus.focusedProperty().addListener(
                (obs, oldVal, newVal) -> {
                    if (!newVal) {
                        Object prospectusObject = debentureTableModel.currentDebentureProperty().getValue().getProspectus();
                        String prospectus = hyperlinkStringConverter.toString(prospectusObject);
                        if (!prospectus.equals(cachedProspectus)) {
                            Integer instrumentId = debentureTableModel.currentDebentureProperty().getValue().getInstrumentId();
                            System.out.println("Write to db id: " + instrumentId + " prospectus: "+ prospectus);
                            finSecService.updateDebentureProspectus(instrumentId, prospectus);
                            cachedProspectus = prospectus;
                        }
                    }
                }
        );

        detailComments.focusedProperty().addListener(
                (obs, oldVal, newVal) -> {
                    if (!newVal) {
                        String comments = debentureTableModel.currentDebentureProperty().getValue().getComments();
                        if ( null != comments && !comments.equals(cachedComments)) {
                            Integer instrumentId = debentureTableModel.currentDebentureProperty().getValue().getInstrumentId();
                            System.out.println("Write to db id: " + instrumentId + " comments: "+ comments);
                            finSecService.updateDebentureComments(instrumentId, comments);
                            cachedComments = comments;
                        }
                    }
                }
        );

    }

    
    @FXML
    void handleToggle(ActionEvent event) {
        boolean newStateVisible = !detailPane.isVisible();
        detailPane.setVisible(newStateVisible);
        detailPane.setManaged(newStateVisible);
        if (newStateVisible) {
            debentureTableModel.currentDebentureProperty().bind(debentureTable.getSelectionModel().selectedItemProperty());
        } else {
            debentureTableModel.currentDebentureProperty().unbind();
        }
    }
    
 
    @FXML
    void updateGoogleDoc() {
        
        try {
            finSecService.publishToGoogleDoc(
                    debentureTableModel.getDebentureList().stream()
            );
        } catch (IOException | GeneralSecurityException ex) {
            log.error("Error attempting to publish debentures to Google Docs", ex);
        }
        
    }
    
}
