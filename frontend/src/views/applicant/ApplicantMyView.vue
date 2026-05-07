<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { listByApplicant, submitApplication, updateDocuments } from '@/api/applications'
import type { Application } from '@/api/types'
import { ApiError } from '@/api/client'
import { statusLabel } from '@/utils/labels'

const STORAGE_KEY = 'msc_applicant_id'

const applicantId = ref(1)
const apps = ref<Application[]>([])
const docEdits = reactive<Record<number, string>>({})

const loading = ref(false)
const error = ref<string | null>(null)
const busyId = ref<number | null>(null)

function rememberId() {
  localStorage.setItem(STORAGE_KEY, String(applicantId.value))
}

async function load() {
  loading.value = true
  error.value = null
  rememberId()
  try {
    const list = await listByApplicant(applicantId.value)
    apps.value = [...list].sort((a, b) =>
      (b.updatedAt ?? '').localeCompare(a.updatedAt ?? ''),
    )
    const seen = new Set<number>()
    for (const a of apps.value) {
      seen.add(a.id)
      if (docEdits[a.id] === undefined)
        docEdits[a.id] = a.documentUrl ?? ''
    }
    for (const key of Object.keys(docEdits)) {
      const id = Number(key)
      if (!seen.has(id)) delete docEdits[id]
    }
  } catch (e) {
    apps.value = []
    error.value =
      e instanceof ApiError ? e.message : '加载失败，请确认后端已启动。'
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  const raw = localStorage.getItem(STORAGE_KEY)
  if (raw) {
    const n = Number(raw)
    if (Number.isFinite(n) && n > 0) applicantId.value = n
  }
  load()
})

async function onSubmitApp(id: number) {
  busyId.value = id
  error.value = null
  try {
    await submitApplication(id)
    await load()
  } catch (e) {
    error.value =
      e instanceof ApiError ? e.message : '提交失败（检查项目名与材料 URL）。'
  } finally {
    busyId.value = null
  }
}

async function onUpdateDoc(id: number) {
  const url = (docEdits[id] ?? '').trim()
  if (!url) {
    error.value = '请先填写新的材料 URL'
    return
  }
  busyId.value = id
  error.value = null
  try {
    await updateDocuments(id, url)
    await load()
  } catch (e) {
    error.value = e instanceof ApiError ? e.message : '更新材料失败。'
  } finally {
    busyId.value = null
  }
}

function canSubmit(a: Application) {
  return a.status === 'DRAFT' || a.status === 'NEED_MORE_DOCUMENTS'
}

function needDocUi(a: Application) {
  return a.status === 'NEED_MORE_DOCUMENTS' || a.status === 'DRAFT'
}
</script>

<template>
  <div class="card">
    <h1>申请人 · 我的申请</h1>
    <p class="muted">
      GET /api/applications/applicant/{applicantId}；提交与补材料使用 PUT。
    </p>

    <div class="field">
      <label for="aid">申请人 ID</label>
      <div class="row-actions" style="margin-top: 0">
        <input
          id="aid"
          v-model.number="applicantId"
          type="number"
          min="1"
          style="max-width: 220px"
        />
        <button type="button" :disabled="loading" @click="load">查询</button>
      </div>
    </div>

    <p v-if="loading" class="muted">加载中…</p>
    <div v-if="error" class="banner-error">{{ error }}</div>

    <div class="table-wrap">
      <table>
        <thead>
          <tr>
            <th>ID</th>
            <th>项目</th>
            <th>状态</th>
            <th>材料 URL</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="a in apps" :key="a.id">
            <td>{{ a.id }}</td>
            <td>{{ a.programName ?? '—' }}</td>
            <td>
              <span class="tag">{{ statusLabel(a.status) }}</span>
            </td>
            <td>{{ a.documentUrl ?? '—' }}</td>
            <td>
              <div class="stack" style="gap: 0.35rem">
                <template v-if="needDocUi(a)">
                  <input
                    v-model="docEdits[a.id]"
                    type="text"
                    placeholder="新材料 URL"
                    style="max-width: 220px"
                  />
                  <button
                    type="button"
                    class="btn-ghost"
                    :disabled="busyId === a.id"
                    @click="onUpdateDoc(a.id)"
                  >
                    更新材料 PUT …/documents
                  </button>
                </template>
                <button
                  v-if="canSubmit(a)"
                  type="button"
                  :disabled="busyId === a.id"
                  @click="onSubmitApp(a.id)"
                >
                  提交申请 PUT …/submit
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>
