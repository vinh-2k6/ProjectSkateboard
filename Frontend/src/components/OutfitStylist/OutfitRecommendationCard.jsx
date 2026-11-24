import React, { useState } from 'react';
import { Link } from 'react-router-dom';

const OutfitRecommendationCard = ({ outfit }) => {
    const [imgLoading, setImgLoading] = useState(true);
    const [imgError, setImgError] = useState(false);

    if (!outfit) return null;

    const getCategoryIcon = (category) => {
        const cat = (category || '').toLowerCase();
        if (cat.includes('áo') || cat.includes('top')) return '👕';
        if (cat.includes('quần') || cat.includes('bottom')) return '👖';
        if (cat.includes('phụ kiện') || cat.includes('access')) return '🧢';
        return '✨';
    };

    return (
        <div className="w-full bg-gradient-to-br from-gray-900 to-black text-white rounded-2xl p-4 shadow-xl border border-gray-800 text-xs my-2">
            {/* Header set đồ */}
            <div className="flex items-start justify-between gap-2 border-b border-gray-800 pb-3 mb-3">
                <div>
                    <div className="flex items-center gap-1.5 mb-1 flex-wrap">
                        <span className="text-[10px] bg-indigo-500/30 text-indigo-300 border border-indigo-500/40 px-2 py-0.5 rounded-full font-semibold">
                            ✨ AI STYLIST
                        </span>
                        {outfit.style && (
                            <span className="text-[10px] bg-gray-800 text-gray-300 px-2 py-0.5 rounded-full">
                                {outfit.style}
                            </span>
                        )}
                        {outfit.occasion && (
                            <span className="text-[10px] bg-amber-500/20 text-amber-300 border border-amber-500/30 px-2 py-0.5 rounded-full">
                                🎯 {outfit.occasion}
                            </span>
                        )}
                    </div>
                    <h4 className="font-bold text-sm text-white tracking-wide">
                        {outfit.outfitName || 'Gợi Ý Phối Đồ Cá Tính'}
                    </h4>
                </div>
            </div>

            {/* Ảnh AI Lookbook vẽ cả người mẫu mặc set đồ */}
            {outfit.imageUrl && !imgError && (
                <div className="mb-3 rounded-xl overflow-hidden border border-gray-700/80 bg-gray-950 relative group shadow-inner">
                    <div className="relative aspect-[3/4] max-h-64 w-full flex items-center justify-center overflow-hidden bg-gray-900">
                        {imgLoading && (
                            <div className="absolute inset-0 flex flex-col items-center justify-center bg-gray-900 text-gray-400 text-[11px] gap-2 z-10">
                                <span className="w-5 h-5 border-2 border-indigo-400 border-t-transparent rounded-full animate-spin"></span>
                                <span className="animate-pulse">AI đang vẽ phối đồ Lookbook...</span>
                            </div>
                        )}
                        <img 
                            src={outfit.imageUrl} 
                            alt={outfit.outfitName} 
                            className={`w-full h-full object-cover object-top transition duration-500 group-hover:scale-105 ${imgLoading ? 'opacity-0' : 'opacity-100'}`}
                            onLoad={() => setImgLoading(false)}
                            onError={() => {
                                setImgError(true);
                                setImgLoading(false);
                            }}
                        />
                        <div className="absolute top-2 left-2 bg-black/60 backdrop-blur-md px-2 py-0.5 rounded-full text-[10px] text-indigo-300 font-semibold border border-white/10 z-20">
                            🎨 AI Fashion Lookbook
                        </div>
                    </div>
                </div>
            )}

            {/* Đôi giày chủ đạo nếu có */}
            {outfit.shoe && (
                <div className="bg-gray-800/80 rounded-xl p-2.5 mb-3 border border-gray-700/60 flex items-center gap-2.5">
                    {outfit.shoe.image && (
                        <img 
                            src={outfit.shoe.image} 
                            alt={outfit.shoe.name} 
                            className="w-12 h-12 object-cover rounded-lg bg-gray-900 flex-shrink-0"
                            onError={(e) => { e.target.src = "https://images.unsplash.com/photo-1542291026-7eec264c27ff"; }}
                        />
                    )}
                    <div className="flex-1 min-w-0">
                        <span className="text-[10px] text-gray-400 block uppercase tracking-wider">Đôi giày chủ đạo</span>
                        <span className="font-bold text-white text-xs truncate block">{outfit.shoe.name}</span>
                        <div className="flex items-center gap-2">
                            <span className="text-amber-400 font-extrabold text-[11px]">
                                {outfit.shoe.price?.toLocaleString()} ₫
                            </span>
                            <span className="text-[10px] text-gray-400 font-medium">
                                (Size 39 - 43)
                            </span>
                        </div>
                    </div>
                    <Link 
                        to={`/products/${outfit.shoe.id}`}
                        className="text-[11px] bg-white/10 hover:bg-white/20 text-gray-200 px-2 py-1 rounded-lg transition whitespace-nowrap"
                    >
                        Xem →
                    </Link>
                </div>
            )}

            {/* Danh sách trang phục gợi ý */}
            <div className="space-y-2 mb-3">
                <span className="text-[10px] font-semibold text-gray-400 uppercase tracking-wider block">
                    Gợi ý kết hợp:
                </span>
                {outfit.items && outfit.items.map((item, idx) => (
                    <div key={idx} className="bg-gray-800/50 rounded-xl p-2.5 border border-gray-800/80">
                        <div className="flex items-center justify-between gap-1 mb-1">
                            <span className="font-semibold text-white flex items-center gap-1.5 text-xs">
                                <span>{getCategoryIcon(item.category)}</span>
                                <span>{item.category}: {item.name}</span>
                            </span>
                            {item.color && (
                                <span className="text-[10px] text-gray-300 bg-gray-700/60 px-1.5 py-0.5 rounded border border-gray-600/50">
                                    {item.color}
                                </span>
                            )}
                        </div>
                        {item.note && (
                            <p className="text-[11px] text-gray-400 leading-relaxed mt-0.5 pl-5">
                                {item.note}
                            </p>
                        )}
                    </div>
                ))}
            </div>

            {/* Lời khuyên từ Stylist */}
            {outfit.stylingTips && (
                <div className="bg-amber-950/40 border border-amber-500/30 rounded-xl p-2.5 text-[11px] text-amber-200/90 leading-relaxed">
                    <span className="font-bold text-amber-300 block mb-0.5">💡 Mẹo từ Stylist:</span>
                    {outfit.stylingTips}
                </div>
            )}
        </div>
    );
};

export default OutfitRecommendationCard;
