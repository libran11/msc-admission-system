<script setup lang="ts">
</script>

<template>
  <div class="card">
    <h1>MSC 招生系统 · 前端</h1>
    <p class="muted">
      本项目为课程小组分工中的<strong>管理员 / 审核员</strong>界面（Vue
      3），并附带申请人端页面方便联调演示。
    </p>
    <h2>快捷入口</h2>
    <ul class="stack">
      <li><router-link to="/admin">管理员 · 全部申请与筛选</router-link></li>
      <li>
        <router-link to="/review">审核员 · 待处理与审核操作</router-link>
      </li>
      <li>
        <router-link to="/applicant/create">申请人 · 创建申请</router-link>
      </li>
      <li>
        <router-link to="/applicant/my">申请人 · 我的申请状态</router-link>
      </li>
    </ul>
    <p class="muted" style="margin-top: 1rem">
      请先启动后端（默认端口 8081）与 MySQL，再运行 <code>npm run dev</code>。
    </p>
  </div>
</template>
