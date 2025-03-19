package models;

import java.util.List;

public class Order {
    private String orderId;
    private List<Produit> items;
    private double totalPrice;
    private String status;
    private String fullName;
    private String address;
    private String email;
    private String phone;
    private String paymentMethod;
    private String deliveryDate;

    public Order(String orderId, List<Produit> items, double totalPrice, String status,
                 String fullName, String address, String email, String phone,
                 String paymentMethod, String deliveryDate) {
        this.orderId = orderId;
        this.items = items;
        this.totalPrice = totalPrice;
        this.status = status;
        this.fullName = fullName;
        this.address = address;
        this.email = email;
        this.phone = phone;
        this.paymentMethod = paymentMethod;
        this.deliveryDate = deliveryDate;
    }

    public String getOrderId() {
        return orderId;
    }
}
