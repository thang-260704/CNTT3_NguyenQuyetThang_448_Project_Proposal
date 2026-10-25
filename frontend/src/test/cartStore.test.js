import { useCartStore } from '../store/cartStore';

const product = { id: 1, name: 'Tai nghe', price: 500000, stock: 3, imageUrl: '' };

describe('cartStore (FR-03 giỏ hàng phía client)', () => {
  beforeEach(() => useCartStore.setState({ items: [] }));

  it('TC-FE-01: thêm sản phẩm mới vào giỏ', () => {
    useCartStore.getState().addItem(product, 1);
    expect(useCartStore.getState().items).toHaveLength(1);
    expect(useCartStore.getState().totalQuantity()).toBe(1);
  });

  it('TC-FE-02: thêm trùng sản phẩm thì cộng dồn số lượng, không vượt tồn kho', () => {
    const { addItem } = useCartStore.getState();
    addItem(product, 2);
    addItem(product, 5);
    expect(useCartStore.getState().items[0].quantity).toBe(3);
  });

  it('TC-FE-03: sửa số lượng giới hạn trong khoảng 1..tồn kho', () => {
    useCartStore.getState().addItem(product, 1);
    useCartStore.getState().updateQuantity(1, 0);
    expect(useCartStore.getState().items[0].quantity).toBe(1);
    useCartStore.getState().updateQuantity(1, 99);
    expect(useCartStore.getState().items[0].quantity).toBe(3);
  });

  it('TC-FE-04: xóa sản phẩm và tính tổng tiền', () => {
    const { addItem } = useCartStore.getState();
    addItem(product, 2);
    addItem({ id: 2, name: 'Áo', price: 100000, stock: 10 }, 3);
    expect(useCartStore.getState().totalPrice()).toBe(1300000);
    useCartStore.getState().removeItem(1);
    expect(useCartStore.getState().totalPrice()).toBe(300000);
  });

  it('TC-FE-05: giỏ hàng được lưu vào LocalStorage', () => {
    useCartStore.getState().addItem(product, 1);
    const saved = JSON.parse(localStorage.getItem('quickshop-cart'));
    expect(saved.state.items[0].id).toBe(1);
  });
});
