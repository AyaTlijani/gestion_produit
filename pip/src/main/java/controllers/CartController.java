package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import models.Panier;
import models.Produit;
import javafx.scene.control.ListCell;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;

import java.io.IOException;

public class CartController {

    @FXML private ListView<Produit> cartListView;
    @FXML private Label totalLabel;
    @FXML private Button removeButton;
    @FXML private Label emptyCartLabel;
    @FXML private Button buyNowButton; // Added for "Buy Now"

    private Panier panier;

    public void initialize() {
        panier = Panier.getInstance();
        updateCartView(); // Refresh the UI initially

        // Enable/disable remove button based on selection
        cartListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            removeButton.setDisable(newValue == null);
        });

        // Initially disable "Buy Now" if cart is empty
        buyNowButton.setDisable(panier.getCartItems().isEmpty());
    }

    public void removeFromCart(Produit produit) {
        Panier panier = Panier.getInstance();
        panier.removeFromCart(produit);  // Increases stock in database
        // Sync produit with database state
        Produit updatedProduit = panier.getProductById(produit.getId());
        if (updatedProduit != null) {
            produit.setQuantite(updatedProduit.getQuantite());  // Sync stock for display
        }
        updateCartView();  // Refresh UI
    }

    private void updateCartView() {
        cartListView.setCellFactory(param -> new ListCell<Produit>() {
            @Override
            protected void updateItem(Produit item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null && !empty) {
                    setText(item.toString()); // Use the toString method that includes the quantity
                } else {
                    setText(null);
                }
            }
        });

        cartListView.getItems().setAll(panier.getCartItems()); // Refresh the items in the cart list view

        if (panier.getCartItems().isEmpty()) {
            emptyCartLabel.setVisible(true);
            emptyCartLabel.setText("Your cart is empty.");
            cartListView.setVisible(false);
            buyNowButton.setDisable(true); // Disable "Buy Now" when cart is empty
        } else {
            emptyCartLabel.setVisible(false);
            cartListView.setVisible(true);
            buyNowButton.setDisable(false); // Enable "Buy Now" when cart has items
        }

        updateTotal(); // Update the total price as well
    }

    private void updateTotal() {
        totalLabel.setText("Total: " + panier.getTotalPrice() + " TND");
    }

    @FXML
    private void goBack() {
        Stage currentStage = (Stage) cartListView.getScene().getWindow();
        currentStage.close();
    }

    @FXML
    private void handleRemoveButtonClick() {
        Produit selectedProduit = cartListView.getSelectionModel().getSelectedItem();
        if (selectedProduit != null) {
            removeFromCart(selectedProduit); // Call the method to remove the product from the cart
        }
    }

    @FXML
    private void buyNow() {
        System.out.println("Buy Now clicked! Cart items: " + panier.getCartItems().size());
        if (panier.getCartItems().isEmpty()) {
            showAlert("Erreur", "Le panier est vide. Ajoutez des articles avant de procéder au paiement.");
            return;
        }

        try {
            java.net.URL location = getClass().getResource("/Checkout.fxml"); // Updated path
            if (location == null) {
                System.out.println("Checkout.fxml not found at /Checkout.fxml");
                showAlert("Erreur", "Fichier Checkout.fxml introuvable.");
                return;
            }
            System.out.println("Loading Checkout.fxml from: " + location);
            FXMLLoader loader = new FXMLLoader(location);
            Parent root = loader.load();
            System.out.println("Loaded successfully");
            CheckoutController controller = loader.getController();
            controller.setCart(panier);
            Stage stage = new Stage();
            stage.setScene(new Scene(root, 400, 500));
            stage.setTitle("Paiement");
            stage.showAndWait();
            updateCartView();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger la page de paiement : " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}


