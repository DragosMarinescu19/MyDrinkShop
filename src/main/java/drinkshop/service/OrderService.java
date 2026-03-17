package drinkshop.service;

import drinkshop.domain.Order;
import drinkshop.domain.OrderItem;
import drinkshop.domain.Product;
import drinkshop.repository.Repository;

import java.util.List;

public class OrderService {

    private final Repository<Integer, Order> orderRepo;
    private final Repository<Integer, Product> productRepo;

    public OrderService(Repository<Integer, Order> orderRepo, Repository<Integer, Product> productRepo) {
        this.orderRepo = orderRepo;
        this.productRepo = productRepo;

    }

    public void addOrder(Order o) {
        orderRepo.save(o);
    }

    public void updateOrder(Order o) {
        orderRepo.update(o);
    }

    public void deleteOrder(int id) {
        orderRepo.delete(id);
    }

    public List<Order> getAllOrders() {
//        return StreamSupport.stream(orderRepo.findAll().spliterator(), false)
//                .collect(Collectors.toList());
        return orderRepo.findAll();
    }

    public Order findById(int id) {
        return orderRepo.findOne(id);
    }

    public double computeTotal(Order o) {
        return o.getItems().stream()
                .mapToDouble(i -> productRepo.findOne(i.getProduct().getId()).getPret() * i.getQuantity())
                .sum();
    }

    public void addItem(Order o, OrderItem item) {
        o.getItems().add(item);
        orderRepo.update(o);
    }

    public void removeItem(Order o, OrderItem item) {
        o.getItems().remove(item);
        orderRepo.update(o);
    }
}