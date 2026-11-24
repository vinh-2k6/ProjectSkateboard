import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import api from '../services/api';
import { cartService } from '../services/cartService';

export default function ProductDetail() {
  const { id } = useParams();
  const navigate = useNavigate();

  // Lấy userId từ Auth Context hoặc LocalStorage
  const userId = localStorage.getItem('userId');

  const [product, setProduct] = useState(null);
  const [loading, setLoading] = useState(true);
  const [quantity, setQuantity] = useState(1);
  const [selectedSize, setSelectedSize] = useState(39);
  const [adding, setAdding] = useState(false);
  const [message, setMessage] = useState(null);

  const DEFAULT_IMAGE = "https://images.unsplash.com/photo-1542291026-7eec264c27ff";

  useEffect(() => {
    const fetchProduct = async () => {
      try {
        setLoading(true);
        const response = await api.get(`/products/${id}`);
        // API trả về dạng ApiResponse { message, data: { ... } }
        const data = response.data?.data || response.data;
        setProduct(data);
        if (data?.sizes && data.sizes.length > 0) {
          setSelectedSize(data.sizes[0]);
        }
      } catch (error) {
        console.error("Lỗi khi tải chi tiết sản phẩm:", error);
      } finally {
        setLoading(false);
      }
    };

    if (id) {
      fetchProduct();
    }
  }, [id]);

  const handleQuantityChange = (type) => {
    if (type === 'decrease' && quantity > 1) {
      setQuantity((prev) => prev - 1);
    } else if (type === 'increase' && quantity < (product?.quantity || 99)) {
      setQuantity((prev) => prev + 1);
    }
  };

  const handleAddToCart = async () => {
    if (!product) return;

    if (!selectedSize) {
      setMessage({ type: 'error', text: 'Vui lòng chọn kích thước giày!' });
      return;
    }

    const targetProductId = product.id || id;

    try {
      setAdding(true);
      setMessage(null);

      // Gọi cartService thêm vào giỏ hàng kèm kích thước giày
      await cartService.addToCart(userId, targetProductId, quantity, selectedSize);

      setMessage({ type: 'success', text: `Đã thêm sản phẩm (Size ${selectedSize}, SL: ${quantity}) vào giỏ hàng!` });
    } catch (error) {
      console.error('Lỗi thêm sản phẩm:', error);
      const errorMsg = error.response?.data?.message || 'Thêm vào giỏ hàng thất bại. Vui lòng thử lại!';
      setMessage({ type: 'error', text: errorMsg });
    } finally {
      setAdding(false);
    }
  };

  if (loading) {
    return <div className="text-center py-20 font-medium text-gray-500">Đang tải thông tin sản phẩm...</div>;
  }

  if (!product) {
    return (
      <div className="text-center py-20">
        <p className="text-red-500 font-medium mb-4">Không tìm thấy sản phẩm!</p>
        <button onClick={() => navigate('/products')} className="underline text-blue-600 hover:text-blue-800">
          Quay lại danh sách
        </button>
      </div>
    );
  }

  const imageUrl = product.image || product.imageUrl || DEFAULT_IMAGE;
  const availableSizes = (product?.sizes && product.sizes.length > 0) ? product.sizes : [39, 40, 41, 42, 43];

  return (
    <div className="max-w-6xl mx-auto px-4 py-8">
      {/* Nút Quay lại */}
      <button 
        onClick={() => navigate(-1)} 
        className="text-gray-500 hover:text-black mb-6 inline-flex items-center gap-1.5 text-sm font-medium transition"
      >
        ← Quay lại
      </button>

      {/* Phần trên: Ảnh sản phẩm + Thông tin mua hàng */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-10 lg:gap-14 items-start">
        {/* Cột trái: Khung ảnh sản phẩm */}
        <div className="bg-gray-50 border border-gray-100 rounded-3xl p-8 flex items-center justify-center min-h-[380px] shadow-sm sticky top-24">
          <img 
            src={imageUrl} 
            alt={product.name} 
            className="w-full h-auto object-contain max-h-[420px] transition duration-300 hover:scale-105"
            onError={(e) => {
              e.target.src = DEFAULT_IMAGE;
            }}
          />
        </div>

        {/* Cột phải: Tên, giá, chọn số lượng, đặt mua */}
        <div className="space-y-6">
          <div className="flex items-center gap-2">
            <span className="text-xs font-bold tracking-wider text-blue-600 uppercase bg-blue-50 px-2.5 py-1 rounded-md">
              {product.brand?.name || 'NIKE'}
            </span>
            <span className="text-xs font-bold tracking-wider text-gray-500 uppercase bg-gray-100 px-2.5 py-1 rounded-md">
              {product.category?.name || 'SNEAKER'}
            </span>
          </div>

          <h1 className="text-2xl sm:text-3xl font-extrabold text-gray-900 leading-tight">
            {product.name}
          </h1>

          <div className="flex items-baseline gap-3">
            <p className="text-3xl font-extrabold text-blue-600">
              {product.price?.toLocaleString('vi-VN')} ₫
            </p>
          </div>

          <hr className="border-gray-200" />

          {/* Tình trạng kho */}
          <div className="flex items-center gap-2 text-sm">
            <span className="text-gray-500">Tình trạng:</span>
            {product.quantity > 0 ? (
              <span className="text-green-600 font-semibold bg-green-50 px-2 py-0.5 rounded-full text-xs">
                Còn hàng ({product.quantity} sản phẩm)
              </span>
            ) : (
              <span className="text-red-600 font-semibold bg-red-50 px-2 py-0.5 rounded-full text-xs">
                Hết hàng
              </span>
            )}
          </div>

          {/* Chọn kích thước giày (Size) */}
          <div className="space-y-3 pt-2">
            <div className="flex items-center justify-between">
              <span className="text-sm font-bold text-gray-800">
                Kích thước (EU):{' '}
                <span className="text-blue-600 font-extrabold text-base ml-1">
                  {selectedSize ? `Size ${selectedSize}` : 'Chưa chọn'}
                </span>
              </span>
              <span className="text-xs text-gray-500 font-medium">
                Kích thước chuẩn (39 - 43)
              </span>
            </div>

            <div className="grid grid-cols-5 gap-2.5 sm:gap-3">
              {availableSizes.map((size) => {
                const isSelected = selectedSize === size;
                return (
                  <button
                    key={size}
                    type="button"
                    onClick={() => {
                      setSelectedSize(size);
                      if (message?.type === 'error') setMessage(null);
                    }}
                    className={`h-12 rounded-xl font-bold text-sm sm:text-base transition-all duration-200 flex items-center justify-center border-2 ${
                      isSelected
                        ? 'bg-black text-white border-black shadow-md scale-[1.02]'
                        : 'bg-white text-gray-700 border-gray-200 hover:border-gray-400 hover:bg-gray-50'
                    }`}
                  >
                    {size}
                  </button>
                );
              })}
            </div>
            <p className="text-[12px] text-gray-500 italic">
              * Tất cả các đôi giày đều có sẵn các kích cỡ từ 39 đến 43.
            </p>
          </div>

          {/* Chọn số lượng */}
          <div className="flex items-center gap-4">
            <span className="text-sm font-medium text-gray-700">Số lượng:</span>
            <div className="flex items-center border border-gray-300 rounded-lg overflow-hidden bg-white shadow-sm">
              <button
                onClick={() => handleQuantityChange('decrease')}
                className="px-3.5 py-1.5 hover:bg-gray-100 text-gray-600 font-medium transition"
                disabled={quantity <= 1}
              >
                -
              </button>
              <span className="px-5 py-1.5 font-bold text-sm text-gray-800 border-x border-gray-200 min-w-[45px] text-center">
                {quantity}
              </span>
              <button
                onClick={() => handleQuantityChange('increase')}
                className="px-3.5 py-1.5 hover:bg-gray-100 text-gray-600 font-medium transition"
                disabled={quantity >= product.quantity}
              >
                +
              </button>
            </div>
          </div>

          {/* Nút Thêm vào giỏ */}
          <button
            onClick={handleAddToCart}
            disabled={adding || product.quantity <= 0}
            className="w-full bg-black text-white py-4 rounded-xl font-bold hover:bg-gray-800 transition duration-200 shadow-md hover:shadow-lg disabled:opacity-50"
          >
            {adding ? 'Đang thêm...' : product.quantity <= 0 ? 'Sản phẩm đã hết hàng' : 'Thêm Vào Giỏ Hàng'}
          </button>

          {/* Banner gợi ý hỏi AI */}
          <div className="p-3.5 bg-amber-50/80 border border-amber-200/80 rounded-xl flex items-center justify-between text-xs shadow-sm">
            <div>
              <p className="font-semibold text-amber-900">✨ Cần tư vấn chi tiết hoặc cách phối đồ?</p>
              <p className="text-amber-700 text-[11px] mt-0.5">Bấm biểu tượng Chatbot AI ở góc phải để nhận tư vấn tức thì!</p>
            </div>
            <span className="text-2xl">🤖</span>
          </div>

          {message && (
            <div
              className={`p-3.5 rounded-xl text-sm text-center font-medium ${
                message.type === 'success'
                  ? 'bg-green-50 text-green-700 border border-green-200'
                  : 'bg-red-50 text-red-700 border border-red-200'
              }`}
            >
              {message.text}
            </div>
          )}
        </div>
      </div>

      {/* Phần dưới: MÔ TẢ CHI TIẾT SẢN PHẨM (Format chuẩn, xuống dòng đẹp mắt) */}
      <div className="mt-14 pt-10 border-t border-gray-200">
        <div className="bg-white rounded-2xl p-6 sm:p-10 border border-gray-200 shadow-sm">
          <div className="flex items-center gap-3 pb-4 mb-6 border-b border-gray-200">
            <span className="w-1.5 h-6 bg-black rounded-full inline-block"></span>
            <h2 className="text-xl font-extrabold text-gray-900 tracking-wide uppercase">
              Mô Tả Sản Phẩm
            </h2>
          </div>

          {/* Văn bản mô tả giữ nguyên ngắt dòng, icon và khoảng cách chuẩn */}
          <div className="text-gray-700 text-sm sm:text-base leading-relaxed whitespace-pre-line font-normal space-y-4">
            {product.description || 'Chưa có thông tin mô tả chi tiết cho sản phẩm này.'}
          </div>
        </div>
      </div>
    </div>
  );
}
