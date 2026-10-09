import { z } from 'zod'
import { env } from '@/lib/env'

/** The backend's error body (RFC 9457 problem JSON). */
const problemSchema = z.object({
  title: z.string().optional(),
  detail: z.string().optional(),
})

export class ApiError extends Error {
  readonly status: number

  constructor(status: number, message: string) {
    super(message)
    this.name = 'ApiError'
    this.status = status
  }
}

/**
 * Calls the backend, throws ApiError on any non-2xx status, and validates the
 * success body with the given schema.
 */
export async function apiFetch<T>(path: string, schema: z.ZodType<T>, init?: RequestInit): Promise<T> {
  const url = env.VITE_API_URL ? new URL(path, env.VITE_API_URL) : path
  const response = await fetch(url, {
    ...init,
    headers: { 'Content-Type': 'application/json', ...init?.headers },
  })
  const body: unknown = await response.json().catch(() => undefined)

  if (!response.ok) {
    const problem = problemSchema.safeParse(body)
    const message = problem.success ? (problem.data.detail ?? problem.data.title) : undefined
    throw new ApiError(response.status, message ?? `Request failed with status ${response.status}`)
  }
  return schema.parse(body)
}
