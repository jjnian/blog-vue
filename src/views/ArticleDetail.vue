<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import {
  Calendar, Eye, Flame, MessageCircle, Tag as TagIcon,
  Folder, ArrowLeft, ChevronRight, Clock
} from 'lucide-vue-next';
import { getArticleBySlug } from '@/api/blog';
import type { Article } from '@/api/blog';
import CommentSection from '@/components/CommentSection.vue';

const route = useRoute();
const router = useRouter();

const article = ref<Article | null>(null);
const loading = ref(true);
const error = ref('');

const slug = computed(() => route.params.slug as string);

function normalizeDate(value?: string): string {
  if (!value) return '-';
  const d = new Date(value);
  if (Number.isNaN(d.getTime())) return value;
  const pad = (n: number) => String(n).padStart(2, '0');
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
}

// Simple markdown renderer (same as Write.vue)
function renderMarkdown(text: string): string {
  if (!text) return '';
  return text
    .replace(/^### (.+)$/gm, '<h3 class="text-lg font-bold mt-6 mb-2 text-gray-800">$1</h3>')
    .replace(/^## (.+)$/gm, '<h2 class="text-xl font-bold mt-8 mb-3 text-gray-800">$1</h2>')
    .replace(/^# (.+)$/gm, '<h1 class="text-2xl font-bold mt-10 mb-4 text-gray-800">$1</h1>')
    .replace(/^---$/gm, '<hr class="my-6 border-gray-100" />')
    .replace(/\*\*(.+?)\*\*/g, '<strong class="font-bold text-gray-900">$1</strong>')
    .replace(/\*(.+?)\*/g, '<em class="italic">$1</em>')
    .replace(/`([^`]+)`/g, '<code class="bg-gray-100 px-1.5 py-0.5 rounded text-[0.85em] font-mono text-pink-600">$1</code>')
    .replace(/\[(.+?)\]\((.+?)\)/g, '<a href="$2" class="text-[#49b1f5] hover:underline" target="_blank" rel="noopener">$1</a>')
    .replace(/^> (.+)$/gm, '<blockquote class="border-l-4 border-[#49b1f5]/40 pl-4 my-4 text-gray-500 italic">$1</blockquote>')
    .replace(/^- (.+)$/gm, '<li class="ml-4 list-disc text-gray-700 my-0.5">$1</li>')
    .replace(/^(\d+)\. (.+)$/gm, '<li class="ml-4 list-decimal text-gray-700 my-0.5">$2</li>')
    .replace(/\n\n/g, '</p><p class="my-3 text-gray-700 leading-7">')
    .replace(/\n/g, '<br />')
    .replace(/^(.+)/, '<p class="my-3 text-gray-700 leading-7">$1')
    .replace(/(.+)$/, '$1</p>');
}

const renderedContent = computed(() => renderMarkdown(article.value?.content || ''));

async function loadArticle() {
  loading.value = true;
  error.value = '';
  try {
    article.value = await getArticleBySlug(slug.value);
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : '文章加载失败';
  } finally {
    loading.value = false;
  }
}

onMounted(loadArticle);
</script>

<template>
  <div>
    <!-- Loading skeleton -->
    <div v-if="loading" class="space-y-4">
      <div class="bg-white rounded-2xl border border-gray-100 shadow-sm p-6 animate-pulse">
        <div class="h-48 bg-gray-100 rounded-xl mb-6"></div>
        <div class="h-7 bg-gray-100 rounded w-3/4 mb-3"></div>
        <div class="h-4 bg-gray-100 rounded w-1/2 mb-6"></div>
        <div class="space-y-2">
          <div class="h-3 bg-gray-100 rounded"></div>
          <div class="h-3 bg-gray-100 rounded"></div>
          <div class="h-3 bg-gray-100 rounded w-5/6"></div>
        </div>
      </div>
    </div>

    <!-- Error state -->
    <div v-else-if="error" class="bg-white rounded-2xl border border-gray-100 shadow-sm p-10 text-center">
      <p class="text-sm text-gray-500 mb-4">{{ error }}</p>
      <button
        @click="router.back()"
        class="inline-flex items-center gap-1.5 text-sm text-[#49b1f5] hover:underline"
      >
        <ArrowLeft :size="14" stroke-width="2" />
        返回
      </button>
    </div>

    <!-- Article -->
    <article v-else-if="article" class="space-y-6">
      <!-- Back breadcrumb -->
      <div class="flex items-center gap-1.5 text-xs text-gray-400">
        <button @click="router.push('/')" class="hover:text-[#49b1f5] transition-colors duration-200">首页</button>
        <ChevronRight :size="12" stroke-width="2" />
        <span
          v-if="article.category"
          @click="router.push('/categories')"
          class="hover:text-[#49b1f5] transition-colors duration-200 cursor-pointer"
        >{{ article.category.name }}</span>
        <ChevronRight v-if="article.category" :size="12" stroke-width="2" />
        <span class="text-gray-500 line-clamp-1">{{ article.title }}</span>
      </div>

      <!-- Main card -->
      <div class="bg-white rounded-2xl border border-gray-100 shadow-sm overflow-hidden">
        <!-- Cover image -->
        <div v-if="article.coverImage || article.title" class="h-52 sm:h-72 overflow-hidden bg-gray-100">
          <img
            :src="article.coverImage || `https://picsum.photos/seed/${article.title}/1200/500`"
            :alt="article.title"
            class="w-full h-full object-cover"
            referrerPolicy="no-referrer"
          />
        </div>

        <div class="p-6 sm:p-8">
          <!-- Title -->
          <h1 class="text-xl sm:text-2xl font-bold text-gray-900 leading-snug mb-4">
            {{ article.title }}
          </h1>

          <!-- Meta row -->
          <div class="flex flex-wrap items-center gap-x-5 gap-y-2 text-[11px] text-gray-400 pb-5 border-b border-gray-50">
            <span class="flex items-center gap-1.5">
              <Calendar :size="12" stroke-width="2" />
              发布于 {{ normalizeDate(article.publishedAt || article.createdAt) }}
            </span>
            <span class="flex items-center gap-1.5">
              <Eye :size="12" stroke-width="2" />
              {{ article.views || 0 }} 次阅读
            </span>
            <span class="flex items-center gap-1.5">
              <MessageCircle :size="12" stroke-width="2" />
              {{ article.comments || 0 }} 评论
            </span>
            <span v-if="article.category" class="flex items-center gap-1.5">
              <Folder :size="12" stroke-width="2" />
              {{ article.category.name }}
            </span>
          </div>

          <!-- Tags -->
          <div v-if="article.tags && article.tags.length > 0" class="flex flex-wrap gap-1.5 mt-4 pb-5 border-b border-gray-50">
            <span
              v-for="tag in article.tags"
              :key="tag.id"
              class="inline-flex items-center gap-1 text-[10px] px-2.5 py-0.5 bg-orange-50 text-orange-500 rounded-full border border-orange-100 font-medium"
            >
              <TagIcon :size="9" stroke-width="2" />
              {{ tag.name }}
            </span>
          </div>

          <!-- Content -->
          <div
            class="prose-sm mt-6 text-gray-700 text-[14px] leading-7 [&_h1]:text-2xl [&_h1]:font-bold [&_h1]:mt-10 [&_h1]:mb-4 [&_h2]:text-xl [&_h2]:font-bold [&_h2]:mt-8 [&_h2]:mb-3 [&_h3]:text-lg [&_h3]:font-bold [&_h3]:mt-6 [&_h3]:mb-2 [&_p]:my-3 [&_p]:leading-7 [&_blockquote]:border-l-4 [&_blockquote]:border-blue-200 [&_blockquote]:pl-4 [&_blockquote]:my-4 [&_blockquote]:text-gray-500 [&_code]:bg-gray-100 [&_code]:px-1.5 [&_code]:py-0.5 [&_code]:rounded [&_code]:text-pink-600 [&_code]:text-[0.85em] [&_a]:text-[#49b1f5] [&_a]:hover:underline [&_ul]:pl-5 [&_ol]:pl-5 [&_li]:my-1"
            v-html="renderedContent"
          ></div>

          <!-- Footer -->
          <div class="mt-10 pt-5 border-t border-gray-50 flex items-center justify-between">
            <button
              @click="router.back()"
              class="inline-flex items-center gap-1.5 text-xs text-gray-400 hover:text-[#49b1f5] transition-colors duration-200"
            >
              <ArrowLeft :size="13" stroke-width="2" />
              返回上一页
            </button>
            <div class="flex items-center gap-1.5 text-[11px] text-gray-300">
              <Clock :size="11" stroke-width="2" />
              最后更新：{{ normalizeDate(article.createdAt) }}
            </div>
          </div>
        </div>
      </div>

      <!-- Comment section -->
      <div class="bg-white rounded-2xl border border-gray-100 shadow-sm p-6 sm:p-8">
        <CommentSection
          :article-id="article.id"
          :allow-comment="article.isTop !== undefined ? true : true"
        />
      </div>
    </article>
  </div>
</template>
