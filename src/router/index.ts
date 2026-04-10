import { createRouter, createWebHistory } from 'vue-router';
import Home from '../views/Home.vue';
import { getPublicMenus, getUserMenus, type MenuResponse } from '@/api/blog';

const routes = [
  { path: '/', name: '主页', component: Home },
  { path: '/archives', name: '时间线', component: () => import('../views/Archives.vue') },
  { path: '/tags', name: '标签', component: () => import('../views/Tags.vue') },
  { path: '/categories', name: '分类', component: () => import('../views/Categories.vue') },
  { path: '/music', name: '音乐', component: () => import('../views/Music.vue') },
  { path: '/movies', name: '电影', component: () => import('../views/Movies.vue') },
  { path: '/wishes', name: '许愿树', component: () => import('../views/Wishes.vue') },
  { path: '/messagewall', name: '留言墙', component: () => import('../views/NameWall.vue') },
  { path: '/friends', name: '交友', component: () => import('../views/FriendsMap.vue') },
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

const flattenMenuPaths = (menus: MenuResponse[]): string[] => {
  const paths: string[] = [];

  const walk = (items: MenuResponse[]) => {
    items.forEach((item) => {
      if (item.visible === false) return;
      if (item.path && item.path !== '#' && !item.path.startsWith('/admin')) {
        paths.push(item.path);
      }
      if (item.children?.length) {
        walk(item.children);
      }
    });
  };

  walk(menus || []);
  return Array.from(new Set(paths));
};

const isPathAllowed = (path: string, allowed: string[]) => {
  return allowed.some((allowedPath) => {
    if (allowedPath === path) return true;
    if (allowedPath === '/') return path === '/';
    return path.startsWith(`${allowedPath}/`);
  });
};

router.beforeEach(async (to, _from, next) => {
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

  if (to.path.startsWith('/admin')) {
    next();
    return;
  }

  if (to.path === '/login') {
    next();
    return;
  }

  try {
    const menus = token ? await getUserMenus() : await getPublicMenus();
    const allowedPaths = flattenMenuPaths(menus);
    const specialPaths = token ? ['/', '/profile', '/write'] : ['/'];

    if (!specialPaths.includes(to.path) && !isPathAllowed(to.path, allowedPaths)) {
      next({ path: token ? '/' : '/login', query: token ? undefined : { redirect: to.fullPath } });
      return;
    }
  } catch {
    if (token) {
      next({ path: '/login', query: { redirect: to.fullPath } });
      return;
    }

    if (!token && to.path !== '/' && to.path !== '/login') {
      next({ path: '/login', query: { redirect: to.fullPath } });
      return;
    }
  }

  next();
});

export default router;
