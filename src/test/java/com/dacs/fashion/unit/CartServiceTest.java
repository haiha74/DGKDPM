
package com.dacs.fashion.unit;

import com.dacs.fashion.dto.CartItemDTO;
import com.dacs.fashion.entity.Cart;
import com.dacs.fashion.entity.CartItem;
import com.dacs.fashion.entity.ProductVariant;
import com.dacs.fashion.entity.User;
import com.dacs.fashion.repository.CartRepository;
import com.dacs.fashion.repository.CartItemRepository;
import com.dacs.fashion.repository.ProductVariantRepository;
import com.dacs.fashion.repository.UserRepository;
import com.dacs.fashion.service.CartService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductVariantRepository variantRepository;

    @InjectMocks
    private CartService cartService;

    private User user;
    private Cart cart;
    private CartItem item;
    private ProductVariant variant;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUserId(1L);

        cart = new Cart();
        cart.setCartId(10L);
        cart.setUser(user);
        cart.setItems(new ArrayList<>());

        variant = new ProductVariant();
        variant.setVariantId(100L);
        variant.setSize("M");
        variant.setColor("Đen");
        variant.setStock(10);
        variant.setStatus("ACTIVE");

        item = new CartItem();
        item.setCartItemId(15L);
        item.setCart(cart);
        item.setVariant(variant);
        item.setQuantity(2);

        cart.getItems().add(item);
    }

    // ==================================================
    // HAM HO TRO
    // ==================================================

    private void mockCart() {
        when(cartRepository.findByUser_UserId(1L))
                .thenReturn(Optional.of(cart));
    }

    private void mockVariant() {
        when(variantRepository.findById(100L))
                .thenReturn(Optional.of(variant));
    }

    private void mockItem() {
        when(cartItemRepository.findById(15L))
                .thenReturn(Optional.of(item));
    }

    private CartItemDTO createDTO(
            Long variantId, Integer quantity) {

        CartItemDTO dto = new CartItemDTO();
        dto.setUserId(1L);
        dto.setVariantId(variantId);
        dto.setQuantity(quantity);
        return dto;
    }

    private CartItemDTO dto(Integer quantity) {
        return createDTO(100L, quantity);
    }

    // ==================================================
    // PHAN 1: 15 UNIT TEST GOC - TC25 DEN TC39
    // ==================================================

    @Test
    @DisplayName("TC25 - Xem giỏ hàng có sản phẩm")
    void TC25_getCartWithItems() {
        mockCart();

        Cart result = cartService.getCart(1L);

        assertNotNull(result);
        assertEquals(1, result.getItems().size());
        assertEquals(2, result.getItems().get(0).getQuantity());
        assertEquals("M", result.getItems().get(0)
                .getVariant().getSize());
        assertEquals("Đen", result.getItems().get(0)
                .getVariant().getColor());
    }

    @Test
    @DisplayName("TC26 - Xem giỏ hàng trống")
    void TC26_getEmptyCart() {
        cart.getItems().clear();
        mockCart();

        Cart result = cartService.getCart(1L);

        assertNotNull(result);
        assertTrue(result.getItems().isEmpty());
    }

    @Test
    @DisplayName("TC27 - Thêm sản phẩm hợp lệ")
    void TC27_addValidProduct() {
        mockCart();
        mockVariant();

        when(cartItemRepository.findByCartAndVariant(
                cart, variant))
                .thenReturn(Optional.empty());

        cartService.addToCart(dto(1));

        verify(cartItemRepository).save(
                argThat(saved ->
                        saved.getQuantity() == 1
                        && saved.getVariant() == variant
                        && saved.getCart() == cart)
        );

        verify(cartItemRepository).flush();
    }

    @Test
    @DisplayName("TC28 - Thêm 3 sản phẩm khi tồn kho 10")
    void TC28_addMultipleValidQuantity() {
        mockCart();
        mockVariant();

        when(cartItemRepository.findByCartAndVariant(
                cart, variant))
                .thenReturn(Optional.empty());

        cartService.addToCart(dto(3));

        verify(cartItemRepository).save(
                argThat(saved -> saved.getQuantity() == 3)
        );
    }

    @Test
    @DisplayName("TC29 - Thêm vượt tồn kho")
    void TC29_addOverStock() {
        mockCart();
        mockVariant();

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> cartService.addToCart(dto(11))
        );

        assertEquals("Số lượng vượt quá tồn kho",
                ex.getMessage());

        verify(cartItemRepository, never())
                .save(any(CartItem.class));
    }

    @Test
    @DisplayName("TC30 - Thêm biến thể không tồn tại")
    void TC30_invalidVariant() {
        mockCart();

        when(variantRepository.findById(999L))
                .thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> cartService.addToCart(
                        createDTO(999L, 1))
        );

        assertEquals("Không tìm thấy biến thể sản phẩm",
                ex.getMessage());
    }

    @Test
    @DisplayName("TC31 - Không thêm giỏ khi thiếu userId")
    void TC31_addWithoutUserId() {
        CartItemDTO data = dto(1);
        data.setUserId(null);

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> cartService.addToCart(data)
        );

        assertEquals("Không xác định được người dùng",
                ex.getMessage());

        verifyNoInteractions(cartItemRepository);
    }

    @Test
    @DisplayName("TC32 - Thêm lại cùng biến thể")
    void TC32_addExistingVariant() {
        item.setQuantity(1);
        mockCart();
        mockVariant();

        when(cartItemRepository.findByCartAndVariant(
                cart, variant))
                .thenReturn(Optional.of(item));

        cartService.addToCart(dto(2));

        assertEquals(3, item.getQuantity());
        verify(cartItemRepository).save(item);
    }

    @Test
    @DisplayName("TC33 - Thêm hai biến thể khác nhau")
    void TC33_addDifferentVariants() {
        ProductVariant variant2 = new ProductVariant();
        variant2.setVariantId(101L);
        variant2.setSize("L");
        variant2.setColor("Trắng");
        variant2.setStock(10);
        variant2.setStatus("ACTIVE");

        mockCart();

        when(variantRepository.findById(100L))
                .thenReturn(Optional.of(variant));

        when(variantRepository.findById(101L))
                .thenReturn(Optional.of(variant2));

        when(cartItemRepository.findByCartAndVariant(
                eq(cart), any(ProductVariant.class)))
                .thenReturn(Optional.empty());

        cartService.addToCart(dto(1));
        cartService.addToCart(createDTO(101L, 1));

        ArgumentCaptor<CartItem> captor =
                ArgumentCaptor.forClass(CartItem.class);

        verify(cartItemRepository, times(2))
                .save(captor.capture());

        List<CartItem> saved = captor.getAllValues();

        assertEquals(2, saved.size());
        assertEquals(100L,
                saved.get(0).getVariant().getVariantId());
        assertEquals(101L,
                saved.get(1).getVariant().getVariantId());
    }

    @Test
    @DisplayName("TC34 - Tăng số lượng từ 1 lên 3")
    void TC34_increaseQuantity() {
        item.setQuantity(1);
        mockItem();
        mockCart();

        Cart result = cartService.updateQuantity(15L, 3);

        assertNotNull(result);
        assertEquals(3, item.getQuantity());
        verify(cartItemRepository).save(item);
    }

    @Test
    @DisplayName("TC35 - Giảm số lượng từ 3 xuống 1")
    void TC35_decreaseQuantity() {
        item.setQuantity(3);
        mockItem();
        mockCart();

        cartService.updateQuantity(15L, 1);

        assertEquals(1, item.getQuantity());
        verify(cartItemRepository).save(item);
    }

    @Test
    @DisplayName("TC36 - Cập nhật vượt tồn kho")
    void TC36_updateOverStock() {
        item.setQuantity(1);
        mockItem();

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> cartService.updateQuantity(15L, 11)
        );

        assertEquals("Chỉ còn 10 sản phẩm trong kho",
                ex.getMessage());

        assertEquals(1, item.getQuantity());
    }

    @Test
    @DisplayName("TC37 - Số lượng 0 xóa item theo code hiện tại")
    void TC37_invalidQuantity() {
        item.setQuantity(1);
        mockItem();
        mockCart();

        Cart result = cartService.updateQuantity(15L, 0);

        assertNotNull(result);
        assertTrue(cart.getItems().isEmpty());

        verify(cartRepository).saveAndFlush(cart);
    }

    @Test
    @DisplayName("TC38 - Xóa đúng một sản phẩm")
    void TC38_removeOneItem() {
        ProductVariant variant2 = new ProductVariant();
        variant2.setVariantId(101L);

        CartItem other = new CartItem();
        other.setCartItemId(16L);
        other.setCart(cart);
        other.setVariant(variant2);
        other.setQuantity(1);

        cart.getItems().add(other);
        mockItem();

        cartService.removeItem(15L);

        assertEquals(1, cart.getItems().size());
        assertSame(other, cart.getItems().get(0));

        verify(cartRepository).saveAndFlush(cart);
    }

    @Test
    @DisplayName("TC39 - Xóa sản phẩm cuối cùng")
    void TC39_removeLastItem() {
        assertEquals(1, cart.getItems().size());
        mockItem();

        cartService.removeItem(15L);

        assertTrue(cart.getItems().isEmpty());
        verify(cartRepository).saveAndFlush(cart);
    }

    // ==================================================
    // PHAN 2: UNIT TEST BO SUNG - QUYEN SO HUU
    // ==================================================

    @Test
    @DisplayName("UT16 - User sở hữu CartItem")
    void UT16_validOwner() {
        mockItem();

        assertTrue(cartService.isItemOwner(15L, 1L));
    }

    @Test
    @DisplayName("UT17 - User khác không sở hữu CartItem")
    void UT17_invalidOwner() {
        mockItem();

        assertFalse(cartService.isItemOwner(15L, 2L));
    }

    @Test
    @DisplayName("UT18 - Kiểm tra quyền với itemId null")
    void UT18_nullItemId() {
        assertFalse(cartService.isItemOwner(null, 1L));
        verifyNoInteractions(cartItemRepository);
    }

    @Test
    @DisplayName("UT19 - Kiểm tra quyền với userId null")
    void UT19_nullOwnerId() {
        assertFalse(cartService.isItemOwner(15L, null));
        verifyNoInteractions(cartItemRepository);
    }

    @Test
    @DisplayName("UT20 - Kiểm tra quyền item không tồn tại")
    void UT20_ownerItemNotFound() {
        when(cartItemRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                RuntimeException.class,
                () -> cartService.isItemOwner(999L, 1L)
        );
    }

    @Test
    @DisplayName("UT21 - Item không có Cart")
    void UT21_ownerCartNull() {
        item.setCart(null);
        mockItem();

        assertFalse(cartService.isItemOwner(15L, 1L));
    }

    @Test
    @DisplayName("UT22 - Cart không có User")
    void UT22_ownerUserNull() {
        cart.setUser(null);
        mockItem();

        assertFalse(cartService.isItemOwner(15L, 1L));
    }

    // ==================================================
    // PHAN 3: TAO VA XEM GIO HANG
    // ==================================================

    @Test
    @DisplayName("UT23 - Tạo giỏ cho User mới")
    void UT23_createCart() {
        when(cartRepository.findByUser_UserId(1L))
                .thenReturn(Optional.empty());

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(cartRepository.save(any(Cart.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Cart result = cartService.getCart(1L);

        assertNotNull(result);
        assertEquals(1L, result.getUser().getUserId());
        assertTrue(result.getItems().isEmpty());

        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    @DisplayName("UT24 - Không tạo giỏ nếu User không tồn tại")
    void UT24_createCartUserNotFound() {
        when(cartRepository.findByUser_UserId(1L))
                .thenReturn(Optional.empty());

        when(userRepository.findById(1L))
                .thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> cartService.getCart(1L)
        );

        assertEquals("Không tìm thấy tài khoản người dùng",
                ex.getMessage());

        verify(cartRepository, never())
                .save(any(Cart.class));
    }

    @Test
    @DisplayName("UT25 - Tự tạo items khi null")
    void UT25_cartItemsNull() {
        cart.setItems(null);
        mockCart();

        Cart result = cartService.getCart(1L);

        assertNotNull(result.getItems());
        assertTrue(result.getItems().isEmpty());
    }

    // ==================================================
    // PHAN 4: XOA TOAN BO GIO HANG
    // ==================================================

    @Test
    @DisplayName("UT26 - Xóa toàn bộ giỏ có sản phẩm")
    void UT26_clearCartSuccess() {
        mockCart();

        cartService.clearCart(1L);

        assertTrue(cart.getItems().isEmpty());
        verify(cartRepository).save(cart);
        verify(cartRepository).flush();
    }

    @Test
    @DisplayName("UT27 - Xóa giỏ đang trống")
    void UT27_clearEmptyCart() {
        cart.getItems().clear();
        mockCart();

        cartService.clearCart(1L);

        assertTrue(cart.getItems().isEmpty());
        verify(cartRepository).save(cart);
        verify(cartRepository).flush();
    }

    @Test
    @DisplayName("UT28 - Xóa giỏ không tồn tại")
    void UT28_clearCartNotFound() {
        when(cartRepository.findByUser_UserId(1L))
                .thenReturn(Optional.empty());

        assertDoesNotThrow(
                () -> cartService.clearCart(1L)
        );

        verify(cartRepository, never())
                .save(any(Cart.class));
    }

    @Test
    @DisplayName("UT29 - Xóa giỏ có items null")
    void UT29_clearCartItemsNull() {
        cart.setItems(null);
        mockCart();

        assertDoesNotThrow(
                () -> cartService.clearCart(1L)
        );

        verify(cartRepository, never())
                .save(any(Cart.class));
    }

    // ==================================================
    // PHAN 5: THEM SAN PHAM DU LIEU BAT THUONG
    // ==================================================

    @Test
    @DisplayName("UT30 - DTO null bị từ chối")
    void UT30_addDtoNull() {
        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> cartService.addToCart(null)
        );

        assertEquals("Dữ liệu thêm giỏ hàng không hợp lệ",
                ex.getMessage());
    }

    @Test
    @DisplayName("UT31 - VariantId null bị từ chối")
    void UT31_variantIdNull() {
        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> cartService.addToCart(
                        createDTO(null, 1))
        );

        assertEquals("Vui lòng chọn size và màu sắc",
                ex.getMessage());
    }

    @Test
    @DisplayName("UT32 - Không thêm biến thể INACTIVE")
    void UT32_inactiveVariant() {
        variant.setStatus("INACTIVE");
        mockCart();
        mockVariant();

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> cartService.addToCart(dto(1))
        );

        assertEquals("Biến thể sản phẩm hiện không hoạt động",
                ex.getMessage());
    }

    @Test
    @DisplayName("UT33 - Không thêm sản phẩm hết hàng")
    void UT33_outOfStock() {
        variant.setStock(0);
        mockCart();
        mockVariant();

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> cartService.addToCart(dto(1))
        );

        assertEquals("Sản phẩm đã hết hàng",
                ex.getMessage());
    }

    @Test
    @DisplayName("UT34 - Tồn kho null được xem là hết hàng")
    void UT34_nullStock() {
        variant.setStock(null);
        mockCart();
        mockVariant();

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> cartService.addToCart(dto(1))
        );

        assertEquals("Sản phẩm đã hết hàng",
                ex.getMessage());
    }

    @Test
    @DisplayName("UT35 - Cộng dồn vượt tồn kho")
    void UT35_existingItemOverStock() {
        item.setQuantity(8);
        mockCart();
        mockVariant();

        when(cartItemRepository.findByCartAndVariant(
                cart, variant))
                .thenReturn(Optional.of(item));

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> cartService.addToCart(dto(5))
        );

        assertTrue(ex.getMessage().contains(
                "Tổng số lượng trong giỏ vượt quá tồn kho"));

        assertEquals(8, item.getQuantity());

        verify(cartItemRepository, never())
                .save(any(CartItem.class));
    }

    @Test
    @DisplayName("UT36 - Quantity cũ null khi cộng dồn")
    void UT36_existingQuantityNull() {
        item.setQuantity(null);
        mockCart();
        mockVariant();

        when(cartItemRepository.findByCartAndVariant(
                cart, variant))
                .thenReturn(Optional.of(item));

        cartService.addToCart(dto(2));

        assertEquals(2, item.getQuantity());
        verify(cartItemRepository).save(item);
    }

    // ==================================================
    // PHAN 6: CAP NHAT SO LUONG BAT THUONG
    // ==================================================

    @Test
    @DisplayName("UT37 - Cập nhật với itemId null")
    void UT37_updateNullItemId() {
        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> cartService.updateQuantity(null, 2)
        );

        assertEquals("Thiếu mã sản phẩm trong giỏ",
                ex.getMessage());
    }

    @Test
    @DisplayName("UT38 - Cập nhật item không tồn tại")
    void UT38_updateItemNotFound() {
        when(cartItemRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                RuntimeException.class,
                () -> cartService.updateQuantity(999L, 2)
        );
    }

    @Test
    @DisplayName("UT39 - Item không có Cart")
    void UT39_updateCartNull() {
        item.setCart(null);
        mockItem();

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> cartService.updateQuantity(15L, 2)
        );

        assertEquals("Giỏ hàng không hợp lệ",
                ex.getMessage());
    }

    @Test
    @DisplayName("UT40 - Item không có Variant")
    void UT40_updateVariantNull() {
        item.setVariant(null);
        mockItem();

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> cartService.updateQuantity(15L, 2)
        );

        assertEquals("Không tìm thấy biến thể sản phẩm",
                ex.getMessage());
    }

    @Test
    @DisplayName("UT41 - Cập nhật khi hết hàng")
    void UT41_updateOutOfStock() {
        variant.setStock(0);
        mockItem();

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> cartService.updateQuantity(15L, 2)
        );

        assertEquals("Sản phẩm đã hết hàng",
                ex.getMessage());
    }

    // ==================================================
    // PHAN 7: XOA SAN PHAM BAT THUONG
    // ==================================================

    @Test
    @DisplayName("UT42 - Xóa itemId null")
    void UT42_removeNullId() {
        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> cartService.removeItem(null)
        );

        assertEquals("Thiếu mã sản phẩm trong giỏ",
                ex.getMessage());
    }

    @Test
    @DisplayName("UT43 - Xóa item không tồn tại")
    void UT43_removeItemNotFound() {
        when(cartItemRepository.findById(999L))
                .thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> cartService.removeItem(999L)
        );

        assertEquals("Không tìm thấy sản phẩm trong giỏ",
                ex.getMessage());
    }

    @Test
    @DisplayName("UT44 - Xóa item không có Cart")
    void UT44_removeCartNull() {
        item.setCart(null);
        mockItem();

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> cartService.removeItem(15L)
        );

        assertEquals("Giỏ hàng không hợp lệ",
                ex.getMessage());
    }

    @Test
    @DisplayName("UT45 - Item không thuộc collection Cart")
    void UT45_removeMissingFromCollection() {
        cart.getItems().clear();
        mockItem();

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> cartService.removeItem(15L)
        );

        assertEquals(
                "Sản phẩm không tồn tại trong giỏ hàng",
                ex.getMessage()
        );

        verify(cartRepository, never())
                .saveAndFlush(any(Cart.class));
    }
}
