package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import models.Categorie;
import models.Produit;
import services.ServiceProd;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;

public class AdminProductController {

    @FXML private TextField nomField, descriptionField, prixField, quantiteField, fournisseurField;
    @FXML private ComboBox<Categorie> categorieComboBox;
    @FXML private CheckBox disponibiliteCheck;
    @FXML private Button saveButton, deleteButton, chooseImageButton;
    @FXML private Label imagePathLabel;
    @FXML private VBox overlaySection;
    @FXML private Pane overlayPane;
    @FXML private ImageView backgroundImageView, productImageView;

    private ServiceProd serviceProd = new ServiceProd();
    private Produit selectedProduit;
    private boolean viewMode = false;
    private File selectedImageFile;
    private double dragStartX, dragStartY;

    @FXML
    public void initialize() {
        categorieComboBox.getItems().setAll(Categorie.values());
        categorieComboBox.setValue(Categorie.ARTISANAT);

        if (selectedProduit != null) {
            nomField.setText(selectedProduit.getNom());
            descriptionField.setText(selectedProduit.getDescription());
            prixField.setText(String.valueOf(selectedProduit.getPrix()));
            quantiteField.setText(String.valueOf(selectedProduit.getQuantite()));
            fournisseurField.setText(String.valueOf(selectedProduit.getFournisseur()));
            categorieComboBox.setValue(selectedProduit.getCategorie());
            disponibiliteCheck.setSelected(selectedProduit.isDisponibilite());
            if (selectedProduit.getImage() != null) {
                imagePathLabel.setText(selectedProduit.getImage());
                try {
                    Image productImage;
                    if (getClass().getResource(selectedProduit.getImage()) != null) {
                        System.out.println("Loading product image as resource: " + selectedProduit.getImage());
                        productImage = new Image(getClass().getResource(selectedProduit.getImage()).toString());
                    } else {
                        System.out.println("Loading product image as file: " + selectedProduit.getImage());
                        Path filePath = Paths.get("src/main/resources" + selectedProduit.getImage());
                        if (Files.exists(filePath)) {
                            productImage = new Image(filePath.toUri().toString());
                        } else {
                            System.out.println("Product image not found, loading placeholder");
                            productImage = new Image(getClass().getResource("/images/placeholder.jpg").toString());
                        }
                    }
                    productImageView.setImage(productImage);
                } catch (Exception e) {
                    System.err.println("Failed to load product image: " + selectedProduit.getImage());
                    e.printStackTrace();
                }
            }
        }

        if (selectedProduit == null) {
            saveButton.setVisible(true);
            deleteButton.setVisible(false);
            chooseImageButton.setVisible(true);
            overlaySection.setVisible(false);
            setFieldsEditable(true);
        } else if (viewMode) {
            saveButton.setVisible(false);
            deleteButton.setVisible(false);
            chooseImageButton.setVisible(false);
            overlaySection.setVisible(true);
            setFieldsEditable(false);
        } else {
            saveButton.setVisible(true);
            deleteButton.setVisible(true);
            chooseImageButton.setVisible(true);
            overlaySection.setVisible(false);
            setFieldsEditable(true);
        }
    }

    public void setSelectedProduit(Produit produit) {
        this.selectedProduit = produit;
        if (selectedProduit != null) {
            nomField.setText(selectedProduit.getNom());
            descriptionField.setText(selectedProduit.getDescription());
            prixField.setText(String.valueOf(selectedProduit.getPrix()));
            quantiteField.setText(String.valueOf(selectedProduit.getQuantite()));
            fournisseurField.setText(String.valueOf(selectedProduit.getFournisseur()));
            categorieComboBox.setValue(selectedProduit.getCategorie());
            disponibiliteCheck.setSelected(selectedProduit.isDisponibilite());
            if (selectedProduit.getImage() != null) {
                imagePathLabel.setText(selectedProduit.getImage());
                try {
                    Image productImage;
                    if (getClass().getResource(selectedProduit.getImage()) != null) {
                        System.out.println("Loading product image as resource: " + selectedProduit.getImage());
                        productImage = new Image(getClass().getResource(selectedProduit.getImage()).toString());
                    } else {
                        System.out.println("Loading product image as file: " + selectedProduit.getImage());
                        Path filePath = Paths.get("src/main/resources" + selectedProduit.getImage());
                        if (Files.exists(filePath)) {
                            productImage = new Image(filePath.toUri().toString());
                        } else {
                            System.out.println("Product image not found, loading placeholder");
                            productImage = new Image(getClass().getResource("/images/placeholder.jpg").toString());
                        }
                    }
                    productImageView.setImage(productImage);
                } catch (Exception e) {
                    System.err.println("Failed to load product image: " + selectedProduit.getImage());
                    e.printStackTrace();
                }
            }
        }
    }

    public void setViewMode(boolean viewMode) {
        this.viewMode = viewMode;
    }

    private void setFieldsEditable(boolean editable) {
        nomField.setDisable(!editable);
        descriptionField.setDisable(!editable);
        prixField.setDisable(!editable);
        quantiteField.setDisable(!editable);
        fournisseurField.setDisable(!editable);
        categorieComboBox.setDisable(!editable);
        disponibiliteCheck.setDisable(!editable);
    }

    @FXML
    private void chooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image pour le produit");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );
        selectedImageFile = fileChooser.showOpenDialog(nomField.getScene().getWindow());
        if (selectedImageFile != null) {
            try {
                Path imagesDir = Paths.get("src/main/resources/images");
                if (!Files.exists(imagesDir)) {
                    Files.createDirectories(imagesDir);
                }
                Path targetPath = imagesDir.resolve(selectedImageFile.getName());
                Files.copy(selectedImageFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
                imagePathLabel.setText("/images/" + selectedImageFile.getName());
            } catch (IOException e) {
                showAlert("Erreur", "Erreur lors du téléchargement de l'image : " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void chooseBackgroundImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image de fond");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );
        File backgroundFile = fileChooser.showOpenDialog(nomField.getScene().getWindow());
        if (backgroundFile != null) {
            try {
                Path imagesDir = Paths.get("src/main/resources/images/backgrounds");
                if (!Files.exists(imagesDir)) {
                    Files.createDirectories(imagesDir);
                }
                Path targetPath = imagesDir.resolve(backgroundFile.getName());
                Files.copy(backgroundFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
                Image backgroundImage = new Image(targetPath.toUri().toString());
                backgroundImageView.setImage(backgroundImage);
            } catch (IOException e) {
                showAlert("Erreur", "Erreur lors du téléchargement de l'image de fond : " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void startDrag(MouseEvent event) {
        dragStartX = event.getSceneX() - productImageView.getTranslateX();
        dragStartY = event.getSceneY() - productImageView.getTranslateY();
    }

    @FXML
    private void dragImage(MouseEvent event) {
        double newX = event.getSceneX() - dragStartX;
        double newY = event.getSceneY() - dragStartY;

        newX = Math.max(0, Math.min(newX, overlayPane.getWidth() - productImageView.getFitWidth()));
        newY = Math.max(0, Math.min(newY, overlayPane.getHeight() - productImageView.getFitHeight()));

        productImageView.setTranslateX(newX);
        productImageView.setTranslateY(newY);
    }

    @FXML
    private void saveProduit() {
        try {
            if (nomField.getText().trim().isEmpty()) {
                showAlert("Erreur", "Le champ Nom ne peut pas être vide.");
                return;
            }
            if (descriptionField.getText().trim().isEmpty()) {
                showAlert("Erreur", "Le champ Description ne peut pas être vide.");
                return;
            }
            if (prixField.getText().trim().isEmpty()) {
                showAlert("Erreur", "Le champ Prix ne peut pas être vide.");
                return;
            }
            if (quantiteField.getText().trim().isEmpty()) {
                showAlert("Erreur", "Le champ Quantité ne peut pas être vide.");
                return;
            }
            if (fournisseurField.getText().trim().isEmpty()) {
                showAlert("Erreur", "Le champ Fournisseur ne peut pas être vide.");
                return;
            }
            if (categorieComboBox.getValue() == null) {
                showAlert("Erreur", "Veuillez sélectionner une catégorie.");
                return;
            }
            if (imagePathLabel.getText().equals("Aucune image sélectionnée") && selectedImageFile == null) {
                showAlert("Erreur", "Veuillez sélectionner une image pour le produit.");
                return;
            }

            String prixText = prixField.getText().trim();
            String quantiteText = quantiteField.getText().trim();
            String fournisseurText = fournisseurField.getText().trim();

            if (!prixText.matches("\\d+(\\.\\d+)?")) {
                showAlert("Erreur", "Le prix doit être un nombre valide (ex. 10.99).");
                return;
            }
            if (!quantiteText.matches("\\d+")) {
                showAlert("Erreur", "La quantité doit être un nombre entier valide.");
                return;
            }
            if (!fournisseurText.matches("\\d+")) {
                showAlert("Erreur", "Le fournisseur doit être un nombre entier valide.");
                return;
            }

            double prix = Double.parseDouble(prixText);
            int quantite = Integer.parseInt(quantiteText);

            if (prix < 0) {
                showAlert("Erreur", "Le prix ne peut pas être négatif.");
                return;
            }
            if (quantite < 0) {
                showAlert("Erreur", "La quantité ne peut pas être négative.");
                return;
            }

            String imagePath = imagePathLabel.getText();
            if (selectedProduit == null) {
                Produit produit = new Produit(
                        0,
                        nomField.getText(),
                        descriptionField.getText(),
                        prix,
                        quantite,
                        disponibiliteCheck.isSelected(),
                        Integer.parseInt(fournisseurText),
                        categorieComboBox.getValue(),
                        imagePath
                );
                serviceProd.ajouter(produit);
            } else {
                selectedProduit.setNom(nomField.getText());
                selectedProduit.setDescription(descriptionField.getText());
                selectedProduit.setPrix(prix);
                selectedProduit.setQuantite(quantite);
                selectedProduit.setDisponibilite(disponibiliteCheck.isSelected());
                selectedProduit.setFournisseur(Integer.parseInt(fournisseurText));
                selectedProduit.setCategorie(categorieComboBox.getValue());
                selectedProduit.setImage(imagePath);
                serviceProd.modifier(selectedProduit);
            }

            clearFields();
            goBack();
        } catch (SQLException e) {
            System.err.println("Database error in saveProduit: " + e.getMessage());
            showAlert("Erreur de Base de Données", "Une erreur est survenue : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void deleteProduit() {
        if (selectedProduit != null) {
            try {
                serviceProd.supprimer(selectedProduit.getId());
                clearFields();
                goBack();
            } catch (SQLException e) {
                System.err.println("Database error in deleteProduit: " + e.getMessage());
                showAlert("Erreur de Base de Données", "Une erreur est survenue : " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/productList.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) nomField.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            System.err.println("Failed to load productList.fxml: " + e.getMessage());
            showAlert("Erreur", "Impossible de charger la liste des produits : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void clearFields() {
        nomField.clear();
        descriptionField.clear();
        prixField.clear();
        quantiteField.clear();
        fournisseurField.clear();
        categorieComboBox.setValue(Categorie.ARTISANAT);
        disponibiliteCheck.setSelected(false);
        imagePathLabel.setText("Aucune image sélectionnée");
        selectedImageFile = null;
        backgroundImageView.setImage(null);
        productImageView.setImage(null);
        productImageView.setTranslateX(0);
        productImageView.setTranslateY(0);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}