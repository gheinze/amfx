package space.redoak.finance.loan;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.money.MonetaryAmount;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import space.redoak.util.JasperReportRegistry;

/**
 *
 * @author glenn
 */
public class AmortizationReportService {

    private static final String AMORTIZATION_SCHEDULE_REPORT = "/space/redoak/finance/loan/AmortizationSchedule.jasper";
    private static final int MONTHS_PER_YEAR = 12;


public File generatePdfSchedule(final AmortizationAttributes amAttrs, String preparedFor, String preparedBy) throws JRException, IOException {

        List<ScheduledPayment> payments = AmortizationCalculator.generateSchedule(amAttrs);

        // TODO: name, title, etc should be configurable parameters as well        
        Map<String, Object> customParameters = new HashMap<>();
        customParameters.put("amount", amAttrs.getLoanAmount());
        customParameters.put("rate", amAttrs.getInterestRateAsPercent());

        MonetaryAmount requestedMonthlyPayment = amAttrs.getRegularPayment();
        MonetaryAmount periodicPayment = (null == requestedMonthlyPayment) ? AmortizationCalculator.getPeriodicPayment(amAttrs) : requestedMonthlyPayment;
        customParameters.put("monthlyPayment", periodicPayment);

        customParameters.put("term", amAttrs.getTermInMonths());
        if (!amAttrs.isInterestOnly()) {
            customParameters.put("amortizationYears", amAttrs.getAmortizationPeriodInMonths() / MONTHS_PER_YEAR);
            customParameters.put("amortizationMonths", amAttrs.getAmortizationPeriodInMonths() % MONTHS_PER_YEAR);
            customParameters.put("compoundPeriod",
                    TimePeriod.getTimePeriodWithPeriodCountOf(amAttrs.getCompoundingPeriodsPerYear()).getDisplayName()
            );
        }
        customParameters.put("mortgagee", preparedBy);
        customParameters.put("mortgagor", preparedFor);

        return writePdfScheduleToFile(AMORTIZATION_SCHEDULE_REPORT, payments, customParameters);

    }

    
    private File writePdfScheduleToFile(
            final String reportPath,
            final List dataList,
            Map<String, Object> customParameters
    ) throws JRException, IOException {

        JRBeanCollectionDataSource ds = new JRBeanCollectionDataSource(dataList);
        JasperReport compiledReport = JasperReportRegistry.getCompiledReport(reportPath);
        JasperPrint jasperPrint = JasperFillManager.fillReport(compiledReport, customParameters, ds);
//        JasperExportManager.exportReportToPdfStream(jasperPrint, outputStream);

        File pdfFile = File.createTempFile("amSchedule", ".pdf");
        JasperExportManager.exportReportToPdfFile(jasperPrint, pdfFile.getCanonicalPath());
        
        return pdfFile;
        
    }
    
    
}
