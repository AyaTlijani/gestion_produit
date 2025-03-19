package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import models.Produit;
import services.ServiceProd;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.List;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import javafx.scene.Parent;


public class UserProductController {

    @FXML
    private GridPane productGrid;

    private ServiceProd serviceProd = new ServiceProd();

    @FXML
    public void initialize() {
        loadProducts();
    }

    private void loadProducts() {
        try {
            List<Produit> products = serviceProd.afficher();
            if (products.isEmpty()) {
                Label noProductsLabel = new Label("Aucun produit disponible.");
                noProductsLabel.getStyleClass().add("no-products-label");
                productGrid.add(noProductsLabel, 0, 0);
                return;
            }

            int column = 0;
            int row = 0;
            for (Produit product : products) {
                System.out.println("Loading product: " + product.getNom() + ", Image: " + product.getImage());
                VBox card = createProductCard(product);
                if (card != null) {
                    productGrid.add(card, column, row);
                    column++;
                    if (column == 3) { // 3 cards per row
                        column = 0;
                        row++;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Failed to load products: " + e.getMessage());
            e.printStackTrace();
            Label errorLabel = new Label("Erreur lors du chargement des produits.");
            errorLabel.getStyleClass().add("error-label");
            productGrid.add(errorLabel, 0, 0);
        }
    }

    private VBox createProductCard(Produit product) {
        VBox card = new VBox(10);
        card.getStyleClass().add("product-card");
        card.setPadding(new Insets(10));
        card.setPrefWidth(250);
        card.setPrefHeight(300);

        // Product Image (only if available)
        String imagePath = product.getImage();
        if (imagePath != null) {
            ImageView imageView = new ImageView();
            imageView.setFitWidth(230);
            imageView.setFitHeight(200);
            imageView.setPreserveRatio(true);
            try {
                Image image;
                if (getClass().getResource(imagePath) != null) {
                    System.out.println("Loading image as resource: " + imagePath);
                    image = new Image(getClass().getResource(imagePath).toString());
                } else {
                    System.out.println("Loading image as file: " + imagePath);
                    Path filePath = Paths.get("src/main/resources" + imagePath);
                    if (Files.exists(filePath)) {
                        image = new Image(filePath.toUri().toString());
                    } else {
                        System.out.println("Image not found for " + imagePath + ", skipping image");
                        image = null;
                    }
                }
                if (image != null) {
                    imageView.setImage(image);
                    card.getChildren().add(imageView);
                }
            } catch (Exception e) {
                System.err.println("Failed to load image for product " + product.getNom() + ": " + e.getMessage());
            }
        } else {
            System.out.println("No image path for product " + product.getNom() + ", skipping image");
        }

        // Product Name
        Label nameLabel = new Label(product.getNom() != null ? product.getNom() : "Sans nom");
        nameLabel.getStyleClass().add("product-name");

        // Product Price
        Label priceLabel = new Label(String.format("%.2f TND", product.getPrix()));
        priceLabel.getStyleClass().add("product-price");

        card.getChildren().addAll(nameLabel, priceLabel);

        // Make card clickable to show details
        card.setOnMouseClicked(event -> showProductDetails(product));

        return card;
    }

    private void showProductDetails(Produit product) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/productDetails.fxml"));
            VBox root = loader.load();
            ProductDetailsController controller = loader.getController();
            controller.setProduct(product);
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Détails du Produit - " + (product.getNom() != null ? product.getNom() : "Sans nom"));
            stage.show();
        } catch (IOException e) {
            System.err.println("Failed to load productDetails.fxml: " + e.getMessage());
            e.printStackTrace();
        }
    }
    @FXML
    public void viewCart() {
        try {
            System.out.println("View Cart button clicked!");
            // Load the ViewCart.fxml file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ViewCart.fxml"));
            Parent root = loader.load();

            // Create a new scene for the cart view
            Scene cartScene = new Scene(root);

            // Create a new stage (window) for the cart view
            Stage cartStage = new Stage();
            cartStage.setTitle("Your Cart");
            cartStage.setScene(cartScene);
            cartStage.show();

            System.out.println("Cart window opened successfully!");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error loading ViewCart.fxml");
        }
    }








}



