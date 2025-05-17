module univalle.tedesoft.uno {
    requires javafx.controls;
    requires javafx.fxml;

    opens univalle.tedesoft.uno to javafx.fxml;
    opens univalle.tedesoft.uno.controller to javafx.fxml;

    exports univalle.tedesoft.uno;
}