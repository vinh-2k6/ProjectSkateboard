import api from "./api";

export const getProducts = async (
    page = 1,
    size = 6,
    brand = "",
    category = "",
    minPrice = null,
    maxPrice = null
) => {
    const params = { page, size };

    if (brand) params.brand = brand;
    if (category) params.category = category;
    if (minPrice !== null && minPrice !== "" && !Number.isNaN(Number(minPrice))) {
        params.minPrice = Number(minPrice);
    }
    if (maxPrice !== null && maxPrice !== "" && !Number.isNaN(Number(maxPrice))) {
        params.maxPrice = Number(maxPrice);
    }

    const response = await api.get("/products", { params });
    return response.data;
};

export const getProductById = async (id) => {
    const response = await api.get(`/products/${id}`);
    return response.data;
};

export const getProductsByName = async (name) => {
    const response = await api.get(`/products/name`, {
        params: { name }
    });
    return response.data;
};