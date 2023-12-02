module com.example.progettoprog3latoserver {
    requires javafx.controls;
    requires javafx.fxml;
    requires json.simple;


    opens com.example.progettoprog3latoserver to javafx.fxml;
    exports com.example.progettoprog3latoserver;
}