import { z } from 'zod'

export const messageTypes = ['SMS', 'WHATSAPP', 'EMAIL'] as const
export const messageTypeSchema = z.enum(messageTypes)
export type MessageType = z.infer<typeof messageTypeSchema>

/** E.164: a plus sign, country code and number, up to 15 digits. Same rule as the backend. */
const E164 = /^\+[1-9]\d{6,14}$/

export const sendMessageSchema = z.object({
  type: messageTypeSchema,
  to: z
    .string()
    .trim()
    .min(1, 'Enter a phone number')
    .regex(E164, 'Use international format: + then country code and number, e.g. +14155550100'),
  message: z.string().trim().min(1, 'Enter a message').max(1600, 'Keep the message under 1600 characters'),
})
export type SendMessageInput = z.infer<typeof sendMessageSchema>

export const sendMessageResponseSchema = z.object({
  messageId: z.string(),
  type: messageTypeSchema,
})
export type SendMessageResponse = z.infer<typeof sendMessageResponseSchema>
