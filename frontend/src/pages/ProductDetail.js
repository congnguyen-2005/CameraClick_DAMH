import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { productApi } from '../api/productApi';
import { useCart } from '../context/CartContext';
import { useAuth } from '../context/AuthContext';
import './ProductDetail.css';

export default function ProductDetail() {
  const { id } = useParams();
  const [product, setProduct] = useState(null);
  const [quantity, setQuantity] = useState(1);
  const [color, setColor] = useState('');
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [adding, setAdding] = useState(false);
  const { addToCart } = useCart();
  const { user } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    productApi.getById(id).then((res) => setProduct(res.data));
  }, [id]);

  const formatPrice = (price) =>
    new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(price);

  const colorOptions = (product?.colors || '').split(',').map((c) => c.trim()).filter(Boolean);

  const handleAddToCart = async () => {
    if (!user) {
      navigate('/login');
      return;
    }
    setError('');
    setAdding(true);
    try {
      await addToCart(product.id, quantity, color);
      setMessage('Đã thêm vào giỏ hàng!');
      setTimeout(() => setMessage(''), 2000);
    } catch (err) {
      setError(err.response?.data?.message || 'Không thể thêm vào giỏ hàng, vui lòng thử lại.');
    } finally {
      setAdding(false);
    }
  };

  if (!product) return <p className="loading-text">Đang tải...</p>;

  return (
    <div className="product-detail-page">
      <div className="product-detail-image">
        {product.imageUrl ? <img src={product.imageUrl} alt={product.name} /> : <div className="placeholder">📷</div>}
      </div>
      <div className="product-detail-info">
        <h1>{product.name}</h1>
        <p className="bd-brand">Thương hiệu: {product.brandName || 'Đang cập nhật'}</p>
        <p className="bd-meta">
          Danh mục: {product.categoryName || 'Chưa phân loại'}
          {product.sku && ` | Mã SP: ${product.sku}`}
        </p>
        <p className="bd-price">{formatPrice(product.price)}</p>
        <p className="bd-stock">
          {product.stock > 0 ? `Còn ${product.stock} sản phẩm trong kho` : 'Hiện đã hết hàng'}
        </p>
        <p className="bd-description">{product.description || 'Chưa có mô tả cho sản phẩm này.'}</p>

        {colorOptions.length > 0 && (
          <div className="bd-options">
            <span className="bd-options-label">Màu thân máy:</span>
            {colorOptions.map((c) => (
              <button
                key={c}
                type="button"
                className={`bd-option-btn ${color === c ? 'active' : ''}`}
                onClick={() => setColor(c)}
              >
                {c}
              </button>
            ))}
          </div>
        )}

        {product.stock > 0 && (
          <div className="bd-actions">
            <input
              type="number"
              min="1"
              max={product.stock}
              value={quantity}
              onChange={(e) => setQuantity(Math.max(1, Number(e.target.value)))}
            />
            <button className="btn-add-cart" onClick={handleAddToCart} disabled={adding}>
              {adding ? 'Đang thêm...' : 'Thêm vào giỏ hàng'}
            </button>
          </div>
        )}
        {message && <p className="bd-success">{message}</p>}
        {error && <p className="bd-error">{error}</p>}
      </div>
    </div>
  );
}
