import { useState, type FormEvent } from 'react'
import { sendMessage, type SendMessageResult } from './api'
import './App.css'

type Status =
  | { state: 'idle' }
  | { state: 'sending' }
  | { state: 'sent'; result: SendMessageResult }
  | { state: 'failed'; title: string; text: string }

/** Twilio's trial accounts refuse numbers that are not verified; say what to do instead. */
function describeError(error: unknown, to: string): { title: string; text: string } {
  const message = error instanceof Error ? error.message : 'Something went wrong'
  if (/verified recipient/i.test(message)) {
    return {
      title: 'Number not verified',
      text:
        `This number can't receive messages yet. The Twilio account is on a free trial, which only sends SMS to ` +
        `verified numbers. Ask the account owner to add ${to} as a verified number in the Twilio Console ` +
        `(Phone Numbers > Verified Caller IDs), then try again.`,
    }
  }
  return { title: 'Message not sent', text: message }
}

export function App() {
  const [status, setStatus] = useState<Status>({ state: 'idle' })
  const sending = status.state === 'sending'

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const form = new FormData(event.currentTarget)
    const to = String(form.get('to')).trim()
    setStatus({ state: 'sending' })
    try {
      const result = await sendMessage({
        type: String(form.get('type')),
        to,
        message: String(form.get('message')).trim(),
      })
      setStatus({ state: 'sent', result })
    } catch (error) {
      setStatus({ state: 'failed', ...describeError(error, to) })
    }
  }

  return (
    <main className="page">
      <form className="card" onSubmit={handleSubmit}>
        <h1>Message Bridge</h1>
        <p className="muted">Send a test message through the message-bridge API.</p>

        <label htmlFor="type">Channel</label>
        <select id="type" name="type" defaultValue="SMS">
          <option value="SMS">SMS</option>
          <option value="WHATSAPP" disabled>
            WhatsApp (coming soon)
          </option>
          <option value="EMAIL" disabled>
            Email (coming soon)
          </option>
        </select>

        <label htmlFor="to">To</label>
        <input
          id="to"
          name="to"
          type="tel"
          required
          pattern="[+][1-9][0-9]{6,14}"
          title="International format: + then country code and number, e.g. +14155550100"
          placeholder="+14155550100"
          autoComplete="off"
          aria-describedby="to-hint"
        />
        <p id="to-hint" className="hint">
          International format with country code. On a Twilio trial, only numbers verified in your Twilio Console
          receive messages.
        </p>

        <label htmlFor="message">Message</label>
        <textarea
          id="message"
          name="message"
          rows={3}
          required
          maxLength={1600}
          defaultValue="sms_2fa"
          aria-describedby="message-hint"
        />
        <p id="message-hint" className="note">
          <strong>Twilio free trial: templates only.</strong> A trial account cannot send your own text. Use one of
          these template names and Twilio sends its own sample message: <code>sms_2fa</code>,{' '}
          <code>sms_appointment_reminders</code>, <code>sms_order_confirmation</code>,{' '}
          <code>sms_delivery_updates</code> or <code>sms_internal_alerts</code>.
        </p>

        <button type="submit" disabled={sending}>
          {sending ? 'Sending…' : 'Send message'}
        </button>

        <div role="status" aria-live="polite">
          {status.state === 'sent' ? (
            <p className="result success">
              Message sent. {status.result.type} id: <code>{status.result.messageId}</code>
            </p>
          ) : null}
          {status.state === 'failed' ? (
            <div className="result error">
              <strong>{status.title}</strong>
              <p>{status.text}</p>
            </div>
          ) : null}
        </div>
      </form>
    </main>
  )
}
