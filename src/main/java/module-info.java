module org.example.sportconnect {
    requires javafx.controls;
    requires javafx.fxml;
    requires jakarta.persistence;
    requires org.hibernate.orm.core;
    requires java.naming;
    requires java.sql;
    requires jbcrypt;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.fontawesome5;

    opens org.example.sportconnect.models to org.hibernate.orm.core;

    opens org.example.sportconnect.controllers to javafx.fxml;

    opens org.example.sportconnect to javafx.fxml;

    exports org.example.sportconnect;
    exports org.example.sportconnect.controllers;
}