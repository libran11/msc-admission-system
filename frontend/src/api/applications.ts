import { apiFetch } from './client'
import type {
  Application,
  ApplicationCreateRequest,
  ApplicationStatus,
  ReviewRecord,
  ReviewRequestBody,
} from './types'

/** 全量列表 GET /api/applications */
export function listApplications() {
  return apiFetch<Application[]>('/api/applications')
}

/** 按状态筛选 GET /api/applications/status/{status} */
export function listApplicationsByStatus(status: ApplicationStatus) {
  return apiFetch<Application[]>(`/api/applications/status/${status}`)
}

/** 单条详情 GET /api/applications/{id} */
export function getApplication(id: number) {
  return apiFetch<Application>(`/api/applications/${id}`)
}

export function getReviewHistory(applicationId: number) {
  return apiFetch<ReviewRecord[]>(
    `/api/applications/${applicationId}/reviews`,
  )
}

/** 申请人名下申请 GET /api/applications/applicant/{applicantId} */
export function listByApplicant(applicantId: number) {
  return apiFetch<Application[]>(`/api/applications/applicant/${applicantId}`)
}

export function createApplication(body: ApplicationCreateRequest) {
  return apiFetch<Application>('/api/applications', {
    method: 'POST',
    body: JSON.stringify(body),
  })
}

/** PUT /api/applications/{id}/submit */
export function submitApplication(id: number) {
  return apiFetch<Application>(`/api/applications/${id}/submit`, {
    method: 'PUT',
  })
}

/** PUT /api/applications/{id}/start-review */
export function startReview(id: number) {
  return apiFetch<Application>(`/api/applications/${id}/start-review`, {
    method: 'PUT',
  })
}

/** PUT /api/applications/{id}/review */
export function submitReview(id: number, body: ReviewRequestBody) {
  return apiFetch<Application>(`/api/applications/${id}/review`, {
    method: 'PUT',
    body: JSON.stringify(body),
  })
}

/** PUT /api/applications/{id}/documents */
export function updateDocuments(id: number, documentUrl: string) {
  return apiFetch<Application>(`/api/applications/${id}/documents`, {
    method: 'PUT',
    body: JSON.stringify({ documentUrl }),
  })
}
