<script setup lang="ts">
import { User, Mail, Github, Twitter, Code, Palette, Coffee, BookOpen, Briefcase, GraduationCap, Star, Music } from 'lucide-vue-next';

const skills = [
  { name: 'Vue.js / React', level: 90, color: 'bg-emerald-400' },
  { name: 'TypeScript / JavaScript', level: 85, color: 'bg-blue-400' },
  { name: 'Node.js / Express', level: 80, color: 'bg-green-500' },
  { name: 'UI / UX Design', level: 75, color: 'bg-purple-400' },
  { name: 'Three.js / WebGL', level: 60, color: 'bg-pink-400' }
];

const experiences = [
  { year: '2023 - 至今', title: '高级前端开发工程师', company: '星辰科技创新', desc: '负责核心业务系统的架构设计与前端性能优化，推动团队使用 TypeScript 和 Vue 3，主导了多个 3D 可视化大屏项目。', icon: Briefcase },
  { year: '2021 - 2023', title: '前端开发工程师', company: '云端网络互联', desc: '参与千万级日活产品的前端开发，负责复杂交互模块的落地，优化首屏加载时间提升 40%。', icon: Code },
  { year: '2017 - 2021', title: '软件工程 学士', company: '某知名大学', desc: '在校期间多次获得 ACM 编程竞赛奖项，并作为核心成员参与开源社区贡献。', icon: GraduationCap }
];
</script>

<template>
  <div class="space-y-10">
    <!-- Hero Profile -->
    <div class="glass rounded-[3rem] p-8 md:p-14 hover:shadow-2xl hover:shadow-[#49b1f5]/20 transition-all duration-700 border border-white/60 relative overflow-hidden reveal-animation">
      <div class="absolute top-0 right-0 w-64 h-64 bg-gradient-to-br from-[#49b1f5]/10 to-purple-400/10 rounded-full blur-3xl -translate-y-1/2 translate-x-1/2 pointer-events-none"></div>
      
      <div class="flex items-center justify-center space-x-4 mb-10 pb-5 border-b border-gray-100/50 relative z-10">
        <User :size="32" class="text-[#49b1f5]" stroke-width="1.5" />
        <h1 class="font-serif font-bold text-3xl md:text-4xl text-center">关于我</h1>
      </div>
      
      <div class="flex flex-col md:flex-row items-center md:items-start gap-10 py-6 relative z-10">
        <div class="w-48 h-48 rounded-full overflow-hidden border-4 border-white shadow-2xl shrink-0 hover:rotate-[360deg] transition-transform duration-1000 relative group">
          <img src="https://picsum.photos/seed/avatar/200/200" alt="Avatar" class="w-full h-full object-cover" v-zoom />
          <div class="absolute inset-0 bg-black/20 group-hover:bg-transparent transition-colors duration-500"></div>
        </div>
        
        <div class="flex-1 space-y-6 text-gray-600 leading-relaxed text-center md:text-left">
          <h2 class="text-3xl font-serif font-bold text-gray-800">你好，我是纪念 👋</h2>
          <p class="text-lg font-serif italic text-[#49b1f5]">"做一个干净的人，写优雅的代码"</p>
          
          <div class="space-y-4 text-sm md:text-base">
            <p>我是一名充满热情的全栈开发者与数字游民，喜欢探索新技术，热爱开源文化。在这里，我会分享我在编程、生活、阅读中的点滴感悟。</p>
            <p>我相信代码不仅是逻辑的堆砌，更是表达思想的艺术。在键盘的敲击声中，我寻找着属于自己的诗意与远方。除了写代码，我也热衷于摄影、独立音乐和科幻小说。</p>
          </div>
          
          <div class="pt-6 flex justify-center md:justify-start space-x-6">
            <a href="#" class="w-12 h-12 rounded-full glass flex items-center justify-center text-gray-500 hover:text-[#49b1f5] hover:bg-white transition-all duration-300 hover:scale-110 hover:-translate-y-1 shadow-sm">
              <Github :size="24" stroke-width="1.5" />
            </a>
            <a href="#" class="w-12 h-12 rounded-full glass flex items-center justify-center text-gray-500 hover:text-[#49b1f5] hover:bg-white transition-all duration-300 hover:scale-110 hover:-translate-y-1 shadow-sm">
              <Twitter :size="24" stroke-width="1.5" />
            </a>
            <a href="#" class="w-12 h-12 rounded-full glass flex items-center justify-center text-gray-500 hover:text-[#49b1f5] hover:bg-white transition-all duration-300 hover:scale-110 hover:-translate-y-1 shadow-sm">
              <Mail :size="24" stroke-width="1.5" />
            </a>
          </div>
        </div>
      </div>
    </div>
    
    <div class="grid grid-cols-1 md:grid-cols-2 gap-10">
      <!-- Skills -->
      <div class="glass rounded-[3rem] p-8 md:p-12 hover:shadow-2xl hover:shadow-[#49b1f5]/20 transition-all duration-700 border border-white/60 reveal-animation">
        <div class="flex items-center space-x-3 mb-8">
          <Code :size="28" class="text-[#49b1f5]" stroke-width="1.5" />
          <h3 class="font-serif font-bold text-2xl text-gray-800">技能栈</h3>
        </div>
        
        <div class="space-y-6">
          <div v-for="skill in skills" :key="skill.name" class="space-y-2">
            <div class="flex justify-between text-sm font-medium text-gray-600">
              <span>{{ skill.name }}</span>
              <span>{{ skill.level }}%</span>
            </div>
            <div class="h-2 w-full bg-gray-100 rounded-full overflow-hidden">
              <div :class="['h-full rounded-full transition-all duration-1000', skill.color]" :style="{ width: skill.level + '%' }"></div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- Hobbies -->
      <div class="glass rounded-[3rem] p-8 md:p-12 hover:shadow-2xl hover:shadow-[#49b1f5]/20 transition-all duration-700 border border-white/60 reveal-animation">
        <div class="flex items-center space-x-3 mb-8">
          <Star :size="28" class="text-[#49b1f5]" stroke-width="1.5" />
          <h3 class="font-serif font-bold text-2xl text-gray-800">生活碎片</h3>
        </div>
        
        <div class="grid grid-cols-2 gap-4">
          <div class="p-6 rounded-2xl bg-white/40 border border-white/50 flex flex-col items-center justify-center gap-3 hover:bg-white/60 transition-colors group">
            <Palette :size="32" class="text-pink-400 group-hover:scale-110 transition-transform" stroke-width="1.5" />
            <span class="font-medium text-gray-700">设计与绘画</span>
          </div>
          <div class="p-6 rounded-2xl bg-white/40 border border-white/50 flex flex-col items-center justify-center gap-3 hover:bg-white/60 transition-colors group">
            <Coffee :size="32" class="text-amber-500 group-hover:scale-110 transition-transform" stroke-width="1.5" />
            <span class="font-medium text-gray-700">手冲咖啡</span>
          </div>
          <div class="p-6 rounded-2xl bg-white/40 border border-white/50 flex flex-col items-center justify-center gap-3 hover:bg-white/60 transition-colors group">
            <BookOpen :size="32" class="text-emerald-500 group-hover:scale-110 transition-transform" stroke-width="1.5" />
            <span class="font-medium text-gray-700">科幻文学</span>
          </div>
          <div class="p-6 rounded-2xl bg-white/40 border border-white/50 flex flex-col items-center justify-center gap-3 hover:bg-white/60 transition-colors group">
            <Music :size="32" class="text-indigo-400 group-hover:scale-110 transition-transform" stroke-width="1.5" />
            <span class="font-medium text-gray-700">独立音乐</span>
          </div>
        </div>
      </div>
    </div>
    
    <!-- Experience Timeline -->
    <div class="glass rounded-[3rem] p-8 md:p-14 hover:shadow-2xl hover:shadow-[#49b1f5]/20 transition-all duration-700 border border-white/60 reveal-animation">
      <div class="flex items-center space-x-3 mb-12">
        <Briefcase :size="28" class="text-[#49b1f5]" stroke-width="1.5" />
        <h3 class="font-serif font-bold text-2xl text-gray-800">个人经历</h3>
      </div>
      
      <div class="relative border-l-2 border-blue-100 ml-4 md:ml-6 space-y-12 pb-4">
        <div v-for="(exp, index) in experiences" :key="index" class="relative pl-8 md:pl-12 group">
          <!-- Timeline Dot -->
          <div class="absolute -left-[11px] top-1 w-5 h-5 rounded-full bg-white border-4 border-[#49b1f5] group-hover:scale-125 group-hover:shadow-[0_0_10px_rgba(73,177,245,0.5)] transition-all duration-300"></div>
          
          <div class="flex flex-col md:flex-row md:items-center justify-between mb-2 gap-2">
            <h4 class="text-xl font-bold text-gray-800 flex items-center gap-2">
              <component :is="exp.icon" :size="20" class="text-[#49b1f5]" />
              {{ exp.title }}
            </h4>
            <span class="text-sm font-bold text-[#49b1f5] bg-blue-50 px-4 py-1 rounded-full w-fit">{{ exp.year }}</span>
          </div>
          <h5 class="text-base font-medium text-gray-500 mb-4">{{ exp.company }}</h5>
          <p class="text-gray-600 leading-relaxed bg-white/40 p-5 rounded-2xl border border-white/50 group-hover:bg-white/60 transition-colors">
            {{ exp.desc }}
          </p>
        </div>
      </div>
    </div>
  </div>
</template>
