<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import { getApplication, getReviewHistory } from '@/api/applications'
import type { Application, ReviewRecord } from '@/api/types'
import { ApiError } from '@/api/client'
import { statusLabel } from '@/utils/labels'

const route = useRoute()

const id = computed(() => Number(route.params.id))

const app = ref<Application | null>(null)
const reviews = ref<ReviewRecord[]>([])
const loading = ref(false)
const error = ref<string | null>(null)

async function load() {
  if (!Number.isFinite(id.value)) {
    error.value = '无效的申请 ID'
    return
  }
  loading.value = true
  error.value = null
  try {
    const [detail, history] = await Promise.all([
      getApplication(id.value),
      getReviewHistory(id.value),
    ])
    app.value = detail
    reviews.value = [...history].sort((a, b) =>
      (b.reviewedAt ?? '').localeCompare(a.reviewedAt ?? ''),
    )
  } catch (e) {
    app.value = null
    reviews.value = []
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
    <p>
      <RouterLink to="/admin">← 返回列表</RouterLink>
    </p>
    <h1>管理员 · 申请详情 #{{ id }}</h1>

    <p v-if="loading" class="muted">加载中…</p>
    <div v-if="error" class="banner-error">{{ error }}</div>

    <template v-if="!loading && app">
      <div class="stack" style="margin-top: 0.75rem">
        <p><strong>状态</strong>：{{ statusLabel(app.status) }}</p>
        <p><strong>申请人 ID</strong>：{{ app.applicantId }}</p>
        <p><strong>项目</strong>：{{ app.programName ?? '—' }}</p>
        <p>
          <strong>文书 / 说明</strong>：
          <span class="muted">{{ app.personalStatement ?? '—' }}</span>
        </p>
        <p>
          <strong>材料链接 documentUrl</strong>：
          <template v-if="app.documentUrl">
            <a :href="app.documentUrl" target="_blank" rel="noopener">{{
              app.documentUrl
            }}</a>
          </template>
          <template v-else>—</template>
        </p>
        <p class="muted">
          创建 {{ app.createdAt?.replace('T', ' ')?.slice(0, 19) }} · 更新
          {{ app.updatedAt?.replace('T', ' ')?.slice(0, 19) }}
        </p>
      </div>

      <h2>审核历史</h2>
      <p v-if="reviews.length === 0" class="muted">暂无审核记录</p>
      <div v-else class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>时间</th>
              <th>审核员 ID</th>
              <th>决定</th>
              <th>备注</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="r in reviews" :key="r.id">
              <td>{{ r.reviewedAt?.replace('T', ' ')?.slice(0, 19) }}</td>
              <td>{{ r.reviewerId }}</td>
              <td>{{ r.decision }}</td>
              <td>{{ r.comment ?? '—' }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </template>
  </div>
</template>
