package com.company.gui;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class HelloFX extends Application {

    @Override
    public void start(Stage stage) {
        Label label = new Label("✅ JavaFX is working!");
        Scene scene = new Scene(label, 400, 200);
        stage.setScene(scene);
        stage.setTitle("ESD Framework GUI Test");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
