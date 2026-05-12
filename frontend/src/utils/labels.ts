import type { ApplicationStatus } from '@/api/types'

const MAP: Record<ApplicationStatus, string> = {
  DRAFT: '草稿',
  SUBMITTED: '已提交',
  UNDER_REVIEW: '审核中',
  NEED_MORE_DOCUMENTS: '待补材料',
  ACCEPTED: '已录取',
  REJECTED: '已拒绝',
}

export function statusLabel(s: ApplicationStatus): string {
  return MAP[s] ?? s
}
