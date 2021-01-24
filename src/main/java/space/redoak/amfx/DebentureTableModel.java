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
public class DebentureTableModel {

    private final ObservableList<Debenture> debentureList;

    private final ObjectProperty<Debenture> currentDebentureProperty = new SimpleObjectProperty<>();

    
    public DebentureTableModel(List<Debenture> debentures) {        
        debentureList = FXCollections.observableArrayList(debentures);
    }
    
    
    public final ObjectProperty<Debenture> currentDebentureProperty() {
        return this.currentDebentureProperty;
    }


    public final Debenture getCurrentDebenture() {
        return this.currentDebentureProperty().get();
    }


    public final void setCurrentDebenture(final Debenture currentDebenturer) {
        this.currentDebentureProperty().set(currentDebenturer);
    }


    public ObservableList<Debenture> getDebentureList() {
        return debentureList;
    }   
    
}
