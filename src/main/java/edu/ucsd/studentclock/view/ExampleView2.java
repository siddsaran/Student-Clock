package edu.ucsd.studentclock.view;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

public class ExampleView2 extends VBox {
    private Button backButton = new Button("Switch back");

    public ExampleView2() {
        this.setAlignment(Pos.CENTER);
        this.setSpacing(20);
        this.setStyle("-fx-background-color: #FFFFFF;");

        this.getChildren().addAll(backButton);
    }

    public Button getBackButton() {
        return backButton;
    }
}