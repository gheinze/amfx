package space.redoak.finance.math;

import java.math.BigDecimal;
import java.math.RoundingMode;
import javax.money.Monetary;
import javax.money.MonetaryAmount;
import javax.money.MonetaryOperator;

/**
 *
 * @author glenn
 */
public class HalfUpMonetaryRounder implements MonetaryOperator {

    @Override
    public MonetaryAmount apply(MonetaryAmount amount) {
        
        int fractionDigits = amount.getCurrency().getDefaultFractionDigits();
        
        BigDecimal roundedAmount = amount.getNumber().numberValue(BigDecimal.class)
                .setScale(fractionDigits, RoundingMode.HALF_UP)
                ;
        
        return Monetary.getDefaultAmountFactory()
                .setCurrency(amount.getCurrency())
                .setNumber(roundedAmount)
                .create()
                ;
    }
    
}
