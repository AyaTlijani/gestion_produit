package controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import models.Categorie;
import models.Produit;
import services.ServiceProd;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ProductListController {

    @FXML private Accordion categoryAccordion;
    @FXML private ImageView logoImage;
    @FXML private Button addButton;

    private ServiceProd serviceProd = new ServiceProd();

    @FXML
    public void initialize() {
        loadProduits();

        if (logoImage != null) {
            logoImage.setFitWidth(50);
            logoImage.setFitHeight(50);
        }
    }

    private void loadProduits() {
        try {
            List<Produit> produits = serviceProd.afficher();
            Map<Categorie, List<Produit>> productsByCategory = produits.stream()
                    .collect(Collectors.groupingBy(Produit::getCategorie));

            categoryAccordion.getPanes().clear();
            for (Categorie categorie : Categorie.values()) {
                List<Produit> categoryProducts = productsByCategory.getOrDefault(categorie, List.of());
                if (!categoryProducts.isEmpty()) {
                    TableView<Produit> tableView = new TableView<>();
                    tableView.setPrefWidth(800);

                    TableColumn<Produit, String> imageColumn = new TableColumn<>("Image");
                    imageColumn.setPrefWidth(100);
                    imageColumn.setCellFactory(param -> new TableCell<>() {
                        private final ImageView imageView = new ImageView();

                        @Override
                        protected void updateItem(String imagePath, boolean empty) {
                            super.updateItem(imagePath, empty);
                            if (empty || imagePath == null) {
                                setGraphic(null);
                            } else {
                                try {
                                    Image image;
                                    if (getClass().getResource(imagePath) != null) {
                                        System.out.println("Loading image as resource: " + imagePath);
                                        image = new Image(getClass().getResource(imagePath).toString(), 50, 50, true, true);
                                    } else {
                                        System.out.println("Loading image as file: " + imagePath);
                                        Path filePath = Paths.get("src/main/resources" + imagePath);
                                        if (Files.exists(filePath)) {
                                            image = new Image(filePath.toUri().toString(), 50, 50, true, true);
                                        } else {
                                            System.out.println("Image not found, loading placeholder");
                                            image = new Image(getClass().getResource("/images/placeholder.jpg").toString(), 50, 50, true, true);
                                        }
                                    }
                                    imageView.setImage(image);
                                    setGraphic(imageView);
                                } catch (Exception e) {
                                    setGraphic(null);
                                    System.err.println("Failed to load image: " + imagePath);
                                    e.printStackTrace();
                                }
                            }
                        }
                    });

                    TableColumn<Produit, String> nomColumn = new TableColumn<>("Nom");
                    nomColumn.setPrefWidth(150);
                    nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));

                    TableColumn<Produit, Double> prixColumn = new TableColumn<>("Prix");
                    prixColumn.setPrefWidth(100);
                    prixColumn.setCellValueFactory(new PropertyValueFactory<>("prix"));

                    TableColumn<Produit, Integer> quantiteColumn = new TableColumn<>("Quantité");
                    quantiteColumn.setPrefWidth(100);
                    quantiteColumn.setCellValueFactory(new PropertyValueFactory<>("quantite"));

                    TableColumn<Produit, String> categorieColumn = new TableColumn<>("Catégorie");
                    categorieColumn.setPrefWidth(120);
                    categorieColumn.setCellValueFactory(cellData ->
                            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getCategorie().toString()));

                    TableColumn<Produit, Void> actionColumn = new TableColumn<>("Actions");
                    actionColumn.setPrefWidth(230);
                    actionColumn.setCellFactory(param -> new TableCell<>() {
                        private final Button updateButton = new Button("Modifier");
                        private final Button deleteButton = new Button("Supprimer");
                        private final Button detailsButton = new Button("Détails");
                        private final HBox buttonBox = new HBox(5, updateButton, deleteButton, detailsButton);

                        {
                            updateButton.getStyleClass().add("button");
                            deleteButton.getStyleClass().add("button");
                            detailsButton.getStyleClass().add("button");

                            updateButton.setOnAction(event -> {
                                Produit produit = getTableView().getItems().get(getIndex());
                                try {
                                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/adminProduct.fxml"));
                                    Parent root = loader.load();
                                    AdminProductController controller = loader.getController();
                                    controller.setSelectedProduit(produit);
                                    controller.setViewMode(false);
                                    Scene scene = new Scene(root);
                                    Stage stage = (Stage) getTableView().getScene().getWindow();
                                    stage.setScene(scene);
                                    stage.show();
                                } catch (IOException e) {
                                    System.err.println("Failed to load adminProduct.fxml: " + e.getMessage());
                                    e.printStackTrace();
                                }
                            });

                            deleteButton.setOnAction(event -> {
                                Produit produit = getTableView().getItems().get(getIndex());
                                Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION, "Êtes-vous sûr de vouloir supprimer ce produit ?", ButtonType.YES, ButtonType.NO);
                                confirmAlert.showAndWait().ifPresent(response -> {
                                    if (response == ButtonType.YES) {
                                        try {
                                            serviceProd.supprimer(produit.getId());
                                            loadProduits();
                                        } catch (SQLException e) {
                                            System.err.println("Failed to delete product: " + e.getMessage());
                                            Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur lors de la suppression : " + e.getMessage());
                                            alert.showAndWait();
                                        }
                                    }
                                });
                            });

                            detailsButton.setOnAction(event -> {
                                Produit produit = getTableView().getItems().get(getIndex());
                                try {
                                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/adminProduct.fxml"));
                                    Parent root = loader.load();
                                    AdminProductController controller = loader.getController();
                                    controller.setSelectedProduit(produit);
                                    controller.setViewMode(true);
                                    Scene scene = new Scene(root);
                                    Stage stage = (Stage) getTableView().getScene().getWindow();
                                    stage.setScene(scene);
                                    stage.show();
                                } catch (IOException e) {
                                    System.err.println("Failed to load adminProduct.fxml: " + e.getMessage());
                                    e.printStackTrace();
                                }
                            });
                        }

                        @Override
                        protected void updateItem(Void item, boolean empty) {
                            super.updateItem(item, empty);
                            if (empty) {
                                setGraphic(null);
                            } else {
                                setGraphic(buttonBox);
                            }
                        }
                    });

                    tableView.getColumns().addAll(imageColumn, nomColumn, prixColumn, quantiteColumn, categorieColumn, actionColumn);
                    tableView.setItems(FXCollections.observableArrayList(categoryProducts));

                    TitledPane titledPane = new TitledPane(categorie.name(), tableView);
                    categoryAccordion.getPanes().add(titledPane);
                }
            }
        } catch (SQLException e) {
            System.err.println("Failed to load products: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void addProduit() {
        try {
            System.out.println("Loading adminProduct.fxml");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/adminProduct.fxml"));
            Parent root = loader.load();
            AdminProductController controller = loader.getController();
            System.out.println("Controller loaded: " + (controller != null));
            controller.setSelectedProduit(null);
            controller.setViewMode(false);
            Scene scene = new Scene(root);
            Stage stage = (Stage) addButton.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            System.err.println("Failed to load adminProduct.fxml: " + e.getMessage());
            e.printStackTrace();
        }
    }
}