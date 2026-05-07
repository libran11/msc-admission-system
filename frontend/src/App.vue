<script setup lang="ts">
import { RouterLink, RouterView } from 'vue-router'
</script>

<template>
  <div class="layout">
    <header class="top">
      <div class="brand">MSC 招生系统</div>
      <nav class="nav">
        <RouterLink to="/">首页</RouterLink>
        <RouterLink to="/admin">管理员</RouterLink>
        <RouterLink to="/review">审核员</RouterLink>
        <RouterLink to="/applicant/create">申请人·创建</RouterLink>
        <RouterLink to="/applicant/my">申请人·我的申请</RouterLink>
      </nav>
    </header>
    <main class="main">
      <RouterView />
    </main>
  </div>
</template>
