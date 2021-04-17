package space.redoak.amfx;

import java.time.LocalDate;
import java.util.Objects;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import space.redoak.finance.securities.InstrumentEntity;

/**
 *
 * @author glenn
 */
public class Instrument {

        private final Integer                         instrumentId;
        private final SimpleStringProperty            symbol;
        private final SimpleStringProperty            description;
        private final ObservableValue<LocalDate>      readDate;
        private final ObservableValue<Float>          closePrice;
        private final ObservableValue<Float>          strikePrice;
        private final SimpleStringProperty            comments;

        public Instrument(InstrumentEntity instrument) {
            this.instrumentId = instrument.getInstrumentId();
            this.symbol = new SimpleStringProperty(instrument.getSymbol());
            this.description = new SimpleStringProperty(instrument.getDescr());
            this.readDate = new SimpleObjectProperty(instrument.getReadDte());
            this.closePrice = new SimpleObjectProperty(instrument.getClosePrice());
            this.strikePrice = new SimpleObjectProperty(instrument.getStrikePrice());
            this.comments = new SimpleStringProperty(instrument.getComments());
        }
        
        
        public final Integer getInstrumentId() { return instrumentId; }

        public final StringProperty symbolProperty() { return this.symbol; }
        public final String getSymbol() { return this.symbolProperty().get(); }
        public final void setSymbol(final String symbol) { this.symbolProperty().set(symbol); }

        public final StringProperty descriptionProperty() { return this.description; }
        public final String getDescription() { return this.descriptionProperty().get(); }
        public final void setDescription(final String description) { this.descriptionProperty().set(description); }

        public final ObservableValue<LocalDate> readDateProperty() { return this.readDate; }
        public final LocalDate getReadDate() { return this.readDateProperty().getValue(); }

        public final ObservableValue<Float> closePriceProperty() { return this.closePrice; }
        public final Float getClosePrice() { return this.closePriceProperty().getValue(); }

        public final ObservableValue<Float> strikePriceProperty() { return this.strikePrice; }
        public final Float getStrikePrice() { return this.strikePriceProperty().getValue(); }

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
        final Instrument other = (Instrument) obj;
        if (!Objects.equals(this.symbol, other.symbol)) {
            return false;
        }
        return true;
    }

    
}
