
package Project.Service;

import Project.Exception.ResourceAlreadyExistsException;
import Project.Exception.ResourceNotFoundException;
import Project.Model.DTO.Response.CategoryDTO;
import Project.Model.Entity.Category;
import Project.Repository.CategoryRepository;

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
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    private Category category;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setId(1);
        category.setName("Shoes");
        category.setDescription("All types of shoes");
    }

    // =========================================
    // 1. CREATE CATEGORY
    // =========================================

    @Test
    void createCategory_shouldReturnCategoryWhenCreatedSuccessfully() {

        // Arrange
        when(categoryRepository.existsByName("Shoes"))
                .thenReturn(false);

        when(categoryRepository.save(any(Category.class)))
                .thenReturn(category);

        // Act
        Category result = categoryService.createCategory(category);

        // Assert
        assertNotNull(result);

        assertAll(
                () -> assertEquals(1, result.getId()),
                () -> assertEquals("Shoes", result.getName()),
                () -> assertEquals("All types of shoes", result.getDescription())
        );

        verify(categoryRepository).existsByName("Shoes");
        verify(categoryRepository).save(category);
    }

    @Test
    void createCategory_shouldThrowExceptionWhenCategoryAlreadyExists() {

        // Arrange
        when(categoryRepository.existsByName("Shoes"))
                .thenReturn(true);

        // Act & Assert
        ResourceAlreadyExistsException exception =
                assertThrows(
                        ResourceAlreadyExistsException.class,
                        () -> categoryService.createCategory(category)
                );

        assertTrue(exception.getMessage().contains("Shoes"));

        verify(categoryRepository, never()).save(any(Category.class));
    }

    // =========================================
    // 2. FETCH ALL CATEGORIES
    // =========================================

    @Test
    void fetchCategories_shouldReturnListOfCategoryDTO() {

        // Arrange
        Category category2 = new Category();
        category2.setId(2);
        category2.setName("Clothes");
        category2.setDescription("All types of clothes");

        when(categoryRepository.findAll())
                .thenReturn(List.of(category, category2));

        // Act
        List<CategoryDTO> result = categoryService.fetchCategories();

        // Assert
        assertEquals(2, result.size());

        assertAll(
                () -> assertEquals(1, result.get(0).getId()),
                () -> assertEquals("Shoes", result.get(0).getName()),
                () -> assertEquals("All types of shoes", result.get(0).getDescription()),

                () -> assertEquals(2, result.get(1).getId()),
                () -> assertEquals("Clothes", result.get(1).getName()),
                () -> assertEquals("All types of clothes", result.get(1).getDescription())
        );

        verify(categoryRepository).findAll();
    }

    @Test
    void fetchCategories_shouldReturnEmptyListWhenNoCategoryExists() {

        // Arrange
        when(categoryRepository.findAll())
                .thenReturn(List.of());

        // Act
        List<CategoryDTO> result = categoryService.fetchCategories();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(categoryRepository).findAll();
    }

    // =========================================
    // 3. FETCH CATEGORY BY ID
    // =========================================

    @Test
    void fetchCategoryById_shouldReturnCategoryDTOWhenFound() {

        // Arrange
        when(categoryRepository.findById(1))
                .thenReturn(Optional.of(category));

        // Act
        CategoryDTO result = categoryService.fetchCategoryById(1);

        // Assert
        assertNotNull(result);

        assertAll(
                () -> assertEquals(1, result.getId()),
                () -> assertEquals("Shoes", result.getName()),
                () -> assertEquals("All types of shoes", result.getDescription())
        );

        verify(categoryRepository).findById(1);
    }

    @Test
    void fetchCategoryById_shouldThrowExceptionWhenNotFound() {

        // Arrange
        when(categoryRepository.findById(99))
                .thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> categoryService.fetchCategoryById(99)
                );

        assertTrue(exception.getMessage().contains("99"));

        verify(categoryRepository).findById(99);
    }

    // =========================================
    // 4. UPDATE CATEGORY BY ID
    // =========================================

    @Test
    void updateCategoryById_shouldUpdateDescriptionWhenNameUnchanged() {

        // Arrange
        Category updateCategory = new Category();
        updateCategory.setName("Shoes");
        updateCategory.setDescription("Updated description");

        when(categoryRepository.findById(1))
                .thenReturn(Optional.of(category));

        when(categoryRepository.save(any(Category.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        CategoryDTO result = categoryService.updateCategoryById(1, updateCategory);

        // Assert
        assertAll(
                () -> assertEquals(1, result.getId()),
                () -> assertEquals("Shoes", result.getName()),
                () -> assertEquals("Updated description", result.getDescription())
        );

        verify(categoryRepository).save(category);

        verify(categoryRepository, never())
                .existsByNameAndIdNot(anyString(), anyInt());
    }

    @Test
    void updateCategoryById_shouldUpdateNameAndDescriptionWhenNameChanged() {

        // Arrange
        Category updateCategory = new Category();
        updateCategory.setName("Clothes");
        updateCategory.setDescription("New description");

        when(categoryRepository.findById(1))
                .thenReturn(Optional.of(category));

        when(categoryRepository.existsByNameAndIdNot("Clothes", 1))
                .thenReturn(false);

        when(categoryRepository.save(any(Category.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        CategoryDTO result = categoryService.updateCategoryById(1, updateCategory);

        // Assert
        assertAll(
                () -> assertEquals(1, result.getId()),
                () -> assertEquals("Clothes", result.getName()),
                () -> assertEquals("New description", result.getDescription())
        );

        verify(categoryRepository).existsByNameAndIdNot("Clothes", 1);
        verify(categoryRepository).save(category);
    }

    @Test
    void updateCategoryById_shouldThrowExceptionWhenNewNameAlreadyExists() {

        // Arrange
        Category updateCategory = new Category();
        updateCategory.setName("Clothes");
        updateCategory.setDescription("New description");

        when(categoryRepository.findById(1))
                .thenReturn(Optional.of(category));

        when(categoryRepository.existsByNameAndIdNot("Clothes", 1))
                .thenReturn(true);

        // Act & Assert
        ResourceAlreadyExistsException exception =
                assertThrows(
                        ResourceAlreadyExistsException.class,
                        () -> categoryService.updateCategoryById(1, updateCategory)
                );

        assertTrue(exception.getMessage().contains("Clothes"));

        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void updateCategoryById_shouldThrowExceptionWhenCategoryNotFound() {

        // Arrange
        Category updateCategory = new Category();
        updateCategory.setName("Clothes");
        updateCategory.setDescription("New description");

        when(categoryRepository.findById(99))
                .thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> categoryService.updateCategoryById(99, updateCategory)
                );

        assertTrue(exception.getMessage().contains("99"));

        verify(categoryRepository, never()).save(any(Category.class));
    }

    // =========================================
    // 5. DELETE CATEGORY BY ID
    // =========================================

    @Test
    void deleteCategoryById_shouldDeleteWhenCategoryExists() {

        // Arrange
        when(categoryRepository.existsById(1))
                .thenReturn(true);

        // Act
        categoryService.deleteCategoryById(1);

        // Assert
        verify(categoryRepository).existsById(1);
        verify(categoryRepository).deleteById(1);
    }

    @Test
    void deleteCategoryById_shouldThrowExceptionWhenCategoryNotFound() {

        // Arrange
        when(categoryRepository.existsById(99))
                .thenReturn(false);

        // Act & Assert
        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> categoryService.deleteCategoryById(99)
                );

        assertTrue(exception.getMessage().contains("99"));

        verify(categoryRepository, never()).deleteById(anyInt());
    }

    // =========================================
    // 6. GET CATEGORY NAMES
    // =========================================

    @Test
    void getCategoryNames_shouldReturnAllCategoryNames() {

        // Arrange
        Category category2 = new Category();
        category2.setId(2);
        category2.setName("Clothes");
        category2.setDescription("All types of clothes");

        when(categoryRepository.findAll())
                .thenReturn(List.of(category, category2));

        // Act
        List<String> result = categoryService.getCategoryNames();

        // Assert
        assertEquals(List.of("Shoes", "Clothes"), result);

        verify(categoryRepository).findAll();
    }

    @Test
    void getCategoryNames_shouldReturnEmptyListWhenNoCategoryExists() {

        // Arrange
        when(categoryRepository.findAll())
                .thenReturn(List.of());

        // Act
        List<String> result = categoryService.getCategoryNames();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(categoryRepository).findAll();
    }
}
