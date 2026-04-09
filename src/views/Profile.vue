<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import {
  User, Mail, Shield, Calendar, Clock, LogOut, PenLine,
  Settings, ChevronRight, FileText, Star
} from 'lucide-vue-next';
import { useAuth } from '@/stores/auth';
import { getCurrentUser } from '@/api/auth';

const router = useRouter();
const auth = useAuth();

const userInfo = ref(auth.user.value);
const loading = ref(false);

onMounted(async () => {
  if (!auth.isLoggedIn.value) {
    router.push('/login');
    return;
  }
  // 刷新用户信息
  loading.value = true;
  try {
    const data = await getCurrentUser();
    userInfo.value = {
      id: data.id,
      username: data.username,
      nickname: data.nickname,
      avatar: data.avatar,
      email: data.email,
      roles: (data as any).roles || auth.user.value?.roles || [],
    };
  } catch {
    // fallback to stored info
  } finally {
    loading.value = false;
  }
});

const handleLogout = () => {
  auth.clearAuth();
  router.push('/');
};

const formatDate = (v?: string) => {
  if (!v) return '-';
  return new Date(v).toLocaleDateString('zh-CN');
};

const roleLabel = (r: string) => {
  const map: Record<string, string> = {
    SUPER_ADMIN: '超级管理员',
    ADMIN: '管理员',
    EDITOR: '编辑',
    USER: '普通用户',
    GUEST: '访客',
  };
  return map[r] || r;
};

const roleBadgeClass = (r: string) => {
  if (r.includes('SUPER')) return 'bg-purple-100 text-purple-600 border-purple-200';
  if (r.includes('ADMIN')) return 'bg-blue-100 text-blue-600 border-blue-200';
  if (r === 'EDITOR') return 'bg-green-100 text-green-600 border-green-200';
  return 'bg-gray-100 text-gray-500 border-gray-200';
};
</script>

<template>
  <div class="min-h-screen pb-20 pt-4">
    <div class="max-w-2xl mx-auto px-4 space-y-5">

      <!-- Profile Card -->
      <div class="bg-white/80 backdrop-blur-xl rounded-3xl border border-white/80 shadow-[0_8px_30px_rgba(73,177,245,0.1)] overflow-hidden">
        <!-- Banner -->
        <div class="h-28 bg-gradient-to-r from-[#49b1f5]/30 via-blue-100/50 to-purple-200/30 relative">
          <div class="absolute inset-0 opacity-30" style="background-image: url('data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iNDAiIGhlaWdodD0iNDAiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+PGNpcmNsZSBjeD0iMiIgY3k9IjIiIHI9IjIiIGZpbGw9IiM0OWIxZjUiIGZpbGwtb3BhY2l0eT0iMC4xIi8+PC9zdmc+')"></div>
        </div>

        <div class="px-8 pb-8 -mt-14 relative">
          <!-- Avatar -->
          <div class="flex items-end justify-between mb-5">
            <div class="w-24 h-24 rounded-2xl border-4 border-white shadow-xl overflow-hidden bg-gradient-to-br from-[#49b1f5] to-purple-500 flex items-center justify-center">
              <img
                v-if="userInfo?.avatar"
                :src="userInfo.avatar"
                :alt="userInfo?.nickname"
                class="w-full h-full object-cover"
              />
              <span v-else class="text-3xl font-bold text-white">
                {{ (userInfo?.nickname || userInfo?.username || 'U').charAt(0).toUpperCase() }}
              </span>
            </div>

            <!-- Actions -->
            <div class="flex items-center gap-2 mt-14">
              <router-link
                to="/write"
                class="flex items-center gap-1.5 px-4 py-2 rounded-xl bg-[#49b1f5] text-white text-sm font-semibold shadow-md shadow-[#49b1f5]/30 hover:bg-[#3a9de8] hover:-translate-y-0.5 transition-all"
              >
                <PenLine :size="14" stroke-width="2" /> 写文章
              </router-link>
              <router-link
                v-if="auth.isAdmin.value"
                to="/admin"
                class="flex items-center gap-1.5 px-4 py-2 rounded-xl border border-gray-200 text-gray-500 text-sm font-medium hover:border-[#49b1f5]/40 hover:text-[#49b1f5] transition-all"
              >
                <Settings :size="14" /> 后台
              </router-link>
            </div>
          </div>

          <!-- Name & roles -->
          <div v-if="loading" class="animate-pulse space-y-2 mb-4">
            <div class="h-6 w-40 bg-gray-100 rounded"></div>
            <div class="h-4 w-24 bg-gray-100 rounded"></div>
          </div>
          <div v-else>
            <h1 class="text-2xl font-bold text-[#2c3e50]">
              {{ userInfo?.nickname || userInfo?.username }}
            </h1>
            <p class="text-sm text-gray-400 mt-0.5">@{{ userInfo?.username }}</p>

            <!-- Roles -->
            <div class="flex flex-wrap gap-2 mt-3">
              <span
                v-for="role in userInfo?.roles"
                :key="role"
                class="inline-flex items-center gap-1 px-2.5 py-1 rounded-lg border text-xs font-semibold"
                :class="roleBadgeClass(role)"
              >
                <Shield :size="10" />
                {{ roleLabel(role) }}
              </span>
            </div>
          </div>
        </div>
      </div>

      <!-- Info Card -->
      <div class="bg-white/80 backdrop-blur-xl rounded-2xl border border-white/80 shadow-sm p-6 space-y-4">
        <h2 class="text-xs font-bold tracking-widest uppercase text-gray-400">账号信息</h2>

        <div class="space-y-3">
          <div class="flex items-center gap-4 py-2.5 border-b border-gray-50">
            <div class="w-8 h-8 rounded-xl bg-blue-50 flex items-center justify-center flex-shrink-0">
              <User :size="15" class="text-blue-400" />
            </div>
            <div class="flex-1">
              <p class="text-xs text-gray-400">用户名</p>
              <p class="text-sm font-medium text-[#2c3e50]">{{ userInfo?.username || '-' }}</p>
            </div>
          </div>

          <div class="flex items-center gap-4 py-2.5 border-b border-gray-50">
            <div class="w-8 h-8 rounded-xl bg-purple-50 flex items-center justify-center flex-shrink-0">
              <User :size="15" class="text-purple-400" />
            </div>
            <div class="flex-1">
              <p class="text-xs text-gray-400">昵称</p>
              <p class="text-sm font-medium text-[#2c3e50]">{{ userInfo?.nickname || '-' }}</p>
            </div>
          </div>

          <div class="flex items-center gap-4 py-2.5">
            <div class="w-8 h-8 rounded-xl bg-green-50 flex items-center justify-center flex-shrink-0">
              <Mail :size="15" class="text-green-400" />
            </div>
            <div class="flex-1">
              <p class="text-xs text-gray-400">邮箱</p>
              <p class="text-sm font-medium text-[#2c3e50]">{{ userInfo?.email || '未绑定' }}</p>
            </div>
          </div>
        </div>
      </div>

      <!-- Quick Links -->
      <div class="bg-white/80 backdrop-blur-xl rounded-2xl border border-white/80 shadow-sm overflow-hidden">
        <h2 class="text-xs font-bold tracking-widest uppercase text-gray-400 px-6 pt-5 pb-3">快捷入口</h2>

        <router-link
          to="/write"
          class="flex items-center gap-4 px-6 py-3.5 hover:bg-gray-50/80 transition-colors border-b border-gray-50 group"
        >
          <div class="w-8 h-8 rounded-xl bg-[#49b1f5]/10 flex items-center justify-center flex-shrink-0">
            <PenLine :size="15" class="text-[#49b1f5]" />
          </div>
          <span class="flex-1 text-sm font-medium text-[#2c3e50]">写新文章</span>
          <ChevronRight :size="15" class="text-gray-300 group-hover:text-[#49b1f5] group-hover:translate-x-1 transition-all" />
        </router-link>

        <router-link
          v-if="auth.isAdmin.value"
          to="/admin"
          class="flex items-center gap-4 px-6 py-3.5 hover:bg-gray-50/80 transition-colors border-b border-gray-50 group"
        >
          <div class="w-8 h-8 rounded-xl bg-purple-50 flex items-center justify-center flex-shrink-0">
            <Settings :size="15" class="text-purple-400" />
          </div>
          <span class="flex-1 text-sm font-medium text-[#2c3e50]">管理后台</span>
          <ChevronRight :size="15" class="text-gray-300 group-hover:text-[#49b1f5] group-hover:translate-x-1 transition-all" />
        </router-link>

        <router-link
          to="/"
          class="flex items-center gap-4 px-6 py-3.5 hover:bg-gray-50/80 transition-colors group"
        >
          <div class="w-8 h-8 rounded-xl bg-orange-50 flex items-center justify-center flex-shrink-0">
            <FileText :size="15" class="text-orange-400" />
          </div>
          <span class="flex-1 text-sm font-medium text-[#2c3e50]">浏览文章</span>
          <ChevronRight :size="15" class="text-gray-300 group-hover:text-[#49b1f5] group-hover:translate-x-1 transition-all" />
        </router-link>
      </div>

      <!-- Logout -->
      <button
        @click="handleLogout"
        class="w-full flex items-center justify-center gap-3 py-3.5 rounded-2xl border-2 border-red-100 bg-red-50/50 text-red-400 text-sm font-semibold hover:bg-red-50 hover:border-red-200 transition-all duration-300 hover:-translate-y-0.5 group"
      >
        <LogOut :size="16" class="group-hover:rotate-12 transition-transform" />
        退出登录
      </button>

    </div>
  </div>
</template>
