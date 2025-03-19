package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import models.Produit;
import models.Panier;
import services.ServiceProd;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;

public class ProductDetailsController {

    @FXML private ImageView detailImageView;
    @FXML private Label nameLabel;
    @FXML private Label descriptionLabel;
    @FXML private Label priceLabel;
    @FXML private Label quantityLabel;
    @FXML private Label categoryLabel;
    @FXML private Label availabilityLabel;
    @FXML private Button orderButton;
    @FXML private Spinner<Integer> quantitySpinner;

    private Produit selectedProduct;
    private ServiceProd serviceProd = new ServiceProd();
    private Panier panier = Panier.getInstance();

    public void setProduct(Produit product) {
        // Fetch latest from database
        this.selectedProduct = panier.getProductById(product.getId());
        System.out.println("Loading product details for: " + (selectedProduct != null ? selectedProduct.getNom() : "null") +
                ", Stock: " + (selectedProduct != null ? selectedProduct.getQuantite() : -1));
        if (selectedProduct != null) {
            nameLabel.setText(selectedProduct.getNom() != null ? selectedProduct.getNom() : "Sans nom");
            descriptionLabel.setText(selectedProduct.getDescription() != null ? selectedProduct.getDescription() : "Aucune description");
            priceLabel.setText(String.format("%.2f TND", selectedProduct.getPrix()));
            quantityLabel.setText("Quantité : " + selectedProduct.getQuantite());
            categoryLabel.setText("Catégorie : " + (selectedProduct.getCategorie() != null ? selectedProduct.getCategorie().name() : "Inconnue"));
            availabilityLabel.setText("Disponibilité : " + (selectedProduct.isDisponibilite() ? "Oui" : "Non"));

            String imagePath = selectedProduct.getImage();
            if (imagePath != null) {
                try {
                    Image image;
                    if (getClass().getResource(imagePath) != null) {
                        image = new Image(getClass().getResource(imagePath).toString());
                    } else {
                        Path filePath = Paths.get("src/main/resources" + imagePath);
                        image = Files.exists(filePath) ? new Image(filePath.toUri().toString()) : null;
                    }
                    detailImageView.setImage(image != null ? image : null);
                } catch (Exception e) {
                    System.err.println("Failed to load image for details: " + e.getMessage());
                }
            }

            updateSpinner();
            updateOrderButtonState();
        }
    }

    @FXML
    private void placeOrder() {
        if (selectedProduct != null) {
            int quantityToAdd = quantitySpinner.getValue();
            int initialStock = selectedProduct.getQuantite();

            try {
                panier.addToCart(selectedProduct, quantityToAdd);
                // Refresh from database and log for debugging
                selectedProduct = panier.getProductById(selectedProduct.getId());
                if (selectedProduct == null) {
                    throw new SQLException("Failed to refresh product from database!");
                }
                System.out.println("After addToCart: " + selectedProduct.getNom() + ", Stock: " + selectedProduct.getQuantite() +
                        ", Disponibilité: " + selectedProduct.isDisponibilite());
                if (selectedProduct.getQuantite() > initialStock - quantityToAdd) {
                    throw new SQLException("Stock not reduced correctly by Panier!");
                }

                serviceProd.modifier(selectedProduct);

                quantityLabel.setText("Quantité : " + selectedProduct.getQuantite());
                availabilityLabel.setText("Disponibilité : " + (selectedProduct.isDisponibilite() ? "Oui" : "Non"));
                updateSpinner();
                updateOrderButtonState();

                showAlert("Succès", "Added " + quantityToAdd + " " + selectedProduct.getNom() + " to cart! New stock: " + selectedProduct.getQuantite());

            } catch (SQLException e) {
                System.err.println("Erreur de mise à jour de la quantité : " + e.getMessage());
                showAlert("Erreur", "Échec de la mise à jour de la quantité : " + e.getMessage());
            } catch (IllegalArgumentException e) {
                showAlert("Erreur", e.getMessage());
            }
        } else {
            showAlert("Erreur", "Aucun produit sélectionné.");
        }
    }

    @FXML
    private void viewCart() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/ViewCart.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Panier");
            stage.show();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible de charger le panier : " + e.getMessage());
        }
    }

    private void updateOrderButtonState() {
        orderButton.setDisable(selectedProduct == null || selectedProduct.getQuantite() <= 0);
    }

    private void updateSpinner() {
        if (selectedProduct != null) {
            int max = Math.max(1, selectedProduct.getQuantite());
            SpinnerValueFactory<Integer> valueFactory =
                    new SpinnerValueFactory.IntegerSpinnerValueFactory(1, max, 1);
            quantitySpinner.setValueFactory(valueFactory);
            quantitySpinner.setDisable(selectedProduct.getQuantite() <= 0);
        }
    }

    @FXML
    private void close() {
        Stage stage = (Stage) nameLabel.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}