import { useMutation } from '@tanstack/react-query'
import { sendMessage } from '@/features/message/api'

export function useSendMessage() {
  return useMutation({ mutationFn: sendMessage })
}
