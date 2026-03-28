<script setup lang="ts">
import { Tag } from 'lucide-vue-next';

const tags = [
  { name: 'Java', count: 12, size: 'text-3xl', color: 'text-red-400' },
  { name: 'Spring', count: 8, size: 'text-2xl', color: 'text-green-500' },
  { name: 'MySQL', count: 15, size: 'text-4xl', color: 'text-blue-500' },
  { name: 'Docker', count: 5, size: 'text-xl', color: 'text-cyan-500' },
  { name: 'PostgreSQL', count: 3, size: 'text-lg', color: 'text-indigo-500' },
  { name: 'Hexo', count: 2, size: 'text-base', color: 'text-purple-500' },
  { name: '生活', count: 20, size: 'text-5xl', color: 'text-pink-400' },
  { name: 'DBA', count: 4, size: 'text-lg', color: 'text-yellow-600' },
  { name: 'SQL', count: 7, size: 'text-xl', color: 'text-orange-500' },
  { name: 'Cloud', count: 6, size: 'text-xl', color: 'text-sky-400' },
  { name: '感悟', count: 18, size: 'text-4xl', color: 'text-rose-400' },
  { name: 'Blog', count: 9, size: 'text-2xl', color: 'text-emerald-500' }
];
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
