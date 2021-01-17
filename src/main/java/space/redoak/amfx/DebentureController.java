package space.redoak.amfx;

import java.text.DecimalFormat;
import java.util.List;
import java.util.stream.Collectors;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
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
public class DebentureController {

    @FXML private BorderPane debentureDetail;
    
    @FXML private TableView<RowData> debentureTable;

    @FXML private TableColumn<RowData, String> symbolColumn;
    @FXML private TableColumn<RowData, String> descriptionColumn;
    @FXML private TableColumn<RowData, Float> parRateColumn;
    @FXML private TableColumn<RowData, Float> effectiveRateColumn;
    @FXML private TableColumn<RowData, String> maturityColumn;
    @FXML private TableColumn<RowData, String> closeColumn;
    @FXML private TableColumn<RowData, String> volumeColumn;
    @FXML private TableColumn<RowData, String> dateColumn;
    @FXML private TableColumn<RowData, String> underlyingSymbolColumn;
    @FXML private TableColumn<RowData, String> underlyingClosePriceColumn;
    @FXML private TableColumn<RowData, String> underlyingReadDateColumn;
    @FXML private TableColumn<RowData, String> conversionPriceColumn;
    @FXML private TableColumn<RowData, Float> conversionRateColumn;
    @FXML private TableColumn<RowData, String> convertedColumn;
    @FXML private TableColumn<RowData, Hyperlink> prospectusColumn;
    @FXML private TableColumn<RowData, String> commentsColumn;
    
    private static final DecimalFormat interestFormatter = new DecimalFormat("##.00");
    private static final DecimalFormat moneyFormatter = new DecimalFormat("###,###.00");
    private static final DecimalFormat integerFormatter = new DecimalFormat("###,###,###");
    
    @Autowired FinSecService finSecService;
    
    
    @FXML
    public void initialize() {
        prepareDebentureTable();
        populateDebentureTable();
    }
    
    public void prepareDebentureTable() {
        
        symbolColumn.setCellValueFactory(param -> param.getValue().symbol);
        descriptionColumn.setCellValueFactory(param -> param.getValue().description);
        parRateColumn.setCellValueFactory(param -> param.getValue().percentage);
        effectiveRateColumn.setCellValueFactory(param -> param.getValue().effectiveRate);
        maturityColumn.setCellValueFactory(param -> param.getValue().maturityDate);
        closeColumn.setCellValueFactory(param -> param.getValue().closePrice);
        volumeColumn.setCellValueFactory(param -> param.getValue().volume);
        dateColumn.setCellValueFactory(param -> param.getValue().readDate);
        
        underlyingSymbolColumn.setCellValueFactory(param -> param.getValue().underlyingSymbol);
        underlyingClosePriceColumn.setCellValueFactory(param -> param.getValue().underlyingClosePrice);
        underlyingReadDateColumn.setCellValueFactory(param -> param.getValue().underlyingReadDate);

        conversionPriceColumn.setCellValueFactory(param -> param.getValue().conversionPrice);
        conversionRateColumn.setCellValueFactory(param -> param.getValue().conversionRate);
        convertedColumn.setCellValueFactory(param -> param.getValue().converted);

        prospectusColumn.setCellValueFactory(param -> param.getValue().prospectus);
        commentsColumn.setCellValueFactory(param -> param.getValue().comments);


        debentureTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        debentureTable.getSelectionModel().setCellSelectionEnabled(true);
        TableCutAndPaste.installCopyPasteHandler(debentureTable);
        
    }

    public void populateDebentureTable() {
    
        Page<DebentureEntity> debenturePage = finSecService.getDebentures(Pageable.unpaged());
        List<RowData> observableCollection = debenturePage.getContent().stream()
                .map(d -> new RowData(d))
                .collect(Collectors.toList())
                ;
        
        debentureTable.setItems(FXCollections.observableArrayList(observableCollection));        
        //debentureTable.setVisible(true);   
    }

    
    public static class RowData {

        private final SimpleStringProperty symbol;
        private final SimpleStringProperty description;
        private final ObservableValue<Float> percentage;
        private final ObservableValue<Float> effectiveRate;
        private final SimpleStringProperty maturityDate;
        private final SimpleStringProperty closePrice;
        private final SimpleStringProperty volume;
        private final SimpleStringProperty readDate;
        private final SimpleStringProperty underlyingSymbol;
        private final SimpleStringProperty underlyingClosePrice;
        private final SimpleStringProperty underlyingReadDate;
        private final SimpleStringProperty conversionPrice;
        private final ObservableValue<Float> conversionRate;
        private final SimpleStringProperty converted;
        private final ObservableValue<Hyperlink> prospectus;
        private final SimpleStringProperty comments;

        private RowData(DebentureEntity debenture) {
            this.symbol = new SimpleStringProperty(debenture.getSymbol());
            this.description = new SimpleStringProperty(debenture.getDescr());
            this.percentage = new SimpleObjectProperty(debenture.getPercentage());
            this.effectiveRate = new SimpleObjectProperty(debenture.getEffectiveRate());
            this.maturityDate = new SimpleStringProperty( null == debenture.getMaturityDte() ? "" : debenture.getMaturityDte().toString());
            this.closePrice = new SimpleStringProperty(null == debenture.getClosePrice() ? "" : moneyFormatter.format(debenture.getClosePrice()));
            this.volume = new SimpleStringProperty(null == debenture.getVolumeTraded() ? "" : integerFormatter.format(debenture.getVolumeTraded()));
            this.readDate = new SimpleStringProperty(null == debenture.getReadDte() ? "" : debenture.getReadDte().toString());
            this.underlyingSymbol = new SimpleStringProperty(null == debenture.getUnderlyingSymbol() ? "" : debenture.getUnderlyingSymbol());
            this.underlyingClosePrice = new SimpleStringProperty(null == debenture.getUnderlyingClosePrice() ? "" : moneyFormatter.format(debenture.getUnderlyingClosePrice()));
            this.underlyingReadDate = new SimpleStringProperty(null == debenture.getUnderlyingReadDte() ? "" : debenture.getUnderlyingReadDte().toString());
            this.conversionPrice = new SimpleStringProperty(null == debenture.getConversionPrice() ? "" : moneyFormatter.format(debenture.getConversionPrice()));
            this.conversionRate = new SimpleObjectProperty(debenture.getConversionRate());
            this.converted = new SimpleStringProperty(null == debenture.getConverted() ? "" : moneyFormatter.format(debenture.getConverted()));
            this.prospectus = new SimpleObjectProperty(null == debenture.getProspectus() ? null: createHyperlink(debenture.getProspectus()));
            this.comments = new SimpleStringProperty(null == debenture.getComments() ? "" : debenture.getComments());            
        }
        
        private Hyperlink createHyperlink(String url) {
            Hyperlink hyperlink = new Hyperlink(url);
            hyperlink.setOnAction((ActionEvent t) -> {
                App.hostServices.showDocument(hyperlink.getText());
            });            
            return hyperlink;
        }
        
    }
    
}
