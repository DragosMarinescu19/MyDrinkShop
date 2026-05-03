package drinkshop.service;

import drinkshop.domain.*;
import drinkshop.repository.Repository;
import drinkshop.service.validator.ProductValidator;

import java.util.List;
import java.util.stream.Collectors;

public class ProductService {

    private final Repository<Integer, Product> productRepo;
    private final ProductValidator validator;

    public ProductService(Repository<Integer, Product> productRepo, ProductValidator validator) {
        this.productRepo = productRepo;
        if(validator==null)
            this.validator= new ProductValidator();
        else
            this.validator = validator;
    }

    public void addProduct(Product p) {
        validator.validate(p);
        productRepo.save(p);
    }

    public void updateProduct(int id, String name, double price, CategorieBautura categorie, TipBautura tip) {
        Product updated = new Product(id, name, price, categorie, tip);
        validator.validate(updated);
        productRepo.update(updated);
    }
    public Product getProductByName(String name, Double maxPrice, String category){
        if (name == null || name.length() < 2) { // Predicat 1
            return null;}
        List<Product> products = getAllProducts();
        for (Product p : products) { //Pred 2
            if (p.getPret() <= maxPrice) { // Pred 3
                if (p.getCategorie().name().equals(category)) { //Pred 4
                    if (p.getNume().contains(name)) { // Pred 5
                        return p;
                    }
                }
            }
        }
        return null;
    }

    public void deleteProduct(int id) {
        productRepo.delete(id);
    }

    public List<Product> getAllProducts() {
//        Iterable<Product> it=productRepo.findAll();
//        ArrayList<Product> products=new ArrayList<>();
//        it.forEach(products::add);
//        return products;

//        return StreamSupport.stream(productRepo.findAll().spliterator(), false)
//                    .collect(Collectors.toList());
        return productRepo.findAll();
    }

    public Product findById(int id) {
        return productRepo.findOne(id);
    }

    public List<Product> filterByCategorie(CategorieBautura categorie) {
        if (categorie == CategorieBautura.ALL) return getAllProducts();
        return getAllProducts().stream()
                .filter(p -> p.getCategorie() == categorie)
                .collect(Collectors.toList());
    }

    public List<Product> filterByTip(TipBautura tip) {
        if (tip == TipBautura.ALL) return getAllProducts();
        return getAllProducts().stream()
                .filter(p -> p.getTip() == tip)
                .collect(Collectors.toList());
    }
}