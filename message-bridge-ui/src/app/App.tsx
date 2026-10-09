import { QueryClientProvider } from '@tanstack/react-query'
import { Card, CardDescription, CardHeader, CardPanel, CardTitle } from '@/components/ui/card'
import { SendMessageForm } from '@/features/message/components/SendMessageForm'
import { queryClient } from '@/lib/query-client'

export function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <main className="flex min-h-svh items-center justify-center bg-background p-4">
        <Card className="w-full max-w-md">
          <CardHeader>
            <CardTitle>
              <h1>Message Bridge</h1>
            </CardTitle>
            <CardDescription>Send a test message through the message-bridge API.</CardDescription>
          </CardHeader>
          <CardPanel>
            <SendMessageForm />
          </CardPanel>
        </Card>
      </main>
    </QueryClientProvider>
  )
}
