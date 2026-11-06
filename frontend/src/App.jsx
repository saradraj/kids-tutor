import { useEffect, useRef, useState } from "react";
import {sendMessage, startSession} from "./api.js";
import Flashcards from './Flashcards.jsx';

const LEVELS = [
    { level: 1, name: 'Easy', sample: '3 + 4', color: 'mint'},
    { level: 2, name: 'Medium', sample: '15 - 7', color: 'sky'},
    { level: 3, name: 'Time Tables', sample: '6 * 7', color: 'grape'},
];

const KEYS = ['1', '2', '3', '4', '5', '6', '7', '8', '9', '⌫', '0', '✓'];

export default function App() {
    const [session, setSession] = useState(null);
    const [cardsLevel, setCardsLevel] = useState(null);
    if(cardsLevel) {
        return <Flashcards level={cardsLevel} onExit={() => setCardsLevel(null)} />;
    }
    return session
        ? <Tutor session={session} onExit={() => setSession(null)} />
        : <LevelPicker onPick={setSession} onPickCards={setCardsLevel} />;
}

function LevelPicker({ onPick, onPickCards }) {
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const [mode, setMode] = useState('tutor');

    async function pick(level) {
        if(mode === 'cards') {
            onPickCards(LEVELS.find((l) => l.level === level));
            return;
        }
        setLoading(true);
        setError('');
        try {
            const data = await startSession(level);
            onPick({ id: data.sessionId, problem: data.problem, level });
        } catch {
            setError("Pip can't connect right now. Is the server running?");
            setLoading(false);
        }
    }

    return (
        <main className="screen picker">
            <Pip size="big" />
            <h1>Math with Pip</h1>
            <div className="mode-toggle" role="group" aria-label="Practice mode">
                <button className={mode === 'tutor' ? 'active' : ''} onClick={() => setMode('tutor')}>Learn with Pip</button>
                <button className={mode === 'cards' ? 'active' : ''} onClick={() => setMode('cards')}>Flashcards</button>
            </div>
            <p className="subtitle">Pick a level to start!</p>
            <div className="levels">
                {LEVELS.map((l) => (
                    <button key={l.level} className={`level-card ${l.color}`} disabled={loading} onClick={() => pick(l.level)}>
                        <span className="sample">{l.sample}</span>
                        <span className="name">{l.name}</span>
                    </button>
                ))}
            </div>
            {error && <p className="error">{error}</p>}
        </main>
    );
}

function Tutor({ session, onExit }) {
    const [messages, setMessages] = useState([
        { from: 'pip', text: "Hi! I'm Pip. What do you think the answer is?" },
    ]);
    const [problem, setProblem] = useState(session.problem);
    const [solved, setSolved] = useState(false);
    const [stars, setStars] = useState(0);
    const [input, setInput] = useState('');
    const [thinking, setThinking] = useState(false);
    const [celebrate, setCelebrate] = useState(false);
    const chatEnd = useRef(null);

    useEffect(() => {
        chatEnd.current?.scrollIntoView({ behavior: 'smooth' });
    }, [messages, thinking]);

    async function send(text) {
        const trimmed = text.trim();
        if (!trimmed || thinking) return;
        setMessages((m) => [...m, { from: 'kid', text: trimmed }]);
        setInput('');
        setThinking(true);
        try {
            const data = await sendMessage(session.id, trimmed);
            setMessages((m) => [...m, { from: 'pip', text: data.reply }]);
            if (data.solved && !solved) {
                setStars((s) => s + 1);
                setCelebrate(true);
                setTimeout(() => setCelebrate(false), 1800);
            }
            setSolved(data.solved);
            setProblem(data.problem);
        } catch {
            setMessages((m) => [...m, { from: 'pip', text: "Oops, I couldn't hear you. Can you try again?" }]);
        } finally {
            setThinking(false);
        }
    }

    function pressKey(key) {
        if (key === '⌫') setInput((v) => v.slice(0, -1));
        else if (key === '✓') send(input);
        else setInput((v) => (v.length < 4 ? v + key : v));
    }

    const level = LEVELS.find((l) => l.level === session.level);

    return (
        <main className="screen tutor">
            <header className="topbar">
                <button className="back" onClick={onExit} aria-label="Back to levels">←</button>
                <span className={`badge ${level.color}`}>{level.name}</span>
                <span className="stars" aria-label={`${stars} stars`}>⭐ {stars}</span>
            </header>

            <section className={`problem ${solved ? 'solved' : ''}`}>
                <span className="problem-text">{problem} {solved ? '✓' : '= ?'}</span>
                {celebrate && <Confetti />}
            </section>

            <section className="chat" aria-live="polite">
                {messages.map((m, i) => (
                    <div key={i} className={`row ${m.from}`}>
                        {m.from === 'pip' && <Pip />}
                        <div className="bubble">{m.text}</div>
                    </div>
                ))}
                {thinking && (
                    <div className="row pip">
                        <Pip />
                        <div className="bubble typing"><span /><span /><span /></div>
                    </div>
                )}
                <div ref={chatEnd} />
            </section>

            {solved && (
                <button className="next" disabled={thinking} onClick={() => send('Yes, give me another problem!')}>
                    Next problem →
                </button>
            )}

            <form className="answer" onSubmit={(e) => { e.preventDefault(); send(input); }}>
                <input
                    value={input}
                    onChange={(e) => setInput(e.target.value)}
                    maxLength={200}
                    placeholder="Your answer"
                    disabled={thinking}
                    autoComplete="off"
                />
                <button type="submit" disabled={thinking || !input.trim()}>Send</button>
            </form>

            <div className="keypad">
                {KEYS.map((k) => (
                    <button
                        key={k}
                        className={k === '✓' ? 'key go' : k === '⌫' ? 'key erase' : 'key'}
                        disabled={thinking || (k === '✓' && !input.trim())}
                        onClick={() => pressKey(k)}
                        aria-label={k === '⌫' ? 'Delete' : k === '✓' ? 'Send answer' : k}
                    >
                        {k}
                    </button>
                ))}
            </div>
        </main>
    );
}

function Pip({ size }) {
    return <span className={`pip-avatar ${size || ''}`} aria-hidden="true">🦉</span>;
}

function Confetti() {
    const pieces = Array.from({ length: 24 }, (_, i) => i);
    return (
        <div className="confetti" aria-hidden="true">
            {pieces.map((i) => (
                <span key={i} style={{ '--x': `${(i * 37) % 100}%`, '--d': `${(i % 6) * 0.08}s`, '--c': i % 4 }} />
            ))}
        </div>
    );
}