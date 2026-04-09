<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { Tag } from 'lucide-vue-next';
import { getTags } from '@/api/blog';

const sizes = ['text-base', 'text-lg', 'text-xl', 'text-2xl', 'text-3xl', 'text-4xl'];
const colors = ['text-red-400', 'text-green-500', 'text-blue-500', 'text-cyan-500', 'text-indigo-500', 'text-purple-500', 'text-pink-400', 'text-yellow-600', 'text-orange-500', 'text-sky-400', 'text-rose-400', 'text-emerald-500'];

const tags = ref<Array<{ name: string; count: number; size: string; color: string }>>([]);

onMounted(async () => {
  try {
    const list = await getTags();
    tags.value = list.map((item, index) => ({
      name: item.name,
      count: Number(item.articleCount || 1),
      size: sizes[index % sizes.length],
      color: colors[index % colors.length],
    }));
  } catch {
    tags.value = [];
  }
});
</script>

<template>
  <div class="glass rounded-[3rem] p-8 md:p-14 hover:shadow-2xl hover:shadow-[#49b1f5]/20 transition-all duration-700 border border-white/60 text-center">
    <div class="flex items-center justify-center space-x-4 mb-10 pb-5 border-b border-gray-100/50">
      <Tag :size="32" class="text-[#49b1f5]" stroke-width="1.5" />
      <h1 class="font-serif font-bold text-3xl md:text-4xl">标签</h1>
    </div>
    
    <div class="text-gray-500 mb-12 font-serif italic">
      目前共计 {{ tags.length }} 个标签
    </div>
    
    <div class="flex flex-wrap justify-center items-center gap-6 md:gap-10 py-10">
      <a 
        v-for="tag in tags" 
        :key="tag.name"
        href="#"
        class="hover:scale-110 transition-transform duration-300 cursor-pointer font-bold hover:!text-[#49b1f5]"
        :class="[tag.size, tag.color]"
      >
        {{ tag.name }}
      </a>
    </div>
  </div>
</template>
