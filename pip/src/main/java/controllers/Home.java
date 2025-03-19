package controllers;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Home extends Application {

    @Override
    public void start(Stage stage) {
        try {
            System.out.println(getClass().getResource("/productList.fxml"));
            System.out.println(getClass().getResource("/adminProduct.fxml"));
            System.out.println(getClass().getResource("/style.css"));

            // Start with the product list view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/productList.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            stage.setScene(scene);

            // Apply CSS
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

            stage.setTitle("Gestion des Produits");
            stage.show();
        } catch (IOException e) {
            System.err.println("Erreur de chargement du fichier FXML : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}