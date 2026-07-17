import React from 'react';
import './Footer.css';

export default function Footer() {
  return (
    <footer className="footer">
      <div className="footer-content">
        <div className="footer-section">
          <h3>CameraClick</h3>
          <p>Website chuyên cung cấp các dòng máy ảnh chính hãng với công nghệ Microservices hiện đại.</p>
        </div>
        <div className="footer-section">
          <h3>Liên hệ</h3>
          <p>Email: support@cameraclick.com</p>
          <p>Hotline: 1900 xxxx</p>
        </div>
        <div className="footer-section">
          <h3>Chính sách</h3>
          <p>Chính sách bảo hành</p>
          <p>Chính sách đổi trả</p>
        </div>
      </div>
      <div className="footer-bottom">
        <p>© 2026 CameraClick - Đồ án Xây dựng Website bán máy ảnh theo kiến trúc Microservices</p>
      </div>
    </footer>
  );
}
