package mydrinkshop;

import drinkshop.domain.Product;
import drinkshop.repository.Repository;
import drinkshop.service.ProductService;
import drinkshop.service.validator.ProductValidator;
import drinkshop.service.validator.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductServiceIntegration_V_Test {

    @Mock
    private Repository<Integer, Product> productRepo; // Rămâne Mock

    private ProductValidator realValidator; // Devine Real
    private ProductService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        realValidator = new ProductValidator();
        service = new ProductService(productRepo, realValidator); // Injectare manuală
    }

    @Test
    void testAddProduct_IntegrationV_ValidProduct() {
        Product p = new Product(1, "Fanta", 6.0, null, null);

        // Logica reală din ProductValidator va rula
        service.addProduct(p);

        // Ne asigurăm că a ajuns la Repo
        verify(productRepo, times(1)).save(p);
    }

    @Test
    void testAddProduct_IntegrationV_InvalidName() {
        // Produs cu nume gol
        Product p = new Product(2, "", 5.0, null, null);

        // Validatorul real trebuie să crape
        ValidationException ex = assertThrows(ValidationException.class, () -> {
            service.addProduct(p);
        });

        assertEquals("Numele nu poate fi gol!\n", ex.getMessage());

        // Nu s-a salvat nimic
        verify(productRepo, never()).save(any());
    }
}