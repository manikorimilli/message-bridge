export type SendMessageRequest = {
  type: string
  to: string
  message: string
}

export type SendMessageResult = {
  messageId: string
  type: string
}

/**
 * Empty by default: the page calls its own /v1/..., which the Vite dev server and
 * vercel.json forward to the API. Set VITE_API_URL to call another API directly.
 */
const API_URL: string = import.meta.env.VITE_API_URL ?? ''

export async function sendMessage(request: SendMessageRequest): Promise<SendMessageResult> {
  let response: Response
  try {
    response = await fetch(`${API_URL}/v1/message`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(request),
    })
  } catch {
    throw new Error('Could not reach the server. Render’s free plan can take about a minute to wake up; try again.')
  }

  const body: unknown = await response.json().catch(() => null)

  if (!response.ok) {
    // The API returns errors as JSON with a readable "detail" field.
    const detail = isObject(body) && typeof body.detail === 'string' ? body.detail : null
    throw new Error(detail ?? `Request failed with status ${response.status}`)
  }
  if (!isObject(body) || typeof body.messageId !== 'string') {
    throw new Error('The server sent an unexpected response')
  }
  return { messageId: body.messageId, type: String(body.type) }
}

function isObject(value: unknown): value is Record<string, unknown> {
  return typeof value === 'object' && value !== null
}
