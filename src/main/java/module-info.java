module com.example.repasoexamen2023javafx {
    requires javafx.controls;
    requires javafx.fxml;
    requires static lombok;
    requires java.sql;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;

    opens com.example.repasoexamen2023javafx to javafx.fxml;
    exports com.example.repasoexamen2023javafx;
}