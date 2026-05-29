import { useState, useEffect } from 'react'
import styles from './ComputingLoader.module.css'

const STEPS = [
  'Scanning 56 cars in database...',
  'Applying your filters...',
  'Scoring by safety & mileage...',
  'Running AI recommendation engine...',
  'Preparing your shortlist...',
]

export default function ComputingLoader() {
  const [stepIndex, setStepIndex] = useState(0)
  const [progress, setProgress]   = useState(0)

  // Advance steps every ~900ms
  useEffect(() => {
    const id = setInterval(() => {
      setStepIndex(i => Math.min(i + 1, STEPS.length - 1))
    }, 900)
    return () => clearInterval(id)
  }, [])

  // Smooth progress bar over 5s
  useEffect(() => {
    const id = setInterval(() => {
      setProgress(p => Math.min(p + 1.4, 99))
    }, 70)
    return () => clearInterval(id)
  }, [])

  return (
    <div className={styles.wrap}>
      <div className={styles.avatar}>AI</div>
      <div className={styles.card}>
        <div className={styles.header}>
          <span className={styles.title}>Analyzing your preferences</span>
          <span className={styles.pct}>{Math.round(progress)}%</span>
        </div>

        <div className={styles.bar}>
          <div className={styles.fill} style={{ width: `${progress}%` }} />
        </div>

        <div className={styles.steps}>
          {STEPS.map((step, i) => (
            <div
              key={step}
              className={`${styles.step} ${i < stepIndex ? styles.done : ''} ${i === stepIndex ? styles.active : ''}`}
            >
              <span className={styles.dot} />
              <span className={styles.stepText}>{step}</span>
            </div>
          ))}
        </div>
      </div>
    </div>
  )
}
