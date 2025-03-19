package models;

import tools.MyDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Panier {

    private static Panier instance;
    private List<Produit> cart = new ArrayList<>();
    private Connection connection;

    private Panier() {
        connection = MyDB.getInstance().getConnection();
        try {
            Statement stmt = connection.createStatement();
            stmt.executeQuery("SELECT 1");
            System.out.println("Panier: Database connection successful");
            System.out.println("Auto-commit status: " + connection.getAutoCommit());
        } catch (SQLException e) {
            System.out.println("Panier: Database connection failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static Panier getInstance() {
        if (instance == null) {
            instance = new Panier();
        }
        return instance;
    }

    public void addToCart(Produit produit) {
        addToCart(produit, 1); // Default to adding 1 item
    }

    public void addToCart(Produit produit, int quantityToAdd) {
        System.out.println("Adding to cart: " + produit.getNom() + ", Quantity: " + quantityToAdd + ", Stock: " + produit.getQuantite() + ", ID: " + produit.getId());

        // Check database stock first
        Produit dbProduct = getProductById(produit.getId());
        if (dbProduct.getQuantite() < quantityToAdd) {
            System.out.println("Not enough stock for " + produit.getNom() + "! Available: " + dbProduct.getQuantite());
            throw new IllegalArgumentException("Insufficient stock: only " + dbProduct.getQuantite() + " available.");
        }

        for (Produit item : cart) {
            if (item.getId() == produit.getId()) {
                item.setQuantite(item.getQuantite() + quantityToAdd); // Increase cart quantity
                updateStock(produit.getId(), -quantityToAdd); // Reduce stock
                saveToDatabase();
                System.out.println("Increased cart quantity: " + item.getNom() + ", New Qty: " + item.getQuantite());
                return;
            }
        }

        // New item in cart
        Produit produitClone = new Produit(produit.getId(), produit.getNom(), produit.getDescription(),
                produit.getPrix(), quantityToAdd, produit.isDisponibilite(),
                produit.getFournisseur(), produit.getCategorie(), produit.getImage());
        cart.add(produitClone);
        updateStock(produit.getId(), -quantityToAdd);
        saveToDatabase();
        System.out.println("Added to cart: " + produit.getNom() + ", Quantity: " + quantityToAdd);
    }

    public void removeFromCart(Produit produit) {
        System.out.println("Removing from cart - Product ID: " + produit.getId() + ", Name: " + produit.getNom());
        boolean found = false;
        Produit itemToRemove = null;
        for (Produit item : cart) {
            if (item.getId() == produit.getId()) {
                itemToRemove = item;
                found = true;
                break;
            }
        }
        if (found && itemToRemove != null) {
            int quantityToReturn = itemToRemove.getQuantite();
            System.out.println("Item found: " + itemToRemove.getNom() + ", Quantity to return: " + quantityToReturn);
            cart.remove(itemToRemove);
            updateStock(produit.getId(), quantityToReturn);
            saveToDatabase();
            System.out.println("Removed: " + produit.getNom() + ", Should return stock: " + quantityToReturn);
        } else {
            System.out.println("ERROR: Product ID " + produit.getId() + " not found in cart");
        }
        System.out.println("Cart size after removal: " + cart.size());
    }

    private void updateStock(int productId, int quantityChange) {
        try {
            System.out.println("Updating stock - ID: " + productId + ", Change: " + quantityChange);
            String checkQuery = "SELECT quantite FROM produit WHERE idProduit = ?";
            PreparedStatement checkStmt = connection.prepareStatement(checkQuery);
            checkStmt.setInt(1, productId);
            ResultSet rs = checkStmt.executeQuery();
            int currentStock = -1;
            if (rs.next()) {
                currentStock = rs.getInt("quantite");
                System.out.println("Current stock before update: " + currentStock);
            } else {
                System.out.println("ERROR: Product ID " + productId + " not found in produit table");
                return;
            }

            String query = "UPDATE produit SET quantite = quantite + ?, disponibilite = (quantite + ? > 0) WHERE idProduit = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, quantityChange);
            statement.setInt(2, quantityChange);
            statement.setInt(3, productId);
            System.out.println("Executing query: UPDATE produit SET quantite = quantite + " + quantityChange + ", disponibilite = (quantite + " + quantityChange + " > 0) WHERE idProduit = " + productId);
            int rows = statement.executeUpdate();
            System.out.println("Stock update executed - Rows affected: " + rows);
            if (rows == 0) {
                System.out.println("ERROR: Stock update failed - No rows affected for ID: " + productId);
            } else {
                System.out.println("Stock update successful - Expected new stock: " + (currentStock + quantityChange));
            }

            rs = checkStmt.executeQuery();
            if (rs.next()) {
                int finalStock = rs.getInt("quantite");
                System.out.println("Stock after update (verified): " + finalStock);
                if (finalStock != currentStock + quantityChange) {
                    System.out.println("WARNING: Stock in database (" + finalStock + ") does not match expected (" + (currentStock + quantityChange) + ")");
                }
            }
        } catch (SQLException e) {
            System.out.println("Stock update failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public List<Produit> getCartItems() {
        return cart;
    }

    public double getTotalPrice() {
        double total = 0;
        for (Produit product : cart) {
            total += product.getPrix() * product.getQuantite();
        }
        return total;
    }

    public void saveToDatabase() {
        try {
            String clearQuery = "DELETE FROM panier";
            PreparedStatement clearStmt = connection.prepareStatement(clearQuery);
            clearStmt.executeUpdate();

            String query = "INSERT INTO panier (idProduit, quantite, prixTotal, calculerPrix) VALUES (?, ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(query);

            for (Produit produit : cart) {
                statement.setInt(1, produit.getId());
                statement.setInt(2, produit.getQuantite());
                statement.setDouble(3, produit.getPrix() * produit.getQuantite());
                statement.setDouble(4, produit.getPrix() * produit.getQuantite());
                statement.addBatch();
            }

            statement.executeBatch();
            System.out.println("Cart saved to panier table");
        } catch (SQLException e) {
            System.out.println("Failed to save cart: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void loadFromDatabase() {
        try {
            String query = "SELECT * FROM panier";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);

            cart.clear();

            while (resultSet.next()) {
                int idProduit = resultSet.getInt("idProduit");
                int quantite = resultSet.getInt("quantite");
                double prixTotal = resultSet.getDouble("prixTotal");

                Produit produit = getProductById(idProduit);
                if (produit != null) {
                    produit.setQuantite(quantite);
                    cart.add(produit);
                }
            }
            System.out.println("Cart loaded from panier table");
        } catch (SQLException e) {
            System.out.println("Failed to load cart: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Produit getProductById(int idProduit) {
        try {
            String query = "SELECT * FROM produit WHERE idProduit = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, idProduit);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                String nom = resultSet.getString("nom");
                String description = resultSet.getString("description");
                double prix = resultSet.getDouble("prix");
                boolean disponibilite = resultSet.getBoolean("disponibilite");
                int fournisseur = resultSet.getInt("fournisseur");
                Categorie categorie = Categorie.valueOf(resultSet.getString("categorie"));
                String image = resultSet.getString("image");
                int quantite = resultSet.getInt("quantite");

                return new Produit(idProduit, nom, description, prix, quantite, disponibilite, fournisseur, categorie, image);
            }
        } catch (SQLException e) {
            System.out.println("Failed to get product: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}