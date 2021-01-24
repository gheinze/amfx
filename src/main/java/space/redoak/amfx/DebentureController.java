package space.redoak.amfx;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.JFXToggleButton;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;
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

    private static final ThreadLocal<DecimalFormat> INTEREST_FORMATTER = ThreadLocal.withInitial(() -> new DecimalFormat("##.000"));
    private static final ThreadLocal<DecimalFormat> MONEY_FORMATTER    = ThreadLocal.withInitial(() -> new DecimalFormat("###,###.00"));
    private static final ThreadLocal<DecimalFormat> INTEGER_FORMATTER  = ThreadLocal.withInitial(() -> new DecimalFormat("###,###,###"));

    private final static Pattern INTEREST_PATTERN         = Pattern.compile("^\\d{1,2}(\\.\\d{0,3})?$");
    private final static Pattern CURRENCY_PATTERN         = Pattern.compile("^\\d{1,8}(\\.\\d{0,2})?$");
    private final static Pattern DOUBLE_DIGIT_INT_PATTERN = Pattern.compile("^\\d{1,2}$");

    private final StringConverter<Float> interestStringConverter = new StringConverter<>() {
        @Override
        public String toString​(Float object) {
            return nullSafeDecimalFormat(object, INTEREST_FORMATTER);
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
    @FXML private TableColumn<Debenture, Hyperlink> prospectusColumn;
    @FXML private TableColumn<Debenture, String> commentsColumn;
    
    @FXML private JFXToggleButton detailToggle;
    @FXML private AnchorPane detailPane;
    @FXML private JFXTextField detailInterestRate;
    @FXML private JFXButton saveButton;
    @FXML private JFXButton revertButton;

        
        
    private DebentureTableModel debentureTableModel;
    
    @Autowired FinSecService finSecService;
    
    
    @FXML
    public void initialize() {        
        prepareDebentureTable();
        populateDebentureTable();
        prepareToolBar();
        prepareDetailPaneFields();
        detailPane.setVisible(false);
        detailPane.setManaged(false);
    }
    
    public void prepareDebentureTable() {
        
        symbolColumn.setCellValueFactory(row -> row.getValue().symbolProperty());
        descriptionColumn.setCellValueFactory(row -> row.getValue().descriptionProperty());
        
        parRateColumn.setCellValueFactory(row -> row.getValue().percentageProperty());
        parRateColumn.setCellFactory((AbstractConvertCellFactory<Debenture, Float>) value -> nullSafeDecimalFormat(value, INTEREST_FORMATTER));
        
        effectiveRateColumn.setCellValueFactory(row -> row.getValue().effectiveRateProperty());
        effectiveRateColumn.setCellFactory((AbstractConvertCellFactory<Debenture, Float>) value -> nullSafeDecimalFormat(value, INTEREST_FORMATTER));

        maturityColumn.setCellValueFactory(row -> row.getValue().maturityDateProperty());
        
        closeColumn.setCellValueFactory(row -> row.getValue().closePriceProperty());
        closeColumn.setCellFactory((AbstractConvertCellFactory<Debenture, Float>) value -> nullSafeDecimalFormat(value, MONEY_FORMATTER));

        volumeColumn.setCellValueFactory(row -> row.getValue().volumeProperty());
        volumeColumn.setCellFactory((AbstractConvertCellFactory<Debenture, Integer>) value -> nullSafeDecimalFormat(value, INTEGER_FORMATTER));

        dateColumn.setCellValueFactory(row -> row.getValue().readDateProperty());
        
        underlyingSymbolColumn.setCellValueFactory(row -> row.getValue().underlyingSymbolProperty());
        
        underlyingClosePriceColumn.setCellValueFactory(row -> row.getValue().underlyingClosePriceProperty());
        underlyingClosePriceColumn.setCellFactory((AbstractConvertCellFactory<Debenture, Float>) value -> nullSafeDecimalFormat(value, MONEY_FORMATTER));
        
        underlyingReadDateColumn.setCellValueFactory(row -> row.getValue().underlyingReadDateProperty());

        conversionPriceColumn.setCellValueFactory(row -> row.getValue().conversionPriceProperty());
        conversionPriceColumn.setCellFactory((AbstractConvertCellFactory<Debenture, Float>) value -> nullSafeDecimalFormat(value, MONEY_FORMATTER));

        conversionRateColumn.setCellValueFactory(row -> row.getValue().conversionRateProperty());
        conversionRateColumn.setCellFactory((AbstractConvertCellFactory<Debenture, Float>) value -> nullSafeDecimalFormat(value, INTEREST_FORMATTER));

        convertedColumn.setCellValueFactory(row -> row.getValue().convertedProperty());
        convertedColumn.setCellFactory((AbstractConvertCellFactory<Debenture, Float>) value -> nullSafeDecimalFormat(value, MONEY_FORMATTER));

        prospectusColumn.setCellValueFactory(row -> row.getValue().prospectusProperty());
        
        commentsColumn.setCellValueFactory(row -> row.getValue().commentsProperty());

        debentureTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        //debentureTable.getSelectionModel().setCellSelectionEnabled(true);
        TableCutAndPaste.installCopyPasteHandler(debentureTable);
        
    }


    private boolean recordModified = false;
    private String cachedInterestRate;
    
    private ChangeListener<Debenture> debentureListener = (ObservableValue<? extends Debenture> obs, Debenture oldDebenture, Debenture newDebenture) -> {

        if (oldDebenture != null) {
            detailInterestRate.textProperty().unbindBidirectional(oldDebenture.percentageProperty());
        }

        if (newDebenture == null) {
            detailInterestRate.clear();
        } else {
            Bindings.bindBidirectional(detailInterestRate.textProperty(), newDebenture.percentageProperty(), interestStringConverter);
            recordModified = false;
            cachedInterestRate = detailInterestRate.textProperty().get();
            saveButton.disableProperty().bind(
                Bindings.createBooleanBinding(() -> {
                    return cachedInterestRate.equals(detailInterestRate.textProperty().get());
                },
                detailInterestRate.textProperty()
            ));
            revertButton.disableProperty().bind(saveButton.disableProperty());

        }
        
    };


    private void populateDebentureTable() {
    
        Page<DebentureEntity> debenturePage = finSecService.getDebentures(Pageable.unpaged());
        List<Debenture> debentureList = debenturePage.getContent().stream()
                .map(d -> new Debenture(d))
                .collect(Collectors.toList())
                ;
        
        debentureTableModel = new DebentureTableModel(debentureList);
        debentureTableModel.currentDebentureProperty().addListener(debentureListener);
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
    
    
    private String nullSafeDecimalFormat(Number val, ThreadLocal<DecimalFormat> threadSafeFormatter) {
        return null == val ? "" : threadSafeFormatter.get().format(val);
    }

    
    private void prepareDetailPaneFields() {
        
        detailInterestRate.textProperty().addListener(
                (var observable, var oldValue, var newValue) -> {
                    if (newValue.isBlank()) {
                        detailInterestRate.setText("");
                    } else if (!INTEREST_PATTERN.matcher(newValue).matches()) {
                        detailInterestRate.setText(oldValue);
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
            System.out.println("Selected row: " + debentureTableModel.getCurrentDebenture().getSymbol());
        } else {
            debentureTableModel.currentDebentureProperty().unbind();
        }
    }

    @FXML
    void handleRevert(ActionEvent event) {
        detailInterestRate.setText(cachedInterestRate);
    }

    @FXML
    void handleSave(ActionEvent event) {

    }




    
 
}
