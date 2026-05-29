import { useRef } from 'react'
import styles from './InputBar.module.css'

export default function InputBar({ onSend, disabled }) {
  const ref = useRef(null)

  function handleSend() {
    const val = ref.current.value.trim()
    if (!val || disabled) return
    ref.current.value = ''
    onSend(val)
  }

  function handleKey(e) {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault()
      handleSend()
    }
  }

  return (
    <div className={styles.bar}>
      <input
        ref={ref}
        type="text"
        className={styles.input}
        placeholder="Describe what car you're looking for…"
        onKeyDown={handleKey}
        disabled={disabled}
        autoComplete="off"
        autoFocus
      />
      <button
        className={styles.sendBtn}
        onClick={handleSend}
        disabled={disabled}
      >
        {disabled ? '…' : 'Send'}
      </button>
    </div>
  )
}
