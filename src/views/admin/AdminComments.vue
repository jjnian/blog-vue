<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue';
import {
  Search, RefreshCw, CheckCircle, XCircle, Trash2,
  ChevronLeft, ChevronRight, MessageSquare
} from 'lucide-vue-next';
import { getAdminComments, updateCommentStatus, deleteComment } from '@/api/admin';
import type { AdminComment } from '@/api/admin';

const comments = ref<AdminComment[]>([]);
const loading = ref(false);
const total = ref(0);
const pages = ref(1);

const filter = reactive({
  page: 1,
  size: 10,
  status: '',
  keyword: '',
});

const statusOptions = [
  { value: '', label: '全部' },
  { value: 'PENDING', label: '待审核' },
  { value: 'APPROVED', label: '已通过' },
  { value: 'REJECTED', label: '已拒绝' },
];

const fetchComments = async () => {
  loading.value = true;
  try {
    const res = await getAdminComments(filter);
    comments.value = res.records;
    total.value = Number(res.total);
    pages.value = Number(res.pages);
  } catch {
    // ignore
  } finally {
    loading.value = false;
  }
};

onMounted(fetchComments);

const handleApprove = async (comment: AdminComment) => {
  try {
    await updateCommentStatus(comment.id, 'APPROVED');
    comment.status = 'APPROVED';
  } catch {
    // ignore
  }
};

const handleReject = async (comment: AdminComment) => {
  try {
    await updateCommentStatus(comment.id, 'REJECTED');
    comment.status = 'REJECTED';
  } catch {
    // ignore
  }
};

const handleDelete = async (comment: AdminComment) => {
  if (!confirm('确定要删除这条评论吗？')) return;
  try {
    await deleteComment(comment.id);
    fetchComments();
  } catch {
    // ignore
  }
};

const formatDate = (v?: string) => {
  if (!v) return '-';
  return new Date(v).toLocaleString('zh-CN', { month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' });
};

const statusConfig: Record<string, { label: string; class: string }> = {
  PENDING: { label: '待审核', class: 'bg-yellow-100 text-yellow-600' },
  APPROVED: { label: '已通过', class: 'bg-green-100 text-green-600' },
  REJECTED: { label: '已拒绝', class: 'bg-red-100 text-red-400' },
};
</script>

<template>
  <div class="space-y-4">
    <!-- Toolbar -->
    <div class="bg-white/80 backdrop-blur-xl rounded-2xl border border-white/80 shadow-sm p-4 flex flex-wrap items-center gap-3">
      <div class="flex items-center gap-1 bg-gray-50 rounded-xl p-1">
        <button
          v-for="opt in statusOptions"
          :key="opt.value"
          @click="filter.status = opt.value; filter.page = 1; fetchComments()"
          class="px-3 py-1.5 rounded-lg text-xs font-medium transition-all"
          :class="filter.status === opt.value ? 'bg-white text-[#49b1f5] shadow-sm' : 'text-gray-400 hover:text-gray-600'"
        >{{ opt.label }}</button>
      </div>

      <div class="relative flex-1 min-w-40">
        <Search :size="15" class="absolute left-3 top-1/2 -translate-y-1/2 text-gray-300" />
        <input
          v-model="filter.keyword"
          @keydown.enter="filter.page = 1; fetchComments()"
          type="text"
          placeholder="搜索评论内容"
          class="w-full pl-9 pr-4 py-2 text-sm bg-gray-50 border border-gray-100 rounded-xl focus:outline-none focus:border-[#49b1f5] transition-colors"
        />
      </div>
      <button @click="fetchComments" class="p-2 rounded-xl border border-gray-100 text-gray-400 hover:text-[#49b1f5] hover:border-[#49b1f5]/30 transition-colors">
        <RefreshCw :size="14" :class="{ 'animate-spin': loading }" />
      </button>
    </div>

    <!-- Comments List -->
    <div class="bg-white/80 backdrop-blur-xl rounded-2xl border border-white/80 shadow-sm overflow-hidden">
      <div v-if="loading" class="flex items-center justify-center gap-2 py-10 text-gray-300 text-sm">
        <RefreshCw :size="16" class="animate-spin" /> 加载中...
      </div>
      <div v-else-if="comments.length === 0" class="text-center py-10 text-gray-300">
        <MessageSquare :size="32" class="mx-auto mb-2 opacity-40" />
        暂无评论数据
      </div>

      <div v-else class="divide-y divide-gray-50">
        <div
          v-for="comment in comments"
          :key="comment.id"
          class="flex items-start gap-4 px-5 py-4 hover:bg-gray-50/50 transition-colors"
        >
          <!-- Avatar -->
          <div class="w-9 h-9 rounded-full bg-gradient-to-br from-[#49b1f5]/20 to-purple-400/20 flex items-center justify-center text-sm font-bold text-[#49b1f5] flex-shrink-0 mt-0.5">
            {{ (comment.guestName || '匿名').charAt(0).toUpperCase() }}
          </div>

          <!-- Content -->
          <div class="flex-1 min-w-0">
            <div class="flex items-center gap-2 mb-1 flex-wrap">
              <span class="text-sm font-medium text-[#2c3e50]">{{ comment.guestName || '匿名用户' }}</span>
              <span
                class="px-2 py-0.5 rounded-lg text-xs font-medium"
                :class="statusConfig[comment.status]?.class || 'bg-gray-100 text-gray-500'"
              >{{ statusConfig[comment.status]?.label || comment.status }}</span>
              <span class="text-xs text-gray-300">{{ formatDate(comment.createdAt) }}</span>
              <span v-if="comment.articleId" class="text-xs text-gray-300">文章 #{{ comment.articleId }}</span>
            </div>
            <p class="text-sm text-gray-600 leading-relaxed line-clamp-3">{{ comment.content }}</p>
          </div>

          <!-- Actions -->
          <div class="flex items-center gap-1 flex-shrink-0">
            <button
              v-if="comment.status !== 'APPROVED'"
              @click="handleApprove(comment)"
              class="p-2 rounded-lg hover:bg-green-50 text-gray-300 hover:text-green-500 transition-all"
              title="通过"
            ><CheckCircle :size="15" /></button>
            <button
              v-if="comment.status !== 'REJECTED'"
              @click="handleReject(comment)"
              class="p-2 rounded-lg hover:bg-red-50 text-gray-300 hover:text-red-400 transition-all"
              title="拒绝"
            ><XCircle :size="15" /></button>
            <button
              @click="handleDelete(comment)"
              class="p-2 rounded-lg hover:bg-red-50 text-gray-300 hover:text-red-400 transition-all"
              title="删除"
            ><Trash2 :size="15" /></button>
          </div>
        </div>
      </div>

      <!-- Pagination -->
      <div class="px-5 py-3 border-t border-gray-50 flex items-center justify-between">
        <span class="text-xs text-gray-400">共 {{ total }} 条评论</span>
        <div class="flex items-center gap-2">
          <button @click="filter.page--; fetchComments()" :disabled="filter.page <= 1" class="p-1.5 rounded-lg border border-gray-100 text-gray-400 disabled:opacity-40 hover:border-[#49b1f5]/30 hover:text-[#49b1f5] transition-all"><ChevronLeft :size="14" /></button>
          <span class="text-xs text-gray-500">{{ filter.page }} / {{ pages }}</span>
          <button @click="filter.page++; fetchComments()" :disabled="filter.page >= pages" class="p-1.5 rounded-lg border border-gray-100 text-gray-400 disabled:opacity-40 hover:border-[#49b1f5]/30 hover:text-[#49b1f5] transition-all"><ChevronRight :size="14" /></button>
        </div>
      </div>
    </div>
  </div>
</template>
