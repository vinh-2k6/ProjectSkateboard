import api from './api';

export const sendChatMessage = async (message, productId = null) => {
    const payload = { message };
    if (productId) {
        payload.productId = Number(productId);
    }
    const response = await api.post('/chat', payload);
    return response.data;
};
