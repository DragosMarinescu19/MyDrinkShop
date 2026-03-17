package drinkshop.ui;

import drinkshop.domain.*;
import drinkshop.repository.Repository;
import drinkshop.repository.file.FileOrderRepository;
import drinkshop.repository.file.FileProductRepository;
import drinkshop.repository.file.FileRetetaRepository;
import drinkshop.repository.file.FileStocRepository;
import drinkshop.service.DrinkShopService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class DrinkShopApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        // ---------- Initializare Repository-uri care citesc din fisiere ----------
        Repository<Integer, Product> productRepo = new FileProductRepository("data/products.txt");
        Repository<Integer, Order> orderRepo = new FileOrderRepository("data/orders.txt", productRepo);
        Repository<Integer, Reteta> retetaRepo = new FileRetetaRepository("data/retete.txt");
        Repository<Integer, Stoc> stocRepo = new FileStocRepository("data/stocuri.txt");

        // ---------- Initializare Service ----------
        DrinkShopService service = new DrinkShopService(productRepo, orderRepo, retetaRepo, stocRepo);

        // ---------- Incarcare FXML ----------

        FXMLLoader loader = new FXMLLoader(getClass().getResource("drinkshop.fxml"));
        Scene scene = new Scene(loader.load());

        // ---------- Setare Service in Controller ----------
        DrinkShopController controller = loader.getController();
        controller.setService(service);

        // ---------- Afisare Fereastra ----------
        stage.setTitle("Coffee Shop Management");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}