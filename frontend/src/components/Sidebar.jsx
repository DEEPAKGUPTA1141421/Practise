import styles from './Sidebar.module.css'

const EXAMPLES = [
  'Safe family SUV under 15 lakhs, good mileage',
  'Petrol sedan under 12L for highway driving',
  'Affordable automatic hatchback under 8 lakhs',
  'Diesel 7-seater MPV under 20L for long trips',
]

const PREF_LABELS = {
  budget:       { label: 'Budget',       fmt: v => `₹${(v / 100000).toFixed(0)}L` },
  bodyType:     { label: 'Type',         fmt: v => v },
  fuelType:     { label: 'Fuel',         fmt: v => v },
  transmission: { label: 'Transmission', fmt: v => v },
  usageType:    { label: 'Usage',        fmt: v => v },
  seatingCapacity: { label: 'Min Seats', fmt: v => `${v}+` },
}

export default function Sidebar({ preferences, onExampleClick }) {
  const filled = preferences
    ? Object.entries(PREF_LABELS).filter(([k]) => preferences[k] != null)
    : []

  const priorities = preferences?.priorities?.filter(Boolean) ?? []

  return (
    <aside className={styles.sidebar}>
      <section className={styles.section}>
        <h3 className={styles.sectionTitle}>Your Profile</h3>
        {filled.length === 0 && priorities.length === 0 ? (
          <p className={styles.empty}>
            Start chatting — your preferences appear here as we talk.
          </p>
        ) : (
          <div className={styles.chips}>
            {filled.map(([key, meta]) => (
              <div key={key} className={styles.chip}>
                <span className={styles.chipLabel}>{meta.label}</span>
                <span className={styles.chipValue}>{meta.fmt(preferences[key])}</span>
              </div>
            ))}
            {priorities.length > 0 && (
              <div className={`${styles.chip} ${styles.chipPriority}`}>
                <span className={styles.chipLabel}>Priorities</span>
                <span className={styles.chipValue}>{priorities.join(', ')}</span>
              </div>
            )}
          </div>
        )}
      </section>

      <section className={styles.section}>
        <h3 className={styles.sectionTitle}>Try asking</h3>
        <div className={styles.examples}>
          {EXAMPLES.map((ex) => (
            <button
              key={ex}
              className={styles.example}
              onClick={() => onExampleClick(ex)}
            >
              {ex}
            </button>
          ))}
        </div>
      </section>
    </aside>
  )
}
