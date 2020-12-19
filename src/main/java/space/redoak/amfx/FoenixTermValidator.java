package space.redoak.amfx;

import com.jfoenix.controls.JFXTextField;
import com.jfoenix.validation.base.ValidatorBase;

/**
 *
 * @author glenn
 */
public class FoenixTermValidator extends ValidatorBase  {

    private final JFXTextField yearField;
    private final JFXTextField monthField;
    
    public FoenixTermValidator(JFXTextField yearField, JFXTextField monthField) {
        this.yearField = yearField;
        this.monthField = monthField;
    }
    
    
    @Override
    protected void eval() {
        
        int years = 0;
        int months = 0;
        
        try { 
            years = Integer.parseInt(yearField.getText());
        } catch(NumberFormatException nfe) {
            yearField.setText("0");
        }
        
        try { 
            months = Integer.parseInt(monthField.getText());
        } catch(NumberFormatException nfe) {
            monthField.setText("0");
        }
        
        boolean errorState = ( (years + months) == 0 );
        
        hasErrors.set(errorState);
        
    }
    

}
