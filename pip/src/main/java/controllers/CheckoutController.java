package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.stage.Stage;
import models.Panier;
import models.Order;
import models.OrderTracker;
import java.time.LocalDate;

public class CheckoutController {
    @FXML private TextField fullNameField;
    @FXML private TextField addressField;
    @FXML private TextField cardNumberField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private ComboBox<String> paymentMethodCombo;
    @FXML private DatePicker deliveryDatePicker;
    @FXML private TextField cvvField;

    private Panier cart;

    public void initialize() {
        paymentMethodCombo.getItems().addAll("Visa", "MasterCard", "PayPal");
        paymentMethodCombo.setValue("Visa");
    }

    public void setCart(Panier cart) {
        this.cart = cart;
    }

    @FXML
    private void handlePayment() {
        if (validateForm()) {
            Order newOrder = new Order(
                    "CMD-" + System.currentTimeMillis(),
                    cart.getCartItems(),
                    cart.getTotalPrice(),
                    "En préparation",
                    fullNameField.getText(),
                    addressField.getText(),
                    emailField.getText(),
                    phoneField.getText(),
                    paymentMethodCombo.getValue(),
                    deliveryDatePicker.getValue() != null ? deliveryDatePicker.getValue().toString() : "N/A"
            );

            OrderTracker.getInstance().addOrder(newOrder);
            cart.getCartItems().clear();
            showAlert("Succès", "Paiement effectué avec succès ! Commande ID: " + newOrder.getOrderId(), AlertType.INFORMATION);

            Stage stage = (Stage) fullNameField.getScene().getWindow();
            stage.close();
        }
    }

    private boolean validateForm() {
        String fullName = fullNameField.getText().trim();
        String address = addressField.getText().trim();
        String cardNumber = cardNumberField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String cvv = cvvField.getText().trim();

        if (fullName.isEmpty() || address.isEmpty() || cardNumber.isEmpty() || email.isEmpty() || phone.isEmpty() || cvv.isEmpty()) {
            showAlert("Champs manquants", "Veuillez remplir tous les champs obligatoires.", AlertType.ERROR);
            return false;
        }

        if (!cardNumber.matches("\\d{16}")) {
            showAlert("Carte invalide", "Le numéro de carte doit contenir 16 chiffres.", AlertType.ERROR);
            return false;
        }

        if (!cvv.matches("\\d{3,4}")) {
            showAlert("CVV invalide", "Le CVV doit contenir 3 ou 4 chiffres.", AlertType.ERROR);
            return false;
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showAlert("Email invalide", "Veuillez entrer un email valide.", AlertType.ERROR);
            return false;
        }

        if (!phone.matches("\\d{8,15}")) {
            showAlert("Téléphone invalide", "Le numéro de téléphone doit contenir 8 à 15 chiffres.", AlertType.ERROR);
            return false;
        }

        if (deliveryDatePicker.getValue() != null && deliveryDatePicker.getValue().isBefore(LocalDate.now())) {
            showAlert("Date invalide", "La date de livraison doit être future.", AlertType.ERROR);
            return false;
        }

        return true;
    }

    private void showAlert(String title, String message, AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}