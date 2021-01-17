package space.redoak.amfx;

import javafx.application.Application;
import javafx.application.HostServices;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;


/**
 * JavaFX App
 */
@SpringBootApplication
@ComponentScan(basePackages = {"space.redoak.amfx", "space.redoak.finance", "com.redoak.util"})
public class App extends Application {

    private static String[] savedArgs;
    private static ConfigurableApplicationContext context;
    
    private static Scene scene;
    public static HostServices hostServices;

    
    public static Object createControllerForType(Class type) {
        return context.getBean(type);
    }
      
    
    public static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        fxmlLoader.setControllerFactory(type -> createControllerForType(type));
        return fxmlLoader.load();
    }

    
    public static void main(String[] args) {
        savedArgs=args;
        launch();
    }



    // ---------------------
    
    @Override
    public void init() throws Exception {
        this.context = SpringApplication.run(App.class, savedArgs);
    }
    
    @Override
    public void stop() throws Exception {
        context.close();
        System.gc();
        System.runFinalization();
    }    


    @Override
    public void start(Stage stage) throws IOException {
        scene = new Scene(loadFXML("app"), 1200, 600);
        scene.getStylesheets().add(getClass().getResource("amfx.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("Asset Manager");
        stage.show();
        hostServices = getHostServices();
    }





}