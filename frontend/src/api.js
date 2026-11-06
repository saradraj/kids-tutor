const API_BASE = import.meta.env.VITE_API_BASE_URL ?? '';

async function post(path, body) {
    const res = await fetch(`${API_BASE}${path}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body),
    });
    if (!res.ok) throw new Error(`Request failed: ${res.status}`);
    return res.json();
}

async function get(path) {
    const res = await fetch(`${API_BASE}${path}`);
    if (!res.ok) throw new Error(`Request failed: ${res.status}`);
    return res.json();
}

export const startSession = (level) => post('/api/sessions', { level });

export const sendMessage = (sessionId, text) =>
    post(`/api/sessions/${sessionId}/messages`, { text });

export const getFlashcards = (level, count = 10) =>
    get(`/api/flashcards?level=${level}&count=${count}`);

export const checkFlashcard = (card, answer) =>
    post('/api/flashcards/check', { left: card.left, op: card.op, right: card.right, answer });