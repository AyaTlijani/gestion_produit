package mains;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;

public class MainFX extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            //  Parent root = FXMLLoader.load(getClass().getResource("/productList.fxml")); // Load user view
         Parent root = FXMLLoader.load(getClass().getResource("/userProduct.fxml")); // Load user view


            Scene scene = new Scene(root);
            primaryStage.setTitle("ArtisanConnect - Boutique");
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            System.err.println("Failed to load userProduct.fxml: " + e.getMessage());
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Impossible de charger la page boutique. Vérifiez les fichiers FXML et les images.");
            alert.showAndWait();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}