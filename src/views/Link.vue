<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { Link as LinkIcon } from 'lucide-vue-next';
import { getLinks } from '@/api/blog';

const links = ref<Array<{ name: string; desc: string; url: string; avatar: string }>>([]);

onMounted(async () => {
  try {
    const list = await getLinks();
    links.value = list.map((item, index) => ({
      name: item.name,
      desc: item.description || '暂无描述',
      url: item.url,
      avatar: item.avatar || `https://picsum.photos/seed/link-${index}/100/100`,
    }));
  } catch {
    links.value = [];
  }
});
</script>

<template>
  <div class="glass rounded-[3rem] p-8 md:p-14 hover:shadow-2xl hover:shadow-[#49b1f5]/20 transition-all duration-700 border border-white/60 text-center">
    <div class="flex items-center justify-center space-x-4 mb-10 pb-5 border-b border-gray-100/50">
      <LinkIcon :size="32" class="text-[#49b1f5]" stroke-width="1.5" />
      <h1 class="font-serif font-bold text-3xl md:text-4xl">友情链接</h1>
    </div>
    
    <div class="text-gray-500 mb-12 font-serif italic">
      海内存知己，天涯若比邻。
    </div>
    
    <div class="grid grid-cols-1 md:grid-cols-2 gap-6 py-10">
      <a 
        v-for="link in links" 
        :key="link.name"
        :href="link.url"
        target="_blank"
        rel="noopener noreferrer"
        class="flex items-center p-6 rounded-3xl glass hover:bg-white/90 hover:shadow-lg hover:-translate-y-2 transition-all duration-500 group border border-white/50 text-left"
      >
        <img :src="link.avatar" :alt="link.name" class="w-16 h-16 rounded-full mr-6 group-hover:rotate-[360deg] transition-transform duration-1000 shadow-md" v-zoom />
        <div>
          <h3 class="font-bold text-xl text-gray-800 group-hover:text-[#49b1f5] transition-colors mb-2">{{ link.name }}</h3>
          <p class="text-sm text-gray-500 line-clamp-2">{{ link.desc }}</p>
        </div>
      </a>
    </div>
  </div>
</template>
