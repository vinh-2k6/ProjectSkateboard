import api from './api';

export const orderService = {
  // POST /orders
  createOrder: async (orderData) => {
    const response = await api.post('/orders', orderData);
    return response.data;
  },

  // GET /orders
  getOrders: async () => {
    const response = await api.get('/orders');
    return response.data?.data || response.data;
  },

  // GET /orders/{id}
  getOrderById: async (id) => {
    const response = await api.get(`/orders/${id}`);
    return response.data?.data || response.data;
  },

  // PATCH /orders/{id}?status=CANCELLED
  cancelOrder: async (id) => {
    const response = await api.patch(`/orders/${id}`, null, {
      params: {
        status: 'CANCELLED'
      }
    });

    return response.data;
  },

  // PATCH /orders/{id}?status=SHIPPED
  updateOrderStatus: async (id, status) => {
    const response = await api.patch(`/orders/${id}`, null, {
      params: {
        status
      }
    });

    return response.data;
  }
};
