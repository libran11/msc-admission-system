<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { createApplication } from '@/api/applications'
import { ApiError } from '@/api/client'

const router = useRouter()

const applicantId = ref<number>(1)
const programName = ref('MSc Computer Science')
const personalStatement = ref('')
const documentUrl = ref('demo.pdf')

const loading = ref(false)
const error = ref<string | null>(null)
const createdId = ref<number | null>(null)

async function onSubmit() {
  loading.value = true
  error.value = null
  createdId.value = null
  try {
    const app = await createApplication({
      applicantId: applicantId.value,
      programName: programName.value,
      personalStatement: personalStatement.value || undefined,
      documentUrl: documentUrl.value || undefined,
    })
    createdId.value = app.id
    localStorage.setItem('msc_applicant_id', String(applicantId.value))
  } catch (e) {
    error.value =
      e instanceof ApiError ? e.message : '创建失败，请确认后端已启动。'
  } finally {
    loading.value = false
  }
}

function goMy() {
  router.push({ name: 'applicant-my' })
}

onMounted(() => {
  if (typeof localStorage !== 'undefined') {
    const r = localStorage.getItem('msc_applicant_id')
    if (r) {
      const n = Number(r)
      if (Number.isFinite(n)) applicantId.value = n
    }
  }
})
</script>

<template>
  <div class="card">
    <h1>申请人 · 创建申请</h1>
    <p class="muted">POST /api/applications，创建后为 DRAFT 草稿状态。</p>

    <div v-if="error" class="banner-error">{{ error }}</div>
    <p v-if="createdId !== null" class="muted">
      创建成功，申请 ID = {{ createdId }}。请到「我的申请」中提交。
      <button type="button" class="btn-ghost" @click="goMy">去我的申请</button>
    </p>

    <form class="stack" style="margin-top: 0.75rem" @submit.prevent="onSubmit">
      <div class="field">
        <label for="aid">申请人 ID applicantId</label>
        <input id="aid" v-model.number="applicantId" type="number" min="1" required />
      </div>
      <div class="field">
        <label for="pn">项目名称 programName</label>
        <input id="pn" v-model="programName" type="text" required />
      </div>
      <div class="field">
        <label for="ps">个人陈述 personalStatement（可选）</label>
        <textarea id="ps" v-model="personalStatement" />
      </div>
      <div class="field">
        <label for="du">材料 documentUrl（占位 URL，需非空才能提交）</label>
        <input id="du" v-model="documentUrl" type="text" />
      </div>
      <button type="submit" :disabled="loading">
        {{ loading ? '提交中…' : '创建草稿' }}
      </button>
    </form>
  </div>
</template>
