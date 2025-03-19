package models;

import java.util.ArrayList;
import java.util.List;

public class OrderTracker {
    private static OrderTracker instance;
    private List<Order> orders;

    private OrderTracker() {
        orders = new ArrayList<>();
    }

    public static OrderTracker getInstance() {
        if (instance == null) {
            instance = new OrderTracker();
        }
        return instance;
    }

    public void addOrder(Order order) {
        orders.add(order);
    }
}