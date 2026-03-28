<script setup lang="ts">
import { Clock } from 'lucide-vue-next';
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
            <span class="font-bold text-[#49b1f5] text-sm">2024-10-13</span>
          </div>
          <h3 class="font-bold text-lg mb-1 text-gray-800">MySQL 8.0 深度安装指南</h3>
          <p class="text-gray-500 text-sm line-clamp-2">探索 MySQL 8.0 的安装艺术，从 environment 配置到性能优化，为你的数据之旅奠定坚实基础。</p>
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
            <span class="font-bold text-gray-400 text-sm group-hover:text-[#49b1f5] transition-colors">2023-07-16</span>
          </div>
          <h3 class="font-bold text-lg mb-1 text-gray-800">Spring AOP：切面编程的魅力</h3>
          <p class="text-gray-500 text-sm line-clamp-2">深入理解 Spring AOP 的核心哲学，让你的代码在解耦与复用之间找到完美的平衡。</p>
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
            <span class="font-bold text-gray-400 text-sm group-hover:text-[#49b1f5] transition-colors">2023-06-24</span>
          </div>
          <h3 class="font-bold text-lg mb-1 text-gray-800">Java 虚拟机：内存与性能的交响乐</h3>
          <p class="text-gray-500 text-sm line-clamp-2">揭开 JVM 的神秘面纱，探索内存模型与垃圾回收的律动，让你的 Java 应用起舞。</p>
        </div>
      </div>
    </div>
  </div>
</template>
