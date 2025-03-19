package models;

public class OrderItem {
    private String orderId;
    private String nomProduit;
    private double prix;
    private int quantite;
    private String deliveryDate;

    public OrderItem(String orderId, String nomProduit, double prix, int quantite, String deliveryDate) {
        this.orderId = orderId;
        this.nomProduit = nomProduit;
        this.prix = prix;
        this.quantite = quantite;
        this.deliveryDate = deliveryDate;
    }

    // Getters
    public String getOrderId() { return orderId; }
    public String getNomProduit() { return nomProduit; }
    public double getPrix() { return prix; }
    public int getQuantite() { return quantite; }
    public String getDeliveryDate() { return deliveryDate; }

    // Setters (optional, if needed)
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public void setNomProduit(String nomProduit) { this.nomProduit = nomProduit; }
    public void setPrix(double prix) { this.prix = prix; }
    public void setQuantite(int quantite) { this.quantite = quantite; }
    public void setDeliveryDate(String deliveryDate) { this.deliveryDate = deliveryDate; }
}
