/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package space.redoak.amfx;

import com.jfoenix.controls.JFXTextField;
import com.jfoenix.validation.RequiredFieldValidator;
import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javafx.beans.value.ObservableValue;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javax.money.MonetaryAmount;
import space.redoak.finance.loan.AmortizationAttributes;
import space.redoak.finance.loan.TimePeriod;
import space.redoak.jfx.DecimalTextField;

import org.javamoney.moneta.Money;
import space.redoak.finance.loan.AmortizationCalculator;

/**
 * FXML Controller class
 *
 * @author glenn
 */
public class MortgageAmountFormController implements Initializable {


    private final PseudoClass errorClass = PseudoClass.getPseudoClass("error");
    
    private final Map<String, Boolean> validationFailures = new HashMap<>();
    
    
    @FXML
    private DecimalTextField loanAmount;

    @FXML
    private DecimalTextField interestRate;

    @FXML
    private CheckBox amortized;

    @FXML
    private Spinner<Integer> amYearsSpinner;
    
    @FXML
    private Spinner<Integer> amMonthsSpinner;
    
    @FXML
    private ComboBox<TimePeriod> compoundingPeriod;

    @FXML
    private Spinner<Integer> termYearsSpinner;
    
    @FXML
    private Spinner<Integer> termMonthsSpinner;
    
    @FXML
    private DatePicker startDate;

    @FXML
    private DatePicker adjustmentDate;

    @FXML
    private ComboBox<TimePeriod> paymentFrequency;

    @FXML
    private Label regularPayment;

    @FXML
    private DecimalTextField preferredPayment;

    @FXML
    private Button scheduleButton;

    @FXML
    private Button pdfButton;

    @FXML
    private JFXTextField extraMaterial;
    
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        RequiredFieldValidator validator = new RequiredFieldValidator();
        validator.setMessage("Input Required");
        extraMaterial.getValidators().add(validator);
        extraMaterial.focusedProperty().addListener((o, oldVal, newVal) -> {
            if (!newVal) {
                extraMaterial.validate();
            }
        });


        loanAmount.setText("100000");
        
        interestRate.setPattern(DecimalTextField.INTEREST_PATTERN);
        interestRate.setText("7");
        
        List<TimePeriod> compoundingPeriods = Arrays.stream(TimePeriod.values())
                .filter(tp -> tp.isCompoundingPeriod())
                .sorted()
                .collect(Collectors.toList())
                ;        
        compoundingPeriod.getItems().addAll(compoundingPeriods);
        compoundingPeriod.getSelectionModel().select(TimePeriod.SemiAnnually);
     
        
        List<TimePeriod> paymentPeriods = Arrays.stream(TimePeriod.values())
                .sorted()
                .collect(Collectors.toList())
                ;        
        paymentFrequency.getItems().addAll(paymentPeriods);
        paymentFrequency.getSelectionModel().select(TimePeriod.Monthly);
    
        LocalDate today = LocalDate.now();
        startDate.setValue(today);
        adjustmentDate.setValue(calculateAdjustmentDate(today));
        
        
        setUpNotNullValidation(loanAmount);
        setUpNotNullValidation(interestRate);
        
        setUpTermValidation(amYearsSpinner, amMonthsSpinner);
        setUpTermValidation(termYearsSpinner, termMonthsSpinner);
     
        setUpComboChangeListener(compoundingPeriod);
        setUpComboChangeListener(paymentFrequency);

        triggerFormStateChange(loanAmount.getId(), false);
        
    }
    
    
    private void setUpNotNullValidation(final TextField tf) {
        tf.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            notNullValidationCheck(tf);
        });
    }

    private void notNullValidationCheck(TextField tf) {
        boolean isEmptyField = tf.getText().trim().length() == 0;
        tf.pseudoClassStateChanged(errorClass, isEmptyField);
        triggerFormStateChange(tf.getId(), isEmptyField);
    }
    
    
    private void setUpTermValidation(Spinner<Integer> yearsSpinner, Spinner<Integer> monthsSpinner) {
        yearsSpinner.valueProperty().addListener((ObservableValue<? extends Integer> observable, Integer oldValue, Integer newValue) -> {
            periodExistsValidationCheck(yearsSpinner, monthsSpinner);
        });
        monthsSpinner.valueProperty().addListener((ObservableValue<? extends Integer> observable, Integer oldValue, Integer newValue) -> {
            periodExistsValidationCheck(yearsSpinner, monthsSpinner);
        });
    }
    
    private void periodExistsValidationCheck(Spinner<Integer> yearsSpinner, Spinner<Integer> monthsSpinner) {
        boolean isEmpty = yearsSpinner.getValue().equals(0) && monthsSpinner.getValue().equals(0);
        yearsSpinner.pseudoClassStateChanged(errorClass, isEmpty);
        monthsSpinner.pseudoClassStateChanged(errorClass, isEmpty);
        triggerFormStateChange(yearsSpinner.getId(), isEmpty);
    }
    
    private void setUpComboChangeListener(ComboBox<TimePeriod> comboBox) {
        comboBox.valueProperty().addListener((ObservableValue<? extends TimePeriod> observable, TimePeriod oldValue, TimePeriod newValue) -> {
            triggerFormStateChange(comboBox.getId(), false);
        });
    }

    private void triggerFormStateChange(String componentId, boolean componentHasError) {
        
        validationFailures.put(componentId, componentHasError);
        
        boolean formHasError = validationFailures.entrySet().stream()
                .filter(e -> e.getValue())
                .findAny()
                .isPresent()
                ;
        
        scheduleButton.setDisable(formHasError);
        pdfButton.setDisable(formHasError);
        
        if (!formHasError) {
            MonetaryAmount periodicPayment = AmortizationCalculator.getPeriodicPayment(getAmAttributes());
            regularPayment.setText(periodicPayment.toString().split(" ")[1]);
        } else {
            regularPayment.setText("");
        }
        
    }

    
    
    @FXML
    private void amortizationStateChanged() {
        boolean disabled = !amortized.isSelected();
        amYearsSpinner.setDisable(disabled);
        amMonthsSpinner.setDisable(disabled);
        compoundingPeriod.setDisable(disabled);
        triggerFormStateChange(amortized.getId(), false);
    }
    
    
    private LocalDate calculateAdjustmentDate(LocalDate fromDate) {
        
        int year = fromDate.getYear();
        int month = fromDate.getMonthValue();
        var dayOfMonth = fromDate.getDayOfMonth();

        if (dayOfMonth > 15) {
            if (++month > 12) {
                year++;
                month = 1;
            }
            dayOfMonth = 1;
        } else if (dayOfMonth > 1 && dayOfMonth < 15) {
            dayOfMonth = 15;
        }

        return LocalDate.of(year, month, dayOfMonth);
        
    }

    
    private AmortizationAttributes getAmAttributes() {
        
        BigDecimal amount = new BigDecimal(loanAmount.getText());
        Money moneyAmount = Money.of(amount, "CAD");
        
        AmortizationAttributes attr = new AmortizationAttributes();
        attr.setLoanAmount(moneyAmount);
        //attr.setRegularPayment();
        attr.setStartDate(startDate.getValue());
        attr.setAdjustmentDate(adjustmentDate.getValue());
        attr.setTermInMonths(termYearsSpinner.getValue() * 12 + termMonthsSpinner.getValue());
        attr.setInterestOnly(!amortized.isSelected());
        attr.setAmortizationPeriodInMonths(amYearsSpinner.getValue() * 12 + amMonthsSpinner.getValue());
        attr.setCompoundingPeriodsPerYear(compoundingPeriod.getValue().getPeriodsPerYear());
        attr.setPaymentFrequency(paymentFrequency.getValue().getPeriodsPerYear());
        attr.setInterestRateAsPercent(Double.valueOf(interestRate.getText()));

        return attr;
    }

}