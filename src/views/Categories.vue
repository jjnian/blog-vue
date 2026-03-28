<script setup lang="ts">
import { LayoutGrid } from 'lucide-vue-next';

const categories = [
  { name: '数据库', count: 2, color: 'bg-blue-100 text-blue-600' },
  { name: '后端', count: 2, color: 'bg-green-100 text-green-600' },
  { name: 'DevOps', count: 1, color: 'bg-purple-100 text-purple-600' },
  { name: '随笔', count: 2, color: 'bg-pink-100 text-pink-600' }
];
</script>

<template>
  <div class="glass rounded-[3rem] p-8 md:p-14 hover:shadow-2xl hover:shadow-[#49b1f5]/20 transition-all duration-700 border border-white/60 text-center">
    <div class="flex items-center justify-center space-x-4 mb-10 pb-5 border-b border-gray-100/50">
      <LayoutGrid :size="32" class="text-[#49b1f5]" stroke-width="1.5" />
      <h1 class="font-serif font-bold text-3xl md:text-4xl">分类</h1>
    </div>
    
    <div class="text-gray-500 mb-12 font-serif italic">
      目前共计 {{ categories.length }} 个分类
    </div>
    
    <div class="grid grid-cols-1 md:grid-cols-2 gap-6 py-10">
      <a 
        v-for="cat in categories" 
        :key="cat.name"
        href="#"
        class="flex items-center justify-between p-6 rounded-3xl glass hover:bg-white/90 hover:shadow-lg hover:-translate-y-2 transition-all duration-500 group border border-white/50"
      >
        <div class="flex items-center space-x-4">
          <div class="w-12 h-12 rounded-2xl flex items-center justify-center font-bold text-xl group-hover:scale-110 transition-transform" :class="cat.color">
            {{ cat.name.charAt(0) }}
          </div>
          <span class="font-bold text-xl text-gray-800 group-hover:text-[#49b1f5] transition-colors">{{ cat.name }}</span>
        </div>
        <span class="bg-gray-100 text-gray-500 px-4 py-1.5 rounded-full text-sm font-bold group-hover:bg-[#49b1f5] group-hover:text-white transition-colors">
          {{ cat.count }}
        </span>
      </a>
    </div>
  </div>
</template>
