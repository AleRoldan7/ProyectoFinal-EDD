module cunoc.proyectofinaledd {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;
    requires com.opencsv;
    requires java.desktop;
    requires javafx.swing;

    opens cunoc.proyectofinaledd to javafx.fxml;
    exports cunoc.proyectofinaledd;
    exports ui.estructuras_view;
    exports ui.view;
}