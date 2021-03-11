package space.redoak.util;

import java.util.concurrent.FutureTask;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

/**
 * A TextFlow decorator to capture "console" like output.
 * 
 * @author glenn
 */
public class TextFlowConsole {

    private final TextFlow textFlow;
   
    public TextFlowConsole(TextFlow textFlow) {
        this.textFlow = textFlow;
    }


    public void clear() {
        textFlow.getChildren().clear();
    }


    public void println(String part, Color color) {
        print(part + "\n", color);
    }
    
    public void print(String part, Color color) {
        Text text = asText(part);
        text.setFill(color);
        publish(text);        
    }
    
    
    public void println(String line) {        
        print(line + "\n");
    }

    public void print(String line) {
        publish(asText(line));        
    }

    
    private Text asText(String in) {
        Text text = new Text(in);
        text.setFont(new Font(15));
        return text;
    }
    
    
    private void publish(Text text) {
        FutureTask<Void> updateUITask = new FutureTask(
            () -> { 
                    ObservableList list = textFlow.getChildren(); 
                    list.add(text);
            }, 
            null
        );

        Platform.runLater(updateUITask);
        
    }

    
}
