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
  { path: '/messagewall', name: '留言墙', component: () => import('../views/NameWall.vue') },
  { path: '/friends', name: 'Friends', component: () => import('../views/FriendsMap.vue') },
  { path: '/tools', name: '工具', component: () => import('../views/Tools.vue') },
  { path: '/link', name: '友链', component: () => import('../views/Link.vue') },
  { path: '/about', name: '关于', component: () => import('../views/About.vue') },
  { path: '/login', name: '登录', component: () => import('../views/Login.vue') },
  {
    path: '/profile',
    name: '我',
    component: () => import('../views/Profile.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/write',
    name: '写文章',
    component: () => import('../views/Write.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/admin',
    component: () => import('../views/admin/AdminLayout.vue'),
    meta: { requiresAuth: true, requiresAdmin: true },
    children: [
      { path: '', name: '管理-概览', component: () => import('../views/admin/AdminDashboard.vue') },
      { path: 'users', name: '管理-用户', component: () => import('../views/admin/AdminUsers.vue') },
      { path: 'articles', name: '管理-文章', component: () => import('../views/admin/AdminArticles.vue') },
      { path: 'categories', name: '管理-分类', component: () => import('../views/admin/AdminCategories.vue') },
      { path: 'tags', name: '管理-标签', component: () => import('../views/admin/AdminTags.vue') },
      { path: 'comments', name: '管理-评论', component: () => import('../views/admin/AdminComments.vue') },
      { path: 'roles', name: '管理-角色', component: () => import('../views/admin/AdminRoles.vue') },
    ],
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition;
    }
    return { top: 0 };
  }
});

router.beforeEach((to, _from, next) => {
  const token = localStorage.getItem('token');
  const userStr = localStorage.getItem('user');
  let isAdmin = false;

  if (userStr) {
    try {
      const user = JSON.parse(userStr);
      isAdmin = user.roles?.some((r: string) => r === 'ADMIN' || r === 'SUPER_ADMIN') ?? false;
    } catch {
      // ignore
    }
  }

  if (to.meta.requiresAuth && !token) {
    next({ path: '/login', query: { redirect: to.fullPath } });
    return;
  }

  if (to.meta.requiresAdmin && !isAdmin) {
    next({ path: '/' });
    return;
  }

  next();
});

export default router;
