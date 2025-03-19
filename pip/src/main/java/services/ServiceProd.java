package services;

import models.Categorie;
import models.Produit;
import tools.MyDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ServiceProd implements IService<Produit> {

    @Override
    public void ajouter(Produit produit) throws SQLException {
        Connection conn = MyDB.getInstance().getConnection();
        String query = "INSERT INTO Produit (nom, description, prix, quantite, disponibilite, fournisseur, categorie, image) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, produit.getNom());
            stmt.setString(2, produit.getDescription());
            stmt.setDouble(3, produit.getPrix());
            stmt.setInt(4, produit.getQuantite());
            stmt.setBoolean(5, produit.isDisponibilite());
            stmt.setInt(6, produit.getFournisseur());
            stmt.setString(7, produit.getCategorie() != null ? produit.getCategorie().name() : null);
            stmt.setString(8, produit.getImage());
            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    produit.setId(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            System.err.println("SQL Error in ajouter: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public List<Produit> afficher() throws SQLException {
        List<Produit> produits = new ArrayList<>();
        Connection conn = MyDB.getInstance().getConnection();
        String query = "SELECT * FROM Produit";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                String categorieStr = rs.getString("categorie");
                Categorie categorie = (categorieStr != null) ? Categorie.valueOf(categorieStr) : Categorie.ARTISANAT;

                Produit produit = new Produit(
                        rs.getInt("idProduit"),
                        rs.getString("nom"),
                        rs.getString("description"),
                        rs.getDouble("prix"),
                        rs.getInt("quantite"),
                        rs.getBoolean("disponibilite"),
                        rs.getInt("fournisseur"),
                        categorie,
                        rs.getString("image")
                );
                produits.add(produit);
            }
        } catch (SQLException e) {
            System.err.println("SQL Error in afficher: " + e.getMessage());
            throw e;
        }
        return produits;
    }

    @Override
    public void modifier(Produit produit) throws SQLException {
        Connection conn = MyDB.getInstance().getConnection();
        String query = "UPDATE Produit SET nom = ?, description = ?, prix = ?, quantite = ?, disponibilite = ?, fournisseur = ?, categorie = ?, image = ? WHERE idProduit = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            conn.setAutoCommit(false); // Start transaction
            try {
                stmt.setString(1, produit.getNom());
                stmt.setString(2, produit.getDescription());
                stmt.setDouble(3, produit.getPrix());
                stmt.setInt(4, produit.getQuantite());
                stmt.setBoolean(5, produit.isDisponibilite());
                stmt.setInt(6, produit.getFournisseur());
                stmt.setString(7, produit.getCategorie() != null ? produit.getCategorie().name() : null);
                stmt.setString(8, produit.getImage());
                stmt.setInt(9, produit.getId());
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected == 0) {
                    throw new SQLException("Aucun produit modifié. Vérifiez l'ID.");
                }
                conn.commit(); // Commit transaction
            } catch (SQLException e) {
                conn.rollback(); // Rollback on error
                System.err.println("SQL Error in modifier: " + e.getMessage());
                throw e;
            } finally {
                conn.setAutoCommit(true); // Reset auto-commit
            }
        }
    }

    @Override
    public void supprimer(int id) throws SQLException {
        Connection conn = MyDB.getInstance().getConnection();
        String query = "DELETE FROM Produit WHERE idProduit = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("SQL Error in supprimer: " + e.getMessage());
            throw e;
        }
    }
}