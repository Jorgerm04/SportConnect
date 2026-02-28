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

    // Abrir modelos a Hibernate
    opens org.example.sportconnect.models to org.hibernate.orm.core;
    // Abrir controladores a JavaFX
    opens org.example.sportconnect.controllers to javafx.fxml;
    opens org.example.sportconnect.components to javafx.fxml;
    // Si tienes MainApp en la raíz:
    opens org.example.sportconnect to javafx.fxml;

    exports org.example.sportconnect;
    exports org.example.sportconnect.controllers;
}