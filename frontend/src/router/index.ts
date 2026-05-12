import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: () => import('@/views/HomeView.vue'),
    },
    {
      path: '/admin',
      name: 'admin',
      component: () => import('@/views/admin/AdminListView.vue'),
    },
    {
      path: '/admin/applications/:id',
      name: 'admin-detail',
      component: () => import('@/views/admin/AdminDetailView.vue'),
      props: true,
    },
    {
      path: '/review',
      name: 'review',
      component: () => import('@/views/reviewer/ReviewerListView.vue'),
    },
    {
      path: '/review/:id',
      name: 'review-detail',
      component: () => import('@/views/reviewer/ReviewerDetailView.vue'),
      props: true,
    },
    {
      path: '/applicant/create',
      name: 'applicant-create',
      component: () => import('@/views/applicant/ApplicantCreateView.vue'),
    },
    {
      path: '/applicant/my',
      name: 'applicant-my',
      component: () => import('@/views/applicant/ApplicantMyView.vue'),
    },
  ],
})

export default router
