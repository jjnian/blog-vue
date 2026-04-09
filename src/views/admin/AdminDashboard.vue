<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { Users, FileText, MessageSquare, Heart, Link as LinkIcon, TrendingUp, Eye, Tag } from 'lucide-vue-next';
import { getAdminStats } from '@/api/admin';
import type { AdminStats } from '@/api/admin';

const stats = ref<AdminStats | null>(null);
const loading = ref(true);

onMounted(async () => {
  try {
    stats.value = await getAdminStats();
  } catch {
    // ignore
  } finally {
    loading.value = false;
  }
});

const statCards = [
  { key: 'userCount', label: '总用户数', icon: Users, color: 'from-blue-400 to-[#49b1f5]', bg: 'bg-blue-50', text: 'text-blue-600' },
  { key: 'articleCount', label: '总文章数', icon: FileText, color: 'from-purple-400 to-purple-500', bg: 'bg-purple-50', text: 'text-purple-600' },
  { key: 'publishedCount', label: '已发布', icon: TrendingUp, color: 'from-green-400 to-emerald-500', bg: 'bg-green-50', text: 'text-green-600' },
  { key: 'commentCount', label: '评论总数', icon: MessageSquare, color: 'from-yellow-400 to-orange-400', bg: 'bg-yellow-50', text: 'text-yellow-600' },
  { key: 'messageCount', label: '留言总数', icon: Heart, color: 'from-pink-400 to-rose-500', bg: 'bg-pink-50', text: 'text-pink-600' },
  { key: 'wishCount', label: '许愿总数', icon: Eye, color: 'from-indigo-400 to-indigo-500', bg: 'bg-indigo-50', text: 'text-indigo-600' },
  { key: 'linkCount', label: '友链数量', icon: LinkIcon, color: 'from-teal-400 to-cyan-500', bg: 'bg-teal-50', text: 'text-teal-600' },
  { key: 'pendingCommentCount', label: '待审评论', icon: Tag, color: 'from-red-400 to-rose-500', bg: 'bg-red-50', text: 'text-red-600' },
];

const getStatValue = (key: string) => {
  if (!stats.value) return '-';
  return (stats.value as Record<string, number>)[key] ?? 0;
};
</script>

<template>
  <div class="space-y-6">
    <!-- Welcome Banner -->
    <div class="bg-gradient-to-r from-[#49b1f5]/20 via-blue-50/50 to-purple-400/10 rounded-2xl p-6 border border-[#49b1f5]/10">
      <h2 class="text-xl font-bold text-[#2c3e50] mb-1">欢迎回到管理后台</h2>
      <p class="text-sm text-gray-400">在这里管理您的博客内容、用户和系统设置</p>
    </div>

    <!-- Stats Grid -->
    <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
      <div
        v-for="card in statCards"
        :key="card.key"
        class="bg-white/80 backdrop-blur-xl rounded-2xl border border-white/80 shadow-sm hover:shadow-md hover:-translate-y-0.5 transition-all duration-300 p-5"
      >
        <div v-if="loading" class="animate-pulse space-y-3">
          <div class="w-10 h-10 rounded-xl bg-gray-100"></div>
          <div class="h-7 w-16 rounded bg-gray-100"></div>
          <div class="h-3 w-20 rounded bg-gray-100"></div>
        </div>
        <template v-else>
          <div class="flex items-center justify-between mb-3">
            <div :class="[card.bg, 'w-10 h-10 rounded-xl flex items-center justify-center']">
              <component :is="card.icon" :size="18" :class="card.text" stroke-width="1.5" />
            </div>
          </div>
          <div class="text-2xl font-bold text-[#2c3e50]">{{ getStatValue(card.key) }}</div>
          <div class="text-xs text-gray-400 mt-1">{{ card.label }}</div>
        </template>
      </div>
    </div>

    <!-- Quick Actions -->
    <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
      <div class="bg-white/80 backdrop-blur-xl rounded-2xl border border-white/80 shadow-sm p-5">
        <h3 class="text-sm font-bold text-[#2c3e50] mb-4">文章状态分布</h3>
        <div class="space-y-3" v-if="stats">
          <div class="flex items-center gap-3">
            <span class="text-xs text-gray-400 w-16 flex-shrink-0">已发布</span>
            <div class="flex-1 h-2 bg-gray-100 rounded-full overflow-hidden">
              <div
                class="h-full bg-gradient-to-r from-green-400 to-emerald-500 rounded-full transition-all duration-1000"
                :style="{ width: stats.articleCount > 0 ? `${(stats.publishedCount / stats.articleCount) * 100}%` : '0%' }"
              ></div>
            </div>
            <span class="text-xs font-medium text-[#2c3e50] w-8 text-right">{{ stats.publishedCount }}</span>
          </div>
          <div class="flex items-center gap-3">
            <span class="text-xs text-gray-400 w-16 flex-shrink-0">草稿</span>
            <div class="flex-1 h-2 bg-gray-100 rounded-full overflow-hidden">
              <div
                class="h-full bg-gradient-to-r from-yellow-400 to-orange-400 rounded-full transition-all duration-1000"
                :style="{ width: stats.articleCount > 0 ? `${(stats.draftCount / stats.articleCount) * 100}%` : '0%' }"
              ></div>
            </div>
            <span class="text-xs font-medium text-[#2c3e50] w-8 text-right">{{ stats.draftCount }}</span>
          </div>
        </div>
      </div>

      <div class="bg-white/80 backdrop-blur-xl rounded-2xl border border-white/80 shadow-sm p-5">
        <h3 class="text-sm font-bold text-[#2c3e50] mb-4">快速操作</h3>
        <div class="space-y-2">
          <router-link
            to="/write"
            class="flex items-center gap-3 px-4 py-3 rounded-xl bg-gradient-to-r from-[#49b1f5]/10 to-[#49b1f5]/5 border border-[#49b1f5]/20 text-[#49b1f5] text-sm font-medium hover:from-[#49b1f5]/20 transition-all"
          >
            <FileText :size="15" stroke-width="2" />
            写新文章
          </router-link>
          <router-link
            to="/admin/articles?status=PENDING_REVIEW"
            class="flex items-center gap-3 px-4 py-3 rounded-xl bg-orange-50 border border-orange-100 text-orange-500 text-sm font-medium hover:bg-orange-100 transition-all"
          >
            <MessageSquare :size="15" stroke-width="2" />
            待审文章 ({{ stats?.pendingCommentCount ?? 0 }})
          </router-link>
        </div>
      </div>
    </div>
  </div>
</template>
