import { useState } from 'react'
import CarCard from './CarCard.jsx'
import styles from './RecommendationList.module.css'

export default function RecommendationList({ recommendations }) {
  const [expanded, setExpanded] = useState(false)

  const first   = recommendations[0]
  const rest    = recommendations.slice(1)
  const hasMore = rest.length > 0

  return (
    <div className={styles.wrap}>
      {/* Always show top pick */}
      <CarCard rec={first} />

      {/* Show remaining only after click */}
      {hasMore && !expanded && (
        <button className={styles.showMore} onClick={() => setExpanded(true)}>
          Show {rest.length} more recommendation{rest.length > 1 ? 's' : ''}
          <span className={styles.arrow}>↓</span>
        </button>
      )}

      {expanded && rest.map((rec) => (
        <CarCard key={rec.rank} rec={rec} />
      ))}
    </div>
  )
}
