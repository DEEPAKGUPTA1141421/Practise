import styles from './CarCard.module.css'

export default function CarCard({ rec }) {
  const { car, rank, whyItFits, tradeoffs, topPick, matchScore } = rec
  const specs = car.specs || {}
  const features = specs.features ?? []
  const price = (car.price / 100000).toFixed(2)
  const scoreWidth = Math.round((matchScore / 10) * 100)

  return (
    <div className={`${styles.card} ${topPick ? styles.topPick : ''}`}>
      {/* Header row */}
      <div className={styles.cardHeader}>
        <div className={styles.rankBadge}>{rank}</div>
        <div className={styles.carName}>
          <span className={styles.make}>{car.make}</span>{' '}
          <span className={styles.model}>{car.model}</span>{' '}
          <span className={styles.variant}>{car.variant}</span>
        </div>
        {topPick && <span className={styles.topBadge}>Top Pick</span>}
        <div className={styles.price}>₹{price}L</div>
      </div>

      {/* Spec grid */}
      <div className={styles.specGrid}>
        <SpecItem label="Fuel"         value={specs.fuelType}         highlight />
        <SpecItem label="Gearbox"      value={specs.transmission} />
        <SpecItem label="Mileage"      value={`${car.mileage} km/l`}  highlight />
        <SpecItem label="Safety"       value={`${car.safetyRating}/5`} highlight />
        <SpecItem label="Seats"        value={specs.seatingCapacity} />
        <SpecItem label="Boot"         value={specs.bootSpace} />
      </div>

      {/* Analysis */}
      <div className={styles.analysis}>
        <div className={styles.analysisBlock}>
          <div className={`${styles.analysisLabel} ${styles.labelGreen}`}>Why it fits</div>
          <p className={styles.analysisText}>{whyItFits}</p>
        </div>
        <div className={styles.analysisBlock}>
          <div className={`${styles.analysisLabel} ${styles.labelGray}`}>Tradeoffs</div>
          <p className={styles.analysisText}>{tradeoffs}</p>
        </div>
      </div>

      {/* Features */}
      {features.length > 0 && (
        <div className={styles.features}>
          {features.slice(0, 5).map((f) => (
            <span key={f} className={styles.featureTag}>{f}</span>
          ))}
        </div>
      )}

      {/* Match score bar */}
      <div className={styles.scoreRow}>
        <span className={styles.scoreLabel}>Match score</span>
        <div className={styles.scoreBar}>
          <div className={styles.scoreBarFill} style={{ width: `${scoreWidth}%` }} />
        </div>
        <span className={styles.scoreValue}>{matchScore.toFixed(1)}/10</span>
      </div>
    </div>
  )
}

function SpecItem({ label, value, highlight }) {
  if (!value) return null
  return (
    <div className={`${styles.specItem} ${highlight ? styles.specHighlight : ''}`}>
      <span className={styles.specLabel}>{label}</span>
      <span className={styles.specValue}>{value}</span>
    </div>
  )
}
