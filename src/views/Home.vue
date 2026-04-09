<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { Calendar, LayoutGrid, Eye, ArrowRight } from 'lucide-vue-next';
import { getArticles } from '@/api/blog';

const posts = ref<Array<{
  title: string;
  date: string;
  link: string;
  excerpt: string;
  views: number;
  comments: number;
  category: string;
  tags: string[];
}>>([]);

const normalizeDate = (value?: string) => {
  if (!value) return '-';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;
  return date.toLocaleDateString('zh-CN');
};

onMounted(async () => {
  try {
    const page = await getArticles({ page: 1, size: 20, status: 'PUBLISHED' });
    posts.value = (page.records || []).map((item) => ({
      title: item.title,
      date: normalizeDate(item.publishedAt || item.createdAt),
      link: item.slug ? `/articles/${item.slug}` : '#',
      excerpt: item.excerpt || '',
      views: Number(item.views || 0),
      comments: Number(item.comments || 0),
      category: item.category?.name || '未分类',
      tags: (item.tags || []).map((tag) => tag.name),
    }));
  } catch {
    posts.value = [];
  }
});
</script>

<template>
  <div class="space-y-10 md:space-y-20">
    <div 
      v-for="(post, index) in posts" 
      :key="post.title"
      class="bg-gradient-to-br from-white/95 via-blue-50/80 to-indigo-50/50 backdrop-blur-xl rounded-[2rem] md:rounded-[3rem] shadow-[0_8px_30px_rgb(0,0,0,0.04)] overflow-hidden flex flex-col md:flex-row hover:shadow-[0_20px_40px_rgba(73,177,245,0.15)] hover:-translate-y-2 md:hover:-translate-y-4 transition-all duration-700 group reveal-animation border border-white/80 hover:border-[#49b1f5]/40 shimmer-container relative"
      :class="{ 'md:flex-row-reverse': index % 2 !== 0 }"
    >
      <!-- Decorative Background Elements -->
      <div class="absolute inset-0 bg-[url('data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjAiIGhlaWdodD0iMjAiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+PGNpcmNsZSBjeD0iMiIgY3k9IjIiIHI9IjIiIGZpbGw9IiM0OWIxZjUiIGZpbGwtb3BhY2l0eT0iMC4wNSIvPjwvc3ZnPg==')] opacity-50 pointer-events-none z-0"></div>
      
      <div class="md:w-[48%] h-64 md:h-96 overflow-hidden relative image-shine z-10">
        <img 
          :src="`https://picsum.photos/seed/${post.title}/800/600`" 
          :alt="post.title"
          class="w-full h-full object-cover group-hover:scale-[1.15] transition-all duration-[1000ms] ease-out brightness-95 group-hover:brightness-110"
          referrerPolicy="no-referrer"
          v-zoom
        />
        <div class="absolute inset-0 bg-black/10 group-hover:bg-black/30 transition-colors duration-700 flex items-center justify-center z-20 pointer-events-none">
          <span class="opacity-0 group-hover:opacity-100 transform translate-y-4 group-hover:translate-y-0 transition-all duration-500 text-white font-sans tracking-widest border border-white/50 px-6 py-2 rounded-full backdrop-blur-sm text-sm">阅读全文</span>
        </div>
      </div>
      <div class="md:w-[52%] p-8 md:p-14 flex flex-col justify-center relative overflow-hidden">
        <!-- Decorative Background Orbs -->
        <div class="absolute -right-20 -top-20 w-64 h-64 bg-gradient-to-br from-[#49b1f5]/10 to-purple-400/10 rounded-full blur-3xl group-hover:scale-150 group-hover:bg-[#49b1f5]/20 transition-all duration-1000 ease-out z-0 pointer-events-none"></div>
        <div class="absolute -left-20 -bottom-20 w-48 h-48 bg-gradient-to-tr from-cyan-300/10 to-blue-300/10 rounded-full blur-2xl group-hover:scale-150 transition-transform duration-1000 ease-out z-0 pointer-events-none"></div>
        
        <div class="relative z-10">
          <div class="flex flex-wrap items-center gap-4 md:gap-8 text-[9px] md:text-[10px] text-gray-400 mb-4 md:mb-8 font-bold tracking-[0.1em] md:tracking-[0.2em] uppercase">
          <span class="flex items-center space-x-2">
            <Calendar :size="12" class="md:w-3.5 md:h-3.5" stroke-width="1.5" />
            <span>{{ post.date }}</span>
          </span>
          <span class="flex items-center space-x-2">
            <LayoutGrid :size="12" class="md:w-3.5 md:h-3.5" stroke-width="1.5" />
            <span>{{ post.category }}</span>
          </span>
          <span class="flex items-center space-x-2">
            <Eye :size="12" class="md:w-3.5 md:h-3.5" stroke-width="1.5" />
            <span>{{ post.views }}</span>
          </span>
        </div>
        <h2 class="text-2xl md:text-4xl font-sans font-bold mb-4 md:mb-8 hover:text-[#49b1f5] transition-all duration-500 cursor-pointer leading-tight md:leading-[1.2]">
          {{ post.title }}
        </h2>
        <p class="text-gray-500 line-clamp-2 mb-6 md:mb-10 leading-relaxed font-light text-base md:text-xl italic">
          {{ post.excerpt }}
        </p>
        <div class="flex flex-wrap gap-2 md:gap-3 mb-6 md:mb-10">
          <span v-for="tag in post.tags" :key="tag" class="text-[8px] md:text-[9px] font-bold tracking-widest uppercase px-3 md:px-4 py-1 md:py-1.5 glass rounded-full text-gray-400 hover:text-[#49b1f5] transition-colors cursor-pointer">
            # {{ tag }}
          </span>
        </div>
        <div class="flex items-center space-x-3 md:space-x-4 text-[#49b1f5] font-bold cursor-pointer group-hover:translate-x-4 md:group-hover:translate-x-6 transition-all duration-500">
          <span class="text-[10px] md:text-xs tracking-[0.2em] md:tracking-[0.3em] uppercase">阅读全文</span>
          <ArrowRight :size="16" class="md:w-5 md:h-5" stroke-width="2" />
        </div>
        </div>
      </div>
    </div>
    
    <!-- Pagination -->
    <div class="flex justify-center pt-20">
      <nav class="flex items-center space-x-5">
        <button class="w-14 h-14 flex items-center justify-center glass rounded-[1.5rem] shadow-sm hover:bg-[#49b1f5] hover:text-white transition-all duration-500 hover:scale-110 font-sans font-bold text-lg">1</button>
        <button class="w-14 h-14 flex items-center justify-center glass rounded-[1.5rem] shadow-sm hover:bg-[#49b1f5] hover:text-white transition-all duration-500 hover:scale-110 font-sans font-bold text-lg">2</button>
        <button class="px-10 h-14 flex items-center justify-center glass rounded-[1.5rem] shadow-sm hover:bg-[#49b1f5] hover:text-white transition-all duration-500 hover:scale-110 font-bold tracking-[0.3em] uppercase text-[10px]">下一页</button>
      </nav>
    </div>
  </div>
</template>
