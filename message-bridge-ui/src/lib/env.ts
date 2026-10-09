import { z } from 'zod'

// Only VITE_ variables reach the bundle, and they are public: never put a secret here.
const envSchema = z.object({
  // Optional. When unset, the page calls its own origin (/v1/...), which the Vite dev
  // server and Vercel forward to the Render API, so the browser never needs CORS.
  VITE_API_URL: z.url().optional(),
})

export const env = envSchema.parse(import.meta.env)
