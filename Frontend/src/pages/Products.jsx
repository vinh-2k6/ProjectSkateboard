import { useEffect, useState } from "react";
import { useSearchParams } from "react-router-dom";
import ProductCard from "../components/ProductCard";
import { getProducts, getProductsByName } from "../services/productService";
import { Client } from "@stomp/stompjs";

const DEFAULT_MIN_PRICE = "1000000";
const DEFAULT_MAX_PRICE = "6000000";

function Products() {
    const [searchParams, setSearchParams] = useSearchParams();

    // 1. Lấy giá trị ban đầu từ URL Params
    const initialBrand = searchParams.get("brand") || "";
    const initialCategory = searchParams.get("category") || "";
    const initialSearch = searchParams.get("search") || "";
    const initialMinPrice = searchParams.get("minPrice") || "";
    const initialMaxPrice = searchParams.get("maxPrice") || "";
    const initialPage = Number(searchParams.get("page")) || 1;

    const [products, setProducts] = useState([]);
    const [page, setPage] = useState(initialPage);
    const [totalPages, setTotalPages] = useState(1);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [priceError, setPriceError] = useState("");

    // State bộ lọc & tìm kiếm
    const [selectedBrand, setSelectedBrand] = useState(initialBrand);
    const [selectedCategory, setSelectedCategory] = useState(initialCategory);
    const [searchTerm, setSearchTerm] = useState(initialSearch);

    // State ô nhập giá và state giá đang áp dụng để gọi API
    const [minPriceInput, setMinPriceInput] = useState(initialMinPrice || DEFAULT_MIN_PRICE);
    const [maxPriceInput, setMaxPriceInput] = useState(initialMaxPrice || DEFAULT_MAX_PRICE);
    const [appliedMinPrice, setAppliedMinPrice] = useState(initialMinPrice);
    const [appliedMaxPrice, setAppliedMaxPrice] = useState(initialMaxPrice);

    const [refreshTrigger, setRefreshTrigger] = useState(0);

    // 2. Cập nhật query parameters trên thanh URL mỗi khi state thay đổi
    useEffect(() => {
        const params = {};
        if (selectedBrand) params.brand = selectedBrand;
        if (selectedCategory) params.category = selectedCategory;
        if (searchTerm.trim()) params.search = searchTerm.trim();
        if (appliedMinPrice !== "") params.minPrice = appliedMinPrice;
        if (appliedMaxPrice !== "") params.maxPrice = appliedMaxPrice;
        if (page > 1) params.page = page;

        setSearchParams(params, { replace: true });
    }, [
        selectedBrand,
        selectedCategory,
        searchTerm,
        appliedMinPrice,
        appliedMaxPrice,
        page,
        setSearchParams
    ]);

    // 3. Fetch dữ liệu dựa theo bộ lọc
    useEffect(() => {
        const fetchProducts = async () => {
            try {
                setLoading(true);
                setError("");

                if (searchTerm.trim() !== "") {
                    const data = await getProductsByName(searchTerm);
                    const result = data.data || [];
                    setProducts(Array.isArray(result) ? result : [result]);
                    setTotalPages(1);
                } else {
                    const data = await getProducts(
                        page,
                        6,
                        selectedBrand,
                        selectedCategory,
                        appliedMinPrice,
                        appliedMaxPrice
                    );
                    setProducts(data.data?.content || []);
                    setTotalPages(data.data?.totalPages || 1);
                }
            } catch (error) {
                console.error(error);

                if (error.response && error.response.status === 404) {
                    setProducts([]);
                    setError("");
                } else {
                    setProducts([]);
                    setError("Không thể tải danh sách sản phẩm");
                }
            } finally {
                setLoading(false);
            }
        };

        fetchProducts();
    }, [
        page,
        selectedBrand,
        selectedCategory,
        searchTerm,
        appliedMinPrice,
        appliedMaxPrice,
        refreshTrigger
    ]);

    // 4. WebSocket
    useEffect(() => {
        const client = new Client({
            brokerURL: "ws://localhost:8090/ws",

            onConnect: () => {
                console.log("WebSocket connected");

                client.subscribe("/topic/products", (message) => {
                    const newProduct = JSON.parse(message.body);
                    console.log("New product received:", newProduct);
                    setRefreshTrigger(prev => prev + 1);
                });
            },

            onStompError: (frame) => {
                console.error("STOMP error:", frame);
            },

            onWebSocketError: (error) => {
                console.error("WebSocket error:", error);
            }
        });

        client.activate();

        return () => {
            client.deactivate();
        };
    }, []);

    const handleSearchChange = (e) => {
        setSearchTerm(e.target.value);
        setPage(1);
    };

    const handleBrandChange = (brand) => {
        setSelectedBrand(prev => prev === brand ? "" : brand);
        setPage(1);
    };

    const handleCategoryChange = (category) => {
        setSelectedCategory(prev => prev === category ? "" : category);
        setPage(1);
    };

    const applyPriceRange = (minVal, maxVal) => {
        const minNum = minVal !== "" ? Number(minVal) : null;
        const maxNum = maxVal !== "" ? Number(maxVal) : null;

        if ((minNum !== null && minNum < 0) || (maxNum !== null && maxNum < 0)) {
            setPriceError("Giá không được nhỏ hơn 0");
            return;
        }
        if (minNum !== null && maxNum !== null && minNum > maxNum) {
            setPriceError("Giá tối thiểu không được lớn hơn giá tối đa");
            return;
        }

        setPriceError("");
        setAppliedMinPrice(minVal);
        setAppliedMaxPrice(maxVal);
        setPage(1);
    };

    const handleApplyPriceClick = (e) => {
        e.preventDefault();
        applyPriceRange(minPriceInput.trim(), maxPriceInput.trim());
    };

    const handleSelectDefaultPriceRange = () => {
        setMinPriceInput(DEFAULT_MIN_PRICE);
        setMaxPriceInput(DEFAULT_MAX_PRICE);
        applyPriceRange(DEFAULT_MIN_PRICE, DEFAULT_MAX_PRICE);
    };

    const handleClearPrice = () => {
        setMinPriceInput("");
        setMaxPriceInput("");
        setPriceError("");
        setAppliedMinPrice("");
        setAppliedMaxPrice("");
        setPage(1);
    };

    const handleClearAllFilters = () => {
        setSelectedBrand("");
        setSelectedCategory("");
        setSearchTerm("");
        setMinPriceInput("");
        setMaxPriceInput("");
        setAppliedMinPrice("");
        setAppliedMaxPrice("");
        setPriceError("");
        setPage(1);
    };

    if (loading) return <div className="loading">Loading products...</div>;

    return (
        <div className="products-page">
            {/* HEADER CONTAINING SEARCH BAR IN THE MIDDLE */}
            <div className="products-header" style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                <div>
                    <p className="small-title">OUR COLLECTION</p>
                    <h1>All Shoes</h1>
                </div>

                {/* Ô TÌM KIẾM */}
                <div className="search-box" style={{ flex: 1, maxWidth: '400px', margin: '0 20px' }}>
                    <input
                        type="text"
                        placeholder="Search products by name..."
                        value={searchTerm}
                        onChange={handleSearchChange}
                        style={{
                            width: '100%',
                            padding: '10px 16px',
                            borderRadius: '20px',
                            border: '1px solid #ccc',
                            outline: 'none',
                            fontSize: '14px'
                        }}
                    />
                </div>

                <div className="product-count">{products.length} products</div>
            </div>

            {error && <div className="error">{error}</div>}

            <div className="products-container">
                <aside className="filter-sidebar">
                    <h3>Filter</h3>

                    {/* Lọc theo Brand */}
                    <div className="filter-group">
                        <h4>Brand</h4>
                        {["Nike", "Adidas", "Puma"].map((brand) => (
                            <label key={brand}>
                                <input
                                    type="checkbox"
                                    checked={selectedBrand === brand}
                                    onChange={() => handleBrandChange(brand)}
                                />
                                {brand}
                            </label>
                        ))}
                    </div>

                    {/* Lọc theo Category */}
                    <div className="filter-group">
                        <h4>Category</h4>
                        {["Sneaker", "Running", "Basketball"].map((category) => (
                            <label key={category}>
                                <input
                                    type="checkbox"
                                    checked={selectedCategory === category}
                                    onChange={() => handleCategoryChange(category)}
                                />
                                {category}
                            </label>
                        ))}
                    </div>

                    {/* Lọc theo Khoảng giá (VND) */}
                    <div className="filter-group">
                        <h4>Price Range (₫)</h4>

                        <button
                            type="button"
                            onClick={handleSelectDefaultPriceRange}
                            style={{
                                width: "100%",
                                padding: "6px 10px",
                                marginBottom: "8px",
                                fontSize: "12px",
                                cursor: "pointer",
                                borderRadius: "6px",
                                border: "1px solid #d1d5db",
                                background:
                                    appliedMinPrice === DEFAULT_MIN_PRICE &&
                                    appliedMaxPrice === DEFAULT_MAX_PRICE
                                        ? "#111827"
                                        : "#f9fafb",
                                color:
                                    appliedMinPrice === DEFAULT_MIN_PRICE &&
                                    appliedMaxPrice === DEFAULT_MAX_PRICE
                                        ? "#ffffff"
                                        : "#111827"
                            }}
                        >
                            1.000.000₫ – 6.000.000₫
                        </button>

                        <form onSubmit={handleApplyPriceClick} style={{ display: "flex", flexDirection: "column", gap: "8px" }}>
                            <input
                                type="number"
                                min="0"
                                step="10000"
                                placeholder="Min (VD: 1000000)"
                                value={minPriceInput}
                                onChange={(e) => setMinPriceInput(e.target.value)}
                                style={{ padding: "6px 8px", borderRadius: "6px", border: "1px solid #ccc" }}
                            />
                            <input
                                type="number"
                                min="0"
                                step="10000"
                                placeholder="Max (VD: 6000000)"
                                value={maxPriceInput}
                                onChange={(e) => setMaxPriceInput(e.target.value)}
                                style={{ padding: "6px 8px", borderRadius: "6px", border: "1px solid #ccc" }}
                            />

                            {priceError && (
                                <p style={{ color: "#dc2626", fontSize: "12px", margin: 0 }}>
                                    {priceError}
                                </p>
                            )}

                            <div style={{ display: "flex", gap: "6px" }}>
                                <button
                                    type="submit"
                                    style={{
                                        flex: 1,
                                        padding: "6px 10px",
                                        cursor: "pointer",
                                        borderRadius: "6px",
                                        border: "none",
                                        background: "#111827",
                                        color: "#fff"
                                    }}
                                >
                                    Apply
                                </button>
                                <button
                                    type="button"
                                    onClick={handleClearAllFilters}
                                    style={{
                                        padding: "6px 10px",
                                        cursor: "pointer",
                                        borderRadius: "6px",
                                        border: "1px solid #ccc",
                                        background: "#fff"
                                    }}
                                >
                                    Clear
                                </button>
                            </div>
                        </form>
                    </div>
                </aside>

                <main className="product-grid">
                    {products.length > 0 ? (
                        products.map(product => (
                            <ProductCard key={product.id} product={product} />
                        ))
                    ) : (
                        <p style={{ gridColumn: '1 / -1', textAlign: 'center', color: '#666' }}>
                            No products found matching your search.
                        </p>
                    )}
                </main>
            </div>

            {/* PAGINATION */}
            {searchTerm.trim() === "" && (
                <div className="pagination">
                    <button
                        className="page-btn prev-next"
                        disabled={page === 1}
                        onClick={() => setPage(page - 1)}
                    >
                        &laquo; Previous
                    </button>

                    <div className="page-numbers">
                        {Array.from({ length: totalPages }, (_, index) => index + 1).map(number => (
                            <button
                                key={number}
                                onClick={() => setPage(number)}
                                className={`page-number ${page === number ? "active" : ""}`}
                            >
                                {number}
                            </button>
                        ))}
                    </div>

                    <button
                        className="page-btn prev-next"
                        disabled={page === totalPages}
                        onClick={() => setPage(page + 1)}
                    >
                        Next &raquo;
                    </button>
                </div>
            )}
        </div>
    );
}

export default Products;