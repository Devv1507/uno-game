module univalle.tedesoft.uno {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.junit.jupiter.api;


    opens univalle.tedesoft.uno to javafx.fxml;
    opens univalle.tedesoft.uno.controller to javafx.fxml;

    exports univalle.tedesoft.uno;
}