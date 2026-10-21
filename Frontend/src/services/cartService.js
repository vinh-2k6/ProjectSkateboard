import api from './api';

export const cartService = {
  // POST /users/{userId}/cart/items
  addToCart: async (userId, productId, quantity) => {
    const payload = {
      productId: Number(productId),
      quantity: Number(quantity)
    };

    const response = await api.post(
      `/users/${userId}/cart/items`,
      payload
    );

    return response.data;
  },

  // GET /users/{userId}/cart
  getCart: async (userId) => {
    const response = await api.get(
      `/users/${userId}/cart`
    );

    return response.data?.data || response.data;
  },

  // PUT /users/{userId}/cart/items/{productId}?newQuantity=2
  updateCartItem: async (userId, productId, newQuantity) => {
    const response = await api.put(
      `/users/${userId}/cart/items/${productId}`,
      null,
      {
        params: {
          newQuantity: Number(newQuantity)
        }
      }
    );

    return response.data;
  },

  // DELETE /users/{userId}/cart/items/{cartItemId}
  deleteCartItem: async (userId, cartItemId) => {
    const response = await api.delete(
      `/users/${userId}/cart/items/${cartItemId}`
    );

    return response.data;
  }
};