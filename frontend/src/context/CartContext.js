import React, { createContext, useCallback, useContext, useState } from 'react';
import { cartApi } from '../api/cartApi';
import { useAuth } from './AuthContext';
const CartContext = createContext(null);

export function CartProvider({ children }) {
  const { user } = useAuth();
  const [cart, setCart] = useState({ items: [], totalAmount: 0, totalItems: 0 });

  const refreshCart = useCallback(async () => {
    if (!user) {
      setCart({ items: [], totalAmount: 0, totalItems: 0 });
      return;
    }
    try {
      const res = await cartApi.getCart();
      setCart(res.data);
    } catch (e) {
      // silently ignore - user might not be logged in yet
    }
  }, [user]);

  const addToCart = async (productId, quantity, color) => {
    const res = await cartApi.addToCart({
      productId,
      quantity: quantity || 1,
      color: color || undefined,
    });
    setCart(res.data);
    return res.data;
  };

  const updateQuantity = async (productId, quantity) => {
    const res = await cartApi.updateQuantity({ productId, quantity });
    setCart(res.data);
  };

  const removeItem = async (productId) => {
    const res = await cartApi.removeItem(productId);
    setCart(res.data);
  };

  const clearCart = async () => {
    await cartApi.clearCart();
    setCart({ items: [], totalAmount: 0, totalItems: 0 });
  };

  return (
    <CartContext.Provider value={{ cart, refreshCart, addToCart, updateQuantity, removeItem, clearCart }}>
      {children}
    </CartContext.Provider>
  );
}

export function useCart() {
  return useContext(CartContext);
}
