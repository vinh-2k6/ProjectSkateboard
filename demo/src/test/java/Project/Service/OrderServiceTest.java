package Project.Service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import Project.Exception.ResourceNotFoundException;
import Project.Model.Entity.Order;
import Project.Model.Entity.OrderItem;
import Project.Model.Entity.Product;
import Project.Model.Entity.User;
import Project.Model.DTO.Request.CreateOrderItemDTO;
import Project.Model.DTO.Request.OrderItemRequestDTO;
import Project.Model.DTO.Response.OrderItemResponseDTO;
import Project.Model.DTO.Response.OrderResponseDTO;
import Project.Repository.OrderRepository;
import Project.Repository.ProductRepository;
import Project.Repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private OrderService orderService;

    private Product product;
    private User user;
    private Order order;
    private OrderItem orderItem;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(10);
        product.setName("Skateboard");
        product.setPrice(500);

        user = new User();
        user.setId(1);
        user.setEmail("test@gmail.com");

        order = new Order();
        order.setId(100);
        order.setUser(user);
        order.setTotalPrice(1000);
        order.setStatus("PENDING");
        order.setCreatedAt(Instant.parse("2026-01-01T10:00:00Z"));

        orderItem = new OrderItem();
        orderItem.setId(1);
        orderItem.setProduct(product);
        orderItem.setQuantity(2);
        orderItem.setPrice(500);
        orderItem.setOrder(order);

        order.setOrderItems(new ArrayList<>(List.of(orderItem)));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // Helper tạo OrderItemRequestDTO
    private OrderItemRequestDTO createOrderItemRequestDTO(
            int productId, int quantity) {

        OrderItemRequestDTO request = new OrderItemRequestDTO();
        request.setProduct_Id(productId);
        request.setQuantity(quantity);

        return request;
    }

    // Helper tạo CreateOrderItemDTO
    private CreateOrderItemDTO createOrderRequest() {
        CreateOrderItemDTO request = new CreateOrderItemDTO();

        request.setItems(
                new ArrayList<>(List.of(
                        createOrderItemRequestDTO(10, 2)
                ))
        );

        return request;
    }

    // Helper giả lập người dùng đã đăng nhập
    private void setAuthenticatedUser(String email) {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        email,
                        null
                );

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);
    }

    // 1. convertOrderItemToDTO
    @Test
    void convertOrderItemToDTO_shouldReturnCorrectDTO() {

        OrderItemResponseDTO result =
                orderService.convertOrderItemToDTO(orderItem);

        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals(10, result.getProduct_Id());
        assertEquals("Skateboard", result.getProduct_Name());
        assertEquals(2, result.getQuantity());
        assertEquals(500, result.getPrice());
    }

    // 2. convertOrderToDTO
    @Test
    void convertOrderToDTO_shouldReturnCorrectDTO() {

        OrderResponseDTO result =
                orderService.convertOrderToDTO(order);

        assertNotNull(result);
        assertEquals(100, result.getId());
        assertEquals(1000, result.getTotalPrice());
        assertEquals("PENDING", result.getStatus());
        assertEquals(order.getCreatedAt(), result.getCreatedAt());

        assertEquals(1, result.getOrderItems().size());

        OrderItemResponseDTO itemDTO =
                result.getOrderItems().get(0);

        assertEquals(1, itemDTO.getId());
        assertEquals(10, itemDTO.getProduct_Id());
        assertEquals("Skateboard", itemDTO.getProduct_Name());
        assertEquals(2, itemDTO.getQuantity());
    }

    // 3. createOrder thành công
    @Test
    void createOrder_shouldCreateOrderSuccessfully() {

        CreateOrderItemDTO request = createOrderRequest();

        setAuthenticatedUser("test@gmail.com");

        when(productRepository.findById(10))
                .thenReturn(Optional.of(product));

        when(userRepository.findByEmail("test@gmail.com"))
                .thenReturn(user);

        // save() trả về chính Order được truyền vào
        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponseDTO result =
                orderService.createOrder(request);

        assertNotNull(result);
        assertEquals(1000, result.getTotalPrice());
        assertEquals("PENGDING", result.getStatus());
        assertEquals(1, result.getOrderItems().size());

        verify(productRepository).findById(10);
        verify(userRepository).findByEmail("test@gmail.com");
        verify(orderRepository).save(any(Order.class));
    }

    // 4. createOrder khi Product không tồn tại
    @Test
    void createOrder_shouldThrowExceptionWhenProductNotFound() {

        CreateOrderItemDTO request = createOrderRequest();

        when(productRepository.findById(10))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> orderService.createOrder(request)
        );

        verify(orderRepository, never()).save(any(Order.class));
    }

    // 5. fetchOrders thành công
    @Test
    void fetchOrders_shouldReturnOrdersOfUser() {

        when(orderRepository.searchOrdersOfUser(1))
                .thenReturn(List.of(order));

        List<OrderResponseDTO> result =
                orderService.fetchOrders(1);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(100, result.get(0).getId());

        verify(orderRepository).searchOrdersOfUser(1);
    }

    // 6. fetchOrders khi không có đơn hàng
    @Test
    void fetchOrders_shouldReturnEmptyList() {

        when(orderRepository.searchOrdersOfUser(1))
                .thenReturn(List.of());

        List<OrderResponseDTO> result =
                orderService.fetchOrders(1);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // 7. fetchOrderById thành công
    @Test
    void fetchOrderById_shouldReturnOrderWhenFound() {

        when(orderRepository.findById(100))
                .thenReturn(Optional.of(order));

        OrderResponseDTO result =
                orderService.fetchOrderById(100);

        assertNotNull(result);
        assertEquals(100, result.getId());
        assertEquals(1000, result.getTotalPrice());

        verify(orderRepository).findById(100);
    }

    // 8. fetchOrderById khi không tìm thấy
    @Test
    void fetchOrderById_shouldThrowExceptionWhenNotFound() {

        when(orderRepository.findById(999))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> orderService.fetchOrderById(999)
        );
    }

    // 9. cancelOrder thành công
    @Test
    void cancelOrder_shouldChangeStatusToCancelled() {

        when(orderRepository.findById(100))
                .thenReturn(Optional.of(order));

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponseDTO result =
                orderService.cancelOrder(100);

        assertNotNull(result);
        assertEquals("CANCELLED", result.getStatus());

        verify(orderRepository).save(order);
    }

    // 10. cancelOrder khi không tìm thấy
    @Test
    void cancelOrder_shouldThrowExceptionWhenNotFound() {

        when(orderRepository.findById(999))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> orderService.cancelOrder(999)
        );

        verify(orderRepository, never()).save(any(Order.class));
    }

    // 11. updateOrderStatus thành công
    @Test
    void updateOrderStatus_shouldUpdateStatusSuccessfully() {

        when(orderRepository.findById(100))
                .thenReturn(Optional.of(order));

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponseDTO result =
                orderService.updateOrderStatus(100, "SHIPPED");

        assertNotNull(result);
        assertEquals("SHIPPED", result.getStatus());

        verify(orderRepository).save(order);
    }

    // 12. updateOrderStatus khi không tìm thấy
    @Test
    void updateOrderStatus_shouldThrowExceptionWhenNotFound() {

        when(orderRepository.findById(999))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> orderService.updateOrderStatus(999, "SHIPPED")
        );

        verify(orderRepository, never()).save(any(Order.class));
    }

    // 13. updateOrderById thành công
    @Test
    void updateOrderById_shouldUpdateOrderItemsSuccessfully() {

        CreateOrderItemDTO request = createOrderRequest();

        when(orderRepository.findById(100))
                .thenReturn(Optional.of(order));

        when(productRepository.findById(10))
                .thenReturn(Optional.of(product));

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponseDTO result =
                orderService.updateOrderById(100, request);

        assertNotNull(result);
        assertEquals(1000, result.getTotalPrice());
        assertEquals(1, result.getOrderItems().size());

        OrderItemResponseDTO updatedItem =
                result.getOrderItems().get(0);

        assertEquals(10, updatedItem.getProduct_Id());
        assertEquals(2, updatedItem.getQuantity());
        assertEquals(500, updatedItem.getPrice());

        verify(orderRepository).findById(100);
        verify(productRepository).findById(10);
        verify(orderRepository).save(order);
    }

    // 14. updateOrderById khi Order không tồn tại
    @Test
    void updateOrderById_shouldThrowExceptionWhenOrderNotFound() {

        CreateOrderItemDTO request = createOrderRequest();

        when(orderRepository.findById(999))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> orderService.updateOrderById(999, request)
        );

        verify(productRepository, never()).findById(anyInt());
        verify(orderRepository, never()).save(any(Order.class));
    }

    // 15. updateOrderById khi Product không tồn tại
    @Test
    void updateOrderById_shouldThrowExceptionWhenProductNotFound() {

        CreateOrderItemDTO request = createOrderRequest();

        when(orderRepository.findById(100))
                .thenReturn(Optional.of(order));

        when(productRepository.findById(10))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> orderService.updateOrderById(100, request)
        );

        verify(orderRepository, never()).save(any(Order.class));
    }

    // 16. getUserIdByOrderId thành công
    @Test
    void getUserIdByOrderId_shouldReturnUserId() {

        when(orderRepository.findById(100))
                .thenReturn(Optional.of(order));

        int result = orderService.getUserIdByOrderId(100);

        assertEquals(1, result);

        verify(orderRepository).findById(100);
    }

    // 17. getUserIdByOrderId khi không tìm thấy Order
    @Test
    void getUserIdByOrderId_shouldThrowExceptionWhenNotFound() {

        when(orderRepository.findById(999))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> orderService.getUserIdByOrderId(999)
        );
    }
}