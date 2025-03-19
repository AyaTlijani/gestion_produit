package models;

public class Produit {
    private int id;
    private String nom;
    private String description;
    private double prix;
    private int quantite;
    private boolean disponibilite;
    private int fournisseur;
    private Categorie categorie;
    private String image;

    public Produit(int id, String nom, String description, double prix, int quantite, boolean disponibilite, int fournisseur, Categorie categorie, String image) {
        this.id = id;
        this.nom = nom;
        this.description = description;
        this.prix = prix;
        this.quantite = quantite;
        this.disponibilite = disponibilite;
        this.fournisseur = fournisseur;
        this.categorie = categorie;
        this.image = image;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public double getPrix() { return prix; }
    public void setPrix(double prix) { this.prix = prix; }
    public int getQuantite() { return quantite; }
    public void setQuantite(int quantite) { this.quantite = quantite; }
    public boolean isDisponibilite() { return disponibilite; }
    public void setDisponibilite(boolean disponibilite) { this.disponibilite = disponibilite; }
    public int getFournisseur() { return fournisseur; }
    public void setFournisseur(int fournisseur) { this.fournisseur = fournisseur; }
    public Categorie getCategorie() { return categorie; }
    public void setCategorie(Categorie categorie) { this.categorie = categorie; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    @Override
    public String toString() {
        return nom + " - " + prix + " TND - Quantity: " + quantite; // Display quantity here
    }


    // Reduce stock by 1
    public boolean reduceStock() {
        if (quantite > 0) {
            System.out.println("Stock reduced from " + quantite + " to " + (quantite - 1));  // Debugging
            quantite--;
            return true;
        }
        return false;
    }

    // Method to add back stock (in case of removal from cart)
    public void addStock(int quantity) {
        quantite += quantity;  // Increase stock by the quantity removed
    }
}