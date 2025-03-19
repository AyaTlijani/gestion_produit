package controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import models.OrderItem;
import tools.MyDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MyOrdersController {

    @FXML private TableView<OrderItem> ordersTable;
    @FXML private TableColumn<OrderItem, String> orderIdColumn;
    @FXML private TableColumn<OrderItem, String> productColumn;
    @FXML private TableColumn<OrderItem, Double> priceColumn;
    @FXML private TableColumn<OrderItem, Integer> quantityColumn;
    @FXML private TableColumn<OrderItem, String> dateColumn;

    private Connection connection = MyDB.getInstance().getConnection();
    private int artisanId; // Set dynamically from login

    public void setArtisanId(int id) {
        this.artisanId = id; // Called when artisan logs in
        loadOrders();
    }

    public void initialize() {
        orderIdColumn.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        productColumn.setCellValueFactory(new PropertyValueFactory<>("nomProduit"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("prix"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantite"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("deliveryDate"));
    }

    private void loadOrders() {
        List<OrderItem> orderItems = new ArrayList<>();
        String query = "SELECT oi.order_id, oi.nomProduit, oi.prix, oi.quantite, o.delivery_date " +
                "FROM order_items oi JOIN orders o ON oi.order_id = o.order_id " +
                "WHERE oi.idArtisan = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, artisanId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                orderItems.add(new OrderItem(
                        rs.getString("order_id"),
                        rs.getString("nomProduit"),
                        rs.getDouble("prix"),
                        rs.getInt("quantite"),
                        rs.getString("delivery_date")
                ));
            }
            ordersTable.setItems(FXCollections.observableArrayList(orderItems));
        } catch (SQLException e) {
            System.out.println("Error loading orders: " + e.getMessage());
            e.printStackTrace();
        }
    }
}