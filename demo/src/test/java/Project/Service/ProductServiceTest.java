package Project.Service;

import Project.Exception.ResourceNotFoundException;
import Project.Model.DTO.Request.ProductRequestDTO;
import Project.Model.DTO.Response.ProductResponseDTO;
import Project.Model.Entity.Brand;
import Project.Model.Entity.Category;
import Project.Model.Entity.Product;
import Project.Repository.BrandRepository;
import Project.Repository.CategoryRepository;
import Project.Repository.ProductRepository;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private BrandRepository brandRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductService productService;


    // =========================
    // HELPER METHODS
    // =========================

    // có thể dùng @BeforeEach
    private Brand createBrand() {
        Brand brand = new Brand();
        brand.setId(1);
        brand.setName("Nike");
        brand.setDescription("Nike brand");
        return brand;
    }

    private Category createCategory() {
        Category category = new Category();
        category.setId(2);
        category.setName("Shoes");
        category.setDescription("Shoes category");
        return category;
    }

    private Product createProduct() {
        Product product = new Product();
        product.setId(10);
        product.setName("Nike Air Max");
        product.setDescription("Running shoes");
        product.setPrice(2000000);
        product.setQuantity(5);
        product.setImage("nike.jpg");
        product.setBrand(createBrand());
        product.setCategory(createCategory());
        return product;
    }

    private ProductRequestDTO createProductRequestDTO() {
        ProductRequestDTO request = new ProductRequestDTO();
        request.setName("Nike Air Max");
        request.setDescription("Running shoes");
        request.setPrice(2000000);
        request.setQuantity(5);
        request.setImage("nike.jpg");
        request.setBrandId(1);
        request.setCategoryId(2);
        return request;
    }


    // =========================
    // 1. convertProductToDTO()
    // =========================

    @Test
    void convertProductToDTO_shouldReturnCorrectDTO() {

        // Arrange
        Product product = createProduct();

        // Act
        ProductResponseDTO result =
                productService.convertProductToDTO(product);

        // Assert
        assertNotNull(result);

        assertEquals(10, result.getId());
        assertEquals("Nike Air Max", result.getName());
        assertEquals("Running shoes", result.getDescription());
        assertEquals(2000000, result.getPrice());
        assertEquals(5, result.getQuantity());
        assertEquals("nike.jpg", result.getImage());

        assertEquals(1, result.getBrand().getId());
        assertEquals("Nike", result.getBrand().getName());
        assertEquals("Nike brand", result.getBrand().getDescription());

        assertEquals(2, result.getCategory().getId());
        assertEquals("Shoes", result.getCategory().getName());
        assertEquals("Shoes category", result.getCategory().getDescription());
    }


    // =========================
    // 2. converDTOtoProduct()
    // =========================

    @Test
    void converDTOtoProduct_shouldReturnProductWhenBrandAndCategoryExist() {

        // Arrange
        ProductRequestDTO request = createProductRequestDTO();

        Brand brand = createBrand();
        Category category = createCategory();

        when(brandRepository.findById(1))
                .thenReturn(Optional.of(brand));

        when(categoryRepository.findById(2))
                .thenReturn(Optional.of(category));

        // Act
        Product result = productService.converDTOtoProduct(request);

        // Assert
        assertNotNull(result);

        assertEquals("Nike Air Max", result.getName());
        assertEquals("Running shoes", result.getDescription());
        assertEquals(2000000, result.getPrice());
        assertEquals(5, result.getQuantity());
        assertEquals("nike.jpg", result.getImage());

        assertEquals(brand, result.getBrand());
        assertEquals(category, result.getCategory());

        verify(brandRepository).findById(1);
        verify(categoryRepository).findById(2);
    }

    @Test
    void converDTOtoProduct_shouldThrowExceptionWhenBrandNotFound() {

        // Arrange
        ProductRequestDTO request = createProductRequestDTO();

        when(brandRepository.findById(1))
                .thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(
                ResourceNotFoundException.class,
                () -> productService.converDTOtoProduct(request)
        );

        verify(brandRepository).findById(1);
        verify(categoryRepository, never()).findById(anyInt());
    }

    @Test
    void converDTOtoProduct_shouldThrowExceptionWhenCategoryNotFound() {

        // Arrange
        ProductRequestDTO request = createProductRequestDTO();

        when(brandRepository.findById(1))
                .thenReturn(Optional.of(createBrand()));

        when(categoryRepository.findById(2))
                .thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(
                ResourceNotFoundException.class,
                () -> productService.converDTOtoProduct(request)
        );

        verify(brandRepository).findById(1);
        verify(categoryRepository).findById(2);
    }


    // =========================
    // 3. createProduct()
    // =========================

    @Test
    void createProduct_shouldReturnDTOWhenCreatedSuccessfully() {

        // Arrange
        ProductRequestDTO request = createProductRequestDTO();

        when(brandRepository.findById(1))
                .thenReturn(Optional.of(createBrand()));

        when(categoryRepository.findById(2))
                .thenReturn(Optional.of(createCategory()));

        // Giả lập save() trả về chính Product được truyền vào
        when(productRepository.save(any(Product.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ProductResponseDTO result = productService.createProduct(request);

        // Assert
        assertNotNull(result);

        assertEquals("Nike Air Max", result.getName());
        assertEquals(2000000, result.getPrice());
        assertEquals("Nike", result.getBrand().getName());
        assertEquals("Shoes", result.getCategory().getName());

        verify(brandRepository).findById(1);
        verify(categoryRepository).findById(2);
        verify(productRepository).save(any(Product.class));
    }


    // =========================
    // 4. fetchProducts()
    // =========================

    @Test
    void fetchProducts_shouldReturnProductPage() {

        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        Product product = createProduct();

        Page<Product> productPage =
                new PageImpl<>(List.of(product), pageable, 1);

        when(productRepository.findAll(pageable))
                .thenReturn(productPage);

        // Act
        Page<ProductResponseDTO> result =
                productService.fetchProducts(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());

        ProductResponseDTO dto = result.getContent().get(0);

        assertEquals(10, dto.getId());
        assertEquals("Nike Air Max", dto.getName());
        assertEquals("Nike", dto.getBrand().getName());

        verify(productRepository).findAll(pageable);
    }


    // =========================
    // 5. fetchProductById()
    // =========================

    @Test
    void fetchProductById_shouldReturnDTOWhenProductExists() {

        // Arrange
        Product product = createProduct();

        when(productRepository.findById(10))
                .thenReturn(Optional.of(product));

        // Act
        ProductResponseDTO result =
                productService.fetchProductById(10);

        // Assert
        assertNotNull(result);

        assertEquals(10, result.getId());
        assertEquals("Nike Air Max", result.getName());
        assertEquals(2000000, result.getPrice());

        verify(productRepository).findById(10);
    }

    @Test
    void fetchProductById_shouldThrowExceptionWhenProductNotFound() {

        // Arrange
        when(productRepository.findById(999))
                .thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(
                ResourceNotFoundException.class,
                () -> productService.fetchProductById(999)
        );

        verify(productRepository).findById(999);
    }


    // =========================
    // 6. searchProducts()
    // =========================

    @Test
    void searchProducts_shouldReturnMatchingProducts() {

        // Arrange
        String keyword = "Nike";
        String brand = "Nike";
        Integer minPrice = 1000000;
        Integer maxPrice = 3000000;
        String type = "Shoes";

        List<Product> products = List.of(createProduct());

        when(productRepository.searchProducts(
                keyword, brand, minPrice, maxPrice, type
        )).thenReturn(products);

        // Act
        List<ProductResponseDTO> result =
                productService.searchProducts(
                        keyword, brand, minPrice, maxPrice, type
                );

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

        assertEquals("Nike Air Max", result.get(0).getName());
        assertEquals("Nike", result.get(0).getBrand().getName());

        verify(productRepository).searchProducts(
                keyword, brand, minPrice, maxPrice, type
        );
    }

    @Test
    void searchProducts_shouldReturnEmptyListWhenNoProductsFound() {

        // Arrange
        when(productRepository.searchProducts(
                "Unknown", null, null, null, null
        )).thenReturn(List.of());

        // Act
        List<ProductResponseDTO> result =
                productService.searchProducts(
                        "Unknown", null, null, null, null
                );

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(productRepository).searchProducts(
                "Unknown", null, null, null, null
        );
    }


    // =========================
    // 7. fetchProductByName()
    // =========================

    @Test
    void fetchProductByName_shouldReturnProductsWithMatchingName() {

        // Arrange
        Product product = createProduct();

        when(productRepository.findByName("Nike Air Max"))
                .thenReturn(List.of(product));

        // Act
        List<ProductResponseDTO> result =
                productService.fetchProductByName("Nike Air Max");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

        assertEquals("Nike Air Max", result.get(0).getName());

        verify(productRepository).findByName("Nike Air Max");
    }

    @Test
    void fetchProductByName_shouldReturnEmptyListWhenNoProductsFound() {

        // Arrange
        when(productRepository.findByName("Unknown"))
                .thenReturn(List.of());

        // Act
        List<ProductResponseDTO> result =
                productService.fetchProductByName("Unknown");

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(productRepository).findByName("Unknown");
    }


    // =========================
    // 8. fetchProductWithBrandAndCategory()
    // =========================

    @Test
    void fetchProductWithBrandAndCategory_shouldReturnMatchingProducts() {

        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        Page<Product> productPage =
                new PageImpl<>(
                        List.of(createProduct()),
                        pageable,
                        1
                );

        when(productRepository.findByBrand_nameAndCategory_name(
                "Nike", "Shoes", pageable
        )).thenReturn(productPage);

        // Act
        Page<ProductResponseDTO> result =
                productService.fetchProductWithBrandAndCategory(
                        "Nike", "Shoes", pageable
                );

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());

        assertEquals(
                "Nike",
                result.getContent().get(0).getBrand().getName()
        );

        assertEquals(
                "Shoes",
                result.getContent().get(0).getCategory().getName()
        );

        verify(productRepository).findByBrand_nameAndCategory_name(
                "Nike", "Shoes", pageable
        );
    }


    // =========================
    // 9. fetchProductWithBrand()
    // =========================

    @Test
    void fetchProductWithBrand_shouldReturnProductsOfBrand() {

        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        Page<Product> productPage =
                new PageImpl<>(
                        List.of(createProduct()),
                        pageable,
                        1
                );

        when(productRepository.findByBrand_name("Nike", pageable))
                .thenReturn(productPage);

        // Act
        Page<ProductResponseDTO> result =
                productService.fetchProductWithBrand("Nike", pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());

        assertEquals(
                "Nike",
                result.getContent().get(0).getBrand().getName()
        );

        verify(productRepository).findByBrand_name("Nike", pageable);
    }


    // =========================
    // 10. fetchProductWithCategory()
    // =========================

    @Test
    void fetchProductWithCategory_shouldReturnProductsOfCategory() {

        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        Page<Product> productPage =
                new PageImpl<>(
                        List.of(createProduct()),
                        pageable,
                        1
                );

        when(productRepository.findByCategory_name("Shoes", pageable))
                .thenReturn(productPage);

        // Act
        Page<ProductResponseDTO> result =
                productService.fetchProductWithCategory("Shoes", pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());

        assertEquals(
                "Shoes",
                result.getContent().get(0).getCategory().getName()
        );

        verify(productRepository).findByCategory_name("Shoes", pageable);
    }


    // =========================
    // 11. updateProductById()
    // =========================

    @Test
    void updateProductById_shouldUpdateProductSuccessfully() {

        // Arrange
        Product existingProduct = createProduct();
        ProductRequestDTO updateRequest = createProductRequestDTO();

        updateRequest.setName("Nike Air Max Updated");
        updateRequest.setPrice(2500000);
        updateRequest.setQuantity(10);

        Brand newBrand = createBrand();
        Category newCategory = createCategory();

        when(productRepository.findById(10))
                .thenReturn(Optional.of(existingProduct));

        when(brandRepository.findById(1))
                .thenReturn(Optional.of(newBrand));

        when(categoryRepository.findById(2))
                .thenReturn(Optional.of(newCategory));

        when(productRepository.save(any(Product.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ProductResponseDTO result =
                productService.updateProductById(10, updateRequest);

        // Assert
        assertNotNull(result);

        assertEquals(10, result.getId());
        assertEquals("Nike Air Max Updated", result.getName());
        assertEquals(2500000, result.getPrice());
        assertEquals(10, result.getQuantity());

        assertEquals("Nike", result.getBrand().getName());
        assertEquals("Shoes", result.getCategory().getName());

        verify(productRepository).findById(10);
        verify(brandRepository).findById(1);
        verify(categoryRepository).findById(2);
        verify(productRepository).save(existingProduct);
    }

    @Test
    void updateProductById_shouldThrowExceptionWhenProductNotFound() {

        // Arrange
        ProductRequestDTO request = createProductRequestDTO();

        when(productRepository.findById(999))
                .thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(
                ResourceNotFoundException.class,
                () -> productService.updateProductById(999, request)
        );

        verify(productRepository).findById(999);

        verify(brandRepository, never()).findById(anyInt());
        verify(categoryRepository, never()).findById(anyInt());
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void updateProductById_shouldThrowExceptionWhenBrandNotFound() {

        // Arrange
        ProductRequestDTO request = createProductRequestDTO();

        when(productRepository.findById(10))
                .thenReturn(Optional.of(createProduct()));

        when(brandRepository.findById(1))
                .thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(
                ResourceNotFoundException.class,
                () -> productService.updateProductById(10, request)
        );

        verify(productRepository).findById(10);
        verify(brandRepository).findById(1);

        verify(categoryRepository, never()).findById(anyInt());
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void updateProductById_shouldThrowExceptionWhenCategoryNotFound() {

        // Arrange
        ProductRequestDTO request = createProductRequestDTO();

        when(productRepository.findById(10))
                .thenReturn(Optional.of(createProduct()));

        when(brandRepository.findById(1))
                .thenReturn(Optional.of(createBrand()));

        when(categoryRepository.findById(2))
                .thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(
                ResourceNotFoundException.class,
                () -> productService.updateProductById(10, request)
        );

        verify(productRepository).findById(10);
        verify(brandRepository).findById(1);
        verify(categoryRepository).findById(2);

        verify(productRepository, never()).save(any(Product.class));
    }


    // =========================
    // 12. deleteProductById()
    // =========================

    @Test
    void deleteProductById_shouldDeleteProductWhenExists() {

        // Arrange
        Product product = createProduct();

        when(productRepository.findById(10))
                .thenReturn(Optional.of(product));

        // Act
        assertDoesNotThrow(
                () -> productService.deleteProductById(10)
        );

        // Assert
        verify(productRepository).findById(10);
        verify(productRepository).delete(product);
    }

    @Test
    void deleteProductById_shouldThrowExceptionWhenProductNotFound() {

        // Arrange
        when(productRepository.findById(999))
                .thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(
                ResourceNotFoundException.class,
                () -> productService.deleteProductById(999)
        );

        verify(productRepository).findById(999);
        verify(productRepository, never()).delete(any(Product.class));
    }
}
