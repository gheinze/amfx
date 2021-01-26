package space.redoak.amfx;

import java.text.DecimalFormat;
import java.util.regex.Pattern;

/**
 *
 * @author glenn
 */
public enum Formats {

    Interest ("##.000",      "^\\d{1,2}(\\.\\d{0,3})?$"),
    Money    ("###,###.00",  "^\\d{1,8}(\\.\\d{0,2})?$"),
    Integer  ("###,###,###", "^\\d{1,2}$"),
    DoubleDigitInteger("00", "^\\d{1,2}$")
    ;
    
    private final ThreadLocal<DecimalFormat> formatter;
    private final Pattern pattern;
    
    private Formats(String format, String pattern) {
        this.formatter = ThreadLocal.withInitial(() -> new DecimalFormat(format));
        this.pattern = Pattern.compile(pattern);
    }

    public ThreadLocal<DecimalFormat> getFormatter() {
        return this.formatter;
    }
    
    public Pattern getPattern() {
        return this.pattern;
    }
    
    public String nullSafeFormat(Number val) {
        return null == val ? "" : formatter.get().format(val);
    }

}
