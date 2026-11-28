import React, { useState, useRef, useEffect } from 'react';
import { useLocation, Link } from 'react-router-dom';
import { sendChatMessage } from '../services/chatService';
import OutfitRecommendationCard from './OutfitStylist/OutfitRecommendationCard';
import MarketComparisonCard from './MarketComparisonCard';

const Chatbot = () => {
    const [isOpen, setIsOpen] = useState(false);
    const [messages, setMessages] = useState([
        { sender: 'bot', text: 'Xin chào! Mình là tư vấn viên AI của ProjectSkateBoard. Bạn có thể hỏi mẫu giày, cách phối đồ, hoặc nhờ mình so sánh giá shop với Shopee, Lazada, Tiki và TikTok Shop.' }
    ]);
    const [input, setInput] = useState('');
    const [loading, setLoading] = useState(false);
    const [pendingType, setPendingType] = useState(null);
    const chatEndRef = useRef(null);

    const location = useLocation();
    // Bắt productId nếu người dùng đang ở trang /products/:id
    const matchProduct = location.pathname.match(/^\/products\/(\d+)/);
    const currentProductId = matchProduct ? matchProduct[1] : null;

    // Tự động cuộn xuống tin nhắn mới nhất
    useEffect(() => {
        chatEndRef.current?.scrollIntoView({ behavior: 'smooth' });
    }, [messages, loading]);

    const handleSend = async (e, customText = null, options = {}) => {
        if (e && e.preventDefault) e.preventDefault();
        const userText = (customText || input).trim();
        if (!userText || loading) return;

        if (!customText) setInput('');
        setMessages((prev) => [...prev, { sender: 'user', text: userText }]);
        const looksLikeCompare = /so sánh|so sanh|shopee|lazada|tiki/i.test(userText);
        setPendingType(options.type || (looksLikeCompare ? 'MARKETPLACE_COMPARISON' : null));
        setLoading(true);

        try {
            const data = await sendChatMessage(userText, currentProductId, options);
            const replyText = data.reply || data.message || 'Không có phản hồi từ hệ thống.';
            setMessages((prev) => [
                ...prev,
                { 
                    sender: 'bot', 
                    text: replyText, 
                    products: data.products,
                    type: data.type,
                    data: data.data
                }
            ]);
        } catch (error) {
            setMessages((prev) => [
                ...prev,
                { sender: 'bot', text: 'Rất tiếc, đã có lỗi xảy ra khi kết nối. Vui lòng thử lại sau!' }
            ]);
        } finally {
            setPendingType(null);
            setLoading(false);
        }
    };

    return (
        <div className="fixed bottom-6 right-6 z-50">
            {/* Cửa sổ Chat */}
            {isOpen && (
                <div className="w-80 sm:w-[28rem] h-[560px] bg-white rounded-2xl shadow-2xl border border-gray-200 flex flex-col mb-4 overflow-hidden animate-in fade-in slide-in-from-bottom-5">
                    {/* Header */}
                    <div className="bg-black text-white p-4 flex justify-between items-center shadow-sm">
                        <div className="flex items-center gap-2">
                            <span className="w-2.5 h-2.5 bg-green-400 rounded-full animate-pulse"></span>
                            <div>
                                <h3 className="font-bold text-sm tracking-wide">ProjectSkateBoard AI</h3>
                                <p className="text-[10px] text-gray-300">Tư vấn, phối đồ & so sánh sàn</p>
                            </div>
                        </div>
                        <button 
                            onClick={() => setIsOpen(false)}
                            className="text-gray-400 hover:text-white text-xl font-bold focus:outline-none"
                        >
                            ✕
                        </button>
                    </div>

                    {/* Banner ngữ cảnh nếu đang xem 1 sản phẩm cụ thể */}
                    {currentProductId && (
                        <div className="bg-amber-50 border-b border-amber-200 px-3 py-2 text-xs text-amber-800 flex items-center justify-between">
                            <span>🎯 Đang xem sản phẩm #{currentProductId}</span>
                            <span className="text-[10px] bg-amber-200 px-1.5 py-0.5 rounded font-medium">Tư vấn · Phối đồ · So sánh sàn</span>
                        </div>
                    )}

                    {/* Nội dung tin nhắn */}
                    <div className="flex-1 p-4 overflow-y-auto space-y-3 bg-gray-50 text-sm">
                        {messages.map((msg, idx) => (
                            <div key={idx} className={`flex flex-col ${msg.sender === 'user' ? 'items-end' : 'items-start'}`}>
                                <div className={`max-w-[88%] p-3 rounded-2xl ${
                                    msg.sender === 'user' 
                                        ? 'bg-black text-white rounded-br-none shadow-sm' 
                                        : 'bg-white text-gray-800 border border-gray-200 rounded-bl-none shadow-sm'
                                }`}>
                                    <p className="whitespace-pre-line leading-relaxed">{msg.text}</p>
                                </div>

                                {/* Render Card Phối Đồ nếu loại message là OUTFIT_RECOMMENDATION */}
                                {msg.type === 'OUTFIT_RECOMMENDATION' && msg.data && (
                                    <div className="w-full mt-1">
                                        <OutfitRecommendationCard outfit={msg.data} />
                                    </div>
                                )}

                                {msg.type === 'MARKETPLACE_COMPARISON' && msg.data && (
                                    <MarketComparisonCard comparison={msg.data} />
                                )}

                                {/* Render danh sách sản phẩm gợi ý thông thường nếu có */}
                                {msg.type !== 'OUTFIT_RECOMMENDATION' && msg.products && msg.products.length > 0 && (
                                    <div className="mt-2 w-full space-y-2">
                                        {msg.products.map((prod, pIdx) => (
                                            <Link 
                                                to={`/products/${prod.id}`}
                                                key={pIdx} 
                                                className="p-2.5 bg-white rounded-xl border border-gray-200 flex items-center gap-3 text-xs shadow-sm hover:border-black transition"
                                            >
                                                {prod.image && (
                                                    <img 
                                                        src={prod.image} 
                                                        alt={prod.name} 
                                                        className="w-10 h-10 object-cover rounded-lg flex-shrink-0"
                                                        onError={(e) => { e.target.src = "https://images.unsplash.com/photo-1542291026-7eec264c27ff"; }}
                                                    />
                                                )}
                                                <div className="flex-1 min-w-0">
                                                    <span className="font-semibold text-gray-900 truncate block">{prod.name}</span>
                                                    <span className="text-black font-bold">{prod.price?.toLocaleString()} đ</span>
                                                </div>
                                                <span className="text-gray-400 text-xs">Xem →</span>
                                            </Link>
                                        ))}
                                    </div>
                                )}
                            </div>
                        ))}
                        {loading && (
                            <div className="flex items-center gap-2 text-gray-500 text-xs italic bg-white p-2.5 rounded-xl border border-gray-100 max-w-[75%]">
                                <span className="inline-block w-2 h-2 bg-indigo-500 rounded-full animate-bounce"></span>
                                <span>
                                    {pendingType === 'MARKETPLACE_COMPARISON'
                                        ? 'Đang đối chiếu Shopee, Lazada, Tiki...'
                                        : pendingType === 'OUTFIT_RECOMMENDATION'
                                            ? 'AI Stylist đang lên set đồ...'
                                            : 'Đang soạn câu trả lời...'}
                                </span>
                            </div>
                        )}
                        <div ref={chatEndRef} />
                    </div>

                    {/* Gợi ý câu hỏi nhanh */}
                    {!loading && (
                        <div className="px-3 py-2 bg-gray-100 border-t border-gray-200 flex gap-1.5 overflow-x-auto text-[11px] no-scrollbar">
                            {currentProductId ? (
                                <>
                                    <button
                                        onClick={() => handleSend(null, "Gợi ý phối đồ phong cách Streetwear với đôi giày này?", { type: 'OUTFIT_RECOMMENDATION' })}
                                        className="whitespace-nowrap px-2.5 py-1 bg-indigo-50 border border-indigo-200 text-indigo-700 font-medium rounded-full hover:bg-indigo-100"
                                    >
                                        ✨ Phối đồ Streetwear
                                    </button>
                                    <button
                                        onClick={() => handleSend(null, "Đôi này phối đồ đi học hoặc đi dạo phố thế nào?", { type: 'OUTFIT_RECOMMENDATION' })}
                                        className="whitespace-nowrap px-2.5 py-1 bg-white border border-gray-300 rounded-full hover:bg-gray-50 text-gray-700"
                                    >
                                        🎒 Set đồ đi học/dạo phố
                                    </button>
                                    <button
                                        onClick={() => handleSend(null, "So sánh giá sản phẩm này với Shopee, Lazada, Tiki", { type: 'MARKETPLACE_COMPARISON' })}
                                        className="whitespace-nowrap px-2.5 py-1 bg-amber-50 border border-amber-200 text-amber-800 font-medium rounded-full hover:bg-amber-100"
                                    >
                                        ⚖️ So sánh sàn
                                    </button>
                                    <button
                                        onClick={() => handleSend(null, "Sản phẩm này có những ưu điểm nổi bật gì?")}
                                        className="whitespace-nowrap px-2.5 py-1 bg-white border border-gray-300 rounded-full hover:bg-gray-50 text-gray-700"
                                    >
                                        🔍 Ưu điểm nổi bật?
                                    </button>
                                </>
                            ) : (
                                <>
                                    <button
                                        onClick={() => handleSend(null, "Gợi ý cho mình một set đồ phong cách Skater năng động", { type: 'OUTFIT_RECOMMENDATION' })}
                                        className="whitespace-nowrap px-2.5 py-1 bg-indigo-50 border border-indigo-200 text-indigo-700 font-medium rounded-full hover:bg-indigo-100"
                                    >
                                        ✨ Gợi ý Outfit Skater
                                    </button>
                                    <button
                                        onClick={() => handleSend(null, "Tìm cho mình giày chạy bộ tốt nhất")}
                                        className="whitespace-nowrap px-2.5 py-1 bg-white border border-gray-300 rounded-full hover:bg-gray-50 text-gray-700"
                                    >
                                        👟 Giày chạy bộ
                                    </button>
                                    <button
                                        onClick={() => handleSend(null, "Tìm giày bóng rổ Nike")}
                                        className="whitespace-nowrap px-2.5 py-1 bg-white border border-gray-300 rounded-full hover:bg-gray-50 text-gray-700"
                                    >
                                        🏀 Giày bóng rổ Nike
                                    </button>
                                    <button
                                        onClick={() => handleSend(null, "So sánh giá với Shopee, Lazada, Tiki", { type: 'MARKETPLACE_COMPARISON' })}
                                        className="whitespace-nowrap px-2.5 py-1 bg-amber-50 border border-amber-200 text-amber-800 font-medium rounded-full hover:bg-amber-100"
                                    >
                                        ⚖️ So sánh giá sàn
                                    </button>
                                </>
                            )}
                        </div>
                    )}

                    {/* Input nhập tin nhắn */}
                    <form onSubmit={handleSend} className="p-3 bg-white border-t border-gray-200 flex gap-2">
                        <input
                            type="text"
                            placeholder={currentProductId ? "Hỏi chi tiết, phối đồ hoặc so sánh sàn..." : "Hỏi tìm giày, phối đồ, so sánh sàn..."}
                            value={input}
                            onChange={(e) => setInput(e.target.value)}
                            className="flex-1 px-3 py-2 border rounded-xl text-sm focus:outline-none focus:border-black"
                        />
                        <button
                            type="submit"
                            disabled={loading || !input.trim()}
                            className="bg-black text-white px-4 py-2 rounded-xl text-sm font-semibold hover:bg-gray-800 transition disabled:opacity-50"
                        >
                            Gửi
                        </button>
                    </form>
                </div>
            )}

            {/* Nút Nổi Toggle Chat */}
            <button
                onClick={() => setIsOpen(!isOpen)}
                className="w-14 h-14 bg-black text-white rounded-full shadow-lg flex items-center justify-center text-2xl hover:scale-105 transition-transform duration-200 focus:outline-none"
            >
                💬
            </button>
        </div>
    );
};

export default Chatbot;
