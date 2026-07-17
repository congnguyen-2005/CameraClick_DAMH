import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useCart } from '../context/CartContext';
import { orderApi } from '../api/orderApi';
import './Checkout.css';

export default function Checkout() {
  const { cart, clearCart } = useCart();
  const navigate = useNavigate();
  const [form, setForm] = useState({
    shippingAddress: '', receiverName: '', receiverPhone: '', paymentMethod: 'COD',
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const formatPrice = (price) =>
    new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(price);

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      const items = cart.items.map((i) => ({ productId: i.productId, quantity: i.quantity, color: i.color }));
      const res = await orderApi.createOrder({ ...form, items });
      await clearCart();
      navigate(`/orders`, { state: { newOrderId: res.data.id } });
    } catch (err) {
      setError(err.response?.data?.message || 'Đặt hàng thất bại, vui lòng thử lại');
    } finally {
      setLoading(false);
    }
  };

  if (!cart.items || cart.items.length === 0) {
    return <p className="loading-text">Giỏ hàng trống, không thể thanh toán.</p>;
  }

  return (
    <div className="checkout-page">
      <h2>Thanh toán đơn hàng</h2>
      <div className="checkout-grid">
        <form className="checkout-form" onSubmit={handleSubmit}>
          {error && <div className="auth-error">{error}</div>}
          <label>Họ tên người nhận</label>
          <input name="receiverName" value={form.receiverName} onChange={handleChange} required />
          <label>Số điện thoại</label>
          <input name="receiverPhone" value={form.receiverPhone} onChange={handleChange} required />
          <label>Địa chỉ giao hàng</label>
          <textarea name="shippingAddress" value={form.shippingAddress} onChange={handleChange} required />
          <label>Phương thức thanh toán</label>
          <select name="paymentMethod" value={form.paymentMethod} onChange={handleChange}>
            <option value="COD">Thanh toán khi nhận hàng (COD)</option>
            <option value="ONLINE">Thanh toán online</option>
          </select>
          <button type="submit" className="btn-primary" disabled={loading}>
            {loading ? 'Đang xử lý...' : 'Đặt hàng'}
          </button>
        </form>

        <div className="checkout-summary">
          <h3>Đơn hàng của bạn</h3>
          {cart.items.map((item) => (
            <div className="checkout-item" key={item.productId}>
              <span>{item.name}{item.color && ` (${item.color})`} x{item.quantity}</span>
              <span>{formatPrice(item.price * item.quantity)}</span>
            </div>
          ))}
          <div className="checkout-total">
            <span>Tổng cộng</span>
            <span>{formatPrice(cart.totalAmount)}</span>
          </div>
        </div>
      </div>
    </div>
  );
}
