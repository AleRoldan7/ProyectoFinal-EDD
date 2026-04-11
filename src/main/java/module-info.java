module cunoc.tutoria.ipc1.proyectofinaledd {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;

    opens cunoc.tutoria.ipc1.proyectofinaledd to javafx.fxml;
    exports cunoc.tutoria.ipc1.proyectofinaledd;
}