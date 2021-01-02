package space.redoak.finance.loan;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import javax.money.Monetary;
import javax.money.MonetaryAmount;
import javax.money.MonetaryOperator;
import org.springframework.stereotype.Service;
import space.redoak.finance.math.AnyUpMonetaryRounder;
import space.redoak.finance.math.HalfUpMonetaryRounder;

/**
 *
 * @author glenn
 */
@Service
public class AmortizationCalculator {

    private static final int STANDARD_DAYS_IN_A_YEAR = 365; // 366 in leap year
    private static final int STANDARD_WEEKS_IN_A_YEAR = 52; // 52 * 7 = 364

    private static final MonetaryOperator ANY_UP_ROUNDING_MODE = new AnyUpMonetaryRounder();
    private static final MonetaryOperator HALF_UP_ROUNDING_MODE = new HalfUpMonetaryRounder();



    public MonetaryAmount getPeriodicPayment(AmortizationAttributes amAttrs) {
        return amAttrs.isInterestOnly() ?
                getInterestOnlyPeriodicPayment(amAttrs) :
                getAmortizedPeriodicPayment(amAttrs)
                ;
    }


    private MonetaryAmount getInterestOnlyPeriodicPayment(AmortizationAttributes amAttrs) {
        MonetaryAmount loanAmount = amAttrs.getLoanAmount();
        double interestRateAsDecimal = amAttrs.getInterestRateAsPercent() / 100.;
        int paymentFrequency = amAttrs.getPaymentFrequency();
        return loanAmount.multiply(interestRateAsDecimal / paymentFrequency).with(ANY_UP_ROUNDING_MODE);
    }


    /*
     * A = P * r / (1 - (1 + r)^-n])
     * Where:
     *   A = periodic payment
     *   P = principal amount borrowed
     *   r = periodic interest rate (as decimal)
     *   n = years
     *
     * See: https://en.wikipedia.org/wiki/Mortgage_calculator#Monthly_payment_formula
     *
     */
    private MonetaryAmount getAmortizedPeriodicPayment(AmortizationAttributes amAttrs) {

        double loanAmount = amAttrs.getLoanAmount().getNumber().doubleValueExact();
        double periodRate = getPeriodRateAsDecimal(amAttrs);
        double amortizationYears = (double)amAttrs.getAmortizationPeriodInMonths() / 12.;

        double paymentFrequency = (double)amAttrs.getPaymentFrequency();
        double periodPayment = loanAmount * (periodRate) /
                (1.0 - Math.pow(1.0 + periodRate, -paymentFrequency * amortizationYears));

        return Monetary.getDefaultAmountFactory()
                .setCurrency(amAttrs.getLoanAmount().getCurrency())
                .setNumber(periodPayment)
                .create()
                .with(ANY_UP_ROUNDING_MODE)
                ;

    }


    /*
     * periodRate = (1 + i / c)^(c/p) - 1
     *
     * Where:
     *   i = nominal annual interest rate
     *   c = number of compounding periods per year
     *   p = number of payments per year
     *
     * See: http://www.vertex42.com/ExcelArticles/amortization-calculation.html
     */
    private double getPeriodRateAsDecimal(AmortizationAttributes amAttrs) {
        double i = amAttrs.getInterestRateAsPercent() / 100.;
        double c = (double)amAttrs.getCompoundingPeriodsPerYear();
        double p = (double)amAttrs.getPaymentFrequency();
        return Math.pow( 1. + (i / c), c / p ) - 1.;
    }


    public MonetaryAmount getPerDiem(MonetaryAmount amount, double annualInterestRatePercent) {
        return amount.multiply(annualInterestRatePercent * 0.01 / STANDARD_DAYS_IN_A_YEAR).with(ANY_UP_ROUNDING_MODE);
    }


    /**
     * The interest that would be owing for one period with the given terms.
     * @param amAttrs
     * @return
     */
    public MonetaryAmount getPeriodInterest(AmortizationAttributes amAttrs) {
        return amAttrs.isInterestOnly() ?
                getInterestOnlyPeriodicPayment(amAttrs) :
                getAmortizedPeriodInterest(amAttrs)
                ;
    }

    private MonetaryAmount getAmortizedPeriodInterest(AmortizationAttributes amAttrs) {
        double periodicRate = getPeriodRateAsDecimal(amAttrs);
        return amAttrs.getLoanAmount().multiply(periodicRate).with(HALF_UP_ROUNDING_MODE);
    }


    public List<ScheduledPayment> generateSchedule(AmortizationAttributes amAttrs) {
        return amAttrs.isInterestOnly() ?
                generateInterestOnlySchedule(amAttrs) :
                generateAmortizedSchedule(amAttrs)
                ;
    }


    public static LocalDate getNextFirstOrFifteenthOfTheMonth(LocalDate baseDate) {

        LocalDate nextDate = null == baseDate ? LocalDate.now() : baseDate;
        int baseDayOfMonth = nextDate.getDayOfMonth();

        if (baseDayOfMonth > 15) {
            return nextDate.plusMonths(1).withDayOfMonth(1);

        } else if (baseDayOfMonth > 1 && baseDayOfMonth < 15) {
            return nextDate.withDayOfMonth(15);
        }

        assert baseDayOfMonth == 15 || baseDayOfMonth == 1;

        return LocalDate.from(nextDate);

    }

    
    private ScheduledPayment getAdjusmentPayment(AmortizationAttributes amAttrs) {
        
        long days = amAttrs.getStartDate().until(amAttrs.getAdjustmentDate(), ChronoUnit.DAYS);
        
        MonetaryAmount perDiem = getPerDiem(
                amAttrs.getLoanAmount(),
                amAttrs.getInterestRateAsPercent()
        ).multiply(days);
        
        ScheduledPayment payment = new ScheduledPayment();
        payment.setPaymentNumber(0);
        payment.setPaymentDate(amAttrs.getAdjustmentDate());
        payment.setInterest(perDiem);
        payment.setPrincipal(perDiem.multiply(0));
        payment.setBalance(amAttrs.getLoanAmount());

        return payment;
    };
    
    
    /* Any item in the list of interest only scheduled payments can be computed without
     * reliance on previous items in the list.
    */
    private List<ScheduledPayment> generateInterestOnlySchedule(AmortizationAttributes amAttrs) {

        return new AbstractList<ScheduledPayment>() {

            private final MonetaryAmount calculatedPayment = getPeriodicPayment(amAttrs);
            private final MonetaryAmount principal = calculatedPayment.multiply(0l);

//            private final MonetaryAmount actualPayment = getActualPayment(amAttrs);
//            private final MonetaryAmount overPayment = actualPayment.subtract(calculatedPayment);
            private final int expectedNumberOfPayments = getNumberOfExpectedPayments(amAttrs) + 1;


            @Override
            public ScheduledPayment get(int paymentNumber) {

                if (paymentNumber == 0) {
                    return getAdjusmentPayment(amAttrs);                    
                }
                
                ScheduledPayment payment =  getTemplatePayment(paymentNumber, expectedNumberOfPayments, amAttrs);

                payment.setInterest(calculatedPayment);
                payment.setPrincipal(principal);
                payment.setBalance(amAttrs.getLoanAmount());

                return payment;

            }

            @Override
            public int size() {
                return expectedNumberOfPayments;
            }

        };
    }

    /* An amortized list of payments will all be pre-computed.
    */
    private List<ScheduledPayment> generateAmortizedSchedule(AmortizationAttributes amAttrs) {

        List<ScheduledPayment> schedule = new ArrayList<>();
        
        schedule.add(getAdjusmentPayment(amAttrs));

        final MonetaryAmount actualPayment = getActualPayment(amAttrs);
        final int expectedNumberOfPayments = getNumberOfExpectedPayments(amAttrs);

        MonetaryAmount remainingBalance = amAttrs.getLoanAmount();
        double periodicRate = getPeriodRateAsDecimal(amAttrs);

        // loop
        for (int paymentNumber = 1; paymentNumber <= expectedNumberOfPayments; paymentNumber++) {

            // Interest amounts rounding take precedence
            MonetaryAmount interest = remainingBalance.multiply(periodicRate).with(HALF_UP_ROUNDING_MODE);

            // The periodic payment is consistent, so anything that is not interest is principal
            MonetaryAmount principal = actualPayment.subtract(interest);

            if (principal.isGreaterThan(remainingBalance)) {
                principal = remainingBalance;
            }
            remainingBalance = remainingBalance.subtract(principal);

            ScheduledPayment payment =  getTemplatePayment(paymentNumber, expectedNumberOfPayments, amAttrs);

            payment.setInterest(interest);
            payment.setPrincipal(principal);
            payment.setBalance(remainingBalance);

            schedule.add(payment);
        }

        return schedule;

    }


    private int getNumberOfExpectedPayments(AmortizationAttributes amAttrs) {
        return (int) Math.ceil(amAttrs.getPaymentFrequency() * amAttrs.getTermInMonths() / 12.);
    }


    private MonetaryAmount getActualPayment(AmortizationAttributes amAttrs) {
        return null == amAttrs.getRegularPayment() ?
            getPeriodicPayment(amAttrs) :
            amAttrs.getRegularPayment();
    }


    private ScheduledPayment getTemplatePayment(int paymentNumber, int totalPayments, AmortizationAttributes amAttrs) {

        if (paymentNumber < 1 || paymentNumber > totalPayments) {
            throw new IndexOutOfBoundsException(String.format("Payment number %d outside of schedule range 1 - %d", paymentNumber, totalPayments));
        }

        ScheduledPayment templatePayment = new ScheduledPayment();
        templatePayment.setPaymentNumber(paymentNumber);
        templatePayment.setPaymentDate(getPaymentDateFrom(paymentNumber, amAttrs));

        return templatePayment;
    }


    private LocalDate getPaymentDateFrom(int paymentNumber, AmortizationAttributes amAttrs) {
        LocalDate scheduleStartDate = amAttrs.getAdjustmentDate();
        int paymentFrequency = amAttrs.getPaymentFrequency();
        TimePeriod timePeriod = TimePeriod.getTimePeriodWithPeriodCountOf(paymentFrequency);
        return timePeriod.getDateFrom(scheduleStartDate, paymentNumber);
    }
    
}
