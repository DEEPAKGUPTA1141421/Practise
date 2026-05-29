import styles from './Header.module.css'

export default function Header({ onNewSearch }) {
  return (
    <header className={styles.header}>
      <div className={styles.brand}>
        <span className={styles.logo}>CF</span>
        <div className={styles.brandText}>
          <span className={styles.name}>CarFinder AI</span>
          <span className={styles.tagline}>56 cars · AI-powered</span>
        </div>
      </div>
      <button className={styles.newBtn} onClick={onNewSearch}>
        New Search
      </button>
    </header>
  )
}
