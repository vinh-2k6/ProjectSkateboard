import React, { useState, useEffect, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { FaStar, FaRegStar, FaRegCommentDots, FaTrashAlt, FaCheckCircle } from 'react-icons/fa';
import { commentService } from '../services/commentService';

export default function ProductReviews({ productId }) {
  const navigate = useNavigate();

  // Thông tin user đăng nhập
  const userId = localStorage.getItem('userId');
  const storedUser = localStorage.getItem('user');
  const currentUser = storedUser ? JSON.parse(storedUser) : null;

  const [comments, setComments] = useState([]);
  const [loading, setLoading] = useState(true);

  // Form state
  const [rating, setRating] = useState(5);
  const [hoverRating, setHoverRating] = useState(0);
  const [content, setContent] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);

  // Filter state: 'ALL' hoặc số sao (5, 4, 3, 2, 1)
  const [filterRating, setFilterRating] = useState('ALL');
  const [sortBy, setSortBy] = useState('NEWEST'); // NEWEST, HIGHEST, LOWEST

  // Load comments
  const fetchComments = async () => {
    try {
      setLoading(true);
      const data = await commentService.getCommentsByProductId(productId);
      setComments(Array.isArray(data) ? data : []);
    } catch (err) {
      console.error('Lỗi khi tải bình luận:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (productId) {
      fetchComments();
    }
  }, [productId]);

  // Thống kê đánh giá
  const totalReviews = comments.length;
  const averageRating = useMemo(() => {
    if (totalReviews === 0) return 0;
    const sum = comments.reduce((acc, c) => acc + (Number(c.rating) || 5), 0);
    return (sum / totalReviews).toFixed(1);
  }, [comments, totalReviews]);

  const ratingCounts = useMemo(() => {
    const counts = { 5: 0, 4: 0, 3: 0, 2: 0, 1: 0 };
    comments.forEach((c) => {
      const star = Math.round(Number(c.rating) || 5);
      if (counts[star] !== undefined) counts[star]++;
    });
    return counts;
  }, [comments]);

  // Xử lý gửi bình luận
  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!userId) {
      setError('Vui lòng đăng nhập để gửi đánh giá!');
      return;
    }

    if (!content.trim()) {
      setError('Vui lòng nhập nội dung đánh giá của bạn!');
      return;
    }

    try {
      setSubmitting(true);
      setError(null);

      await commentService.createComment({
        content: content.trim(),
        rating,
        productId,
        userId
      });

      // Tải lại danh sách bình luận mới nhất
      await fetchComments();

      setContent('');
      setRating(5);
      setSuccess('Đánh giá của bạn đã được đăng thành công!');
      setTimeout(() => setSuccess(null), 4000);
    } catch (err) {
      console.error('Lỗi khi gửi bình luận:', err);
      const msg = err.response?.data?.message || 'Có lỗi xảy ra khi gửi bình luận. Vui lòng thử lại sau!';
      setError(msg);
    } finally {
      setSubmitting(false);
    }
  };

  // Xử lý xóa bình luận
  const handleDelete = async (commentId) => {
    if (!window.confirm('Bạn có chắc chắn muốn xóa đánh giá này?')) return;
    try {
      await commentService.deleteComment(commentId);
      setComments((prev) => prev.filter((c) => c.id !== commentId));
    } catch (err) {
      console.error('Lỗi khi xóa bình luận:', err);
      alert('Không thể xóa bình luận. Vui lòng thử lại!');
    }
  };

  // Lọc và sắp xếp bình luận
  const filteredComments = useMemo(() => {
    let result = [...comments];

    // Lọc theo số sao
    if (filterRating !== 'ALL') {
      result = result.filter((c) => Math.round(Number(c.rating) || 5) === Number(filterRating));
    }

    // Sắp xếp
    if (sortBy === 'NEWEST') {
      result.sort((a, b) => new Date(b.createdAt || 0) - new Date(a.createdAt || 0));
    } else if (sortBy === 'HIGHEST') {
      result.sort((a, b) => (Number(b.rating) || 5) - (Number(a.rating) || 5));
    } else if (sortBy === 'LOWEST') {
      result.sort((a, b) => (Number(a.rating) || 5) - (Number(b.rating) || 5));
    }

    return result;
  }, [comments, filterRating, sortBy]);

  const ratingDescriptions = {
    1: 'Rất tệ',
    2: 'Chưa hài lòng',
    3: 'Bình thường',
    4: 'Hài lòng',
    5: 'Tuyệt vời'
  };

  return (
    <div className="mt-10">
      <div className="bg-white rounded-2xl p-6 sm:p-10 border border-gray-200 shadow-sm">
        {/* Tiêu đề mục Đánh giá */}
        <div className="flex items-center justify-between pb-5 mb-8 border-b border-gray-200">
          <div className="flex items-center gap-3">
            <span className="w-1.5 h-6 bg-black rounded-full inline-block"></span>
            <h2 className="text-xl font-extrabold text-gray-900 tracking-wide uppercase flex items-center gap-2">
              Đánh Giá & Nhận Xét
              <span className="text-sm font-semibold text-gray-500 normal-case bg-gray-100 px-2.5 py-0.5 rounded-full">
                ({totalReviews})
              </span>
            </h2>
          </div>
        </div>

        {/* Khung tổng quan điểm số */}
        <div className="grid grid-cols-1 md:grid-cols-12 gap-6 bg-gray-50 rounded-2xl p-6 mb-8 border border-gray-100 items-center">
          {/* Cột 1: Điểm trung bình */}
          <div className="md:col-span-4 text-center md:border-r md:border-gray-200 md:pr-6">
            <div className="text-5xl font-black text-gray-900 leading-none mb-2">
              {averageRating}
              <span className="text-xl font-medium text-gray-500"> / 5</span>
            </div>
            <div className="flex justify-center gap-1 text-amber-400 text-lg mb-1.5">
              {[1, 2, 3, 4, 5].map((star) => (
                <FaStar
                  key={star}
                  className={star <= Math.round(Number(averageRating)) ? 'text-amber-400' : 'text-gray-300'}
                />
              ))}
            </div>
            <p className="text-xs text-gray-500 font-medium">Dựa trên {totalReviews} lượt đánh giá thực tế</p>
          </div>

          {/* Cột 2: Thanh phân bổ sao */}
          <div className="md:col-span-8 space-y-2">
            {[5, 4, 3, 2, 1].map((star) => {
              const count = ratingCounts[star] || 0;
              const percent = totalReviews > 0 ? (count / totalReviews) * 100 : 0;
              return (
                <div key={star} className="flex items-center gap-3 text-xs">
                  <span className="flex items-center gap-1 font-semibold text-gray-700 w-12">
                    {star} <FaStar className="text-amber-400 text-[11px]" />
                  </span>
                  <div className="flex-1 h-2.5 bg-gray-200 rounded-full overflow-hidden">
                    <div
                      className="h-full bg-amber-400 rounded-full transition-all duration-500"
                      style={{ width: `${percent}%` }}
                    ></div>
                  </div>
                  <span className="text-gray-500 font-medium w-8 text-right">{count}</span>
                </div>
              );
            })}
          </div>
        </div>

        {/* KHU VỰC NHẬP BÌNH LUẬN VÀ ĐÁNH GIÁ */}
        <div className="mb-10 bg-gradient-to-br from-gray-50 to-white rounded-2xl p-6 border border-gray-200 shadow-sm">
          <h3 className="text-base font-bold text-gray-900 mb-4 flex items-center gap-2">
            <FaRegCommentDots className="text-blue-600 text-lg" />
            Viết nhận xét của bạn
          </h3>

          {!userId ? (
            <div className="text-center py-6 px-4 bg-white rounded-xl border border-dashed border-gray-300">
              <p className="text-sm text-gray-600 mb-3">
                Bạn cần đăng nhập tài khoản để có thể đánh giá và bình luận về sản phẩm này.
              </p>
              <button
                type="button"
                onClick={() => navigate('/login')}
                className="inline-flex items-center gap-2 px-5 py-2.5 bg-black text-white text-xs font-bold rounded-lg hover:bg-gray-800 transition shadow"
              >
                Đăng nhập ngay
              </button>
            </div>
          ) : (
            <form onSubmit={handleSubmit} className="space-y-4">
              {/* Chọn số sao */}
              <div className="flex flex-wrap items-center gap-3">
                <span className="text-sm font-semibold text-gray-700">Đánh giá của bạn:</span>
                <div className="flex items-center gap-1">
                  {[1, 2, 3, 4, 5].map((star) => (
                    <button
                      type="button"
                      key={star}
                      onClick={() => setRating(star)}
                      onMouseEnter={() => setHoverRating(star)}
                      onMouseLeave={() => setHoverRating(0)}
                      className="p-1 text-2xl transition-transform hover:scale-110 focus:outline-none"
                    >
                      {star <= (hoverRating || rating) ? (
                        <FaStar className="text-amber-400" />
                      ) : (
                        <FaRegStar className="text-gray-300 hover:text-amber-300" />
                      )}
                    </button>
                  ))}
                </div>
                <span className="text-xs font-bold text-amber-700 bg-amber-50 px-2.5 py-1 rounded-md border border-amber-200">
                  {ratingDescriptions[hoverRating || rating]}
                </span>
              </div>

              {/* Ô nhập nội dung */}
              <div>
                <textarea
                  rows="3"
                  maxLength="255"
                  value={content}
                  onChange={(e) => setContent(e.target.value)}
                  placeholder="Chia sẻ cảm nhận về sản phẩm (độ êm ái, phom dáng, chất liệu, tính năng, gợi ý chọn size...)..."
                  className="w-full p-3.5 text-sm rounded-xl border border-gray-300 focus:outline-none focus:ring-2 focus:ring-black focus:border-transparent transition bg-white"
                ></textarea>
                <div className="flex justify-between items-center text-xs text-gray-400 mt-1">
                  <span>Tối đa 255 ký tự</span>
                  <span>{content.length}/255</span>
                </div>
              </div>

              {/* Thông báo lỗi / thành công */}
              {error && (
                <div className="p-3 text-xs text-red-700 bg-red-50 border border-red-200 rounded-lg">
                  {error}
                </div>
              )}
              {success && (
                <div className="p-3 text-xs text-green-700 bg-green-50 border border-green-200 rounded-lg flex items-center gap-2">
                  <FaCheckCircle className="text-green-600 text-sm" />
                  {success}
                </div>
              )}

              {/* Nút gửi */}
              <div className="flex justify-end">
                <button
                  type="submit"
                  disabled={submitting || !content.trim()}
                  className="px-6 py-2.5 bg-black text-white text-sm font-bold rounded-xl hover:bg-gray-800 disabled:opacity-50 disabled:cursor-not-allowed transition shadow-md"
                >
                  {submitting ? 'Đang gửi...' : 'Gửi Đánh Giá'}
                </button>
              </div>
            </form>
          )}
        </div>

        {/* BỘ LỌC VÀ SẮP XẾP BÌNH LUẬN */}
        <div className="flex flex-wrap items-center justify-between gap-3 pb-4 mb-6 border-b border-gray-100">
          {/* Nút lọc sao */}
          <div className="flex flex-wrap items-center gap-1.5 sm:gap-2">
            <span className="text-xs font-bold text-gray-500 mr-1">Lọc theo:</span>
            <button
              onClick={() => setFilterRating('ALL')}
              className={`px-3 py-1 text-xs font-bold rounded-lg transition ${
                filterRating === 'ALL'
                  ? 'bg-black text-white shadow-sm'
                  : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
              }`}
            >
              Tất cả ({totalReviews})
            </button>
            {[5, 4, 3, 2, 1].map((star) => (
              <button
                key={star}
                onClick={() => setFilterRating(star)}
                className={`px-2.5 py-1 text-xs font-semibold rounded-lg flex items-center gap-1 transition ${
                  filterRating === star
                    ? 'bg-black text-white shadow-sm'
                    : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
                }`}
              >
                {star} <FaStar className="text-amber-400 text-[10px]" /> ({ratingCounts[star] || 0})
              </button>
            ))}
          </div>

          {/* Chọn sắp xếp */}
          <div className="flex items-center gap-2 text-xs">
            <span className="text-gray-500 font-medium">Sắp xếp:</span>
            <select
              value={sortBy}
              onChange={(e) => setSortBy(e.target.value)}
              className="border border-gray-300 rounded-lg px-2.5 py-1 font-semibold text-gray-700 bg-white focus:outline-none focus:ring-1 focus:ring-black text-xs"
            >
              <option value="NEWEST">Mới nhất</option>
              <option value="HIGHEST">Đánh giá cao nhất</option>
              <option value="LOWEST">Đánh giá thấp nhất</option>
            </select>
          </div>
        </div>

        {/* DANH SÁCH CÁC BÌNH LUẬN */}
        {loading ? (
          <div className="text-center py-12 text-sm text-gray-400 font-medium">Đang tải đánh giá...</div>
        ) : filteredComments.length === 0 ? (
          <div className="text-center py-12 bg-gray-50 rounded-2xl border border-dashed border-gray-200">
            <FaRegCommentDots className="mx-auto text-3xl text-gray-300 mb-2" />
            <p className="text-sm font-medium text-gray-500">
              {filterRating !== 'ALL'
                ? `Chưa có đánh giá nào cho mức ${filterRating} sao.`
                : 'Chưa có bình luận nào cho sản phẩm này. Hãy là người đầu tiên đánh giá!'}
            </p>
          </div>
        ) : (
          <div className="space-y-4">
            {filteredComments.map((item) => {
              const itemRating = Math.round(Number(item.rating) || 5);
              const isOwner = userId && Number(item.userId) === Number(userId);

              // Tên người dùng hiển thị
              const displayName =
                item.userName ||
                (isOwner && (currentUser?.fullname || currentUser?.name)) ||
                `Khách hàng #${item.userId || item.id}`;

              // Ngày bình luận
              const formattedDate = item.createdAt
                ? new Date(item.createdAt).toLocaleDateString('vi-VN', {
                    day: '2-digit',
                    month: '2-digit',
                    year: 'numeric'
                  })
                : 'Gần đây';

              // Avatar chữ cái đầu
              const firstLetter = displayName.charAt(0).toUpperCase();

              return (
                <div
                  key={item.id}
                  className="p-5 rounded-2xl border border-gray-100 bg-white hover:border-gray-200 transition duration-200 hover:shadow-sm"
                >
                  <div className="flex items-start justify-between gap-4">
                    {/* Thông tin user + Sao */}
                    <div className="flex items-center gap-3">
                      <div className="w-10 h-10 rounded-full bg-gradient-to-tr from-gray-700 to-black text-white flex items-center justify-center font-bold text-sm shadow-sm">
                        {firstLetter}
                      </div>
                      <div>
                        <div className="flex items-center gap-2">
                          <span className="font-bold text-sm text-gray-900">{displayName}</span>
                          <span className="inline-flex items-center gap-1 text-[11px] font-medium text-green-700 bg-green-50 px-2 py-0.2 rounded-full border border-green-200">
                            <FaCheckCircle className="text-[10px]" /> Đã mua hàng
                          </span>
                        </div>

                        {/* Sao đánh giá */}
                        <div className="flex items-center gap-2 mt-1">
                          <div className="flex text-amber-400 text-xs">
                            {[1, 2, 3, 4, 5].map((s) => (
                              <FaStar
                                key={s}
                                className={s <= itemRating ? 'text-amber-400' : 'text-gray-200'}
                              />
                            ))}
                          </div>
                          <span className="text-[11px] text-gray-400">• {formattedDate}</span>
                        </div>
                      </div>
                    </div>

                    {/* Nút xóa nếu là chủ nhân bình luận */}
                    {isOwner && (
                      <button
                        onClick={() => handleDelete(item.id)}
                        className="text-gray-400 hover:text-red-600 transition p-1.5 rounded-lg hover:bg-red-50 text-xs flex items-center gap-1 font-medium"
                        title="Xóa đánh giá của bạn"
                      >
                        <FaTrashAlt />
                        <span className="hidden sm:inline">Xóa</span>
                      </button>
                    )}
                  </div>

                  {/* Nội dung bình luận */}
                  <div className="mt-3.5 pl-13 text-sm text-gray-700 leading-relaxed font-normal">
                    {item.content}
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
}
