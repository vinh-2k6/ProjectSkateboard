import api from './api';

export const sendChatMessage = async (message, productId = null, options = {}) => {
    const payload = { 
        message,
        ...options
    };
    if (productId) {
        payload.productId = Number(productId);
    }
    const response = await api.post('/chat', payload);
    return response.data;
};
