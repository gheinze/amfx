package space.redoak.finance.securities;

import java.time.format.DateTimeFormatter;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;

/**
 *
 * @author glenn
 */
public class QuoteChart extends LineChart<String, Number> {

    
    private DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE ;
    
    
    public QuoteChart(List<QuoteEntity> quotes) {
        super(new CategoryAxis(), new NumberAxis());

        setCreateSymbols(false);
        
        ObservableList<Data<String, Number>> data = FXCollections.observableArrayList() ;

        SortedList<Data<String, Number>> sortedData = new SortedList<>(
                data,
                (data1, data2) -> data1.getXValue().compareTo(data2.getXValue())
        );
        
        quotes.stream().forEach(q -> {
            String xDate = formatter.format(q.getReadDte());
            Float yClosePrice = q.getClosePrice();
            Data<String, Number> point = new Data<>(xDate, yClosePrice);
            data.add(point);
        });
        
        getData().add(new Series<>(sortedData));
        setAnimated(false);
        
    }    
    
}
