<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import {
  listApplications,
  listApplicationsByStatus,
} from '@/api/applications'
import type { Application, ApplicationStatus } from '@/api/types'
import { APPLICATION_STATUSES } from '@/api/types'
import { ApiError } from '@/api/client'
import { statusLabel } from '@/utils/labels'

const rows = ref<Application[]>([])
const loading = ref(false)
const error = ref<string | null>(null)

/** 空字符串表示「全部」 */
const filterStatus = ref<ApplicationStatus | ''>('')

const titleFilter = computed(() =>
  filterStatus.value ? statusLabel(filterStatus.value) : '全部',
)

async function load() {
  loading.value = true
  error.value = null
  try {
    const data = filterStatus.value
      ? await listApplicationsByStatus(filterStatus.value)
      : await listApplications()
    rows.value = [...data].sort((a, b) =>
      (b.updatedAt ?? '').localeCompare(a.updatedAt ?? ''),
    )
  } catch (e) {
    rows.value = []
    error.value =
      e instanceof ApiError ? e.message : '加载失败，请确认后端已启动。'
  } finally {
    loading.value = false
  }
}

onMounted(load)

const filteredLabel = APPLICATION_STATUSES.map((s) => ({
  value: s,
  label: statusLabel(s),
}))
</script>

<template>
  <div class="card">
    <h1>管理员 · 申请列表</h1>
    <p class="muted">查看系统中全部申请；可按状态筛选。数据来源：GET /api/applications</p>

    <div class="row-actions">
      <label class="muted" style="display: flex; align-items: center; gap: 0.5rem">
        筛选状态
        <select v-model="filterStatus" @change="load">
          <option value="">全部</option>
          <option
            v-for="opt in filteredLabel"
            :key="opt.value"
            :value="opt.value"
          >
            {{ opt.label }}（{{ opt.value }}）
          </option>
        </select>
      </label>
      <button type="button" :disabled="loading" @click="load">刷新</button>
    </div>

    <p v-if="loading" class="muted">加载中…</p>
    <div v-if="error" class="banner-error">{{ error }}</div>

    <p v-if="!loading && !error" class="muted">
      当前：{{ titleFilter }} · 共 {{ rows.length }} 条
    </p>

    <div class="table-wrap">
      <table>
        <thead>
          <tr>
            <th>ID</th>
            <th>申请人 ID</th>
            <th>项目</th>
            <th>状态</th>
            <th>更新时间</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="a in rows" :key="a.id">
            <td>{{ a.id }}</td>
            <td>{{ a.applicantId }}</td>
            <td>{{ a.programName ?? '—' }}</td>
            <td>
              <span class="tag">{{ statusLabel(a.status) }}</span>
            </td>
            <td>{{ a.updatedAt?.replace('T', ' ')?.slice(0, 19) ?? '—' }}</td>
            <td>
              <RouterLink class="btn btn-ghost" :to="`/admin/applications/${a.id}`">
                详情
              </RouterLink>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>
