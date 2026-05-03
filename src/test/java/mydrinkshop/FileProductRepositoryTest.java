package mydrinkshop;

import drinkshop.domain.CategorieBautura;
import drinkshop.domain.Product;
import drinkshop.domain.TipBautura;
import drinkshop.repository.file.FileAbstractRepository;
import drinkshop.service.ProductService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
//Equivalence Class Partitioning, Boundary Value Analisis
@DisplayName("FileProductRepository - ECP & BVA")
@Tag("unit")
class FileProductRepositoryTest {

    private static final String SEP = ";";
    private static Path tempDir;
    private Path testFile;
    private TestableFileProductRepository repo;
    ProductService service = new ProductService(repo,null);

    @BeforeAll
    static void setupSuite() throws IOException {
        tempDir = Files.createTempDirectory("file-product-repo-tests");
    }

    @BeforeEach
    void setup() throws IOException {
        // Arrange: fisier separat per test (izolare)
        testFile = Files.createTempFile(tempDir, "products-", ".txt");
        repo = new TestableFileProductRepository(testFile.toString());
    }

    @AfterEach
    void cleanup() throws IOException {
        Files.deleteIfExists(testFile);
    }

    @Nested
    @DisplayName("ECP")
    class EcpTests {

        @Test
        @DisplayName("VV: save entity valida -> exista in memorie si in fisier")
        @Tag("ecp")
        void shouldSaveValidEntity() throws IOException {
            // Arrange
            Product p = new Product(1, "Espresso", 12.5, CategorieBautura.CLASSIC_COFFEE, TipBautura.BASIC);

            // Act
            Product saved = repo.save(p);
            List<String> lines = Files.readAllLines(testFile);

            // Assert
            assertEquals(p, saved);
            assertEquals("Espresso", repo.findOne(1).getNume());
            assertEquals(1, lines.size());
            assertTrue(lines.get(0).contains("Espresso"));
        }

        @Test
        @DisplayName("IV: save null -> NullPointerException, fisier nemodificat")
        @Tag("ecp")
        void shouldThrowWhenSavingNullEntity() throws IOException {
            // Arrange
            Files.writeString(testFile, "", StandardOpenOption.TRUNCATE_EXISTING);
            int beforeSize = Files.readAllLines(testFile).size();

            // Act + Assert
            try {
                repo.save(null);
            } catch (NullPointerException expected) {
                // expected
            }

            int afterSize = Files.readAllLines(testFile).size();
            assertEquals(beforeSize, afterSize);
        }

        @Test
        @DisplayName("VI: delete id existent -> elimina din memorie si fisier")
        @Tag("ecp")
        void shouldDeleteExistingId() throws IOException {
            // Arrange
            repo.save(new Product(2, "Latte", 15.0, CategorieBautura.CLASSIC_COFFEE, TipBautura.PLANT_BASED));

            // Act
            Product deleted = repo.delete(2);

            // Assert
            assertEquals("Latte", deleted.getNume());
            assertNull(repo.findOne(2));
            assertEquals(0, Files.readAllLines(testFile).size());
        }

        @Test
        @DisplayName("II: delete id inexistent -> null, fisier neschimbat")
        @Tag("ecp")
        void shouldReturnNullWhenDeletingMissingId() throws IOException {
            // Arrange
            repo.save(new Product(3, "Tea", 9.0, CategorieBautura.TEA, TipBautura.WATER_BASED));
            List<String> before = Files.readAllLines(testFile);

            // Act
            Product deleted = repo.delete(999);

            // Assert
            assertNull(deleted);
            assertEquals(before, Files.readAllLines(testFile));
            assertEquals("Tea", repo.findOne(3).getNume());
        }
    }

    @Nested
    @DisplayName("BVA")
    class BvaTests {

        @Test
        @DisplayName("Valid: id la limita inferioara (0)")
        @Tag("bva")
        void shouldHandleIdBoundaryZero() {
            // Arrange
            Product p = new Product(0, "Boundary0", 10.0, CategorieBautura.TEA, TipBautura.WATER_BASED);

            // Act
            repo.save(p);

            // Assert
            assertEquals("Boundary0", repo.findOne(0).getNume());
        }

        @Test
        @DisplayName("Valid: id imediat peste limita (1)")
        @Tag("bva")
        void shouldHandleIdBoundaryOne() {
            // Arrange
            Product p = new Product(1, "Boundary1", 10.0, CategorieBautura.TEA, TipBautura.WATER_BASED);

            // Act
            repo.save(p);

            // Assert
            assertEquals("Boundary1", repo.findOne(1).getNume());
        }

        @Test
        @DisplayName("Invalid: pret la limita inferioara (0.0) - repository persista fara validare")
        @Tag("bva")
        void shouldPersistPriceAtZeroBoundary() {
            // Arrange
            Product p = new Product(10, "ZeroPrice", 0.0, CategorieBautura.TEA, TipBautura.WATER_BASED);

            // Act
            repo.save(p);

            // Assert
            assertEquals(0.0, repo.findOne(10).getPret());
        }

        @Test
        @DisplayName("Valid: pret imediat peste limita (0.01)")
        @Tag("bva")
        void shouldPersistPriceJustAboveBoundary() {
            // Arrange
            Product p = new Product(11, "MinPositivePrice", 0.01, CategorieBautura.TEA, TipBautura.WATER_BASED);

            // Act
            repo.save(p);

            // Assert
            assertEquals(0.01, repo.findOne(11).getPret());
        }
    }

    /**
     * Repository minim pentru test: format linie = id;nume;pret;categorie;tip
     */
    private static final class TestableFileProductRepository extends FileAbstractRepository<Integer, Product> {

        TestableFileProductRepository(String fileName) {
            super(fileName);
            loadFromFile();
        }

        @Override
        protected Integer getId(Product entity) {
            return entity.getId();
        }

        @Override
        protected Product extractEntity(String line) {
            String[] parts = line.split(SEP);
            return new Product(
                    Integer.parseInt(parts[0].trim()),
                    parts[1].trim(),
                    Double.parseDouble(parts[2].trim()),
                    CategorieBautura.valueOf(parts[3].trim()),
                    TipBautura.valueOf(parts[4].trim())
            );
        }

        @Override
        protected String createEntityAsString(Product entity) {
            return entity.getId() + SEP
                    + entity.getNume() + SEP
                    + entity.getPret() + SEP
                    + entity.getCategorie() + SEP
                    + entity.getTip();
        }
    }
}
