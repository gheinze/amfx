package space.redoak.amfx;

import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.JFXToggleButton;
import com.jfoenix.validation.RequiredFieldValidator;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.BooleanSupplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javax.money.MonetaryAmount;
import javax.money.format.AmountFormatQueryBuilder;
import javax.money.format.MonetaryAmountFormat;
import javax.money.format.MonetaryFormats;
import net.sf.jasperreports.engine.JRException;
import org.javamoney.moneta.Money;
import org.javamoney.moneta.format.CurrencyStyle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import space.redoak.App;
import com.accounted4.finance.loan.AmortizationAttributes;
import com.accounted4.finance.loan.AmortizationCalculator;
import com.accounted4.finance.loan.ScheduledPayment;
import com.accounted4.finance.loan.TimePeriod;
import javafx.scene.control.Button;
import space.redoak.finance.loan.AmortizationReportService;
import space.redoak.util.TableCutAndPaste;


@Component
@Scope("prototype")
public class LoanTermsController  {

    @FXML
    private JFXTextField amountJfxTextField;

    @FXML
    private JFXTextField rateJfxTextField;

    @FXML
    private JFXToggleButton amortizedJfxToggleButton;

    @FXML
    private JFXTextField amYearsJfxTextField;

    @FXML
    private JFXTextField amMonthsJfxTextField;

    @FXML
    private JFXComboBox<TimePeriod> compoundingPeriodJfxComboBox;

    @FXML
    private JFXComboBox<TimePeriod> paymentFrequencyJfxComboBox;

    @FXML
    private JFXTextField paymentJfxTextField;

    @FXML
    private JFXTextField perDiemTextField;
    
    @FXML
    private JFXTextField preparedForJfxTextField;

    @FXML
    private JFXTextField preparedByJfxTextField;

    @FXML
    private JFXTextField termYearsJfxTextField;

    @FXML
    private JFXTextField termMonthsJfxTextField;

    @FXML
    private JFXDatePicker startDateJfxDatePicker;

    @FXML
    private JFXDatePicker adjustmentDateJfxDatePicker;

    @FXML
    private JFXTextField paymentOverrideJfxTextField;

    @FXML
    private Button pdfButton;

    @FXML
    private TableView<RowData> scheduleTable;
    
    @FXML
    private TableColumn<RowData, Integer> paymentColumn;

    @FXML
    private TableColumn<RowData, String> dateColumn;

    @FXML
    private TableColumn<RowData, String> interestColumn;

    @FXML
    private TableColumn<RowData, String> principalColumn;

    @FXML
    private TableColumn<RowData, String> balanceColumn;


    
    private final List<BooleanSupplier> fieldValidators = new ArrayList<>();

    private final RequiredFieldValidator foenixRequiredFieldValidator = new RequiredFieldValidator();
    
    private static final MonetaryAmountFormat currencyFormatter = MonetaryFormats.getAmountFormat(
            AmountFormatQueryBuilder
                    .of(Locale.CANADA)
                    .set(CurrencyStyle.SYMBOL)
                    .build()
    );


    @Autowired AmortizationReportService amReportService;
    
    
    @FXML
    public void initialize() {
        
        foenixRequiredFieldValidator.setMessage("required");
    
        prepareNumericField(amountJfxTextField, 100000, Formats.Money.getPattern());
        prepareNumericField(rateJfxTextField, 7, Formats.Interest.getPattern());
        
        setNumericChangeListener(paymentOverrideJfxTextField, Formats.Money.getPattern(), -1, false);
        paymentOverrideJfxTextField.focusedProperty().addListener((o) -> {
            fireFormStateChange();
        });
        
        prepareYearMonthInterval(amYearsJfxTextField, amMonthsJfxTextField, 20, 0);
        prepareCompoundingPeriod();
        
        prepareYearMonthInterval(termYearsJfxTextField, termMonthsJfxTextField, 1, 0);
        
        preparePaymentPeriod();
        prepareStartDates();
        
        startDateJfxDatePicker.valueProperty().addListener(
                (var observable, var oldValue, var newValue) -> {
                    adjustmentDateJfxDatePicker.setValue(
                            calculateAdjustmentDate(startDateJfxDatePicker.getValue())
                    );
                    fireFormStateChange();
                });
        
        amortizedJfxToggleButton.setSelected(true);
        
        prepareScheduleTable();
        
        fireFormStateChange();

    }


    @FXML
    private void amortizationStateChanged() {
        boolean disabled = !amortizedJfxToggleButton.isSelected();
        amYearsJfxTextField.setDisable(disabled);
        amMonthsJfxTextField.setDisable(disabled);
        compoundingPeriodJfxComboBox.setDisable(disabled);
        paymentOverrideJfxTextField.setDisable(disabled);
        fireFormStateChange();
    }

    
    private void updateScheduleTable(AmortizationAttributes amAttributes) {
        List<ScheduledPayment> schedule = AmortizationCalculator.generateSchedule(amAttributes);
        List<RowData> observableCollection = schedule.stream()
                .map(s -> new RowData(s))
                .collect(Collectors.toList())
                ;
        scheduleTable.setItems(FXCollections.observableArrayList(observableCollection));        
        scheduleTable.setVisible(true);
    }

    
    @FXML
    private void pdfButtonClicked() throws JRException, IOException {
        String preparedFor = preparedForJfxTextField.getText().isBlank() ? "Accounted4" : preparedForJfxTextField.getText();
        String preparedBy = preparedByJfxTextField.getText().isBlank() ? "Accounted4" : preparedByJfxTextField.getText();
        File file = amReportService.generatePdfSchedule(getAmAttributes(), preparedFor, preparedBy);
        App.hostServices.showDocument(file.toURI().toString());
        
    }
    
    
    private void prepareNumericField(JFXTextField field, int defaultValue, Pattern numberPattern) {
        
        field.setText(String.valueOf(defaultValue));
        
        // key press filter
        setNumericChangeListener(field, numberPattern);
        
        // ui error presenter
        field.getValidators().add(foenixRequiredFieldValidator);
        field.focusedProperty().addListener((o,oldVal, newVal)->{
            if(!newVal) { field.validate(); }
        });
        
        // register a check to see if this field is valid
        fieldValidators.add(() -> { return !field.getText().isBlank(); });
    }
        
    
    private void prepareCompoundingPeriod() {
        
        List<TimePeriod> compoundingPeriods = Arrays.stream(TimePeriod.values())
                .filter(tp -> tp.isCompoundingPeriod())
                .sorted()
                .collect(Collectors.toList())
                ;        
        compoundingPeriodJfxComboBox.getItems().addAll(compoundingPeriods);
        compoundingPeriodJfxComboBox.getSelectionModel().select(TimePeriod.SemiAnnually);
        setUpComboChangeListener(compoundingPeriodJfxComboBox);

    }
    
    
    private void preparePaymentPeriod() {
        
        List<TimePeriod> paymentPeriods = Arrays.stream(TimePeriod.values())
                .sorted()
                .collect(Collectors.toList())
                ;        
        paymentFrequencyJfxComboBox.getItems().addAll(paymentPeriods);
        paymentFrequencyJfxComboBox.getSelectionModel().select(TimePeriod.Monthly);
        setUpComboChangeListener(paymentFrequencyJfxComboBox);

    }
    
    
    private void prepareYearMonthInterval(
            JFXTextField yearControl,
            JFXTextField monthControl,
            int defaultYears,
            int defaultMonths
    ) {
        
        // Default values
        yearControl.setText(Integer.toString(defaultYears));
        monthControl.setText(Integer.toString(defaultMonths));
        
        // Restrict key press to numeric
        setNumericChangeListener(yearControl, Formats.DoubleDigitInteger.getPattern(), 35, true);
        setNumericChangeListener(monthControl, Formats.DoubleDigitInteger.getPattern(), 12, true);

        // ui error presentation on focus lost
        FoenixTermValidator termValidator = new FoenixTermValidator(yearControl, monthControl);
        yearControl.getValidators().add(termValidator);
        monthControl.getValidators().add(termValidator);

        yearControl.focusedProperty().addListener((o,oldVal, newVal)->{
            if(!newVal) { yearControl.validate(); monthControl.validate(); }
        });
        
        monthControl.focusedProperty().addListener((o,oldVal, newVal)->{
            if(!newVal) { yearControl.validate(); monthControl.validate(); }
        });


        // register a form validation check on the pair
        fieldValidators.add(() -> { 
    
            if (yearControl.getId().equals(amYearsJfxTextField.getId()) && !amortizedJfxToggleButton.isSelected()) {
                return true;
            }
            
            boolean valid = false;

            try {
                int years = Integer.parseInt(yearControl.getText());
                int months = Integer.parseInt(monthControl.getText());
                valid = ((years + months) > 0);
            } catch (NumberFormatException nfe) {
            }
            
            return valid;
            
        });
        
    }
    
    
    private void prepareStartDates() {
        LocalDate today = LocalDate.now();
        startDateJfxDatePicker.setValue(today);
        adjustmentDateJfxDatePicker.setValue(calculateAdjustmentDate(today));        
    }
    
    
    private void prepareScheduleTable() {
        
        paymentColumn.setCellValueFactory(param -> param.getValue().payment);
        dateColumn.setCellValueFactory(param -> param.getValue().date);
        interestColumn.setCellValueFactory(param -> param.getValue().interest);
        interestColumn.setStyle( "-fx-alignment: CENTER-RIGHT;");
        principalColumn.setCellValueFactory(param -> param.getValue().principal);
        principalColumn.setStyle( "-fx-alignment: CENTER-RIGHT;");
        balanceColumn.setCellValueFactory(param -> param.getValue().balance);
        balanceColumn.setStyle( "-fx-alignment: CENTER-RIGHT;");
        
        scheduleTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        scheduleTable.getSelectionModel().setCellSelectionEnabled(true);
        TableCutAndPaste.installCopyPasteHandler(scheduleTable);
        
    }
    
    
    private void setNumericChangeListener(JFXTextField textField, Pattern pattern) {
        setNumericChangeListener(textField, pattern, -1, true);
    }
    
    private void setNumericChangeListener(JFXTextField textField, Pattern pattern, int maxInt, boolean fireUpdate) {
        
        StringProperty textProperty = textField.textProperty();
        
        textProperty.addListener(
                (var observable, var oldValue, var newValue) -> {
                    if (newValue.isBlank()) {
                        // null handling
                        textField.setText("");
                    } else if (!pattern.matcher(newValue).matches()) {
                        // pattern matching
                        textField.setText(oldValue);
                    } else if (maxInt > 0  && Integer.parseInt(newValue) > maxInt) {
                        // integer range matching
                        textField.setText(oldValue);
                    }
                    if (fireUpdate) {
                       fireFormStateChange();
                    }
                }
        );
        
    }
    
    private void setUpComboChangeListener(ComboBox<TimePeriod> comboBox) {
        comboBox.valueProperty().addListener((ObservableValue<? extends TimePeriod> observable, TimePeriod oldValue, TimePeriod newValue) -> {
            fireFormStateChange();
        });
    }

    private void fireFormStateChange() {
        
        boolean formHasError = fieldValidators.stream()
                .filter(v -> !v.getAsBoolean())
                .findAny()
                .isPresent()
                ;
        
        pdfButton.setDisable(formHasError);
        
        if (formHasError) {
            paymentJfxTextField.setText("");
            perDiemTextField.setText("");
            paymentOverrideJfxTextField.setText("");
            scheduleTable.setVisible(false);
        } else {
            
            AmortizationAttributes amAttributes = getAmAttributes();
            
            MonetaryAmount periodicPayment = AmortizationCalculator.getPeriodicPayment(amAttributes);
            String customFormatted = currencyFormatter.format(periodicPayment);
            paymentJfxTextField.setText(customFormatted);
            
            MonetaryAmount perDiem = AmortizationCalculator.getPerDiem(amAttributes.getLoanAmount(), amAttributes.getInterestRateAsPercent());
            customFormatted = currencyFormatter.format(perDiem);
            perDiemTextField.setText(customFormatted);
            
            updateScheduleTable(amAttributes);

        }
        
    }
    
    private LocalDate calculateAdjustmentDate(LocalDate fromDate) {
        
        if (null == fromDate) {
            return null;
        }
        
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


    private Money textToCad(String text) {
        BigDecimal amount = new BigDecimal(text);
        return Money.of(amount, "CAD");        
    }
    
    
    private AmortizationAttributes getAmAttributes() {
        
        AmortizationAttributes attr = new AmortizationAttributes();

        Money loanAmount = textToCad(amountJfxTextField.getText());
        attr.setLoanAmount(loanAmount);
        
        int amYears  = Integer.parseInt(amYearsJfxTextField.getText());
        int amMonths = Integer.parseInt(amMonthsJfxTextField.getText());
        int amPeriod = amYears * 12 + amMonths;
        attr.setAmortizationPeriodInMonths(amPeriod);
        
        int termYears  = Integer.parseInt(termYearsJfxTextField.getText());
        int termMonths = Integer.parseInt(termMonthsJfxTextField.getText());
        int termPeriod = termYears * 12 + termMonths;
        attr.setTermInMonths(termPeriod);
        
        if (!paymentOverrideJfxTextField.getText().isEmpty()) {
            Money overrideMoney = textToCad(paymentOverrideJfxTextField.getText());
            attr.setRegularPayment(overrideMoney);
        } else {
            attr.setRegularPayment(null);
        }

        LocalDate startDate = startDateJfxDatePicker.getValue();
        LocalDate adjustmentDate = adjustmentDateJfxDatePicker.getValue();
        
        if (null == startDate) {
            startDate = LocalDate.now();
            adjustmentDate = calculateAdjustmentDate(startDate);
        } else if (null == adjustmentDate) {
            adjustmentDate = startDate;
        }
        attr.setStartDate(startDate);
        attr.setAdjustmentDate(adjustmentDate);

        attr.setInterestOnly(!amortizedJfxToggleButton.isSelected());
        attr.setCompoundingPeriodsPerYear(compoundingPeriodJfxComboBox.getValue().getPeriodsPerYear());
        attr.setPaymentFrequency(paymentFrequencyJfxComboBox.getValue().getPeriodsPerYear());
        attr.setInterestRateAsPercent(Double.valueOf(rateJfxTextField.getText()));

        return attr;
    }

 
    
    public static class RowData {

        private final ObservableValue<Integer> payment;
        private final SimpleStringProperty date;
        private final SimpleStringProperty interest;
        private final SimpleStringProperty principal;
        private final SimpleStringProperty balance;

        private RowData(ScheduledPayment payment) {
            this.payment = new SimpleObjectProperty<>(payment.getPaymentNumber());
            this.date = new SimpleStringProperty(payment.getPaymentDate().toString());
            this.interest = new SimpleStringProperty(currencyFormatter.format(payment.getInterest()));
            this.principal = new SimpleStringProperty(currencyFormatter.format(payment.getPrincipal()));
            this.balance = new SimpleStringProperty(currencyFormatter.format(payment.getBalance()));
        }
        
    }
    
}
