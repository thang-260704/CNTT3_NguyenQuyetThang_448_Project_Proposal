import { fireEvent, render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import ProductCard from '../components/ProductCard';
import { useCartStore } from '../store/cartStore';

const renderCard = (product) =>
  render(
    <MemoryRouter>
      <ProductCard product={product} />
    </MemoryRouter>,
  );

describe('ProductCard (FR-02)', () => {
  beforeEach(() => useCartStore.setState({ items: [] }));

  it('TC-FE-08: hiển thị ảnh, tên, giá và nút thêm vào giỏ', () => {
    renderCard({ id: 7, name: 'Bàn phím cơ', price: 990000, stock: 5, imageUrl: 'x.jpg' });
    expect(screen.getByText('Bàn phím cơ')).toBeInTheDocument();
    expect(screen.getByText(/990\.000/)).toBeInTheDocument();
    expect(screen.getByRole('img', { name: 'Bàn phím cơ' })).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: 'Thêm vào giỏ hàng' }));
    expect(useCartStore.getState().items[0].id).toBe(7);
  });

  it('TC-FE-09: sản phẩm hết hàng thì khóa nút thêm vào giỏ', () => {
    renderCard({ id: 8, name: 'Sách', price: 100000, stock: 0 });
    expect(screen.getByRole('button', { name: 'Hết hàng' })).toBeDisabled();
  });
});
