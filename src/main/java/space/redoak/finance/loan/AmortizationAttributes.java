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
public class AmortizationAttributes {

    //@DecimalMin(value = "0.00", inclusive = false) @NotNull
    private MonetaryAmount loanAmount;      // original principal amount

    //@DecimalMin("0.00")
    private MonetaryAmount regularPayment;  // monthly payment to be made, assumed monthly, null allowed since it will be calculated

    private LocalDate startDate;            // loan start date
    private LocalDate adjustmentDate;       // date from which amortization calculations commence

    //@Min(1) @Max(360) @NotNull
    private int termInMonths;               // number of months from the adjustment date at which amortization stops and remaining principal is due

    private boolean interestOnly;           // true if this is an interest only calculation (ie no amortization)

    //@Min(1) @Max(360) @NotNull
    private int amortizationPeriodInMonths; // number of months over which to amortize the payments. If payments are made till this date, principal remaining will be 0

    //@Min(1) @Max(52)
    private int compoundingPeriodsPerYear;  // number of times a year interest compounding is calculated. Canadian rules: 2 (semi-annually). American rules: 12 (monthly)

    //@Min(1) @Max(52)
    private int paymentFrequency;           // number of times a year payments will be made

    //@DecimalMin("0.001") @Max(50)  @NotNull
    private double interestRateAsPercent;   // the nominal interest rate being paid (effective rate can be higher if compounding)



    public static AmortizationAttributes getDefaultInstance(MonetaryAmount loanAmount) {

        AmortizationAttributes amAttrs = new AmortizationAttributes();

        amAttrs.setLoanAmount(loanAmount);
        amAttrs.setRegularPayment(loanAmount.multiply(0L));

        LocalDate adjustedDate = AmortizationCalculator.getNextFirstOrFifteenthOfTheMonth(LocalDate.now());
        amAttrs.setAdjustmentDate(adjustedDate);
        amAttrs.setStartDate(adjustedDate);

        amAttrs.setTermInMonths(12);
        amAttrs.setInterestOnly(false);
        amAttrs.setAmortizationPeriodInMonths(240);
        amAttrs.setCompoundingPeriodsPerYear(2);
        amAttrs.setPaymentFrequency(12);
        amAttrs.setInterestRateAsPercent(10.);

        return amAttrs;
    }
  
}
