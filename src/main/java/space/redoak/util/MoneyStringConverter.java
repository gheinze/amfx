package space.redoak.util;

import javafx.util.converter.FloatStringConverter;
import space.redoak.amfx.Formats;

/**
 *
 * @author glenn
 */
public class MoneyStringConverter extends FloatStringConverter {

    @Override
    public String toString(final Float value) {
        return Formats.Money.nullSafeFormat(value);
    }
    
    
    @Override
    public Float fromString(final String value) {
        return value.isEmpty() || !isNumber(value) ? null : super.fromString(value);
    }

    
    public boolean isNumber(String value) {
        int size = value.length();
        for (int i = 0; i < size; i++) {
            if (!Character.isDigit(value.charAt(i))) {
                return false;
            }
        }
        return size > 0;
    }
    
}
