import React from 'react';
import { Link } from 'react-router-dom';
import './ProductCard.css';

export default function ProductCard({ product }) {
  const formatPrice = (price) =>
    new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(price);

  return (
    <Link to={`/products/${product.id}`} className="product-card">
      <div className="product-cover">
        {product.imageUrl ? (
          <img src={product.imageUrl} alt={product.name} />
        ) : (
          <div className="product-cover-placeholder">📷</div>
        )}
      </div>
      <div className="product-info">
        <h3 className="product-title">{product.name}</h3>
        <p className="product-brand">{product.brandName || 'Đang cập nhật'}</p>
        <p className="product-price">{formatPrice(product.price)}</p>
        {product.stock === 0 && <span className="badge-out-of-stock">Hết hàng</span>}
      </div>
    </Link>
  );
}
