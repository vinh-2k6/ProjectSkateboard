package Project.Service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import Project.Exception.ResourceAlreadyExistsException;
import Project.Exception.ResourceNotFoundException;
import Project.Model.Entity.Role;
import Project.Model.DTO.Response.RoleResponseDTO;
import Project.Repository.RoleRepository;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private RoleService roleService;

    private Role role;

    @BeforeEach
    void setUp() {
        role = new Role();
        role.setId(1);
        role.setName("USER");
        role.setDescription("Normal user");
    }

    // 1. createRole thành công
    @Test
    void createRole_shouldSaveRoleSuccessfully() {

        when(roleRepository.existsByName("USER"))
                .thenReturn(false);

        when(roleRepository.save(role))
                .thenReturn(role);

        Role result = roleService.createRole(role);

        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("USER", result.getName());

        verify(roleRepository).existsByName("USER");
        verify(roleRepository).save(role);
    }

    // 2. createRole khi tên Role đã tồn tại
    @Test
    void createRole_shouldThrowExceptionWhenNameAlreadyExists() {

        when(roleRepository.existsByName("USER"))
                .thenReturn(true);

        assertThrows(
                ResourceAlreadyExistsException.class,
                () -> roleService.createRole(role)
        );

        verify(roleRepository, never()).save(any(Role.class));
    }

    // 3. fetchRoles thành công
    @Test
    void fetchRoles_shouldReturnRoleDTOList() {

        Role adminRole = new Role();
        adminRole.setId(2);
        adminRole.setName("ADMIN");
        adminRole.setDescription("Administrator");

        when(roleRepository.findAll())
                .thenReturn(List.of(role, adminRole));

        List<RoleResponseDTO> result = roleService.fetchRoles();

        assertNotNull(result);
        assertEquals(2, result.size());

        assertEquals(1, result.get(0).getId());
        assertEquals("USER", result.get(0).getName());
        assertEquals("Normal user", result.get(0).getDescription());

        assertEquals(2, result.get(1).getId());
        assertEquals("ADMIN", result.get(1).getName());

        verify(roleRepository).findAll();
    }

    // 4. fetchRoles khi danh sách rỗng
    @Test
    void fetchRoles_shouldReturnEmptyList() {

        when(roleRepository.findAll())
                .thenReturn(List.of());

        List<RoleResponseDTO> result = roleService.fetchRoles();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // 5. fetchRoleById thành công
    @Test
    void fetchRoleById_shouldReturnRoleDTOWhenFound() {

        when(roleRepository.findById(1))
                .thenReturn(Optional.of(role));

        RoleResponseDTO result = roleService.fetchRoleById(1);

        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("USER", result.getName());

        verify(roleRepository).findById(1);
    }

    // 6. fetchRoleById khi không tìm thấy
    @Test
    void fetchRoleById_shouldThrowExceptionWhenNotFound() {

        when(roleRepository.findById(999))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> roleService.fetchRoleById(999)
        );
    }

    // 7. updateRoleById: giữ nguyên tên, chỉ cập nhật description
    @Test
    void updateRoleById_shouldUpdateDescriptionWhenNameUnchanged() {

        Role updateRole = new Role();
        updateRole.setName("USER");
        updateRole.setDescription("Updated description");

        when(roleRepository.findById(1))
                .thenReturn(Optional.of(role));

        when(roleRepository.save(any(Role.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RoleResponseDTO result =
                roleService.updateRoleById(1, updateRole);

        assertNotNull(result);
        assertEquals("USER", result.getName());
        assertEquals("Updated description", result.getDescription());

        verify(roleRepository, never())
                .existsByNameAndIdNot(anyString(), anyInt());

        verify(roleRepository).save(role);
    }

    // 8. updateRoleById: đổi tên thành công
    @Test
    void updateRoleById_shouldUpdateNameAndDescription() {

        Role updateRole = new Role();
        updateRole.setName("ADMIN");
        updateRole.setDescription("Administrator");

        when(roleRepository.findById(1))
                .thenReturn(Optional.of(role));

        when(roleRepository.existsByNameAndIdNot("ADMIN", 1))
                .thenReturn(false);

        when(roleRepository.save(any(Role.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RoleResponseDTO result =
                roleService.updateRoleById(1, updateRole);

        assertNotNull(result);
        assertEquals("ADMIN", result.getName());
        assertEquals("Administrator", result.getDescription());

        verify(roleRepository).existsByNameAndIdNot("ADMIN", 1);
        verify(roleRepository).save(role);
    }

    // 9. updateRoleById: tên mới đã được sử dụng
    @Test
    void updateRoleById_shouldThrowExceptionWhenNameAlreadyExists() {

        Role updateRole = new Role();
        updateRole.setName("ADMIN");
        updateRole.setDescription("Administrator");

        when(roleRepository.findById(1))
                .thenReturn(Optional.of(role));

        when(roleRepository.existsByNameAndIdNot("ADMIN", 1))
                .thenReturn(true);

        assertThrows(
                ResourceAlreadyExistsException.class,
                () -> roleService.updateRoleById(1, updateRole)
        );

        verify(roleRepository, never()).save(any(Role.class));
    }

    // 10. updateRoleById: Role không tồn tại
    @Test
    void updateRoleById_shouldThrowExceptionWhenRoleNotFound() {

        Role updateRole = new Role();
        updateRole.setName("ADMIN");

        when(roleRepository.findById(999))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> roleService.updateRoleById(999, updateRole)
        );

        verify(roleRepository, never()).save(any(Role.class));
    }

    // 11. deleteRoleById
    @Test
    void deleteRoleById_shouldDeleteRoleSuccessfully() {

        roleService.deleteRoleById(1);

        verify(roleRepository).deleteById(1);
    }
}