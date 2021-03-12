package space.redoak.amfx;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.scene.control.Hyperlink;
import space.redoak.App;
import space.redoak.finance.securities.DebentureEntity;

/**
 *
 * @author glenn
 */
public class Debenture {

        private final Integer                         instrumentId;
        private final SimpleStringProperty            symbol;
        private final SimpleStringProperty            description;
        private final SimpleObjectProperty<Float>     percentage;
        private final SimpleObjectProperty<LocalDate> issuedDate;
        private final SimpleObjectProperty            effectiveRate;
        private final SimpleObjectProperty<LocalDate> maturityDate;
        private final ObservableValue<Float>          closePrice;
        private final ObservableValue<Integer>        volume;
        private final ObservableValue<LocalDate>      readDate;
        private final SimpleStringProperty            underlyingSymbol;
        private final ObservableValue<Float>          underlyingClosePrice;
        private final ObservableValue<LocalDate>      underlyingReadDate;
        private final SimpleObjectProperty<Float>     conversionPrice;
        private final ObservableValue<Float>          conversionRate;
        private final ObservableValue<Float>          converted;
        private final SimpleObjectProperty            prospectus;
        private final SimpleStringProperty            comments;

        public Debenture(DebentureEntity debenture) {
            this.instrumentId = debenture.getInstrumentId();
            this.symbol = new SimpleStringProperty(debenture.getSymbol());
            this.description = new SimpleStringProperty(debenture.getDescr());
            this.percentage = new SimpleObjectProperty<>(debenture.getPercentage());
            this.issuedDate = new SimpleObjectProperty(debenture.getIssueDte());
            this.effectiveRate = new SimpleObjectProperty(debenture.getEffectiveRate());
            this.maturityDate = new SimpleObjectProperty(debenture.getMaturityDte());
            this.closePrice = new SimpleObjectProperty(debenture.getClosePrice());
            this.volume = new SimpleObjectProperty(debenture.getVolumeTraded());
            this.readDate = new SimpleObjectProperty(debenture.getReadDte());
            this.underlyingSymbol = new SimpleStringProperty(debenture.getUnderlyingSymbol());
            this.underlyingClosePrice = new SimpleObjectProperty(debenture.getUnderlyingClosePrice());
            this.underlyingReadDate = new SimpleObjectProperty(debenture.getUnderlyingReadDte());
            this.conversionPrice = new SimpleObjectProperty(debenture.getConversionPrice());
            this.conversionRate = new SimpleObjectProperty(debenture.getConversionRate());
            this.converted = new SimpleObjectProperty(debenture.getConverted());
            this.prospectus = new SimpleObjectProperty(createHyperlinkOrText(debenture.getProspectus()));
            this.comments = new SimpleStringProperty(debenture.getComments());
            
            effectiveRate.bind(
                    Bindings.createObjectBinding(
                            () -> {
                                if (null == percentage.getValue() || null == closePrice.getValue() || null == maturityDate.getValue()) {
                                    return null;
                                }
                                Float effRate = percentage.getValue() - (
                                        (closePrice.getValue() - 100f) / (ChronoUnit.DAYS.between(LocalDate.now(), maturityDate.getValue()) / 365f)
                                        );
                                return effRate;
                            },
                            percentage, closePrice, maturityDate
                    )
            );
            

        }
        
        
        public static Object createHyperlinkOrText(String url) {
            
            if (null == url) { return ""; }
            
            if (url.toLowerCase().startsWith("http")) {
                Hyperlink hyperlink = new Hyperlink(url);
                hyperlink.setOnAction((ActionEvent t) -> {
                    App.hostServices.showDocument(hyperlink.getText());
                });            
                return hyperlink;
            }
            
            return url;
        }

        public final Integer getInstrumentId() {
            return instrumentId;
        }

        public final StringProperty symbolProperty() { return this.symbol; }
        public final String getSymbol() { return this.symbolProperty().get(); }
        public final void setSymbol(final String symbol) { this.symbolProperty().set(symbol); }

        public final StringProperty descriptionProperty() { return this.description; }
        public final String getDescription() { return this.descriptionProperty().get(); }
        public final void setDescription(final String description) { this.descriptionProperty().set(description); }

        public final SimpleObjectProperty<Float> percentageProperty() { return this.percentage; }
        public final Float getPercentage() { return this.percentageProperty().getValue(); }
        public final void setPercentage(final Float percentage) { this.percentageProperty().set(percentage); }

        public final SimpleObjectProperty<LocalDate> issuedDateProperty() { return this.issuedDate; }
        public final LocalDate getIssuedDate() { return this.issuedDateProperty().get(); }
        public final void setIssuedDate(final LocalDate issuedDate) { this.issuedDateProperty().set(issuedDate); }

        public final SimpleObjectProperty<Float> effectiveRateProperty() { return this.effectiveRate; }
        public final Float getEffectiveRate() { return this.effectiveRateProperty().getValue(); }
        public final void setEffectiveRate(final Float effectiveRate) { this.effectiveRateProperty().set(effectiveRate); }

        public final SimpleObjectProperty<LocalDate> maturityDateProperty() { return this.maturityDate; }
        public final LocalDate getMaturityDate() { return this.maturityDateProperty().get(); }
        public final void setMaturityDate(final LocalDate maturityDate) { this.maturityDateProperty().set(maturityDate); }

        public final ObservableValue<Float> closePriceProperty() { return this.closePrice; }
        public final Float getClosePrice() { return this.closePriceProperty().getValue(); }

        public final ObservableValue<Integer> volumeProperty() { return this.volume; }
        public final Integer getVolume() { return this.volumeProperty().getValue(); }

        public final ObservableValue<LocalDate> readDateProperty() { return this.readDate; }
        public final LocalDate getReadDate() { return this.readDateProperty().getValue(); }

        public final StringProperty underlyingSymbolProperty() { return this.underlyingSymbol; }
        public final String getUnderlyingSymbol() { return this.underlyingSymbolProperty().get(); }
        public final void setUnderlyingSymbol(final String underlyingSymbol) { this.underlyingSymbolProperty().set(underlyingSymbol); }

        public final ObservableValue<Float> underlyingClosePriceProperty() { return this.underlyingClosePrice; }
        public final Float getUnderlyingClosePrice() { return this.underlyingClosePriceProperty().getValue(); }
    
        public final ObservableValue<LocalDate> underlyingReadDateProperty() { return this.underlyingReadDate; }
        public final LocalDate getUnderlyingReadDate() { return this.underlyingReadDateProperty().getValue(); }

        public final SimpleObjectProperty<Float> conversionPriceProperty() { return this.conversionPrice; }
        public final Float getConversionPrice() { return this.conversionPriceProperty().get(); }
        public final void setConversionPrice(final Float conversionPrice) { this.conversionPriceProperty().set(conversionPrice); }

        public final ObservableValue<Float> conversionRateProperty() { return this.conversionRate; }
        public final Float getConversionRate() { return this.conversionRateProperty().getValue(); }

        public final ObservableValue<Float> convertedProperty() { return this.converted; }
        public final Float getConverted() { return this.convertedProperty().getValue(); }

        public final SimpleObjectProperty<Object> prospectusProperty() { return this.prospectus; }
        public final Object getProspectus() { return this.prospectusProperty().get(); }
        public final void setProspectus(final Object prospectus) { this.prospectusProperty().set(prospectus); }

        public final StringProperty commentsProperty() { return this.comments; }
        public final String getComments() { return this.commentsProperty().get(); }
        public final void setComments(final String comments) { this.commentsProperty().set(comments); }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + Objects.hashCode(this.symbol);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Debenture other = (Debenture) obj;
        if (!Objects.equals(this.symbol, other.symbol)) {
            return false;
        }
        return true;
    }

    
    
    private static final String SEPARATOR = "~";
    
    private static final DecimalFormat MONEY_FORMAT = new DecimalFormat("#,###.00");
    private static final DecimalFormat PERCENT_FORMAT = new DecimalFormat("#,###.000");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    //=IF(ISBLANK(H23),"", HYPERLINK(H23, "Prospectus"))

    public String toCsv() {
        
        StringBuilder sb = new StringBuilder();

        sb.append(getSymbol()).append(SEPARATOR);
        sb.append(getDescription()).append(SEPARATOR);
        sb.append(null == getPercentage()    ? "" : PERCENT_FORMAT.format(getPercentage())).append(SEPARATOR);
        sb.append(null == getIssuedDate()    ? "" : DATE_FORMAT.format(getIssuedDate())).append(SEPARATOR);
        sb.append(null == getMaturityDate()  ? "" : DATE_FORMAT.format(getMaturityDate())).append(SEPARATOR);
        sb.append(null == getClosePrice()    ? "" : MONEY_FORMAT.format(getClosePrice())).append(SEPARATOR);
        sb.append(null == getReadDate()      ? "" : DATE_FORMAT.format(getReadDate())).append(SEPARATOR);
        sb.append(getEffectiveRate()).append(SEPARATOR);
        sb.append(null == getUnderlyingSymbol()        ? "" : getUnderlyingSymbol()).append(SEPARATOR);
        sb.append(null == getUnderlyingClosePrice()    ? "" : MONEY_FORMAT.format(getUnderlyingClosePrice())).append(SEPARATOR);
        sb.append(null == getUnderlyingReadDate() ? "" : DATE_FORMAT.format(getUnderlyingReadDate())).append(SEPARATOR);
        sb.append(null == getConversionPrice() ? "" : MONEY_FORMAT.format(getConversionPrice())).append(SEPARATOR);
        sb.append(getConversionRate()).append(SEPARATOR);
        sb.append(getConverted()).append(SEPARATOR);
        sb.append(null == getProspectus() ? "" : getProspectus() instanceof String ? getProspectus() : ((Hyperlink)getProspectus()).getText()).append(SEPARATOR);
        sb.append(null == getComments() ? "" : getComments());
        
        return sb.toString();
    }    
}
