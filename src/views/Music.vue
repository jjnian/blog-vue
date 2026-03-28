<script setup lang="ts">
import { Music } from 'lucide-vue-next';
</script>

<template>
  <div class="glass rounded-[3rem] p-8 md:p-14 hover:shadow-2xl hover:shadow-[#49b1f5]/20 transition-all duration-700 border border-white/60 text-center">
    <div class="flex items-center justify-center space-x-4 mb-10 pb-5 border-b border-gray-100/50">
      <Music :size="32" class="text-[#49b1f5]" stroke-width="1.5" />
      <h1 class="font-serif font-bold text-3xl md:text-4xl">音乐</h1>
    </div>
    
    <div class="py-20 flex flex-col items-center justify-center text-gray-400">
      <Music :size="64" class="mb-6 opacity-20 animate-bounce" stroke-width="1" />
      <p class="text-xl font-serif italic">音乐播放器正在建设中...</p>
    </div>
  </div>
</template>
