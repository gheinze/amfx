package space.redoak.util;

import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.Callback;
import javafx.util.StringConverter;
import javafx.util.converter.DefaultStringConverter;

/**
 * https://lankydan.dev/2017/02/11/editable-tables-in-javafx
 * 
 * "(Fairly) reusable edit cell that commits on loss of focus on the text field.Overriding the commitEdit(...) method is difficult to do without relying on
 knowing the default implementation, which I had to do here.The test code
 includes a key handler on the table that initiates editing on a key press.
 * "
 *
 * @author https://gist.github.com/james-d/be5bbd6255a4640a5357
 * @param <S>
 * @param <T>
 */
public class EditCell<S, T> extends TextFieldTableCell<S, T> {

    private TextField textField;
    private boolean escapePressed = false;
    private TablePosition<S, ?> tablePos = null;


    public static <S> Callback<TableColumn<S, String>, TableCell<S, String>> forTableColumn() {
        return forTableColumn(new DefaultStringConverter());
    }

    public static <S, T> Callback<TableColumn<S, T>, TableCell<S, T>> forTableColumn(
            final StringConverter<T> converter
    ) {
        return list -> new EditCell<S, T>(converter);
    }



    public EditCell(final StringConverter<T> converter) {
        super(converter);
    }


    @Override
    public void startEdit() {
        
        if (!isEditable() || !getTableView().isEditable() || !getTableColumn().isEditable()) {
            return;
        }
        
        super.startEdit();

        if (isEditing()) {
            if (textField == null) {
                textField = getTextField();
            }
            escapePressed = false;
            startEdit(textField);
            final TableView<S> table = getTableView();
            tablePos = table.getEditingCell();
        }
        
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void commitEdit(T newValue) {
        
        if (!isEditing()) {
            return;
        }
        
        final TableView<S> table = getTableView();
        
        if (table != null) {
            // Inform the TableView of the edit being ready to be committed.
            CellEditEvent editEvent = new CellEditEvent(
                    table, tablePos, TableColumn.editCommitEvent(), newValue
            );

            Event.fireEvent(getTableColumn(), editEvent);
        }
        
        // we need to setEditing(false):
        super.cancelEdit(); // this fires an invalid EditCancelEvent.
        
        // update the item within this cell, so that it represents the new value
        updateItem(newValue, false);
        
        if (table != null) {
            // reset the editing cell on the TableView
            table.edit(-1, null);
        }
        
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void cancelEdit() {
        
        if (escapePressed) {
            // this is a cancel event after escape key
            super.cancelEdit();
            setText(getItemText()); // restore the original text in the view
        } else {
            // this is not a cancel event after escape key
            // we interpret it as commit.
            String newText = textField.getText();
            // commit the new text to the model
            this.commitEdit(getConverter().fromString(newText));
        }
        
        setGraphic(null); // stop editing with TextField
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);
        updateItem();
    }

    private TextField getTextField() {

        final TextField textField = new TextField(getItemText());

        textField.setOnAction((ActionEvent event) -> {
            System.out.println("hi");
        });

        // Use onAction here rather than onKeyReleased (with check for Enter),
        textField.setOnAction(event -> {
            if (getConverter() == null) {
                throw new IllegalStateException("StringConverter is null.");
            }
            this.commitEdit(getConverter().fromString(textField.getText()));
            event.consume();
        });

        textField.focusedProperty().addListener(
                (ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
                    if (!newValue) {
                        commitEdit(getConverter().fromString(textField.getText()));
                    }
                });

        textField.setOnKeyPressed(t -> {
            escapePressed = t.getCode() == KeyCode.ESCAPE;
        });
        
        textField.setOnKeyReleased(t -> {
            if (t.getCode() == KeyCode.ESCAPE) {
                throw new IllegalArgumentException(
                        "did not expect esc key releases here.");
            }
        });

        textField.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            
            if (null != event.getCode()) {
                
                switch (event.getCode()) {
                    
                    case ESCAPE -> {
                        textField.setText(getConverter().toString(getItem()));
                        cancelEdit();
                        event.consume();
                    }
                        
                    case RIGHT, TAB -> {
                        getTableView().getSelectionModel().selectNext();
                        event.consume();
                    }
                    case LEFT -> {
                        getTableView().getSelectionModel().selectPrevious();
                        event.consume();
                    }
                        
                    case UP -> {
                        getTableView().getSelectionModel().selectAboveCell();
                        event.consume();
                    }
                        
                    case DOWN -> {
                        getTableView().getSelectionModel().selectBelowCell();
                        event.consume();
                    }
                    
                    default -> {
                    }
                    
                }
                
            }
            
        });

        return textField;
    }

    private String getItemText() {
        return getConverter() == null
                ? getItem() == null ? "" : getItem().toString()
                : getConverter().toString(getItem());
    }

    private void updateItem() {
        
        if (isEmpty()) {
            setText(null);
            setGraphic(null);
            
        } else {
            if (isEditing()) {
                if (textField != null) {
                    textField.setText(getItemText());
                }
                setText(null);
                setGraphic(textField);
            } else {
                setText(getItemText());
                setGraphic(null);
            }
        }
        
    }

    private void startEdit(final TextField textField) {
        
        if (textField != null) {
            textField.setText(getItemText());
        }
        
        setText(null);
        setGraphic(textField);

        if (textField != null) {
            textField.selectAll();
            // requesting focus so that key input can immediately go into the TextField
            textField.requestFocus();
        }
        
    }

}
