package mydrinkshop;

import drinkshop.domain.CategorieBautura;
import drinkshop.domain.Product;
import drinkshop.domain.TipBautura;
import drinkshop.repository.AbstractRepository;
import drinkshop.service.ProductService;
import drinkshop.service.validator.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ProductService.updateProduct - ECP & BVA")
@Tag("unit")
class ProductServiceTest {

    private static final int EXISTING_ID = 1;

    private static Stream<Arguments> ecpCases() {
        return Stream.of(
                Arguments.of("VV: nume valid, pret valid", "Cappuccino", 14.5, true, ""),
                Arguments.of("IV: nume invalid, pret valid", "", 14.5, false, "Numele nu poate fi gol!"),
                Arguments.of("VI: nume valid, pret invalid", "Latte", 0.0, false, "Pret invalid!"),
                Arguments.of("II: nume invalid, pret invalid", "   ", -2.0, false, "Numele nu poate fi gol!|Pret invalid!")
        );
    }

    private static TestContext createContext() {
        InMemoryProductRepository repo = new InMemoryProductRepository();
        ProductService service = new ProductService(repo);

        repo.save(new Product(
                EXISTING_ID,
                "Initial",
                10.0,
                CategorieBautura.CLASSIC_COFFEE,
                TipBautura.BASIC
        ));

        return new TestContext(service, repo);
    }

    @Nested
    @DisplayName("ECP")
    class EcpTests {

        @ParameterizedTest(name = "{0}")
        @MethodSource("mydrinkshop.ProductServiceTest#ecpCases")
        @Tag("ecp")
        void shouldValidateEquivalenceClassesOnUpdate(String caseName,
                                                      String name,
                                                      double price,
                                                      boolean expectedValid,
                                                      String expectedErrors) {
            // Arrange
            TestContext context = createContext();

            // Act + Assert
            if (expectedValid) {
                assertDoesNotThrow(() -> context.service.updateProduct(
                        EXISTING_ID,
                        name,
                        price,
                        CategorieBautura.TEA,
                        TipBautura.WATER_BASED
                ));

                Product updated = context.repo.findOne(EXISTING_ID);
                assertEquals(name, updated.getNume());
                assertEquals(price, updated.getPret());
            } else {
                ValidationException ex = assertThrows(ValidationException.class, () -> context.service.updateProduct(
                        EXISTING_ID,
                        name,
                        price,
                        CategorieBautura.TEA,
                        TipBautura.WATER_BASED
                ));

                for (String expectedError : expectedErrors.split("\\|")) {
                    assertTrue(ex.getMessage().contains(expectedError));
                }

                Product unchanged = context.repo.findOne(EXISTING_ID);
                assertEquals("Initial", unchanged.getNume());
                assertEquals(10.0, unchanged.getPret());
            }
        }
    }

    @Nested
    @DisplayName("BVA")
    class BvaTests {

        @ParameterizedTest(name = "{0}")
        @CsvSource({
                "Pret la limita inferioara (invalid), Espresso, 0.00, false, Pret invalid!",
                "Pret imediat peste limita (valid), Espresso, 0.01, true, ''",
                "Nume gol la limita inferioara (invalid), '', 10.00, false, Numele nu poate fi gol!",
                "Nume cu lungime minima (valid), A, 10.00, true, ''"
        })
        @Tag("bva")
        void shouldValidateBoundaryValuesOnUpdate(String caseName,
                                                  String name,
                                                  double price,
                                                  boolean expectedValid,
                                                  String expectedError) {
            // Arrange
            TestContext context = createContext();

            // Act + Assert
            if (expectedValid) {
                assertDoesNotThrow(() -> context.service.updateProduct(
                        EXISTING_ID,
                        name,
                        price,
                        CategorieBautura.TEA,
                        TipBautura.WATER_BASED
                ));

                Product updated = context.repo.findOne(EXISTING_ID);
                assertEquals(name, updated.getNume());
                assertEquals(price, updated.getPret());
            } else {
                ValidationException ex = assertThrows(ValidationException.class, () -> context.service.updateProduct(
                        EXISTING_ID,
                        name,
                        price,
                        CategorieBautura.TEA,
                        TipBautura.WATER_BASED
                ));

                assertTrue(ex.getMessage().contains(expectedError));

                Product unchanged = context.repo.findOne(EXISTING_ID);
                assertEquals("Initial", unchanged.getNume());
                assertEquals(10.0, unchanged.getPret());
            }
        }
    }

    private record TestContext(ProductService service, InMemoryProductRepository repo) {
    }

    private static final class InMemoryProductRepository extends AbstractRepository<Integer, Product> {
        @Override
        protected Integer getId(Product entity) {
            return entity.getId();
        }
    }
}

