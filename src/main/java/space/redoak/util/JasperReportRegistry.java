package space.redoak.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;

/**
 * Retrieve a compiled Jasper Report by name.
 *
 * @author gheinze
 */
public class JasperReportRegistry {


    private static final Map<String, JasperReport> reportCache = new HashMap<>();


    public static JasperReport getCompiledReport(String jasperReportClassPath) throws IOException, JRException {
        JasperReport report = reportCache.get(jasperReportClassPath);
        return null == report ? loadReport(jasperReportClassPath) : report;
    }

    private static JasperReport loadReport(String jasperReportClassPath) throws IOException, JRException {
        URL resource = JasperReportRegistry.class.getResource(jasperReportClassPath);
        try (InputStream resourceInputStream = resource.openStream()) {
            JasperReport report = (JasperReport) JRLoader.loadObject(resourceInputStream);
            reportCache.put(jasperReportClassPath, report);
            return report;
        }
    }

}

