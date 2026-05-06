package mydrinkshop;

import drinkshop.domain.Product;
import drinkshop.repository.Repository;
import drinkshop.service.ProductService;
import drinkshop.service.validator.ProductValidator;
import drinkshop.service.validator.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static drinkshop.domain.CategorieBautura.CLASSIC_COFFEE;
import static drinkshop.domain.TipBautura.WATER_BASED;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductServiceUnitTest {

    @Mock
    private ProductValidator validator;

    @Mock
    private Repository<Integer, Product> productRepo;

    @InjectMocks
    private ProductService service;

    @BeforeEach
    void setUp() {
        // Initializeaza mock-urile si le injecteaza în service
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testAddProduct_ValidProduct_SavesToRepo() {
        Product validProduct = new Product(1, "Cola", 5.5, null, null);

        doNothing().when(validator).validate(validProduct);

        service.addProduct(validProduct);

        // Verificam dacaf validatorul a fost apelat o singura data
        verify(validator, times(1)).validate(validProduct);
        // Verificam daca metoda save din repository a fost apelata o singura data
        verify(productRepo, times(1)).save(validProduct);
    }

    @Test
    void testAddProduct_InvalidProduct_ThrowsExceptionAndDoesNotSave() {
        // Arrange
        Product invalidProduct = new Product(-1, "", -5.0,CLASSIC_COFFEE,WATER_BASED);

        // Definim comportamentul: Validatorul arunca exceptie pentru produs invalid
        doThrow(new ValidationException("ID invalid!\nNumele nu poate fi gol!\nPret invalid!\n"))
                .when(validator).validate(invalidProduct);

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            service.addProduct(invalidProduct);
        });

        assertTrue(exception.getMessage().contains("ID invalid!"));

        // Ne asiguram ca repository-ul NU a fost atins daca validarea a picat
        verify(validator, times(1)).validate(invalidProduct);
        verify(productRepo, never()).save(any(Product.class));
    }
}