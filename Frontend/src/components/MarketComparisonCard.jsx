import { Link } from 'react-router-dom';

const PLATFORM_STYLE = {
    PROJECT_SKATEBOARD: 'bg-black text-white',
    SHOPEE: 'bg-orange-500 text-white',
    LAZADA: 'bg-blue-600 text-white',
    TIKI: 'bg-sky-500 text-white',
    TIKTOK_SHOP: 'bg-gray-900 text-white',
};

const formatVnd = (value) => {
    if (value == null || Number.isNaN(Number(value))) return null;
    return `${Number(value).toLocaleString('vi-VN')} đ`;
};

const priceLabel = (offer) => {
    if (offer.price != null) return formatVnd(offer.price);
    if (offer.priceMin != null && offer.priceMax != null) {
        return `${formatVnd(offer.priceMin)} – ${formatVnd(offer.priceMax)}`;
    }
    return 'Mở sàn để xem giá';
};

const sourceLabel = (offer) => {
    if (offer.source === 'STORE') return 'Giá shop';
    if (offer.source === 'CATALOG') return 'Đã lưu';
    if (offer.source === 'ESTIMATE') return 'Ước lượng AI';
    return 'Xem trên sàn';
};

const MarketComparisonCard = ({ comparison }) => {
    if (!comparison?.offers?.length) return null;

    const exactPrices = comparison.offers
        .map((offer) => offer.price)
        .filter((price) => price != null);
    const lowest = exactPrices.length > 1 ? Math.min(...exactPrices) : null;

    return (
        <div className="w-full mt-1 rounded-2xl border border-gray-200 bg-white shadow-sm overflow-hidden text-xs">
            <div className="px-3 py-2.5 bg-gray-950 text-white flex items-start justify-between gap-2">
                <div>
                    <p className="text-[10px] uppercase tracking-wide text-amber-300 font-semibold">So sánh đa sàn</p>
                    <p className="font-semibold text-sm leading-snug">
                        {comparison.product?.name || 'Sản phẩm ProjectSkateBoard'}
                    </p>
                </div>
                {comparison.verdict && (
                    <span className="shrink-0 max-w-[46%] text-[10px] leading-snug bg-amber-400 text-black font-semibold px-2 py-1 rounded-lg">
                        {comparison.verdict}
                    </span>
                )}
            </div>

            <div className="divide-y divide-gray-100">
                {comparison.offers.map((offer) => {
                    const isLowest = lowest != null && offer.price != null && offer.price === lowest;
                    const external = offer.url && !offer.url.startsWith('/');
                    const row = (
                        <>
                            <div className="flex items-center gap-2 min-w-0">
                                <span className={`shrink-0 px-1.5 py-0.5 rounded text-[10px] font-bold ${PLATFORM_STYLE[offer.platform] || 'bg-gray-200 text-gray-800'}`}>
                                    {offer.platformLabel || offer.platform}
                                </span>
                                <span className="text-[10px] text-gray-500 truncate">{sourceLabel(offer)}</span>
                            </div>
                            <div className="mt-1 flex items-baseline justify-between gap-2">
                                <span className={`font-bold ${offer.ours ? 'text-black' : 'text-gray-900'}`}>
                                    {priceLabel(offer)}
                                </span>
                                {isLowest && (
                                    <span className="text-[10px] font-semibold text-emerald-700 bg-emerald-50 border border-emerald-200 px-1.5 py-0.5 rounded-full">
                                        Thấp nhất
                                    </span>
                                )}
                            </div>
                            {(offer.shopName || offer.rating || offer.soldCount) && (
                                <p className="mt-1 text-[10px] text-gray-500 truncate">
                                    {[
                                        offer.shopName,
                                        offer.rating ? `${offer.rating} sao` : null,
                                        offer.soldCount ? `${offer.soldCount.toLocaleString('vi-VN')} đã bán` : null,
                                    ].filter(Boolean).join(' · ')}
                                </p>
                            )}
                            {offer.highlights && (
                                <p className="mt-1 text-[11px] text-gray-600 leading-relaxed">{offer.highlights}</p>
                            )}
                        </>
                    );

                    const className = `block px-3 py-2.5 ${offer.ours ? 'bg-amber-50/70' : 'hover:bg-gray-50'}`;
                    if (!offer.url) {
                        return <div key={offer.platform} className={className}>{row}</div>;
                    }
                    if (external) {
                        return (
                            <a
                                key={offer.platform}
                                href={offer.url}
                                target="_blank"
                                rel="noreferrer"
                                className={className}
                            >
                                {row}
                            </a>
                        );
                    }
                    return (
                        <Link key={offer.platform} to={offer.url} className={className}>
                            {row}
                        </Link>
                    );
                })}
            </div>

            {comparison.disclaimer && (
                <p className="px-3 py-2 text-[10px] leading-relaxed text-gray-500 bg-gray-50 border-t border-gray-100">
                    {comparison.disclaimer}
                </p>
            )}
        </div>
    );
};

export default MarketComparisonCard;
