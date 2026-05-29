export async function sendMessage(sessionId, message) {
  const res = await fetch('/api/chat', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ sessionId: sessionId || '', message }),
  })
  const json = await res.json()
  if (json.status !== 'success') throw new Error(json.error || 'Server error')
  return json.data
}
