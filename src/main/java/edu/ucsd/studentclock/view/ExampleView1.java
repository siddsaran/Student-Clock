package edu.ucsd.studentclock.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public class ExampleView1 extends BorderPane {
    private Button switchButton;

    public ExampleView1() {
        VBox headerBox = new VBox(10);
        headerBox.setAlignment(Pos.CENTER);
        headerBox.setPadding(new Insets(20));

        switchButton = new Button("Switch Views");
        headerBox.getChildren().addAll(switchButton);
    }

    public Button getSwitchButton() {
        return switchButton;
    }
}
