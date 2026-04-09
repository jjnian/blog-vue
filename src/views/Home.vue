<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { Volume2, Flame, MessageCircle, ChevronRight } from 'lucide-vue-next';
import { getArticles } from '@/api/blog';

interface Post {
  title: string;
  date: string;
  link: string;
  excerpt: string;
  views: number;
  comments: number;
  category: string;
  tags: string[];
}

const posts = ref<Post[]>([]);
const categoryGroups = ref<{ name: string; posts: Post[] }[]>([]);

const normalizeDate = (value?: string) => {
  if (!value) return '-';
  const d = new Date(value);
  if (Number.isNaN(d.getTime())) return value;
  const pad = (n: number) => String(n).padStart(2, '0');
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`;
};

onMounted(async () => {
  try {
    const page = await getArticles({ page: 1, size: 20, status: 'PUBLISHED' });
    const items: Post[] = (page.records || []).map((item: any) => ({
      title: item.title,
      date: normalizeDate(item.publishedAt || item.createdAt),
      link: item.slug ? `/articles/${item.slug}` : '#',
      excerpt: item.excerpt || '',
      views: Number(item.views || 0),
      comments: Number(item.comments || 0),
      category: item.category?.name || '未分类',
      tags: (item.tags || []).map((tag: any) => tag.name),
    }));
    posts.value = items;

    const catMap = new Map<string, Post[]>();
    items.forEach((post) => {
      if (!catMap.has(post.category)) catMap.set(post.category, []);
      catMap.get(post.category)!.push(post);
    });
    categoryGroups.value = Array.from(catMap.entries()).map(([name, list]) => ({ name, posts: list }));
  } catch {
    posts.value = [];
  }
});
</script>

<template>
  <div class="space-y-8">
    <!-- Welcome Banner -->
    <div class="flex items-center gap-3 px-5 py-3 bg-white rounded-xl border border-gray-100 shadow-sm">
      <Volume2 :size="15" class="text-[#49b1f5] flex-shrink-0" stroke-width="2" />
      <span class="text-gray-500 text-sm">欢迎光临</span>
    </div>

    <!-- Latest Articles -->
    <section>
      <div class="flex items-center justify-between mb-5">
        <div class="flex items-center gap-2.5">
          <span class="w-1 h-5 bg-red-400 rounded-full"></span>
          <h2 class="font-bold text-gray-800 text-base">最新</h2>
        </div>
        <router-link to="/archives" class="flex items-center gap-0.5 text-[#49b1f5] text-xs font-medium hover:gap-2 transition-all duration-200">
          MORE <ChevronRight :size="13" stroke-width="2.5" />
        </router-link>
      </div>

      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
        <router-link
          v-for="post in posts.slice(0, 6)"
          :key="post.link"
          :to="post.link"
          class="bg-white rounded-2xl overflow-hidden border border-gray-100 shadow-sm hover:shadow-md hover:-translate-y-1 transition-all duration-300 group block"
        >
          <div class="h-44 overflow-hidden bg-gray-100">
            <img
              :src="`https://picsum.photos/seed/${post.title}/400/250`"
              :alt="post.title"
              class="w-full h-full object-cover group-hover:scale-[1.06] transition-transform duration-500"
              referrerPolicy="no-referrer"
              v-zoom
            />
          </div>
          <div class="p-4">
            <p class="text-[10px] text-gray-400 mb-2">发布于 {{ post.date }}</p>
            <h3 class="font-bold text-gray-800 text-sm leading-snug mb-3 line-clamp-2 group-hover:text-[#49b1f5] transition-colors duration-200">
              {{ post.title }}
            </h3>
            <div class="flex flex-wrap gap-1.5 mb-3">
              <span class="text-[10px] px-2.5 py-0.5 bg-cyan-50 text-cyan-600 rounded-full border border-cyan-100 font-medium">
                {{ post.category }}
              </span>
              <span
                v-for="tag in post.tags.slice(0, 1)"
                :key="tag"
                class="text-[10px] px-2.5 py-0.5 bg-orange-50 text-orange-500 rounded-full border border-orange-100 font-medium"
              >
                {{ tag }}
              </span>
            </div>
            <div class="flex items-center gap-4 text-[11px] text-gray-400">
              <span class="flex items-center gap-1">
                <Flame :size="11" class="text-red-400" stroke-width="2" />
                {{ post.views }} 热度
              </span>
              <span class="flex items-center gap-1">
                <MessageCircle :size="11" class="text-blue-300" stroke-width="2" />
                {{ post.comments }} 评论
              </span>
            </div>
          </div>
        </router-link>
      </div>
    </section>

    <!-- Category Sections -->
    <section v-for="cat in categoryGroups.slice(0, 4)" :key="cat.name">
      <div class="flex items-center justify-between mb-5">
        <div class="flex items-center gap-2.5">
          <span class="w-1 h-5 bg-red-400 rounded-full"></span>
          <h2 class="font-bold text-gray-800 text-base">{{ cat.name }}</h2>
        </div>
        <router-link to="/categories" class="flex items-center gap-0.5 text-[#49b1f5] text-xs font-medium hover:gap-2 transition-all duration-200">
          MORE <ChevronRight :size="13" stroke-width="2.5" />
        </router-link>
      </div>

      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
        <router-link
          v-for="post in cat.posts.slice(0, 3)"
          :key="post.link"
          :to="post.link"
          class="bg-white rounded-2xl overflow-hidden border border-gray-100 shadow-sm hover:shadow-md hover:-translate-y-1 transition-all duration-300 group block"
        >
          <div class="h-44 overflow-hidden bg-gray-100">
            <img
              :src="`https://picsum.photos/seed/${post.title}/400/250`"
              :alt="post.title"
              class="w-full h-full object-cover group-hover:scale-[1.06] transition-transform duration-500"
              referrerPolicy="no-referrer"
              v-zoom
            />
          </div>
          <div class="p-4">
            <p class="text-[10px] text-gray-400 mb-2">发布于 {{ post.date }}</p>
            <h3 class="font-bold text-gray-800 text-sm leading-snug mb-3 line-clamp-2 group-hover:text-[#49b1f5] transition-colors duration-200">
              {{ post.title }}
            </h3>
            <div class="flex flex-wrap gap-1.5 mb-3">
              <span class="text-[10px] px-2.5 py-0.5 bg-cyan-50 text-cyan-600 rounded-full border border-cyan-100 font-medium">
                {{ post.category }}
              </span>
              <span
                v-for="tag in post.tags.slice(0, 1)"
                :key="tag"
                class="text-[10px] px-2.5 py-0.5 bg-orange-50 text-orange-500 rounded-full border border-orange-100 font-medium"
              >
                {{ tag }}
              </span>
            </div>
            <div class="flex items-center gap-4 text-[11px] text-gray-400">
              <span class="flex items-center gap-1">
                <Flame :size="11" class="text-red-400" stroke-width="2" />
                {{ post.views }} 热度
              </span>
              <span class="flex items-center gap-1">
                <MessageCircle :size="11" class="text-blue-300" stroke-width="2" />
                {{ post.comments }} 评论
              </span>
            </div>
          </div>
        </router-link>
      </div>
    </section>

    <!-- Pagination -->
    <div class="flex justify-center pt-4">
      <nav class="flex items-center gap-2">
        <button class="w-8 h-8 flex items-center justify-center bg-white rounded-lg border border-gray-100 shadow-sm hover:bg-[#49b1f5] hover:text-white hover:border-[#49b1f5] transition-all duration-200 text-sm font-medium text-gray-600">1</button>
        <button class="w-8 h-8 flex items-center justify-center bg-white rounded-lg border border-gray-100 shadow-sm hover:bg-[#49b1f5] hover:text-white hover:border-[#49b1f5] transition-all duration-200 text-sm font-medium text-gray-600">2</button>
        <button class="px-5 h-8 flex items-center justify-center bg-white rounded-lg border border-gray-100 shadow-sm hover:bg-[#49b1f5] hover:text-white hover:border-[#49b1f5] transition-all duration-200 text-sm font-medium text-gray-600">下一页</button>
      </nav>
    </div>
  </div>
</template>
