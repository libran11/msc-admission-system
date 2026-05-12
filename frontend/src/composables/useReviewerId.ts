import { ref } from 'vue'

const STORAGE_KEY = 'msc_reviewer_id'

function readInitial(): number {
  if (typeof localStorage === 'undefined') return 1
  const raw = localStorage.getItem(STORAGE_KEY)
  if (!raw) return 1
  const n = Number(raw)
  return Number.isFinite(n) && n > 0 ? Math.floor(n) : 1
}

/** 审核员 ID 暂存本地（后端无登录时用于接口 body） */
export function useReviewerId() {
  const reviewerId = ref(readInitial())

  function setReviewerId(v: number) {
    reviewerId.value = v
    localStorage.setItem(STORAGE_KEY, String(v))
  }

  return { reviewerId, setReviewerId }
}
