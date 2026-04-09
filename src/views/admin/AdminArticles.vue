<script setup lang="ts">
import { ref, onMounted, reactive, watch } from 'vue';
import { useRoute } from 'vue-router';
import {
  Search, RefreshCw, CheckCircle, XCircle, Trash2, Star, StarOff,
  ChevronLeft, ChevronRight, Eye, Globe, Clock, FileText
} from 'lucide-vue-next';
import { getAdminArticles, updateArticleStatus, reviewArticle, toggleArticleTop, deleteAdminArticle } from '@/api/admin';
import type { AdminArticle } from '@/api/admin';

const route = useRoute();

const articles = ref<AdminArticle[]>([]);
const loading = ref(false);
const total = ref(0);
const pages = ref(1);

const filter = reactive({
  page: 1,
  size: 10,
  status: (route.query.status as string) || '',
  keyword: '',
});

const statusOptions = [
  { value: '', label: '全部' },
  { value: 'PUBLISHED', label: '已发布' },
  { value: 'DRAFT', label: '草稿' },
  { value: 'PENDING_REVIEW', label: '待审核' },
  { value: 'REJECTED', label: '已拒绝' },
];

const fetchArticles = async () => {
  loading.value = true;
  try {
    const res = await getAdminArticles(filter);
    articles.value = res.records;
    total.value = Number(res.total);
    pages.value = Number(res.pages);
  } catch {
    // ignore
  } finally {
    loading.value = false;
  }
};

onMounted(fetchArticles);

watch(() => route.query.status, (s) => {
  filter.status = (s as string) || '';
  filter.page = 1;
  fetchArticles();
});

const handleSearch = () => {
  filter.page = 1;
  fetchArticles();
};

const handleReview = async (article: AdminArticle, action: 'APPROVE' | 'REJECT') => {
  try {
    await reviewArticle(article.id, action);
    article.status = action === 'APPROVE' ? 'PUBLISHED' : 'REJECTED';
  } catch {
    // ignore
  }
};

const handleToggleTop = async (article: AdminArticle) => {
  const newTop = !article.isTop;
  try {
    await toggleArticleTop(article.id, newTop);
    article.isTop = newTop;
  } catch {
    // ignore
  }
};

const handleStatusChange = async (article: AdminArticle, status: string) => {
  try {
    await updateArticleStatus(article.id, status);
    article.status = status;
  } catch {
    // ignore
  }
};

const handleDelete = async (article: AdminArticle) => {
  if (!confirm(`确定要删除文章 "${article.title}" 吗？`)) return;
  try {
    await deleteAdminArticle(article.id);
    fetchArticles();
  } catch {
    // ignore
  }
};

const formatDate = (v?: string) => {
  if (!v) return '-';
  return new Date(v).toLocaleDateString('zh-CN');
};

const statusConfig: Record<string, { label: string; class: string }> = {
  PUBLISHED: { label: '已发布', class: 'bg-green-100 text-green-600' },
  DRAFT: { label: '草稿', class: 'bg-gray-100 text-gray-500' },
  PENDING_REVIEW: { label: '待审核', class: 'bg-yellow-100 text-yellow-600' },
  REJECTED: { label: '已拒绝', class: 'bg-red-100 text-red-400' },
};
</script>

<template>
  <div class="space-y-4">
    <!-- Toolbar -->
    <div class="bg-white/80 backdrop-blur-xl rounded-2xl border border-white/80 shadow-sm p-4 flex flex-wrap items-center gap-3">
      <!-- Status Filter -->
      <div class="flex items-center gap-1 bg-gray-50 rounded-xl p-1">
        <button
          v-for="opt in statusOptions"
          :key="opt.value"
          @click="filter.status = opt.value; filter.page = 1; fetchArticles()"
          class="px-3 py-1.5 rounded-lg text-xs font-medium transition-all"
          :class="filter.status === opt.value
            ? 'bg-white text-[#49b1f5] shadow-sm'
            : 'text-gray-400 hover:text-gray-600'"
        >{{ opt.label }}</button>
      </div>

      <div class="relative flex-1 min-w-40">
        <Search :size="15" class="absolute left-3 top-1/2 -translate-y-1/2 text-gray-300" />
        <input
          v-model="filter.keyword"
          @keydown.enter="handleSearch"
          type="text"
          placeholder="搜索文章标题"
          class="w-full pl-9 pr-4 py-2 text-sm bg-gray-50 border border-gray-100 rounded-xl focus:outline-none focus:border-[#49b1f5] transition-colors"
        />
      </div>
      <button @click="handleSearch" class="flex items-center gap-2 px-4 py-2 rounded-xl bg-[#49b1f5] text-white text-sm font-medium hover:bg-[#3a9de8] transition-colors">
        <Search :size="14" />
      </button>
      <button @click="fetchArticles" class="p-2 rounded-xl border border-gray-100 text-gray-400 hover:text-[#49b1f5] hover:border-[#49b1f5]/30 transition-colors">
        <RefreshCw :size="14" :class="{ 'animate-spin': loading }" />
      </button>
    </div>

    <!-- Table -->
    <div class="bg-white/80 backdrop-blur-xl rounded-2xl border border-white/80 shadow-sm overflow-hidden">
      <div class="overflow-x-auto">
        <table class="w-full text-sm">
          <thead>
            <tr class="border-b border-gray-100">
              <th class="text-left px-5 py-3.5 text-xs font-semibold text-gray-400 uppercase tracking-wider">文章</th>
              <th class="text-left px-5 py-3.5 text-xs font-semibold text-gray-400 uppercase tracking-wider">作者</th>
              <th class="text-left px-5 py-3.5 text-xs font-semibold text-gray-400 uppercase tracking-wider">状态</th>
              <th class="text-left px-5 py-3.5 text-xs font-semibold text-gray-400 uppercase tracking-wider hidden md:table-cell">数据</th>
              <th class="text-left px-5 py-3.5 text-xs font-semibold text-gray-400 uppercase tracking-wider">时间</th>
              <th class="text-right px-5 py-3.5 text-xs font-semibold text-gray-400 uppercase tracking-wider">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="loading">
              <td colspan="6" class="px-5 py-10 text-center text-gray-300 text-sm">
                <div class="flex items-center justify-center gap-2">
                  <RefreshCw :size="16" class="animate-spin" /> 加载中...
                </div>
              </td>
            </tr>
            <tr v-else-if="articles.length === 0">
              <td colspan="6" class="px-5 py-10 text-center text-gray-300 text-sm">
                <FileText :size="32" class="mx-auto mb-2 opacity-30" />
                暂无文章数据
              </td>
            </tr>
            <tr
              v-for="article in articles"
              :key="article.id"
              class="border-b border-gray-50 hover:bg-gray-50/50 transition-colors"
            >
              <td class="px-5 py-4 max-w-xs">
                <div class="flex items-start gap-3">
                  <div v-if="article.isTop" class="mt-0.5">
                    <Star :size="13" class="text-yellow-400 fill-yellow-400 flex-shrink-0" />
                  </div>
                  <div class="min-w-0">
                    <div class="font-medium text-[#2c3e50] truncate">{{ article.title }}</div>
                    <div v-if="article.category" class="text-xs text-gray-400 mt-0.5">{{ article.category.name }}</div>
                  </div>
                </div>
              </td>
              <td class="px-5 py-4 text-gray-500 text-xs whitespace-nowrap">
                {{ article.author?.nickname || '未知' }}
              </td>
              <td class="px-5 py-4">
                <span
                  class="px-2.5 py-1 rounded-lg text-xs font-medium whitespace-nowrap"
                  :class="statusConfig[article.status]?.class || 'bg-gray-100 text-gray-500'"
                >{{ statusConfig[article.status]?.label || article.status }}</span>
              </td>
              <td class="px-5 py-4 hidden md:table-cell">
                <div class="flex items-center gap-3 text-xs text-gray-400">
                  <span class="flex items-center gap-1"><Eye :size="11" /> {{ article.views ?? 0 }}</span>
                </div>
              </td>
              <td class="px-5 py-4 text-gray-400 text-xs whitespace-nowrap">{{ formatDate(article.createdAt) }}</td>
              <td class="px-5 py-4">
                <div class="flex items-center justify-end gap-1">
                  <!-- Review actions for pending -->
                  <template v-if="article.status === 'PENDING_REVIEW'">
                    <button
                      @click="handleReview(article, 'APPROVE')"
                      class="p-2 rounded-lg hover:bg-green-50 text-gray-300 hover:text-green-500 transition-all"
                      title="审核通过"
                    ><CheckCircle :size="15" /></button>
                    <button
                      @click="handleReview(article, 'REJECT')"
                      class="p-2 rounded-lg hover:bg-red-50 text-gray-300 hover:text-red-400 transition-all"
                      title="拒绝"
                    ><XCircle :size="15" /></button>
                  </template>

                  <!-- Publish/unpublish for non-pending -->
                  <template v-else>
                    <button
                      v-if="article.status !== 'PUBLISHED'"
                      @click="handleStatusChange(article, 'PUBLISHED')"
                      class="p-2 rounded-lg hover:bg-green-50 text-gray-300 hover:text-green-500 transition-all"
                      title="发布"
                    ><Globe :size="15" /></button>
                    <button
                      v-else
                      @click="handleStatusChange(article, 'DRAFT')"
                      class="p-2 rounded-lg hover:bg-yellow-50 text-gray-300 hover:text-yellow-500 transition-all"
                      title="撤回为草稿"
                    ><Clock :size="15" /></button>
                  </template>

                  <button
                    @click="handleToggleTop(article)"
                    class="p-2 rounded-lg transition-all"
                    :class="article.isTop ? 'text-yellow-400 hover:bg-yellow-50' : 'text-gray-300 hover:text-yellow-400 hover:bg-yellow-50'"
                    title="置顶"
                  >
                    <component :is="article.isTop ? Star : StarOff" :size="15" :class="{ 'fill-yellow-400': article.isTop }" />
                  </button>

                  <button
                    @click="handleDelete(article)"
                    class="p-2 rounded-lg hover:bg-red-50 text-gray-300 hover:text-red-400 transition-all"
                    title="删除"
                  ><Trash2 :size="15" /></button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Pagination -->
      <div class="px-5 py-3 border-t border-gray-50 flex items-center justify-between">
        <span class="text-xs text-gray-400">共 {{ total }} 篇文章</span>
        <div class="flex items-center gap-2">
          <button
            @click="filter.page--; fetchArticles()"
            :disabled="filter.page <= 1"
            class="p-1.5 rounded-lg border border-gray-100 text-gray-400 disabled:opacity-40 hover:border-[#49b1f5]/30 hover:text-[#49b1f5] transition-all"
          ><ChevronLeft :size="14" /></button>
          <span class="text-xs text-gray-500">{{ filter.page }} / {{ pages }}</span>
          <button
            @click="filter.page++; fetchArticles()"
            :disabled="filter.page >= pages"
            class="p-1.5 rounded-lg border border-gray-100 text-gray-400 disabled:opacity-40 hover:border-[#49b1f5]/30 hover:text-[#49b1f5] transition-all"
          ><ChevronRight :size="14" /></button>
        </div>
      </div>
    </div>
  </div>
</template>
