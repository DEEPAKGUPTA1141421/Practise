import styles from './MessageBubble.module.css'

export default function MessageBubble({ role, text, isError }) {
  const isUser = role === 'user'
  return (
    <div className={`${styles.row} ${isUser ? styles.rowUser : styles.rowAi}`}>
      <div className={`${styles.avatar} ${isUser ? styles.avatarUser : styles.avatarAi}`}>
        {isUser ? 'You' : 'AI'}
      </div>
      <div
        className={`${styles.bubble} ${isUser ? styles.bubbleUser : styles.bubbleAi} ${isError ? styles.bubbleError : ''}`}
      >
        {text.split('\n').map((line, i) =>
          line === '' ? <br key={i} /> : <p key={i}>{line}</p>
        )}
      </div>
    </div>
  )
}
