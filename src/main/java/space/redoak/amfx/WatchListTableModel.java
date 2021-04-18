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

    
    public WatchListTableModel(List<Instrument> list) {
        watchList = FXCollections.observableList(list);
    }
        
    public final ObjectProperty<Instrument> currentInstrumentProperty() {
        return this.currentInstrumentProperty;
    }

    
    public ObservableList<Instrument> getWatchList() {
        return watchList;
    }

    public final Instrument getCurrentInstrument() {
        return this.currentInstrumentProperty().get();
    }


    public final void setCurrentInstrument(final Instrument currentInstrument) {
        this.currentInstrumentProperty().set(currentInstrument);
    }

    
    public void addInstrument(final Instrument instrument) {
        watchList.add(instrument);
    }

    void removeInstrument(Instrument instrument) {
        watchList.remove(instrument);
    }
    
}
