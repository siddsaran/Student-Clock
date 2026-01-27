module edu.ucsd.studentclock {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens edu.ucsd.studentclock to javafx.fxml;
    exports edu.ucsd.studentclock;
}
