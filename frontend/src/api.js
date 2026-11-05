async function post(url, body){
    const response = await fetch(url, {
        method: "POST",
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body),
    });
    if(!response.ok) throw new Error(`Request failed: ${response.status}`);
    return response.json();
}

export const startSession = (level) => post('/api/sessions', { level });
export const sendMessage = (sessionId, text) => post(`/api/sessions/${sessionId}/messages`, { text });