module hsmw.apmcreator {

    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires java.desktop;
    requires org.fxmisc.richtext;

    opens hsmw.creator to javafx.fxml;
    exports hsmw.creator;
}