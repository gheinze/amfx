package space.redoak;

import javafx.application.Application;
import javafx.application.HostServices;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import javafx.scene.layout.StackPane;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.transaction.annotation.EnableTransactionManagement;


/**
 * JavaFX App
 */
@SpringBootApplication
@EnableTransactionManagement

public class App extends Application {

    private static final String RESOURCE_ROOT = "amfx";
    
    private static String[] savedArgs;
    private static ConfigurableApplicationContext context;
    
    private static Scene scene;
    public static HostServices hostServices;

    public static StackPane appStackPane;
    
    public static Object createControllerForType(Class type) {
        return context.getBean(type);
    }
      
    
    public static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(String.format("%s/%s.fxml", RESOURCE_ROOT, fxml)));
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
        appStackPane = (StackPane)loadFXML("app");
        scene = new Scene(appStackPane, 1200, 600);
        scene.getStylesheets().add(getClass().getResource(RESOURCE_ROOT + "/amfx.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("Asset Manager");
        stage.show();
        hostServices = getHostServices();
    }





}