import api from './api';

export const commentService = {
  // Lấy danh sách bình luận theo sản phẩm
  getCommentsByProductId: async (productId) => {
    const response = await api.get(`/products/${productId}/comments`);
    return response.data?.data || response.data || [];
  },

  // Tạo bình luận mới kèm đánh giá (rating)
  createComment: async ({ content, rating, productId, userId }) => {
    const payload = {
      content,
      rating: Number(rating) || 5,
      product: { id: Number(productId) },
      user: { id: Number(userId) },
      isApproved: true
    };
    const response = await api.post('/comments', payload);
    return response.data?.data || response.data;
  },

  // Xóa bình luận
  deleteComment: async (commentId) => {
    const response = await api.delete(`/comments/${commentId}`);
    return response.data?.data || response.data;
  }
};

export default commentService;
