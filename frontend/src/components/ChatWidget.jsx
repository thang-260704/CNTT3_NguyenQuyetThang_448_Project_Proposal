import { useEffect, useRef, useState } from 'react';
import { Link } from 'react-router-dom';
import api, { errorMessage } from '../api/client';
import { formatPrice } from '../utils/format';

const WELCOME = {
  from: 'bot',
  text: 'Xin chào! Mình là trợ lý AI của QuickShop. Bạn cần tìm sản phẩm gì? Ví dụ: "tai nghe", "áo dưới 200k".',
};

export default function ChatWidget() {
  const [open, setOpen] = useState(false);
  const [messages, setMessages] = useState([WELCOME]);
  const [input, setInput] = useState('');
  const [loading, setLoading] = useState(false);
  const bottomRef = useRef(null);

  useEffect(() => {
    bottomRef.current?.scrollIntoView?.({ behavior: 'smooth' });
  }, [messages, open]);

  const send = async (e) => {
    e.preventDefault();
    const text = input.trim();
    if (!text || loading) return;
    setMessages((m) => [...m, { from: 'user', text }]);
    setInput('');
    setLoading(true);
    try {
      const { data } = await api.post('/ai/chat', { message: text });
      setMessages((m) => [...m, { from: 'bot', text: data.reply, suggestions: data.suggestions }]);
    } catch (err) {
      setMessages((m) => [...m, { from: 'bot', text: errorMessage(err, 'Trợ lý đang bận, bạn thử lại sau nhé.') }]);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed bottom-4 right-4 z-40">
      {open && (
        <div className="mb-3 flex h-[28rem] w-[calc(100vw-2rem)] max-w-sm flex-col overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-2xl">
          <div className="flex items-center justify-between bg-brand-500 px-4 py-3 text-white">
            <div>
              <p className="font-semibold">Trợ lý QuickShop AI</p>
              <p className="text-xs text-brand-100">Tư vấn sản phẩm 24/7</p>
            </div>
            <button type="button" onClick={() => setOpen(false)} aria-label="Đóng chat" className="text-xl leading-none">
              ×
            </button>
          </div>
          <div className="flex-1 space-y-3 overflow-y-auto p-3">
            {messages.map((m, i) => (
              <div key={i} className={`flex ${m.from === 'user' ? 'justify-end' : 'justify-start'}`}>
                <div
                  className={`max-w-[85%] whitespace-pre-line rounded-2xl px-3 py-2 text-sm ${
                    m.from === 'user' ? 'bg-brand-500 text-white' : 'bg-slate-100 text-slate-800'
                  }`}
                >
                  {m.text}
                  {m.suggestions?.length > 0 && (
                    <div className="mt-2 space-y-1">
                      {m.suggestions.map((p) => (
                        <Link
                          key={p.id}
                          to={`/products/${p.id}`}
                          className="block rounded-lg bg-white px-2 py-1 text-xs text-slate-700 hover:text-brand-600"
                          onClick={() => setOpen(false)}
                        >
                          {p.name} · <b>{formatPrice(p.price)}</b>
                        </Link>
                      ))}
                    </div>
                  )}
                </div>
              </div>
            ))}
            {loading && <p className="text-xs text-slate-400">Trợ lý đang trả lời...</p>}
            <div ref={bottomRef} />
          </div>
          <form onSubmit={send} className="flex gap-2 border-t border-slate-200 p-2">
            <input
              className="input"
              placeholder="Nhập câu hỏi..."
              value={input}
              maxLength={500}
              onChange={(e) => setInput(e.target.value)}
            />
            <button type="submit" className="btn-primary" disabled={loading || !input.trim()}>
              Gửi
            </button>
          </form>
        </div>
      )}
      <button
        type="button"
        onClick={() => setOpen(!open)}
        className="ml-auto flex h-14 w-14 items-center justify-center rounded-full bg-brand-500 text-white shadow-lg hover:bg-brand-600"
        aria-label="Mở trợ lý AI"
      >
        <svg xmlns="http://www.w3.org/2000/svg" className="h-7 w-7" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2">
          <path strokeLinecap="round" strokeLinejoin="round" d="M8 10h.01M12 10h.01M16 10h.01M21 12c0 4.4-4 8-9 8a9.9 9.9 0 01-4-.8L3 20l1.3-3.9A7.6 7.6 0 013 12c0-4.4 4-8 9-8s9 3.6 9 8z" />
        </svg>
      </button>
    </div>
  );
}
