package com.medialab.multimedia;


import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class StartApplication extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(StartApplication.class.getResource("FirstPage.fxml"));
        Scene scene2 = new Scene(fxmlLoader.load(), 320, 320);
        stage.setTitle("MediaLab MineSweeper");
        stage.setScene(scene2);
        stage.show();

    }

    public static void main(String[] args) {
        launch(args);
    }
}