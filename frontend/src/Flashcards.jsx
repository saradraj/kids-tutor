import { useEffect, useState } from 'react';
import { checkFlashcard, getFlashcards } from './api.js';

const KEYS = ['1', '2', '3', '4', '5', '6', '7', '8', '9', '⌫', '0', '✓'];

export default function Flashcards({ level, onExit }) {
    const [deck, setDeck] = useState([]);
    const [index, setIndex] = useState(0);
    const [input, setInput] = useState('');
    const [result, setResult] = useState(null);
    const [score, setScore] = useState(0);
    const [missed, setMissed] = useState([]);
    const [busy, setBusy] = useState(false);
    const [error, setError] = useState('');

    async function loadDeck(cards) {
        setError('');
        setIndex(0);
        setInput('');
        setResult(null);
        setScore(0);
        setMissed([]);
        if (cards) {
            setDeck(cards);
            return;
        }
        setBusy(true);
        try {
            setDeck(await getFlashcards(level.level));
        } catch {
            setError("Pip can't find the cards right now. Is the server running?");
        } finally {
            setBusy(false);
        }
    }

    useEffect(() => {
        loadDeck();
    }, []);

    const card = deck[index];
    const finished = deck.length > 0 && index >= deck.length;

    async function submit() {
        if (!card || result || busy || !input.trim()) return;
        setBusy(true);
        try {
            const res = await checkFlashcard(card, input.trim());
            setResult({ ...res, given: input.trim() });
            if (res.correct) setScore((s) => s + 1);
            else setMissed((m) => [...m, card]);
        } catch {
            setError("Oops, Pip couldn't check that card. Try again?");
        } finally {
            setBusy(false);
        }
    }

    function next() {
        setResult(null);
        setInput('');
        setIndex((i) => i + 1);
    }

    function pressKey(key) {
        if (result) return;
        if (key === '⌫') setInput((v) => v.slice(0, -1));
        else if (key === '✓') submit();
        else setInput((v) => (v.length < 4 ? v + key : v));
    }

    return (
        <main className="screen tutor">
            <header className="topbar">
                <button className="back" onClick={onExit} aria-label="Back to levels">←</button>
                <span className={`badge ${level.color}`}>{level.name} cards</span>
                <span className="stars" aria-label={`${score} correct`}>⭐ {score}</span>
            </header>

            {error && <p className="error">{error}</p>}

            {finished ? (
                <section className="summary">
                    <div className="summary-score">{score} / {deck.length}</div>
                    <p className="summary-text">
                        {score === deck.length ? 'Perfect! Every card right!' : 'Great practice! Keep going!'}
                    </p>
                    {missed.length > 0 && (
                        <button className="next" onClick={() => loadDeck(missed)}>
                            Try my {missed.length} missed card{missed.length === 1 ? '' : 's'}
                        </button>
                    )}
                    <button className="next secondary" onClick={() => loadDeck()}>New cards</button>
                </section>
            ) : card ? (
                <>
                    <div className="progress" aria-label={`Card ${index + 1} of ${deck.length}`}>
                        <div className="progress-fill" style={{ width: `${(index / deck.length) * 100}%` }} />
                    </div>
                    <p className="progress-text">Card {index + 1} of {deck.length}</p>

                    <div className={`flashcard ${result ? 'flipped' : ''}`}>
                        <div className="flashcard-inner">
                            <div className="card-face card-front">
                                <span className="card-text">{card.text} = ?</span>
                                <span className="card-note">{input || 'Type your answer'}</span>
                            </div>
                            <div className={`card-face card-back ${result?.correct ? 'right' : 'wrong'}`}>
                                {result && (
                                    <>
                                        <span className="card-emoji" aria-hidden="true">{result.correct ? '🎉' : '💪'}</span>
                                        <span className="card-text">{card.text} = {result.answer}</span>
                                        <span className="card-note">
                      {result.correct ? 'You got it!' : `You said ${result.given}. Nice try!`}
                    </span>
                                    </>
                                )}
                            </div>
                        </div>
                    </div>

                    {result ? (
                        <button className="next" onClick={next}>
                            {index + 1 < deck.length ? 'Next card →' : 'See my score'}
                        </button>
                    ) : (
                        <div className="keypad">
                            {KEYS.map((k) => (
                                <button
                                    key={k}
                                    className={k === '✓' ? 'key go' : k === '⌫' ? 'key erase' : 'key'}
                                    disabled={busy || (k === '✓' && !input.trim())}
                                    onClick={() => pressKey(k)}
                                    aria-label={k === '⌫' ? 'Delete' : k === '✓' ? 'Check answer' : k}
                                >
                                    {k}
                                </button>
                            ))}
                        </div>
                    )}
                </>
            ) : (
                !error && <p className="progress-text">Shuffling cards…</p>
            )}
        </main>
    );
}