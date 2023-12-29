module com.example.demo25 {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens com.example.demo25 to javafx.fxml;
    exports com.example.demo25;
}