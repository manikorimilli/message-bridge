import { useState, type FormEvent } from 'react'
import { z } from 'zod'
import { CircleAlertIcon, CircleCheckIcon, InfoIcon } from 'lucide-react'
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert'
import { Button } from '@/components/ui/button'
import { Field, FieldDescription, FieldError, FieldLabel } from '@/components/ui/field'
import { Input } from '@/components/ui/input'
import { Select, SelectItem, SelectPopup, SelectTrigger, SelectValue } from '@/components/ui/select'
import { Textarea } from '@/components/ui/textarea'
import { useSendMessage } from '@/features/message/hooks'
import { sendMessageSchema, type MessageType } from '@/features/message/schemas'
import { ApiError } from '@/lib/api'

/** All channels are listed; only SMS can be picked until the backend supports the others. */
const channels: { value: MessageType; label: string; disabled: boolean }[] = [
  { value: 'SMS', label: 'SMS', disabled: false },
  { value: 'WHATSAPP', label: 'WhatsApp (coming soon)', disabled: true },
  { value: 'EMAIL', label: 'Email (coming soon)', disabled: true },
]

type FieldErrors = Partial<Record<'type' | 'to' | 'message', string[]>>

function errorMessage(error: Error): string {
  if (error instanceof ApiError) {
    return error.message
  }
  return 'Could not reach the server. On Render’s free plan it can take about a minute to wake up, so try again shortly.'
}

export function SendMessageForm() {
  const [type, setType] = useState<MessageType>('SMS')
  const [fieldErrors, setFieldErrors] = useState<FieldErrors>({})
  const sendMessage = useSendMessage()

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const form = new FormData(event.currentTarget)
    const parsed = sendMessageSchema.safeParse({ type, to: form.get('to'), message: form.get('message') })
    if (!parsed.success) {
      setFieldErrors(z.flattenError(parsed.error).fieldErrors)
      return
    }
    setFieldErrors({})
    sendMessage.mutate(parsed.data)
  }

  return (
    <form className="flex flex-col gap-5" noValidate onSubmit={handleSubmit}>
      <Field>
        <FieldLabel>Channel</FieldLabel>
        <Select items={channels} value={type} onValueChange={(value) => value && setType(value)}>
          <SelectTrigger>
            <SelectValue />
          </SelectTrigger>
          <SelectPopup>
            {channels.map((channel) => (
              <SelectItem key={channel.value} value={channel.value} disabled={channel.disabled}>
                {channel.label}
              </SelectItem>
            ))}
          </SelectPopup>
        </Select>
        <FieldDescription>Only SMS is available for now. WhatsApp and Email are coming soon.</FieldDescription>
      </Field>

      <Field name="to" invalid={Boolean(fieldErrors.to)}>
        <FieldLabel>To</FieldLabel>
        <Input name="to" type="tel" autoComplete="off" placeholder="+14155550100" />
        <FieldDescription>
          International format with country code. On a Twilio trial, only numbers verified in your Twilio Console
          can receive messages.
        </FieldDescription>
        {fieldErrors.to ? <FieldError match>{fieldErrors.to[0]}</FieldError> : null}
      </Field>

      <Field name="message" invalid={Boolean(fieldErrors.message)}>
        <FieldLabel>Message</FieldLabel>
        <Textarea name="message" rows={3} defaultValue="sms_2fa" />
        {fieldErrors.message ? <FieldError match>{fieldErrors.message[0]}</FieldError> : null}
      </Field>

      <Alert variant="info">
        <InfoIcon aria-hidden="true" />
        <AlertTitle>Twilio free trial: templates only</AlertTitle>
        <AlertDescription>
          A trial account cannot send your own text. Enter one of these template names and Twilio sends its own
          sample message for it: sms_2fa, sms_appointment_reminders, sms_order_confirmation, sms_delivery_updates
          or sms_internal_alerts. Upgrade the Twilio account to send custom messages.
        </AlertDescription>
      </Alert>

      <Button type="submit" loading={sendMessage.isPending}>
        Send message
      </Button>

      <div role="status" aria-live="polite">
        {sendMessage.isSuccess ? (
          <Alert variant="success">
            <CircleCheckIcon aria-hidden="true" />
            <AlertTitle>Message sent</AlertTitle>
            <AlertDescription>
              <span className="break-all">
                {sendMessage.data.type} id: {sendMessage.data.messageId}
              </span>
            </AlertDescription>
          </Alert>
        ) : null}
        {sendMessage.isError ? (
          <Alert variant="error">
            <CircleAlertIcon aria-hidden="true" />
            <AlertTitle>Message not sent</AlertTitle>
            <AlertDescription>{errorMessage(sendMessage.error)}</AlertDescription>
          </Alert>
        ) : null}
      </div>
    </form>
  )
}
