module com.example.progettoprog3latoserver {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.progettoprog3latoserver to javafx.fxml;
    exports com.example.progettoprog3latoserver;
}