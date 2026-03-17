package drinkshop.reports;

import drinkshop.domain.Order;
import drinkshop.repository.Repository;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class DailyReportService {
    private Repository<Integer, Order> repo;

    public DailyReportService(Repository<Integer, Order> repo) {
        this.repo = repo;
    }

    public double getTotalRevenue() {
        return repo.findAll().stream().mapToDouble(Order::getTotal).sum();
    }

    public int getTotalOrders() {
//        List<Order> orders = StreamSupport.stream(repo.findAll().spliterator(), false)
//                .collect(Collectors.toList());

        return repo.findAll().size();
    }
}