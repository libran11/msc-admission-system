<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { listApplications } from '@/api/applications'
import type { Application, ApplicationStatus } from '@/api/types'
import { ApiError } from '@/api/client'
import { statusLabel } from '@/utils/labels'
import { useReviewerId } from '@/composables/useReviewerId'

const { reviewerId, setReviewerId } = useReviewerId()

const all = ref<Application[]>([])
const loading = ref(false)
const error = ref<string | null>(null)

const queueStatuses: ApplicationStatus[] = ['SUBMITTED', 'UNDER_REVIEW']

const rows = computed(() =>
  all.value
    .filter((a) => queueStatuses.includes(a.status))
    .sort((a, b) =>
      (b.updatedAt ?? '').localeCompare(a.updatedAt ?? ''),
    ),
)

async function load() {
  loading.value = true
  error.value = null
  try {
    all.value = await listApplications()
  } catch (e) {
    all.value = []
    error.value =
      e instanceof ApiError ? e.message : '加载失败，请确认后端已启动。'
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<template>
  <div class="card">
    <h1>审核员 · 待处理队列</h1>
    <p class="muted">
      展示状态为「已提交」「审核中」的申请。请先设置你的审核员 ID（写入本地
      localStorage）：
    </p>

    <div class="field">
      <label for="rev-id">审核员 ID reviewerId</label>
      <div class="row-actions" style="margin-top: 0">
        <input
          id="rev-id"
          v-model.number="reviewerId"
          type="number"
          min="1"
          step="1"
          style="max-width: 200px"
          @change="setReviewerId(reviewerId)"
        />
        <button type="button" @click="setReviewerId(reviewerId)">保存</button>
      </div>
    </div>

    <div class="row-actions">
      <button type="button" :disabled="loading" @click="load">刷新列表</button>
    </div>

    <p v-if="loading" class="muted">加载中…</p>
    <div v-if="error" class="banner-error">{{ error }}</div>

    <p v-if="!loading && !error" class="muted">
      待处理 {{ rows.length }} 条（仅含 SUBMITTED / UNDER_REVIEW）
    </p>

    <div class="table-wrap">
      <table>
        <thead>
          <tr>
            <th>ID</th>
            <th>申请人</th>
            <th>项目</th>
            <th>状态</th>
            <th>更新</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="a in rows" :key="a.id">
            <td>{{ a.id }}</td>
            <td>{{ a.applicantId }}</td>
            <td>{{ a.programName ?? '—' }}</td>
            <td><span class="tag">{{ statusLabel(a.status) }}</span></td>
            <td>{{ a.updatedAt?.replace('T', ' ')?.slice(0, 19) }}</td>
            <td>
              <RouterLink :to="`/review/${a.id}`" class="btn btn-ghost"
                >进入审核</RouterLink
              >
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>
