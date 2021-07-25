package space.redoak.finance.securities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import org.apache.commons.io.FileUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import space.redoak.util.TextFlowConsole;

/**
 *
 * @author glenn
 */
@Service
@Slf4j
public class QuoteLoaderEodScraperService {

    @Value("${eod.dataOutputDir}")
    private String dataOutputDir;
    
    @Value("${eod.columnDelimiter}")
    private String columnDelimiter;
    
    @Value("${eod.webReadThrottleMs}")
    private int webReadThrottleMs;
    
    @Value("${eod.homeUrl}")
    private String homeUrl;
    
    @Value("${eod.baseUrl}")
    private String baseUrl;
    
    @Autowired QuoteLoaderDaoService quoteLoaderDaoService;
    

    public LocalDate getDate(SecurityExchange exchange) throws IOException {

        DateTimeFormatter eodStyleDateFormatter = DateTimeFormatter.ofPattern("MM/dd/yy");
        
        Document doc = Jsoup.connect(homeUrl).get();
        Element quoteTable = doc.select("table.quotes").first();
        Elements rows = quoteTable.select("tr");

        for (int i = 1; i < rows.size(); i++) { //first row is the col names so skip it.

            Element row = rows.get(i);
            Elements cols = row.select("td");

            if (cols.get(1).text().equals(exchange.getExchangeName())) {
                String dateString = cols.get(2).text();
                LocalDate localDate = LocalDate.parse(dateString, eodStyleDateFormatter);
                return localDate;
            }
        }

        throw new IOException(String.format("Failed to retrieve data for exchange %s from %s", exchange, homeUrl));
    }
    
    
    private String getTargetFileName(SecurityExchange exchange, String eodDate) throws IOException {
        return dataOutputDir + "/" + exchange.name() + "_" + eodDate + ".csv";
    }
    

    public void extractEodForExchange(final SecurityExchange exchange, TextFlowConsole textFlowConsole, Runnable completionCallback)
            throws IOException, InterruptedException, SQLException {
        
        String exchangeQuoteBaseUrl = baseUrl + "/" + exchange.name() + "/";        
        LocalDate eodDate = getDate(exchange);
        String fileName = getTargetFileName(exchange, eodDate.toString());

        textFlowConsole.println(String.format(
                "Exchange: %s\nDate: %s\nBase url: %s\nOutput file name: %s\n",
                exchange.name(),
                eodDate.toString(),
                exchangeQuoteBaseUrl,
                fileName
        ));
        
        log.info(String.format("Staging file for EOD quotes: %s", fileName));
        
        File outputFile = new File(fileName);

        extractToFile(exchangeQuoteBaseUrl, outputFile, textFlowConsole);
        
        zipFile(outputFile, textFlowConsole);
        
        quoteLoaderDaoService.loadH2Db(outputFile.getPath(), eodDate.toString(), textFlowConsole);

        outputFile.delete();
        textFlowConsole.println("Removed: " + fileName);
        textFlowConsole.println("\n\nDone!");

        completionCallback.run();
        
    }

    
    private void extractToFile(String exchangeQuoteBaseUrl, File outputFile, TextFlowConsole textFlowConsole)
            throws IOException, InterruptedException {

        char firstLetter = 'A';
        char lastLetter = 'Z';
        
        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(FileUtils.openOutputStream(outputFile), StandardCharsets.UTF_8))) {

            for (char companyStartLetter = firstLetter; companyStartLetter <= lastLetter; companyStartLetter++) {
                
                StringBuilder sb = new StringBuilder();

                Document doc = Jsoup.connect(exchangeQuoteBaseUrl + companyStartLetter + ".htm").get();

                Element quoteTable = doc.select("table.quotes").first();
                Elements rows = quoteTable.select("tr");

//                rows.stream()
//                        .skip(1)  //first row contains column names
//                        .map(r -> generateCsvLineFromRow(r))
//                        .forEach(line -> {
//                           pw.println(line);
//                           sb.append(line).append("\n");
//                        })
//                        ;
                
                for (int rowIndex = 1; rowIndex < rows.size(); rowIndex++) { //first row is the col names so skip it.
                    String csvLine = generateCsvLineFromRow(rows.get(rowIndex));
                    pw.println(csvLine);
                    sb.append(csvLine).append("\n");
                }

                textFlowConsole.println(sb.toString());
                
                if (companyStartLetter != lastLetter) {
                    Thread.sleep(webReadThrottleMs);
                }

            }

        }
        
    }


    private String generateCsvLineFromRow(Element quoteRow) {
        Elements cols = quoteRow.select("td");
        String symbol = cols.get(0).text();
        String name = cols.get(1).text();
        String close = cols.get(4).text();
        String volume = cols.get(5).text();
        return symbol + columnDelimiter + name + columnDelimiter + close + columnDelimiter + volume;
    }
    
    
    private void zipFile(File fileToZip, TextFlowConsole textFlowConsole) throws FileNotFoundException, IOException {

        String srcFileName = fileToZip.getName();
        String zipFileName = fileToZip.getPath() + ".zip";
        
        try(
                FileOutputStream fos = new FileOutputStream(zipFileName);
                ZipOutputStream zipOut = new ZipOutputStream(fos);
	        FileInputStream fis = new FileInputStream(fileToZip);
                
            ) {
            
	        ZipEntry zipEntry = new ZipEntry(srcFileName);
	        zipOut.putNextEntry(zipEntry);
	        byte[] bytes = new byte[1024];
	        int length;
	        while((length = fis.read(bytes)) >= 0) {
	            zipOut.write(bytes, 0, length);
	        }
                
                textFlowConsole.println(String.format("Zipped %s to %s\n", srcFileName, zipFileName));

        }
        
    }
    
    
    
    
}
