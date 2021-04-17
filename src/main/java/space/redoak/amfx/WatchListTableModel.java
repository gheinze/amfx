package space.redoak.amfx;

import java.util.List;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 *
 * @author glenn
 */
public class WatchListTableModel {

    private final ObservableList<Instrument> watchList;

    private final ObjectProperty<Instrument> currentInstrumentProperty = new SimpleObjectProperty<>();

    
    public WatchListTableModel(List<Instrument> instruments) {        
        watchList = FXCollections.observableArrayList(instruments);
    }
    
    
    public final ObjectProperty<Instrument> currentInstrumentProperty() {
        return this.currentInstrumentProperty;
    }


    public final Instrument getCurrentInstrument() {
        return this.currentInstrumentProperty().get();
    }


    public final void setCurrentDebenture(final Instrument currentInstrument) {
        this.currentInstrumentProperty().set(currentInstrument);
    }


    public ObservableList<Instrument> getInstrumentList() {
        return watchList;
    }
    
}
