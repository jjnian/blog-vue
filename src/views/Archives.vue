<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { Clock } from 'lucide-vue-next';
import { getArticles } from '@/api/blog';

const timeline = ref<Array<{ date: string; title: string; excerpt: string }>>([]);

const normalizeDate = (value?: string) => {
  if (!value) return '-';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;
  return date.toLocaleDateString('zh-CN');
};

onMounted(async () => {
  try {
    const page = await getArticles({ page: 1, size: 3, status: 'PUBLISHED' });
    timeline.value = (page.records || []).map((item) => ({
      date: normalizeDate(item.publishedAt || item.createdAt),
      title: item.title,
      excerpt: item.excerpt || '暂无摘要',
    }));
  } catch {
    timeline.value = [];
  }
});
</script>

<template>
  <div class="glass rounded-[3rem] p-8 md:p-14 hover:shadow-2xl hover:shadow-[#49b1f5]/20 transition-all duration-700 border border-white/60">
    <div class="flex items-center space-x-4 mb-10 pb-5 border-b border-gray-100/50">
      <Clock :size="32" class="text-[#49b1f5]" stroke-width="1.5" />
      <h1 class="font-serif font-bold text-3xl md:text-4xl">时间线</h1>
    </div>
    <div class="space-y-8 relative before:absolute before:inset-0 before:ml-5 before:-translate-x-px md:before:mx-auto md:before:translate-x-0 before:h-full before:w-0.5 before:bg-gradient-to-b before:from-transparent before:via-gray-200 before:to-transparent">
      <!-- Timeline Item -->
      <div class="relative flex items-center justify-between md:justify-normal md:odd:flex-row-reverse group is-active">
        <!-- Icon -->
        <div class="flex items-center justify-center w-10 h-10 rounded-full border-4 border-white bg-[#49b1f5] text-white shadow shrink-0 md:order-1 md:group-odd:-translate-x-1/2 md:group-even:translate-x-1/2 z-10">
          <div class="w-2 h-2 bg-white rounded-full"></div>
        </div>
        <!-- Card -->
        <div class="w-[calc(100%-4rem)] md:w-[calc(50%-3rem)] p-6 rounded-2xl glass shadow-sm group-hover:shadow-md transition-all duration-300 group-hover:-translate-y-1">
          <div class="flex items-center justify-between mb-2">
            <span class="font-bold text-[#49b1f5] text-sm">{{ timeline[0]?.date || '—' }}</span>
          </div>
          <h3 class="font-bold text-lg mb-1 text-gray-800">{{ timeline[0]?.title || '暂无文章' }}</h3>
          <p class="text-gray-500 text-sm line-clamp-2">{{ timeline[0]?.excerpt || '暂无摘要' }}</p>
        </div>
      </div>
      
      <!-- Timeline Item -->
      <div class="relative flex items-center justify-between md:justify-normal md:odd:flex-row-reverse group">
        <!-- Icon -->
        <div class="flex items-center justify-center w-10 h-10 rounded-full border-4 border-white bg-gray-200 text-white shadow shrink-0 md:order-1 md:group-odd:-translate-x-1/2 md:group-even:translate-x-1/2 z-10 group-hover:bg-[#49b1f5] transition-colors duration-300">
          <div class="w-2 h-2 bg-white rounded-full"></div>
        </div>
        <!-- Card -->
        <div class="w-[calc(100%-4rem)] md:w-[calc(50%-3rem)] p-6 rounded-2xl glass shadow-sm group-hover:shadow-md transition-all duration-300 group-hover:-translate-y-1">
          <div class="flex items-center justify-between mb-2">
            <span class="font-bold text-gray-400 text-sm group-hover:text-[#49b1f5] transition-colors">{{ timeline[1]?.date || '—' }}</span>
          </div>
          <h3 class="font-bold text-lg mb-1 text-gray-800">{{ timeline[1]?.title || '暂无文章' }}</h3>
          <p class="text-gray-500 text-sm line-clamp-2">{{ timeline[1]?.excerpt || '暂无摘要' }}</p>
        </div>
      </div>
      
      <!-- Timeline Item -->
      <div class="relative flex items-center justify-between md:justify-normal md:odd:flex-row-reverse group">
        <!-- Icon -->
        <div class="flex items-center justify-center w-10 h-10 rounded-full border-4 border-white bg-gray-200 text-white shadow shrink-0 md:order-1 md:group-odd:-translate-x-1/2 md:group-even:translate-x-1/2 z-10 group-hover:bg-[#49b1f5] transition-colors duration-300">
          <div class="w-2 h-2 bg-white rounded-full"></div>
        </div>
        <!-- Card -->
        <div class="w-[calc(100%-4rem)] md:w-[calc(50%-3rem)] p-6 rounded-2xl glass shadow-sm group-hover:shadow-md transition-all duration-300 group-hover:-translate-y-1">
          <div class="flex items-center justify-between mb-2">
            <span class="font-bold text-gray-400 text-sm group-hover:text-[#49b1f5] transition-colors">{{ timeline[2]?.date || '—' }}</span>
          </div>
          <h3 class="font-bold text-lg mb-1 text-gray-800">{{ timeline[2]?.title || '暂无文章' }}</h3>
          <p class="text-gray-500 text-sm line-clamp-2">{{ timeline[2]?.excerpt || '暂无摘要' }}</p>
        </div>
      </div>
    </div>
  </div>
</template>
