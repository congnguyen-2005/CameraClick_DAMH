import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useCart } from '../context/CartContext';
import './Cart.css';

export default function Cart() {
  const { cart, updateQuantity, removeItem } = useCart();
  const navigate = useNavigate();

  const formatPrice = (price) =>
    new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(price);

  if (!cart.items || cart.items.length === 0) {
    return (
      <div className="cart-page">
        <h2>Giỏ hàng của bạn</h2>
        <p className="empty-cart">Giỏ hàng đang trống.</p>
        <Link to="/" className="btn-primary-inline">Tiếp tục mua sắm</Link>
      </div>
    );
  }

  return (
    <div className="cart-page">
      <h2>Giỏ hàng của bạn</h2>
      <div className="cart-list">
        {cart.items.map((item) => (
          <div className="cart-item" key={item.productId}>
            <div className="cart-item-image">
              {item.imageUrl ? <img src={item.imageUrl} alt={item.name} /> : <div className="placeholder">📷</div>}
            </div>
            <div className="cart-item-info">
              <h4>{item.name}</h4>
              {item.color && (
                <p className="cart-item-variant">Màu: {item.color}</p>
              )}
              <p>{formatPrice(item.price)}</p>
            </div>
            <div className="cart-item-qty">
              <button onClick={() => updateQuantity(item.productId, item.quantity - 1)}>-</button>
              <span>{item.quantity}</span>
              <button onClick={() => updateQuantity(item.productId, item.quantity + 1)}>+</button>
            </div>
            <div className="cart-item-total">{formatPrice(item.price * item.quantity)}</div>
            <button className="cart-item-remove" onClick={() => removeItem(item.productId)}>✕</button>
          </div>
        ))}
      </div>

      <div className="cart-summary">
        <div className="cart-summary-row">
          <span>Tổng cộng ({cart.totalItems} sản phẩm)</span>
          <span className="cart-total-amount">{formatPrice(cart.totalAmount)}</span>
        </div>
        <button className="btn-checkout" onClick={() => navigate('/checkout')}>Tiến hành thanh toán</button>
      </div>
    </div>
  );
}
