import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useCart } from '../context/CartContext';
import './Navbar.css';

export default function Navbar() {
  const { user, logout, isAdmin } = useAuth();
  const { cart } = useCart();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <header className="navbar">
      <div className="navbar-inner">
        <Link to="/" className="navbar-brand">Camera<span className="navbar-brand-accent">Click</span></Link>

        <nav className="navbar-links">
          <Link to="/">Trang chủ</Link>
          {isAdmin && <Link to="/admin">Quản trị</Link>}
        </nav>

        <div className="navbar-actions">
          {user ? (
            <>
              <Link to="/cart" className="cart-link">
                🛒 Giỏ hàng
                {cart.totalItems > 0 && <span className="cart-badge">{cart.totalItems}</span>}
              </Link>
              <Link to="/orders">Đơn hàng</Link>
              <Link to="/profile">{user.username}</Link>
              <button className="btn-link" onClick={handleLogout}>Đăng xuất</button>
            </>
          ) : (
            <>
              <Link to="/login">Đăng nhập</Link>
              <Link to="/register" className="btn-primary-sm">Đăng ký</Link>
            </>
          )}
        </div>
      </div>
    </header>
  );
}
