import { apiFetch } from '@/lib/api'
import { sendMessageResponseSchema, type SendMessageInput } from '@/features/message/schemas'

export function sendMessage(input: SendMessageInput) {
  return apiFetch('/v1/message', sendMessageResponseSchema, {
    method: 'POST',
    body: JSON.stringify(input),
  })
}
