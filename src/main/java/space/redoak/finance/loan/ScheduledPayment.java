package space.redoak.finance.loan;

import java.time.LocalDate;
import javax.money.MonetaryAmount;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 *
 * @author glenn
 */
@Getter
@Setter
@ToString
public class ScheduledPayment {

    private int paymentNumber;
    private LocalDate paymentDate;
    private MonetaryAmount interest;
    private MonetaryAmount principal;
    private MonetaryAmount balance;


    public MonetaryAmount getPayment() {
        return getInterest().add( getPrincipal() );
    }

}
