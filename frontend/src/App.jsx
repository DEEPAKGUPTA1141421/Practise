import { useState, useRef, useEffect, useCallback } from 'react'
import Header from './components/Header.jsx'
import Sidebar from './components/Sidebar.jsx'
import MessageBubble from './components/MessageBubble.jsx'
import TypingIndicator from './components/TypingIndicator.jsx'
import CarCard from './components/CarCard.jsx'
import InputBar from './components/InputBar.jsx'
import { sendMessage } from './api/chat.js'
import styles from './App.module.css'

// A message entry is one of:
//   { type: 'user',   text }
//   { type: 'ai',     text, isError? }
//   { type: 'recs',   recommendations }
const WELCOME = {
  type: 'ai',
  text: 'Hi! I\'m your AI car-buying assistant.\n\nTell me what you\'re looking for and I\'ll narrow down the best picks from 56 Indian cars.\n\nTry: "I want a safe family SUV under ₹15L with good mileage."',
}

export default function App() {
  const [messages, setMessages]       = useState([WELCOME])
  const [sessionId, setSessionId]     = useState(() => localStorage.getItem('cf_session') || null)
  const [loading, setLoading]         = useState(false)
  const [preferences, setPreferences] = useState(null)

  const bottomRef = useRef(null)

  // Auto-scroll on new messages
  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages, loading])

  const handleSend = useCallback(async (text) => {
    setMessages(prev => [...prev, { type: 'user', text }])
    setLoading(true)

    try {
      const data = await sendMessage(sessionId, text)
      setSessionId(data.sessionId)
      localStorage.setItem('cf_session', data.sessionId)
      setPreferences(data.preferences)

      // AI text bubble
      setMessages(prev => [
        ...prev,
        { type: 'ai', text: data.message },
      ])

      // Recommendation cards (separate entry so they render below the bubble)
      if (data.responseType === 'recommendation' && data.recommendations?.length > 0) {
        setMessages(prev => [
          ...prev,
          { type: 'recs', recommendations: data.recommendations },
        ])
      }
    } catch (err) {
      setMessages(prev => [
        ...prev,
        { type: 'ai', text: `⚠ ${err.message}`, isError: true },
      ])
    } finally {
      setLoading(false)
    }
  }, [sessionId])

  const handleNewSearch = useCallback(() => {
    setSessionId(null)
    setPreferences(null)
    localStorage.removeItem('cf_session')
    setMessages([{
      type: 'ai',
      text: 'Starting fresh! Tell me what kind of car you\'re looking for.\n\nExample: "I need a 7-seater diesel SUV under ₹20L for highway driving."',
    }])
  }, [])

  const handleExampleClick = useCallback((text) => {
    handleSend(text)
  }, [handleSend])

  return (
    <div className={styles.app}>
      <Header onNewSearch={handleNewSearch} />

      <div className={styles.body}>
        <Sidebar preferences={preferences} onExampleClick={handleExampleClick} />

        <div className={styles.chatPane}>
          {/* Messages */}
          <div className={styles.messages}>
            {messages.map((msg, i) => {
              if (msg.type === 'user') {
                return <MessageBubble key={i} role="user" text={msg.text} />
              }
              if (msg.type === 'ai') {
                return <MessageBubble key={i} role="ai" text={msg.text} isError={msg.isError} />
              }
              if (msg.type === 'recs') {
                return (
                  <div key={i} className={styles.recList}>
                    {msg.recommendations.map((rec) => (
                      <CarCard key={rec.rank} rec={rec} />
                    ))}
                  </div>
                )
              }
              return null
            })}

            {loading && <TypingIndicator />}
            <div ref={bottomRef} />
          </div>

          <InputBar onSend={handleSend} disabled={loading} />
        </div>
      </div>
    </div>
  )
}
