package mydrinkshop;

import drinkshop.domain.Product;
import drinkshop.repository.file.FileProductRepository;
import drinkshop.service.ProductService;
import drinkshop.service.validator.ProductValidator;
import drinkshop.service.validator.ValidationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ProductServiceIntegration_V_R_Test {

    private FileProductRepository realRepo;
    private ProductValidator realValidator;
    private ProductService service;

    private static final String TEST_FILE = "test_products.txt";

    @BeforeEach
    void setUp() throws IOException {
        // Curățăm mediul creând un fișier gol
        Files.deleteIfExists(Path.of(TEST_FILE));
        new File(TEST_FILE).createNewFile();

        realRepo = new FileProductRepository(TEST_FILE);
        realValidator = new ProductValidator();
        service = new ProductService(realRepo, realValidator);
    }

    @AfterEach
    void tearDown() throws IOException {
        // Ștergem fișierul de test după rulare ca să nu lăsăm gunoi
        Files.deleteIfExists(Path.of(TEST_FILE));
    }

    @Test
    void testAddProduct_IntegrationVR_EndToEndSuccess() {
        Product p = new Product(1, "Sprite", 5.5, null, null);

        service.addProduct(p);

        // Nu mai folosim "verify" pentru că nu mai avem mock-uri. folosim assert
        // Citim direct din instanța reală de repository.
        assertEquals(1, service.getAllProducts().size());
        assertEquals("Sprite", service.findById(1).getNume());
    }

    @Test
    void testAddProduct_IntegrationVR_EndToEndFailure() {
        Product p = new Product(-5, "Apa", -2.0, null, null);

        assertThrows(ValidationException.class, () -> {
            service.addProduct(p);
        });

        // Verificăm că baza de date/fișierul a rămas gol
        assertTrue(service.getAllProducts().isEmpty());
    }
}