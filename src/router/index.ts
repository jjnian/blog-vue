import { createRouter, createWebHistory } from 'vue-router';
import Home from '../views/Home.vue';

const routes = [
  { path: '/', name: '主页', component: Home },
  { path: '/archives', name: '时间线', component: () => import('../views/Archives.vue') },
  { path: '/tags', name: '标签', component: () => import('../views/Tags.vue') },
  { path: '/categories', name: '分类', component: () => import('../views/Categories.vue') },
  { path: '/music', name: '音乐', component: () => import('../views/Music.vue') },
  { path: '/movies', name: '电影', component: () => import('../views/Movies.vue') },
  { path: '/wishes', name: '许愿树', component: () => import('../views/Wishes.vue') },
  { path: '/tools', name: '工具', component: () => import('../views/Tools.vue') },
  { path: '/link', name: '友链', component: () => import('../views/Link.vue') },
  { path: '/about', name: '关于', component: () => import('../views/About.vue') },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition;
    } else {
      return { top: 0 };
    }
  }
});

export default router;
