module space.redoak.amfx {

    opens space.redoak.amfx to javafx.fxml;

    exports space.redoak.amfx;

    requires javafx.controls;
    requires javafx.fxml;
    requires java.base;
    requires java.money;
    requires org.javamoney.moneta;
    requires com.jfoenix;

    requires static lombok;

}
