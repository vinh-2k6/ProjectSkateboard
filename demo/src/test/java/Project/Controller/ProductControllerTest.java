package Project.Controller;

import Project.Model.DTO.ProductWebSocketEvent;
import Project.Model.DTO.Request.ProductRequestDTO;
import Project.Model.DTO.Response.ProductResponseDTO;
import Project.Service.ProductService;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.security.oauth2.client.autoconfigure.servlet.OAuth2ClientWebSecurityAutoConfiguration;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    controllers = ProductController.class,
    excludeAutoConfiguration = OAuth2ClientWebSecurityAutoConfiguration.class
)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private SimpMessagingTemplate messagingTemplate;

    private ProductRequestDTO createProductRequestDTO() {

        ProductRequestDTO request = new ProductRequestDTO();

        request.setName("Nike Air Max");
        request.setDescription("Nike running shoes");
        request.setPrice(2500000);
        request.setQuantity(10);
        request.setBrandId(1);
        request.setCategoryId(2);
        request.setImage("https://example.com/nike-air-max.jpg");

        return request;
    }

    private ProductResponseDTO createProductResponseDTO() {

        ProductResponseDTO response = new ProductResponseDTO();

        response.setId(1);
        response.setName("Nike Air Max");
        response.setPrice(2500000);
        response.setQuantity(10);

        return response;
    }

    @Test
    void postProduct_shouldReturnCreatedAndSendWebSocketEvent()
            throws Exception {

        // Arrange
        ProductRequestDTO request = createProductRequestDTO();

        ProductResponseDTO response = createProductResponseDTO();

        when(productService.createProduct(any(ProductRequestDTO.class)))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isCreated());

        // Verify Service
        verify(productService, times(1))
                .createProduct(any(ProductRequestDTO.class));

        // Verify WebSocket
        verify(messagingTemplate, times(1))
                .convertAndSend(
                        eq("/topic/products"),
                        any(ProductWebSocketEvent.class)
                );
    }

    @Test
    void getProducts_shouldFetchAllProducts() throws Exception {

        // Arrange
        Page<ProductResponseDTO> page =
                new PageImpl<>(List.of());

        when(productService.fetchProducts(any(Pageable.class)))
                .thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/products")
                .param("page", "0")
                .param("size", "6"))

                .andExpect(status().isOk());

        verify(productService).fetchProducts(any(Pageable.class));

        verify(productService, never())
                .fetchProductWithBrand(anyString(), any(Pageable.class));

        verify(productService, never())
                .fetchProductWithCategory(anyString(), any(Pageable.class));

        verify(productService, never())
                .fetchProductWithBrandAndCategory(
                        anyString(),
                        anyString(),
                        any(Pageable.class)
                );
    }

    @Test
    void getProducts_shouldFilterByBrand() throws Exception {

        // Arrange
        Page<ProductResponseDTO> page =
                new PageImpl<>(List.of());

        when(productService.fetchProductWithBrand(
                eq("Nike"),
                any(Pageable.class)
        )).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/products")
                .param("brand", "Nike"))

                .andExpect(status().isOk());

        verify(productService).fetchProductWithBrand(
                eq("Nike"),
                any(Pageable.class)
        );
    }

    @Test
    void getProducts_shouldFilterByCategory() throws Exception {

        // Arrange
        Page<ProductResponseDTO> page =
                new PageImpl<>(List.of());

        when(productService.fetchProductWithCategory(
                eq("Shoes"),
                any(Pageable.class)
        )).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/products")
                .param("category", "Shoes"))

                .andExpect(status().isOk());

        verify(productService).fetchProductWithCategory(
                eq("Shoes"),
                any(Pageable.class)
        );
    }

    @Test
    void getProducts_shouldFilterByBrandAndCategory()
            throws Exception {

        // Arrange
        Page<ProductResponseDTO> page =
                new PageImpl<>(List.of());

        when(productService.fetchProductWithBrandAndCategory(
                eq("Nike"),
                eq("Shoes"),
                any(Pageable.class)
        )).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/products")
                .param("brand", "Nike")
                .param("category", "Shoes"))

                .andExpect(status().isOk());

        verify(productService).fetchProductWithBrandAndCategory(
                eq("Nike"),
                eq("Shoes"),
                any(Pageable.class)
        );
    }

    @Test
    void updateProduct_shouldReturnUpdatedProductAndSendEvent()
            throws Exception {

        // Arrange
        ProductRequestDTO request = createProductRequestDTO();

        ProductResponseDTO response = createProductResponseDTO();

        when(productService.updateProductById(
                eq(1),
                any(ProductRequestDTO.class)
        )).thenReturn(response);

        // Act & Assert
        mockMvc.perform(put("/products/{id}", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isOk());

        // Verify Service
        verify(productService, times(1))
                .updateProductById(eq(1), any(ProductRequestDTO.class));

        // Verify WebSocket
        verify(messagingTemplate).convertAndSend(
                eq("/topic/products"),
                any(ProductWebSocketEvent.class)
        );
    }

    @Test
    void deleteProduct_shouldDeleteAndSendEvent()
            throws Exception {

        // Arrange
        doNothing().when(productService).deleteProductById(1);

        // Act & Assert
        mockMvc.perform(delete("/products/{id}", 1))

                .andExpect(status().isOk());

        // Verify Service
        verify(productService, times(1))
                .deleteProductById(1);

        // Verify WebSocket
        verify(messagingTemplate).convertAndSend(
                eq("/topic/products"),
                any(ProductWebSocketEvent.class)
        );
    }
}