package Project.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import Project.Exception.ResourceNotFoundException;
import Project.Model.Entity.Cart;
import Project.Model.Entity.CartItem;
import Project.Model.Entity.Product;
import Project.Model.Entity.ProductSize;
import Project.Model.Entity.User;
import Project.Model.DTO.Response.BrandDTO;
import Project.Model.DTO.Request.CartItemRequestDTO;
import Project.Model.DTO.Response.CartItemResponseDTO;
import Project.Model.DTO.Response.CartResponseDTO;
import Project.Model.DTO.Response.CategoryDTO;
import Project.Model.DTO.Response.ProductResponseDTO;
import Project.Repository.CartItemRepository;
import Project.Repository.CartRepository;
import Project.Repository.ProductRepository;
import Project.Repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor 
public class CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public ProductResponseDTO convertProductToDTO(Product product) {
        ProductResponseDTO productResponseDTO = new ProductResponseDTO();
        productResponseDTO.setId(product.getId());
        productResponseDTO.setName(product.getName());
        productResponseDTO.setDescription(product.getDescription());
        productResponseDTO.setPrice(product.getPrice());
        productResponseDTO.setQuantity(product.getQuantity());
        productResponseDTO.setImage(product.getImage());

        BrandDTO brandDTO = new BrandDTO(product.getBrand().getId(), 
            product.getBrand().getName(), product.getBrand().getDescription());
        productResponseDTO.setBrand(brandDTO);

        CategoryDTO categoryDTO = new CategoryDTO(product.getCategory().getId(),
            product.getCategory().getName(), product.getCategory().getDescription());
        productResponseDTO.setCategory(categoryDTO);

        List<Integer> sizes = new ArrayList<>();

        if (product.getSizes() != null) {
            for (ProductSize productSize : product.getSizes()) {
                Integer size = productSize.getSize();
                sizes.add(size);
            }
        }

        productResponseDTO.setSizes(sizes);
        return productResponseDTO;
    }

    public CartItemResponseDTO convertCartItemToDTO(CartItem cartItem) {
        return CartItemResponseDTO.builder().id(cartItem.getId())
                                            .productResponseDTO(this.convertProductToDTO(cartItem.getProduct()))
                                            .quantity(cartItem.getQuantity())
                                            .price(cartItem.getPrice())
                                            .size(cartItem.getSize())
                                            .build();

    }

    public CartResponseDTO convertCartToDTO(Cart cart) {
        List<CartItemResponseDTO> CartResponseDTOs = cart.getItems().stream()
                                                            .map(cartItem -> {return convertCartItemToDTO(cartItem);})
                                                            .collect(Collectors.toList());
        return CartResponseDTO.builder()
                                .id(cart.getId())
                                .items(CartResponseDTOs)
                                .build();
    }

    public CartResponseDTO AddACartitemToCart(CartItemRequestDTO cartItemRequestDTO, int userId) {

        User user = this.userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Khong tim thay user")
                );

        Product product = this.productRepository
                .findById(cartItemRequestDTO.getProductId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Khong tim thay product")
                );

        int quantity = cartItemRequestDTO.getQuantity();

        // Kiểm tra số lượng
        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "So luong phai lon hon 0"
            );
        }

        // Kiểm tra tồn kho
        if (product.getQuantity() < quantity) {
            throw new IllegalArgumentException(
                    "Khong du so luong san pham trong kho"
            );
        }

        Integer selectedSize = cartItemRequestDTO.getSize();

        if (selectedSize == null) {
            throw new IllegalArgumentException("Size không được để trống");
        }

        boolean validSize = false;

        List<ProductSize> productSizes = product.getSizes();

        for (int i = 0; i < productSizes.size(); i++) {

            ProductSize productSize = productSizes.get(i);

            Integer size = productSize.getSize();

            if (size.equals(selectedSize)) {
                validSize = true;
                break;
            }
        }

        if (validSize == false) {
            throw new IllegalArgumentException("Size không hợp lệ");
        }

        Cart cart = user.getCart();

        if (cart == null) {
            throw new ResourceNotFoundException("User không có giỏ hàng");
        }

        boolean ok = false;

        for (CartItem cartItem : cart.getItems()) {

            if (cartItem.getProduct().getId().equals(product.getId())
                && cartItem.getSize().equals(cartItemRequestDTO.getSize())) {

                // Sản phẩm đã có trong giỏ
                int newQuantity =
                        cartItem.getQuantity() + quantity;

                cartItem.setQuantity(newQuantity);

                cartItem.setPrice(
                        product.getPrice() * newQuantity
                );

                this.cartItemRepository.save(cartItem);

                ok = true;
                break;
            }
        }

        // Sản phẩm chưa có trong giỏ
        if (!ok) {

            CartItem cartItem = new CartItem();

            cartItem.setProduct(product);
            cartItem.setQuantity(quantity);
            cartItem.setPrice(product.getPrice() * quantity);
            cartItem.setCart(cart);
            cartItem.setSize(cartItemRequestDTO.getSize());

            this.cartItemRepository.save(cartItem);

            cart.getItems().add(cartItem);
        }

        // Trừ số lượng trong kho
        product.setQuantity(
                product.getQuantity() - quantity
        );

        this.productRepository.save(product);

        // Lưu Cart
        this.cartRepository.save(cart);

        return convertCartToDTO(cart);
    }

    public CartResponseDTO updateCartItemQuantity(int userId, int cartItemId, int newQuantity) {
        if (newQuantity <= 0) {
            throw new IllegalArgumentException("Số lượng phải lớn hơn 0");
        }

        User user = this.userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy user"));

        Cart cart = user.getCart();
        if (cart == null) {
            throw new ResourceNotFoundException("User không có giỏ hàng");
        }

        CartItem cartItem = cart.getItems()
            .stream()
            .filter(item -> item.getId().equals(cartItemId))
            .findFirst()
            .orElseThrow(() ->
                new ResourceNotFoundException(
                    "Không tìm thấy sản phẩm trong giỏ hàng"
                )
            );

        Product product = cartItem.getProduct();

        int oldQuantity = cartItem.getQuantity();
        int difference = newQuantity - oldQuantity;

        // Nếu tăng số lượng thì kiểm tra tồn kho
        if (difference > 0 && product.getQuantity() < difference) {
            throw new IllegalArgumentException(
                "Số lượng tồn kho không đủ"
            );
        }

        // Điều chỉnh tồn kho
        product.setQuantity(product.getQuantity() - difference);

        // Cập nhật CartItem
        cartItem.setQuantity(newQuantity);
        cartItem.setPrice(product.getPrice() * newQuantity);

        this.productRepository.save(product);
        this.cartItemRepository.save(cartItem);
        this.cartRepository.save(cart);

        return convertCartToDTO(cart);
    }

    public CartResponseDTO fetchCartResponseDTO(int userId) {
        User user = this.userRepository.findById(userId)
                                    .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay user"));
        
        Cart cart = user.getCart();

        if (cart == null) {
            throw new ResourceNotFoundException("User khong co cart");
        }

        return convertCartToDTO(cart);
    }

    public void deleteCartItemById(int userId, int cartItemId) {
        User user = this.userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay user"));

        Cart cart = user.getCart();

        if (cart == null) {
            throw new ResourceNotFoundException("User khong co cart");
        }

        // Hôm sau xem
        // CartItem cartItem = this.cartItemRepository.findById(cartItemId)
        //                                             .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay don hang"));

        CartItem cartItem = null;
        // Tìm CartItem trong giỏ hàng của user
        for (CartItem item : cart.getItems()) {

            if (item.getId().equals(cartItemId)) {
                cartItem = item;
                break;
            }
        }                                            
        
        // Không tìm thấy CartItem trong giỏ hàng
        if (cartItem == null) {
            throw new ResourceNotFoundException("Khong tim thay cart item");
        }
        Product product = cartItem.getProduct();
        product.setQuantity(product.getQuantity() + cartItem.getQuantity());
        this.productRepository.save(product);

        boolean removed = cart.getItems()
                .removeIf(item -> item.getId().equals(cartItemId));

        this.cartRepository.save(cart);
        if (!removed) {
            throw new ResourceNotFoundException("Khong tim thay cart item");
        }
    }
}
