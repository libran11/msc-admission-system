<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import {
  getApplication,
  getReviewHistory,
  startReview,
  submitReview,
} from '@/api/applications'
import type { Application, ReviewDecision, ReviewRecord } from '@/api/types'
import { ApiError } from '@/api/client'
import { statusLabel } from '@/utils/labels'
import { useReviewerId } from '@/composables/useReviewerId'

const route = useRoute()
const { reviewerId, setReviewerId } = useReviewerId()

const id = computed(() => Number(route.params.id))

const app = ref<Application | null>(null)
const reviews = ref<ReviewRecord[]>([])
const loading = ref(true)
const acting = ref(false)
const error = ref<string | null>(null)
const comment = ref('')

async function load() {
  if (!Number.isFinite(id.value)) {
    error.value = '无效的申请 ID'
    loading.value = false
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

const canStartReview = computed(
  () => app.value?.status === 'SUBMITTED' && !acting.value,
)

const canDecision = computed(
  () => app.value?.status === 'UNDER_REVIEW' && !acting.value,
)

async function onStartReview() {
  if (!app.value) return
  acting.value = true
  error.value = null
  try {
    app.value = await startReview(app.value.id)
    await load()
  } catch (e) {
    error.value =
      e instanceof ApiError ? e.message : '操作失败（可能状态已变化）。'
  } finally {
    acting.value = false
  }
}

async function onDecision(decision: ReviewDecision) {
  if (!app.value) return
  acting.value = true
  error.value = null
  try {
    app.value = await submitReview(app.value.id, {
      reviewerId: reviewerId.value,
      decision,
      comment: comment.value.trim() || undefined,
    })
    comment.value = ''
    await load()
  } catch (e) {
    error.value =
      e instanceof ApiError ? e.message : '操作失败（可能状态已变化）。'
  } finally {
    acting.value = false
  }
}
</script>

<template>
  <div class="card">
    <p>
      <RouterLink to="/review">← 返回队列</RouterLink>
    </p>
    <h1>审核 · 申请 #{{ id }}</h1>

    <div class="field">
      <label for="rev">审核员 ID（PUT /review 请求体需要）</label>
      <input
        id="rev"
        v-model.number="reviewerId"
        type="number"
        min="1"
        style="max-width: 240px"
        @change="setReviewerId(reviewerId)"
      />
    </div>

    <p v-if="loading" class="muted">加载中…</p>
    <div v-if="error" class="banner-error">{{ error }}</div>

    <template v-if="!loading && app">
      <p>
        当前状态：<span class="tag">{{ statusLabel(app.status) }}</span>
      </p>
      <div class="stack" style="margin: 0.75rem 0">
        <p><strong>申请人 ID</strong>：{{ app.applicantId }}</p>
        <p><strong>项目</strong>：{{ app.programName ?? '—' }}</p>
        <p>
          <strong>文书</strong>：<span class="muted">{{
            app.personalStatement ?? '—'
          }}</span>
        </p>
        <p>
          <strong>材料</strong>：
          <a v-if="app.documentUrl" :href="app.documentUrl" target="_blank">{{
            app.documentUrl
          }}</a>
          <span v-else class="muted">—</span>
        </p>
      </div>

      <div v-if="app.status === 'SUBMITTED'" class="row-actions">
        <button
          type="button"
          :disabled="!canStartReview"
          @click="onStartReview"
        >
          开始审核（PUT …/start-review）
        </button>
        <span class="muted">仅 SUBMITTED 可点，将进入 UNDER_REVIEW</span>
      </div>

      <template v-if="app.status === 'UNDER_REVIEW'">
        <div class="field">
          <label for="cmt">审核备注 comment（可选）</label>
          <textarea id="cmt" v-model="comment" placeholder="可选" />
        </div>
        <div class="row-actions">
          <button
            type="button"
            class="btn-success"
            :disabled="!canDecision"
            @click="onDecision('APPROVE')"
          >
            录取 APPROVE
          </button>
          <button
            type="button"
            class="btn-danger"
            :disabled="!canDecision"
            @click="onDecision('REJECT')"
          >
            拒绝 REJECT
          </button>
          <button
            type="button"
            class="btn-warn"
            :disabled="!canDecision"
            @click="onDecision('REQUEST_MORE_DOCUMENTS')"
          >
            要求补材料
          </button>
        </div>
        <p class="muted">仅 UNDER_REVIEW 可提交终审。</p>
      </template>

      <p
        v-if="app.status !== 'SUBMITTED' && app.status !== 'UNDER_REVIEW'"
        class="muted"
      >
        当前状态不需要审核员在此页操作；请从队列选择处于「已提交 / 审核中」的申请。
      </p>

      <h2>审核历史</h2>
      <p v-if="reviews.length === 0" class="muted">暂无记录</p>
      <div v-else class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>时间</th>
              <th>审核员</th>
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
