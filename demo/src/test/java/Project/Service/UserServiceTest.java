package Project.Service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import Project.Exception.ResourceAlreadyExistsException;
import Project.Exception.ResourceNotFoundException;
import Project.Model.Entity.Cart;
import Project.Model.Entity.Role;
import Project.Model.Entity.User;
import Project.Model.DTO.Request.UserRequestDTO;
import Project.Model.DTO.Response.UserResponseDTO;
import Project.Repository.RoleRepository;
import Project.Repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User user;
    private Role userRole;
    private Role adminRole;

    @BeforeEach
    void setUp() {
        userRole = new Role();
        userRole.setId(2);
        userRole.setName("USER");
        userRole.setDescription("Normal user");

        adminRole = new Role();
        adminRole.setId(1);
        adminRole.setName("ADMIN");
        adminRole.setDescription("Administrator");

        user = new User();
        user.setId(10);
        user.setName("Test User");
        user.setEmail("test@gmail.com");
        user.setPassword("encoded-password");
        user.setPhone("0123456789");
        user.setAddress("Hanoi");
        user.setRole(userRole);

        Cart cart = new Cart();
        cart.setItems(new ArrayList<>());
        cart.setUser(user);
        user.setCart(cart);
    }

    // Helper tạo UserRequestDTO
    private UserRequestDTO createUserRequestDTO() {
        UserRequestDTO request = new UserRequestDTO();

        request.setName("Test User");
        request.setEmail("test@gmail.com");
        request.setPassword("password123");
        request.setPhone("0123456789");
        request.setAddress("Hanoi");

        return request;
    }

    // 1. convertDTOToUser
    @Test
    void convertDTOToUser_shouldConvertSuccessfully() {

        UserRequestDTO request = createUserRequestDTO();

        when(passwordEncoder.encode("password123"))
                .thenReturn("encoded-password");

        User result = userService.convertDTOToUser(request);

        assertNotNull(result);
        assertEquals("Test User", result.getName());
        assertEquals("test@gmail.com", result.getEmail());
        assertEquals("encoded-password", result.getPassword());
        assertEquals("0123456789", result.getPhone());
        assertEquals("Hanoi", result.getAddress());

        verify(passwordEncoder).encode("password123");
    }

    // 2. convertUserToDTO
    @Test
    void convertUserToDTO_shouldConvertSuccessfully() {

        UserResponseDTO result = userService.convertUserToDTO(user);

        assertNotNull(result);
        assertEquals(10, result.getId());
        assertEquals("Test User", result.getName());
        assertEquals("test@gmail.com", result.getEmail());
        assertEquals("0123456789", result.getPhone());
        assertEquals("Hanoi", result.getAddress());

        assertNotNull(result.getRoleResponseDTO());
        assertEquals(2, result.getRoleResponseDTO().getId());
        assertEquals("USER", result.getRoleResponseDTO().getName());
        assertEquals(
                "Normal user",
                result.getRoleResponseDTO().getDescription()
        );
    }

    // 3. createUser thành công với role USER
    @Test
    void createUser_shouldCreateNormalUserSuccessfully() {

        UserRequestDTO request = createUserRequestDTO();

        when(userRepository.existsByEmail("test@gmail.com"))
                .thenReturn(false);

        when(passwordEncoder.encode("password123"))
                .thenReturn("encoded-password");

        when(roleRepository.findByName("USER"))
                .thenReturn(Optional.of(userRole));

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> {
                    User savedUser = invocation.getArgument(0);
                    savedUser.setId(10);
                    return savedUser;
                });

        UserResponseDTO result = userService.createUser(request);

        assertNotNull(result);
        assertEquals(10, result.getId());
        assertEquals("Test User", result.getName());
        assertEquals("test@gmail.com", result.getEmail());
        assertEquals("USER", result.getRoleResponseDTO().getName());

        verify(userRepository).existsByEmail("test@gmail.com");
        verify(roleRepository).findByName("USER");
        verify(userRepository).save(any(User.class));
    }

    // 4. createUser với email ADMIN đặc biệt
    @Test
    void createUser_shouldAssignAdminRoleForSpecialEmail() {

        UserRequestDTO request = createUserRequestDTO();
        request.setEmail("daotrongvinha1k63tp@gmail.com");

        when(userRepository.existsByEmail("daotrongvinha1k63tp@gmail.com"))
                .thenReturn(false);

        when(passwordEncoder.encode("password123"))
                .thenReturn("encoded-password");

        when(roleRepository.findByName("ADMIN"))
                .thenReturn(Optional.of(adminRole));

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> {
                    User savedUser = invocation.getArgument(0);
                    savedUser.setId(11);
                    return savedUser;
                });

        UserResponseDTO result = userService.createUser(request);

        assertNotNull(result);
        assertEquals("ADMIN", result.getRoleResponseDTO().getName());

        verify(roleRepository).findByName("ADMIN");
    }

    // 5. createUser khi email đã tồn tại
    @Test
    void createUser_shouldThrowExceptionWhenEmailAlreadyExists() {

        UserRequestDTO request = createUserRequestDTO();

        when(userRepository.existsByEmail("test@gmail.com"))
                .thenReturn(true);

        assertThrows(
                ResourceAlreadyExistsException.class,
                () -> userService.createUser(request)
        );

        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    // 6. createUser khi không tìm thấy Role
    @Test
    void createUser_shouldThrowExceptionWhenRoleNotFound() {

        UserRequestDTO request = createUserRequestDTO();

        when(userRepository.existsByEmail("test@gmail.com"))
                .thenReturn(false);

        when(passwordEncoder.encode("password123"))
                .thenReturn("encoded-password");

        when(roleRepository.findByName("USER"))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> userService.createUser(request)
        );

        verify(userRepository, never()).save(any(User.class));
    }

    // 7. fetchUsers
    @Test
    void fetchUsers_shouldReturnUserPage() {

        Pageable pageable = PageRequest.of(0, 10);

        Page<User> userPage =
                new PageImpl<>(List.of(user), pageable, 1);

        when(userRepository.findAll(pageable))
                .thenReturn(userPage);

        Page<UserResponseDTO> result =
                userService.fetchUsers(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());

        UserResponseDTO response = result.getContent().get(0);

        assertEquals(10, response.getId());
        assertEquals("Test User", response.getName());
        assertEquals("USER", response.getRoleResponseDTO().getName());

        verify(userRepository).findAll(pageable);
    }

    // 8. fetchUsers khi danh sách rỗng
    @Test
    void fetchUsers_shouldReturnEmptyPage() {

        Pageable pageable = PageRequest.of(0, 10);

        when(userRepository.findAll(pageable))
                .thenReturn(Page.empty(pageable));

        Page<UserResponseDTO> result =
                userService.fetchUsers(pageable);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertEquals(0, result.getTotalElements());
    }

    // 9. fetchUserById thành công
    @Test
    void fetchUserById_shouldReturnUserWhenFound() {

        when(userRepository.findById(10))
                .thenReturn(Optional.of(user));

        UserResponseDTO result = userService.fetchUserById(10);

        assertNotNull(result);
        assertEquals(10, result.getId());
        assertEquals("Test User", result.getName());
        assertEquals("test@gmail.com", result.getEmail());

        verify(userRepository).findById(10);
    }

    // 10. fetchUserById khi không tìm thấy
    @Test
    void fetchUserById_shouldThrowExceptionWhenNotFound() {

        when(userRepository.findById(999))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> userService.fetchUserById(999)
        );
    }

    // 11. updateUserById thành công
    @Test
    void updateUserById_shouldUpdateUserSuccessfully() {

        UserRequestDTO request = createUserRequestDTO();
        request.setName("Updated Name");
        request.setEmail("updated@gmail.com");
        request.setPhone("0987654321");
        request.setAddress("Ho Chi Minh");

        when(userRepository.findById(10))
                .thenReturn(Optional.of(user));

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UserResponseDTO result =
                userService.updateUserById(10, request);

        assertNotNull(result);
        assertEquals("Updated Name", result.getName());
        assertEquals("updated@gmail.com", result.getEmail());
        assertEquals("0987654321", result.getPhone());
        assertEquals("Ho Chi Minh", result.getAddress());

        // Password và Role không bị thay đổi
        assertEquals("encoded-password", user.getPassword());
        assertEquals(userRole, user.getRole());

        verify(userRepository).save(user);
    }

    // 12. updateUserById khi không tìm thấy User
    @Test
    void updateUserById_shouldThrowExceptionWhenUserNotFound() {

        UserRequestDTO request = createUserRequestDTO();

        when(userRepository.findById(999))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> userService.updateUserById(999, request)
        );

        verify(userRepository, never()).save(any(User.class));
    }

    // 13. deleteUserById
    @Test
    void deleteUserById_shouldDeleteSuccessfully() {

        userService.deleteUserById(10);

        verify(userRepository).deleteById(10);
    }

    // 14. findUserByEmail
    @Test
    void findUserByEmail_shouldReturnUser() {

        when(userRepository.findByEmail("test@gmail.com"))
                .thenReturn(user);

        User result = userService.findUserByEmail("test@gmail.com");

        assertNotNull(result);
        assertSame(user, result);

        verify(userRepository).findByEmail("test@gmail.com");
    }

    // 15. findOrCreateGoogleUser: Google ID đã tồn tại
    @Test
    void findOrCreateGoogleUser_shouldReturnExistingGoogleUser() {

        when(userRepository.findByGoogleId("google-123"))
                .thenReturn(user);

        User result = userService.findOrCreateGoogleUser(
                "google-123",
                "test@gmail.com",
                "Test User"
        );

        assertSame(user, result);

        verify(userRepository).findByGoogleId("google-123");
        verify(userRepository, never()).findByEmail(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    // 16. findOrCreateGoogleUser: tạo tài khoản Google mới
    @Test
    void findOrCreateGoogleUser_shouldCreateNewUser() {

        when(userRepository.findByGoogleId("google-123"))
                .thenReturn(null);

        when(userRepository.findByEmail("new@gmail.com"))
                .thenReturn(null);

        when(roleRepository.findByName("USER"))
                .thenReturn(Optional.of(userRole));

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> {
                    User savedUser = invocation.getArgument(0);
                    savedUser.setId(20);
                    return savedUser;
                });

        User result = userService.findOrCreateGoogleUser(
                "google-123",
                "new@gmail.com",
                "New User"
        );

        assertNotNull(result);
        assertEquals(20, result.getId());
        assertEquals("google-123", result.getGoogleId());
        assertEquals("new@gmail.com", result.getEmail());
        assertEquals("New User", result.getName());

        assertNull(result.getPassword());
        assertNull(result.getPhone());
        assertNull(result.getAddress());

        assertEquals("USER", result.getRole().getName());

        assertNotNull(result.getCart());
        assertSame(result, result.getCart().getUser());
        assertTrue(result.getCart().getItems().isEmpty());

        verify(roleRepository).findByName("USER");
        verify(userRepository).save(any(User.class));
    }

    // 17. findOrCreateGoogleUser: name null hoặc blank
    @Test
    void findOrCreateGoogleUser_shouldUseEmailPrefixWhenNameIsBlank() {

        when(userRepository.findByGoogleId("google-123"))
                .thenReturn(null);

        when(userRepository.findByEmail("new@gmail.com"))
                .thenReturn(null);

        when(roleRepository.findByName("USER"))
                .thenReturn(Optional.of(userRole));

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.findOrCreateGoogleUser(
                "google-123",
                "new@gmail.com",
                " "
        );

        assertEquals("new", result.getName());
    }

    // 18. findOrCreateGoogleUser: không tìm thấy Role USER
    @Test
    void findOrCreateGoogleUser_shouldThrowExceptionWhenRoleNotFound() {

        when(userRepository.findByGoogleId("google-123"))
                .thenReturn(null);

        when(userRepository.findByEmail("new@gmail.com"))
                .thenReturn(null);

        when(roleRepository.findByName("USER"))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> userService.findOrCreateGoogleUser(
                        "google-123",
                        "new@gmail.com",
                        "New User"
                )
        );

        verify(userRepository, never()).save(any(User.class));
    }

    // 19. findOrCreateGoogleUser: email đã liên kết với Google ID khác
    @Test
    void findOrCreateGoogleUser_shouldThrowConflictWhenEmailLinkedToAnotherGoogle() {

        User existingUser = new User();
        existingUser.setId(30);
        existingUser.setEmail("existing@gmail.com");
        existingUser.setGoogleId("another-google-id");

        when(userRepository.findByGoogleId("google-123"))
                .thenReturn(null);

        when(userRepository.findByEmail("existing@gmail.com"))
                .thenReturn(existingUser);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> userService.findOrCreateGoogleUser(
                        "google-123",
                        "existing@gmail.com",
                        "Existing User"
                )
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());

        verify(userRepository, never()).save(any(User.class));
    }

    // 20. findOrCreateGoogleUser: email đã tồn tại nhưng chưa liên kết Google
    @Test
    void findOrCreateGoogleUser_shouldHandleExistingUnlinkedEmail() {

        User existingUser = new User();
        existingUser.setId(30);
        existingUser.setEmail("existing@gmail.com");
        existingUser.setGoogleId(null);

        when(userRepository.findByGoogleId("google-123"))
                .thenReturn(null);

        when(userRepository.findByEmail("existing@gmail.com"))
                .thenReturn(existingUser);

        when(roleRepository.findByName("USER"))
                .thenReturn(Optional.of(userRole));

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.findOrCreateGoogleUser(
                "google-123",
                "existing@gmail.com",
                "Existing User"
        );

        // Theo code hiện tại, service lưu existingUser để gắn Google ID,
        // sau đó tiếp tục tạo một User mới.
        assertEquals("google-123", existingUser.getGoogleId());
        assertNotSame(existingUser, result);
        assertEquals("existing@gmail.com", result.getEmail());

        verify(userRepository, times(2)).save(any(User.class));
    }
}