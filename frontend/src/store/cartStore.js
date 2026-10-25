import { create } from 'zustand';
import { persist } from 'zustand/middleware';

// Giỏ hàng lưu phía Client (Zustand + LocalStorage) theo SRS FR-03.
export const useCartStore = create(
  persist(
    (set, get) => ({
      items: [],

      addItem: (product, quantity = 1) => {
        const items = [...get().items];
        const index = items.findIndex((i) => i.id === product.id);
        if (index >= 0) {
          const nextQty = Math.min(items[index].quantity + quantity, product.stock ?? Infinity);
          items[index] = { ...items[index], quantity: nextQty, stock: product.stock };
        } else {
          items.push({
            id: product.id,
            name: product.name,
            price: Number(product.price),
            imageUrl: product.imageUrl,
            stock: product.stock,
            quantity: Math.min(quantity, product.stock ?? quantity),
          });
        }
        set({ items });
      },

      updateQuantity: (id, quantity) => {
        set({
          items: get()
            .items.map((i) => (i.id === id ? { ...i, quantity: Math.max(1, Math.min(quantity, i.stock ?? quantity)) } : i)),
        });
      },

      removeItem: (id) => set({ items: get().items.filter((i) => i.id !== id) }),

      clear: () => set({ items: [] }),

      totalQuantity: () => get().items.reduce((sum, i) => sum + i.quantity, 0),

      totalPrice: () => get().items.reduce((sum, i) => sum + i.price * i.quantity, 0),
    }),
    { name: 'quickshop-cart' },
  ),
);
