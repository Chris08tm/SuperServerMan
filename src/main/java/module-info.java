module com.ssm.superserverman {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.ssm.superserverman to javafx.fxml;
    opens com.ssm.superserverman.controller to javafx.fxml;

    exports com.ssm.superserverman;
    exports com.ssm.superserverman.controller;

}