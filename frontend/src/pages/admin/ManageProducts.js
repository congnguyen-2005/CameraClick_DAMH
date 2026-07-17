import React, { useEffect, useState } from 'react';
import { productApi } from '../../api/productApi';

const emptyForm = {
  name: '', sku: '', description: '', price: '', stock: '', imageUrl: '',
  colors: '', categoryId: '', brandId: '',
};

export default function ManageProducts() {
  const [products, setProducts] = useState([]);
  const [categories, setCategories] = useState([]);
  const [brands, setBrands] = useState([]);
  const [form, setForm] = useState(emptyForm);
  const [editingId, setEditingId] = useState(null);

  const loadAll = () => {
    productApi.getAll().then((res) => setProducts(res.data));
    productApi.getCategories().then((res) => setCategories(res.data));
    productApi.getBrands().then((res) => setBrands(res.data));
  };

  useEffect(() => { loadAll(); }, []);

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    const payload = {
      ...form,
      price: Number(form.price),
      stock: Number(form.stock) || 0,
      categoryId: form.categoryId || null,
      brandId: form.brandId || null,
    };
    try {
      if (editingId) {
        await productApi.update(editingId, payload);
      } else {
        await productApi.create(payload);
      }
      setForm(emptyForm);
      setEditingId(null);
      loadAll();
    } catch (err) {
      alert(err.response?.data?.message || 'Có lỗi xảy ra, vui lòng thử lại.');
    }
  };

  const handleEdit = (product) => {
    setEditingId(product.id);
    setForm({
      name: product.name, sku: product.sku || '', description: product.description || '',
      price: product.price, stock: product.stock, imageUrl: product.imageUrl || '',
      colors: product.colors || '',
      categoryId: product.categoryId ? String(product.categoryId) : '',
      brandId: product.brandId ? String(product.brandId) : '',
    });
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  const handleCancelEdit = () => {
    setEditingId(null);
    setForm(emptyForm);
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Xóa sản phẩm này?')) return;
    try {
      await productApi.remove(id);
      loadAll();
    } catch (err) {
      alert(err.response?.data?.message || 'Có lỗi xảy ra, vui lòng thử lại.');
    }
  };

  return (
    <div>
      <h2>Quản lý sản phẩm</h2>
      <form className="admin-form" onSubmit={handleSubmit}>
        <label>Tên sản phẩm
          <input name="name" value={form.name} onChange={handleChange} required />
        </label>
        <label>Mã SKU
          <input name="sku" value={form.sku} onChange={handleChange} />
        </label>
        <label>Giá
          <input name="price" type="number" value={form.price} onChange={handleChange} required />
        </label>
        <label>Tồn kho
          <input name="stock" type="number" value={form.stock} onChange={handleChange} />
        </label>
        <label>Ảnh (URL)
          <input name="imageUrl" value={form.imageUrl} onChange={handleChange} />
        </label>
        <label>Màu thân máy có sẵn (vd: Đen,Bạc,Trắng)
          <input name="colors" value={form.colors} onChange={handleChange} placeholder="Đen, Bạc, Trắng..." />
        </label>
        <label>Danh mục
          <select name="categoryId" value={form.categoryId} onChange={handleChange}>
            <option value="">-- Chọn --</option>
            {categories.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
          </select>
        </label>
        <label>Thương hiệu
          <select name="brandId" value={form.brandId} onChange={handleChange}>
            <option value="">-- Chọn --</option>
            {brands.map((b) => <option key={b.id} value={b.id}>{b.name}</option>)}
          </select>
        </label>
        <label>Mô tả
          <textarea name="description" value={form.description} onChange={handleChange} />
        </label>
        <button type="submit">{editingId ? 'Cập nhật' : 'Thêm sản phẩm'}</button>
        {editingId && (
          <button type="button" onClick={handleCancelEdit} style={{ marginLeft: 8 }}>
            Hủy
          </button>
        )}
      </form>

      <table className="admin-table">
        <thead>
          <tr>
            <th>Ảnh</th><th>Tên sản phẩm</th><th>Thương hiệu</th><th>Giá</th><th>Tồn kho</th><th>Danh mục</th><th>Hành động</th>
          </tr>
        </thead>
        <tbody>
          {products.map((p) => (
            <tr key={p.id}>
              <td>
                <div className="admin-thumb">
                  {p.imageUrl ? <img src={p.imageUrl} alt={p.name} /> : <span>📷</span>}
                </div>
              </td>
              <td>{p.name}{p.sku && <div className="admin-sku">SKU: {p.sku}</div>}</td>
              <td>{p.brandName || '—'}</td>
              <td>{p.price?.toLocaleString('vi-VN')}đ</td>
              <td>
                <span className={`admin-stock-badge ${p.stock > 0 ? 'in' : 'out'}`}>
                  {p.stock > 0 ? `Còn ${p.stock}` : 'Hết hàng'}
                </span>
              </td>
              <td>{p.categoryName || '—'}</td>
              <td>
                <button className="btn-danger-sm" onClick={() => handleEdit(p)} style={{ marginRight: 8 }}>Sửa</button>
                <button className="btn-danger-sm" onClick={() => handleDelete(p.id)}>Xóa</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
