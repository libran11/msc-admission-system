/**
 * 统一 HTTP 封装：开发环境走 Vite 代理（空 base），生产可读 VITE_API_BASE。
 */
function apiBase(): string {
  const b = import.meta.env.VITE_API_BASE
  if (typeof b === 'string' && b.trim() !== '') {
    return b.replace(/\/$/, '')
  }
  return ''
}

export class ApiError extends Error {
  constructor(
    message: string,
    public status: number,
  ) {
    super(message)
    this.name = 'ApiError'
  }
}

async function parseErrorBody(res: Response): Promise<string> {
  try {
    const data = (await res.json()) as { message?: string }
    if (data?.message) return data.message
  } catch {
    /* ignore */
  }
  return res.statusText || `HTTP ${res.status}`
}

export async function apiFetch<T>(
  path: string,
  init?: RequestInit,
): Promise<T> {
  const url = `${apiBase()}${path.startsWith('/') ? path : `/${path}`}`
  const res = await fetch(url, {
    ...init,
    headers: {
      ...(init?.body ? { 'Content-Type': 'application/json' } : {}),
      ...(init?.headers ?? {}),
    },
  })
  if (res.status === 204 || res.headers.get('content-length') === '0') {
    return undefined as T
  }
  if (!res.ok) {
    const msg = await parseErrorBody(res)
    throw new ApiError(msg, res.status)
  }
  const text = await res.text()
  if (!text) return undefined as T
  return JSON.parse(text) as T
}
