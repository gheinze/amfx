package space.redoak.amfx;

import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.JFXToggleButton;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.util.StringConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import space.redoak.finance.securities.DebentureEntity;

import space.redoak.finance.securities.FinSecService;
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
            return ((Hyperlink)object).textProperty().getValue();
        }

        @Override
        public Object fromString​(String string) {
            return Debenture.createHyperlinkOrText(string);
        }
    };

    
    @FXML private BorderPane debentureDetail;
    
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

        debentureTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        //debentureTable.getSelectionModel().setCellSelectionEnabled(true);
        TableCutAndPaste.installCopyPasteHandler(debentureTable);
        
    }


    private ChangeListener<Debenture> debentureChangeListener = (ObservableValue<? extends Debenture> obs, Debenture oldDebenture, Debenture newDebenture) -> {

        // Remove detail pane bindings from the previously selected debenture that was in the table
        if (oldDebenture != null) {
            detailInterestRate.textProperty().unbindBidirectional(oldDebenture.percentageProperty());
            //detailMaturityDate;
            detailUnderlyingSymbol.textProperty().unbindBidirectional(oldDebenture.underlyingSymbolProperty());
            detailConversionPrice.textProperty().unbindBidirectional(oldDebenture.conversionPriceProperty());
            detailProspectus.textProperty().unbindBidirectional(oldDebenture.prospectusProperty());
            detailComments.textProperty().unbindBidirectional(oldDebenture.commentsProperty());
            
            
        }

        // If there is no debenture selected, nothing should go into the detail pane
        if (newDebenture == null) {
            detailInterestRate.clear();
            //detailMaturityDate;
            detailUnderlyingSymbol.clear();
            detailConversionPrice.clear();
            detailProspectus.clear();
            detailComments.clear();
           
        // Bind the detail pane to the selected debenture in the table
        } else {
            Bindings.bindBidirectional(detailSymbolLabel.textProperty(), newDebenture.symbolProperty());
            Bindings.bindBidirectional(detailInterestRate.textProperty(), newDebenture.percentageProperty(), interestStringConverter);
            Bindings.bindBidirectional(detailMaturityDate.valueProperty(), newDebenture.maturityDateProperty());
            Bindings.bindBidirectional(detailUnderlyingSymbol.textProperty(), newDebenture.underlyingSymbolProperty());
            Bindings.bindBidirectional(detailConversionPrice.textProperty(), newDebenture.conversionPriceProperty(), moneyStringConverter);
            Bindings.bindBidirectional(detailProspectus.textProperty(), newDebenture.prospectusProperty(), hyperlinkStringConverter);
            Bindings.bindBidirectional(detailComments.textProperty(), newDebenture.commentsProperty());
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
                (obs, oldVal, newVal)
                -> { if (!newVal) { System.out.println("Write to db: " + detailInterestRate.textProperty().getValue()); } }
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
    
 
}
