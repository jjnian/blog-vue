<script setup lang="ts">
import { ref, computed } from 'vue';
import { useRouter, useRoute, RouterView } from 'vue-router';
import {
  LayoutDashboard, Users, FileText, Tag as TagIcon, LayoutGrid,
  MessageSquare, Shield, LogOut, Menu, X, ChevronRight, Settings
} from 'lucide-vue-next';
import { useAuth } from '@/stores/auth';

const router = useRouter();
const route = useRoute();
const auth = useAuth();

const sidebarOpen = ref(true);

const menuItems = [
  { path: '/admin', label: '数据概览', icon: LayoutDashboard, exact: true },
  { path: '/admin/users', label: '用户管理', icon: Users },
  { path: '/admin/articles', label: '文章管理', icon: FileText },
  { path: '/admin/categories', label: '分类管理', icon: LayoutGrid },
  { path: '/admin/tags', label: '标签管理', icon: TagIcon },
  { path: '/admin/comments', label: '评论管理', icon: MessageSquare },
  { path: '/admin/roles', label: '角色管理', icon: Shield },
];

const isActive = (item: typeof menuItems[0]) => {
  if (item.exact) return route.path === item.path;
  return route.path.startsWith(item.path);
};

const handleLogout = () => {
  auth.clearAuth();
  router.push('/login');
};

const pageTitle = computed(() => {
  const match = menuItems.find((item) =>
    item.exact ? route.path === item.path : route.path.startsWith(item.path)
  );
  return match?.label || '管理后台';
});
</script>

<template>
  <div class="flex min-h-screen bg-gradient-to-br from-slate-50 to-blue-50/30">
    <!-- Sidebar -->
    <aside
      class="fixed left-0 top-0 h-full z-50 transition-all duration-300 flex flex-col"
      :class="sidebarOpen ? 'w-60' : 'w-16'"
    >
      <div class="h-full bg-white/90 backdrop-blur-xl border-r border-gray-100 shadow-xl shadow-black/5 flex flex-col">
        <!-- Logo -->
        <div class="flex items-center gap-3 px-4 py-5 border-b border-gray-100">
          <div class="w-8 h-8 rounded-xl bg-gradient-to-br from-[#49b1f5] to-[#3a9de8] flex items-center justify-center flex-shrink-0 shadow-md shadow-[#49b1f5]/30">
            <Settings :size="16" class="text-white" />
          </div>
          <span v-if="sidebarOpen" class="text-sm font-bold text-[#2c3e50] truncate">管理后台</span>
          <button
            @click="sidebarOpen = !sidebarOpen"
            class="ml-auto p-1 rounded-lg hover:bg-gray-100 text-gray-300 hover:text-[#49b1f5] transition-all flex-shrink-0"
          >
            <component :is="sidebarOpen ? X : Menu" :size="16" />
          </button>
        </div>

        <!-- User info -->
        <div v-if="sidebarOpen" class="px-4 py-3 border-b border-gray-50">
          <div class="flex items-center gap-3">
            <div class="w-8 h-8 rounded-full bg-gradient-to-br from-[#49b1f5]/20 to-purple-400/20 flex items-center justify-center flex-shrink-0">
              <span class="text-xs font-bold text-[#49b1f5]">
                {{ (auth.user.value?.nickname || auth.user.value?.username || 'A').charAt(0).toUpperCase() }}
              </span>
            </div>
            <div class="min-w-0">
              <p class="text-xs font-semibold text-[#2c3e50] truncate">{{ auth.user.value?.nickname || auth.user.value?.username }}</p>
              <p class="text-[10px] text-gray-300 truncate">{{ auth.user.value?.roles?.join(', ') }}</p>
            </div>
          </div>
        </div>

        <!-- Navigation -->
        <nav class="flex-1 px-3 py-4 space-y-1 overflow-y-auto">
          <router-link
            v-for="item in menuItems"
            :key="item.path"
            :to="item.path"
            class="flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm transition-all duration-200 group"
            :class="isActive(item)
              ? 'bg-gradient-to-r from-[#49b1f5]/15 to-[#49b1f5]/5 text-[#49b1f5] font-semibold border border-[#49b1f5]/20'
              : 'text-gray-500 hover:bg-gray-50 hover:text-[#2c3e50]'"
          >
            <component :is="item.icon" :size="17" class="flex-shrink-0" :stroke-width="isActive(item) ? 2 : 1.5" />
            <span v-if="sidebarOpen" class="truncate">{{ item.label }}</span>
            <ChevronRight v-if="sidebarOpen && isActive(item)" :size="14" class="ml-auto opacity-50" />
          </router-link>
        </nav>

        <!-- Footer -->
        <div class="px-3 py-3 border-t border-gray-100">
          <router-link
            to="/"
            class="flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm text-gray-400 hover:bg-gray-50 hover:text-[#2c3e50] transition-all mb-1"
          >
            <ChevronRight :size="17" class="rotate-180 flex-shrink-0" stroke-width="1.5" />
            <span v-if="sidebarOpen">返回博客</span>
          </router-link>
          <button
            @click="handleLogout"
            class="w-full flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm text-red-400 hover:bg-red-50 transition-all"
          >
            <LogOut :size="17" class="flex-shrink-0" stroke-width="1.5" />
            <span v-if="sidebarOpen">退出登录</span>
          </button>
        </div>
      </div>
    </aside>

    <!-- Main content -->
    <main class="flex-1 flex flex-col min-h-screen transition-all duration-300" :class="sidebarOpen ? 'ml-60' : 'ml-16'">
      <!-- Top bar -->
      <header class="sticky top-0 z-40 bg-white/80 backdrop-blur-xl border-b border-gray-100 px-6 py-3.5 flex items-center gap-4">
        <h1 class="text-base font-bold text-[#2c3e50]">{{ pageTitle }}</h1>
        <div class="ml-auto flex items-center gap-3">
          <div class="flex items-center gap-2 text-xs text-gray-400">
            <span class="w-2 h-2 rounded-full bg-green-400 animate-pulse"></span>
            系统运行正常
          </div>
        </div>
      </header>

      <!-- Page content -->
      <div class="flex-1 p-6">
        <RouterView />
      </div>
    </main>
  </div>
</template>
