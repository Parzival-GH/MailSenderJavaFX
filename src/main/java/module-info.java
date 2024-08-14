module com.example.mailsender {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;
    requires java.mail;

    opens com.example.mailsender to javafx.fxml;
    exports com.example.mailsender;
}