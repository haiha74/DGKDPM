
package com.dacs.fashion.unit;

import com.dacs.fashion.dto.UserDTO;
import com.dacs.fashion.entity.User;
import com.dacs.fashion.repository.UserRepository;
import com.dacs.fashion.service.AuthService;
import com.dacs.fashion.config.JwtUtil;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthRegisterServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    private UserDTO validDTO;

    @BeforeEach
    void setUp() {
        validDTO = new UserDTO();
        validDTO.setFullname("Nguyễn Văn A");
        validDTO.setEmail("dk01@example.com");
        validDTO.setPassword("Password123");
    }

    // ================================================
    // HÀM HỖ TRỢ
    // ================================================

    private void mockSaveSuccess() {
        when(passwordEncoder.encode(anyString()))
                .thenReturn("hashed_password");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> {
                    User user = invocation.getArgument(0);
                    user.setUserId(1L);
                    return user;
                });
    }

    private UserDTO createDTO(
            String fullname,
            String email,
            String password) {

        UserDTO dto = new UserDTO();
        dto.setFullname(fullname);
        dto.setEmail(email);
        dto.setPassword(password);
        return dto;
    }

    // ================================================
    // DK_01 - ĐĂNG KÝ HỢP LỆ
    // ================================================

    @Test
    @DisplayName("DK_01 - Đăng ký tài khoản hợp lệ")
    void DK01_registerSuccess() {

        mockSaveSuccess();

        User result = authService.register(validDTO);

        assertNotNull(result);
        assertEquals(1L, result.getUserId());
        assertEquals("Nguyễn Văn A", result.getFullname());
        assertEquals("dk01@example.com", result.getEmail());
        assertEquals("hashed_password", result.getPassword());

        assertEquals("USER", result.getRole());
        assertEquals("ACTIVE", result.getStatus());

        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode("Password123");
    }

    // ================================================
    // DK_02 - EMAIL ĐÃ TỒN TẠI
    // ================================================

    @Test
    @DisplayName("DK_02 - Từ chối email đã tồn tại")
    void DK02_duplicateEmail() {

        validDTO.setEmail("existing@example.com");

        when(userRepository.existsByEmailIgnoreCase(
                "existing@example.com"))
                .thenReturn(true);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> authService.register(validDTO)
        );

        assertEquals(
                "Email đã được sử dụng",
                ex.getMessage()
        );

        verify(userRepository, never())
                .save(any(User.class));
    }

    // ================================================
    // DK_03 - BỎ TRỐNG EMAIL
    // ================================================

    @Test
    @DisplayName("DK_03 - Bỏ trống email")
    void DK03_emptyEmail() {

        validDTO.setEmail("");

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> authService.register(validDTO)
        );

        assertEquals(
                "Email không hợp lệ",
                ex.getMessage()
        );

        verify(userRepository, never())
                .save(any(User.class));
    }

    // ================================================
    // DK_04 - BỎ TRỐNG MẬT KHẨU
    // ================================================

    @Test
    @DisplayName("DK_04 - Bỏ trống mật khẩu")
    void DK04_emptyPassword() {

        validDTO.setPassword("");

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> authService.register(validDTO)
        );

        assertEquals(
                "Mật khẩu phải có 8–72 ký tự, gồm chữ và số",
                ex.getMessage()
        );

        verify(userRepository, never())
                .save(any(User.class));
    }

    // ================================================
    // DK_05 - EMAIL SAI ĐỊNH DẠNG
    // ================================================

    @Test
    @DisplayName("DK_05 - Email không đúng định dạng")
    void DK05_invalidEmailFormat() {

        validDTO.setEmail("abc@");

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> authService.register(validDTO)
        );

        assertEquals(
                "Email không hợp lệ",
                ex.getMessage()
        );

        verify(userRepository, never())
                .save(any(User.class));
    }

    // ================================================
    // DK_06 - BỎ TRỐNG TOÀN BỘ
    // ================================================

    @Test
    @DisplayName("DK_06 - Bỏ trống toàn bộ thông tin")
    void DK06_allFieldsEmpty() {

        UserDTO dto = createDTO("", "", "");

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> authService.register(dto)
        );

        // AuthService kiểm tra họ tên trước tiên
        assertEquals(
                "Họ tên phải có từ 2 đến 255 ký tự",
                ex.getMessage()
        );

        verify(userRepository, never())
                .save(any(User.class));
    }

    // ================================================
    // DK_07 - EMAIL CÓ KHOẢNG TRẮNG
    // ================================================

    @Test
    @DisplayName("DK_07 - Chuẩn hóa khoảng trắng email")
    void DK07_emailWithSpaces() {

        validDTO.setEmail(" dk07@example.com ");

        mockSaveSuccess();

        User result = authService.register(validDTO);

        assertNotNull(result);
        assertEquals(
                "dk07@example.com",
                result.getEmail()
        );

        verify(userRepository)
                .existsByEmailIgnoreCase("dk07@example.com");

        verify(userRepository).save(any(User.class));
    }

    // ================================================
    // DK_08 - ĐĂNG KÝ HAI LẦN CÙNG EMAIL
    // ================================================

    @Test
    @DisplayName("DK_08 - Đăng ký hai lần cùng email")
    void DK08_registerTwice() {

        validDTO.setEmail("dk08@example.com");

        when(userRepository.existsByEmailIgnoreCase(
                "dk08@example.com"))
                .thenReturn(false, true);

        mockSaveSuccess();

        // Lần thứ nhất
        User first = authService.register(validDTO);
        assertNotNull(first);

        // Lần thứ hai
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> authService.register(validDTO)
        );

        assertEquals(
                "Email đã được sử dụng",
                ex.getMessage()
        );

        // Chỉ được lưu một lần
        verify(userRepository, times(1))
                .save(any(User.class));
    }

    // ================================================
    // DK_09 - KIỂM TRA DỮ LIỆU ĐƯỢC LƯU
    // ================================================

    @Test
    @DisplayName("DK_09 - Kiểm tra thông tin tài khoản được lưu")
    void DK09_verifySavedUser() {

        validDTO.setEmail("dk09@example.com");
        mockSaveSuccess();

        authService.register(validDTO);

        ArgumentCaptor<User> captor =
                ArgumentCaptor.forClass(User.class);

        verify(userRepository).save(captor.capture());

        User saved = captor.getValue();

        assertEquals(
                "dk09@example.com",
                saved.getEmail()
        );

        assertEquals(
                "Nguyễn Văn A",
                saved.getFullname()
        );

        assertEquals(
                "hashed_password",
                saved.getPassword()
        );

        assertNotEquals(
                "Password123",
                saved.getPassword()
        );

        assertEquals("USER", saved.getRole());
        assertEquals("ACTIVE", saved.getStatus());
        assertEquals("", saved.getPhone());
        assertEquals("", saved.getAddress());
    }

    // ================================================
    // DK_10 - HỌ TÊN CHỨA KÝ TỰ ĐẶC BIỆT
    // ================================================

    @Test
    @DisplayName("DK_10 - Họ tên có dấu gạch nối")
    void DK10_nameWithHyphen() {

        validDTO.setFullname("Nguyễn A-B");
        validDTO.setEmail("dk10@example.com");

        mockSaveSuccess();

        User result = authService.register(validDTO);

        assertNotNull(result);

        assertEquals(
                "Nguyễn A-B",
                result.getFullname()
        );

        verify(userRepository).save(any(User.class));
    }

    // ================================================
    // DK_11 - BỎ TRỐNG HỌ TÊN
    // ================================================

    @Test
    @DisplayName("DK_11 - Bỏ trống họ tên")
    void DK11_emptyFullname() {

        validDTO.setFullname("");

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> authService.register(validDTO)
        );

        assertEquals(
                "Họ tên phải có từ 2 đến 255 ký tự",
                ex.getMessage()
        );

        verify(userRepository, never())
                .save(any(User.class));
    }

    // ================================================
    // DK_12 - EMAIL KHÁC CHỮ HOA / THƯỜNG
    // ================================================

    @Test
    @DisplayName("DK_12 - Email trùng khác chữ hoa thường")
    void DK12_emailCaseInsensitive() {

        validDTO.setEmail("ABC@example.com");

        when(userRepository.existsByEmailIgnoreCase(
                "abc@example.com"))
                .thenReturn(true);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> authService.register(validDTO)
        );

        assertEquals(
                "Email đã được sử dụng",
                ex.getMessage()
        );

        verify(userRepository)
                .existsByEmailIgnoreCase("abc@example.com");

        verify(userRepository, never())
                .save(any(User.class));
    }

    // ================================================
    // DK_13 - MẬT KHẨU DƯỚI 8 KÝ TỰ
    // ================================================

    @Test
    @DisplayName("DK_13 - Mật khẩu dưới 8 ký tự")
    void DK13_shortPassword() {

        validDTO.setPassword("Ab123");

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> authService.register(validDTO)
        );

        assertEquals(
                "Mật khẩu phải có 8–72 ký tự, gồm chữ và số",
                ex.getMessage()
        );

        verify(userRepository, never())
                .save(any(User.class));
    }

    // ================================================
    // DK_14 - MẬT KHẨU KHÔNG CHỨA SỐ
    // ================================================

    @Test
    @DisplayName("DK_14 - Mật khẩu không chứa số")
    void DK14_passwordWithoutNumber() {

        validDTO.setPassword("PasswordABC");

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> authService.register(validDTO)
        );

        assertEquals(
                "Mật khẩu phải có 8–72 ký tự, gồm chữ và số",
                ex.getMessage()
        );

        verify(userRepository, never())
                .save(any(User.class));
    }

    // ================================================
    // DK_15 - HỌ TÊN VƯỢT 255 KÝ TỰ
    // ================================================

    @Test
    @DisplayName("DK_15 - Họ tên vượt quá 255 ký tự")
    void DK15_fullnameTooLong() {

        validDTO.setFullname("A".repeat(256));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> authService.register(validDTO)
        );

        assertEquals(
                "Họ tên phải có từ 2 đến 255 ký tự",
                ex.getMessage()
        );

        verify(userRepository, never())
                .save(any(User.class));
    }

    // ================================================
    // DK_16 - USER DTO NULL
    // ================================================

    @Test
    @DisplayName("DK_16 - Dữ liệu đăng ký null")
    void DK16_nullRegisterDTO() {

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> authService.register(null)
        );

        assertEquals(
                "Dữ liệu đăng ký không hợp lệ",
                ex.getMessage()
        );

        verifyNoInteractions(userRepository);
    }
}
