/** 与后端 ApplicationStatus 枚举一致 */
export type ApplicationStatus =
  | 'DRAFT'
  | 'SUBMITTED'
  | 'UNDER_REVIEW'
  | 'NEED_MORE_DOCUMENTS'
  | 'ACCEPTED'
  | 'REJECTED'

/** 与后端 ReviewDecision 枚举一致 */
export type ReviewDecision =
  | 'APPROVE'
  | 'REJECT'
  | 'REQUEST_MORE_DOCUMENTS'

/** 申请记录 */
export interface Application {
  id: number
  applicantId: number
  programName: string | null
  personalStatement: string | null
  documentUrl: string | null
  status: ApplicationStatus
  createdAt: string
  updatedAt: string
}

/** 审核历史记录 */
export interface ReviewRecord {
  id: number
  applicationId: number
  reviewerId: number
  decision: ReviewDecision
  comment: string | null
  reviewedAt: string
}

export interface ApplicationCreateRequest {
  applicantId: number
  programName: string
  personalStatement?: string
  documentUrl?: string
}

export interface ReviewRequestBody {
  reviewerId: number
  decision: ReviewDecision
  comment?: string
}

export const APPLICATION_STATUSES: ApplicationStatus[] = [
  'DRAFT',
  'SUBMITTED',
  'UNDER_REVIEW',
  'NEED_MORE_DOCUMENTS',
  'ACCEPTED',
  'REJECTED',
]
