
package Project.Service;

import Project.Exception.ResourceAlreadyExistsException;
import Project.Exception.ResourceNotFoundException;
import Project.Model.DTO.Response.BrandDTO;
import Project.Model.Entity.Brand;
import Project.Repository.BrandRepository;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BrandServiceTest {

    @Mock
    private BrandRepository brandRepository;

    @InjectMocks
    private BrandService brandService;

    private Brand brand;

    @BeforeEach
    void setUp() {
        brand = new Brand();
        brand.setId(1);
        brand.setName("Nike");
        brand.setDescription("Sports brand");
    }

    // =========================================
    // 1. CREATE BRAND
    // =========================================

    @Test
    void createBrand_shouldReturnBrandWhenCreatedSuccessfully() {

        // Arrange
        when(brandRepository.existsByName("Nike"))
                .thenReturn(false);

        when(brandRepository.save(any(Brand.class)))
                .thenReturn(brand);

        // Act
        Brand result = brandService.createBrand(brand);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("Nike", result.getName());

        verify(brandRepository).existsByName("Nike");
        verify(brandRepository).save(brand);
    }

    @Test
    void createBrand_shouldThrowExceptionWhenBrandAlreadyExists() {

        // Arrange
        when(brandRepository.existsByName("Nike"))
                .thenReturn(true);

        // Act & Assert
        ResourceAlreadyExistsException exception =
                assertThrows(
                        ResourceAlreadyExistsException.class,
                        () -> brandService.createBrand(brand)
                );

        assertTrue(exception.getMessage().contains("Nike"));

        verify(brandRepository, never()).save(any(Brand.class));
    }

    // =========================================
    // 2. FETCH ALL BRANDS
    // =========================================

    @Test
    void fetchBrands_shouldReturnListOfBrandDTO() {

        // Arrange
        Brand brand2 = new Brand();
        brand2.setId(2);
        brand2.setName("Adidas");
        brand2.setDescription("German sports brand");

        when(brandRepository.findAll())
                .thenReturn(List.of(brand, brand2));

        // Act
        List<BrandDTO> result = brandService.fetchBrands();

        // Assert
        assertEquals(2, result.size());

        assertAll(
                () -> assertEquals(1, result.get(0).getId()),
                () -> assertEquals("Nike", result.get(0).getName()),
                () -> assertEquals("Sports brand", result.get(0).getDescription()),

                () -> assertEquals(2, result.get(1).getId()),
                () -> assertEquals("Adidas", result.get(1).getName())
        );

        verify(brandRepository).findAll();
    }

    @Test
    void fetchBrands_shouldReturnEmptyListWhenNoBrandExists() {

        // Arrange
        when(brandRepository.findAll())
                .thenReturn(List.of());

        // Act
        List<BrandDTO> result = brandService.fetchBrands();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(brandRepository).findAll();
    }

    // =========================================
    // 3. FETCH BRAND BY ID
    // =========================================

    @Test
    void fetchBrandById_shouldReturnBrandDTOWhenFound() {

        // Arrange
        when(brandRepository.findById(1))
                .thenReturn(Optional.of(brand));

        // Act
        BrandDTO result = brandService.fetchBrandById(1);

        // Assert
        assertNotNull(result);

        assertAll(
                () -> assertEquals(1, result.getId()),
                () -> assertEquals("Nike", result.getName()),
                () -> assertEquals("Sports brand", result.getDescription())
        );

        verify(brandRepository).findById(1);
    }

    @Test
    void fetchBrandById_shouldThrowExceptionWhenNotFound() {

        // Arrange
        when(brandRepository.findById(99))
                .thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> brandService.fetchBrandById(99)
                );

        assertTrue(exception.getMessage().contains("99"));

        verify(brandRepository).findById(99);
    }

    // =========================================
    // 4. UPDATE BRAND BY ID
    // =========================================

    @Test
    void updateBrandById_shouldUpdateDescriptionWhenNameUnchanged() {

        // Arrange
        Brand updateBrand = new Brand();
        updateBrand.setName("Nike");
        updateBrand.setDescription("Updated description");

        when(brandRepository.findById(1))
                .thenReturn(Optional.of(brand));

        when(brandRepository.save(any(Brand.class)))
                .thenReturn(brand);

        // Act
        BrandDTO result = brandService.updateBrandById(1, updateBrand);

        // Assert
        assertAll(
                () -> assertEquals(1, result.getId()),
                () -> assertEquals("Nike", result.getName()),
                () -> assertEquals("Updated description", result.getDescription())
        );

        verify(brandRepository).save(brand);

        verify(brandRepository, never())
                .existsByNameAndIdNot(anyString(), anyInt());
    }

    @Test
    void updateBrandById_shouldUpdateNameAndDescriptionWhenNameChanged() {

        // Arrange
        Brand updateBrand = new Brand();
        updateBrand.setName("Adidas");
        updateBrand.setDescription("New description");

        when(brandRepository.findById(1))
                .thenReturn(Optional.of(brand));

        when(brandRepository.existsByNameAndIdNot("Adidas", 1))
                .thenReturn(false);

        when(brandRepository.save(any(Brand.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        BrandDTO result = brandService.updateBrandById(1, updateBrand);

        // Assert
        assertAll(
                () -> assertEquals(1, result.getId()),
                () -> assertEquals("Adidas", result.getName()),
                () -> assertEquals("New description", result.getDescription())
        );

        verify(brandRepository).existsByNameAndIdNot("Adidas", 1);
        verify(brandRepository).save(brand);
    }

    @Test
    void updateBrandById_shouldThrowExceptionWhenNewNameAlreadyExists() {

        // Arrange
        Brand updateBrand = new Brand();
        updateBrand.setName("Adidas");
        updateBrand.setDescription("New description");

        when(brandRepository.findById(1))
                .thenReturn(Optional.of(brand));

        when(brandRepository.existsByNameAndIdNot("Adidas", 1))
                .thenReturn(true);

        // Act & Assert
        ResourceAlreadyExistsException exception =
                assertThrows(
                        ResourceAlreadyExistsException.class,
                        () -> brandService.updateBrandById(1, updateBrand)
                );

        assertTrue(exception.getMessage().contains("Adidas"));

        verify(brandRepository, never()).save(any(Brand.class));
    }

    @Test
    void updateBrandById_shouldThrowExceptionWhenBrandNotFound() {

        // Arrange
        Brand updateBrand = new Brand();
        updateBrand.setName("Adidas");
        updateBrand.setDescription("New description");

        when(brandRepository.findById(99))
                .thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> brandService.updateBrandById(99, updateBrand)
                );

        assertTrue(exception.getMessage().contains("99"));

        verify(brandRepository, never()).save(any(Brand.class));
    }

    // =========================================
    // 5. DELETE BRAND BY ID
    // =========================================

    @Test
    void deleteBrandById_shouldDeleteWhenBrandExists() {

        // Arrange
        when(brandRepository.existsById(1))
                .thenReturn(true);

        // Act
        brandService.deleteBrandById(1);

        // Assert
        verify(brandRepository).existsById(1);
        verify(brandRepository).deleteById(1);
    }

    @Test
    void deleteBrandById_shouldThrowExceptionWhenBrandNotFound() {

        // Arrange
        when(brandRepository.existsById(99))
                .thenReturn(false);

        // Act & Assert
        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> brandService.deleteBrandById(99)
                );

        assertTrue(exception.getMessage().contains("99"));

        verify(brandRepository, never()).deleteById(anyInt());
    }

    // =========================================
    // 6. GET BRAND NAMES
    // =========================================

    @Test
    void getBrandNames_shouldReturnAllBrandNames() {

        // Arrange
        Brand brand2 = new Brand();
        brand2.setId(2);
        brand2.setName("Adidas");
        brand2.setDescription("German sports brand");

        when(brandRepository.findAll())
                .thenReturn(List.of(brand, brand2));

        // Act
        List<String> result = brandService.getBrandNames();

        // Assert
        assertEquals(2, result.size());
        assertEquals(List.of("Nike", "Adidas"), result);

        verify(brandRepository).findAll();
    }

    @Test
    void getBrandNames_shouldReturnEmptyListWhenNoBrandExists() {

        // Arrange
        when(brandRepository.findAll())
                .thenReturn(List.of());

        // Act
        List<String> result = brandService.getBrandNames();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(brandRepository).findAll();
    }
}
