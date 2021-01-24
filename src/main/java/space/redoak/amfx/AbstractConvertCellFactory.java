package space.redoak.amfx;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

/**
 *
 * @author https://stackoverflow.com/questions/11412360/javafx-table-cell-formatting
 * @param <E>
 * @param <T>
 */
public interface AbstractConvertCellFactory<E, T> extends Callback<TableColumn<E, T>, TableCell<E, T>> {

    @Override
    default TableCell<E, T> call(TableColumn<E, T> param) {
        return new TableCell<E, T>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                } else {
                    setText(convert(item));
                }
            }
        };
    }

    String convert(T value);  
    
}
