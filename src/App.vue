<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import {
  Home,
  Clock,
  Tag,
  LayoutGrid,
  Music,
  Film,
  Wrench,
  Link as LinkIcon,
  User,
  Search,
  Menu,
  X,
  ChevronDown,
  ChevronUp,
  ChevronRight,
  TreeDeciduous,
  Users,
  PenLine,
  MessageSquare,
  Settings,
  Shield,
  FileText,
} from 'lucide-vue-next';
import { getArchives, getArticles, getCategories, getTags, getPublicMenus, getUserMenus } from '@/api/blog';
import { useAuth } from '@/stores/auth';

const router = useRouter();
const auth = useAuth();

const isMenuOpen = ref(false);
const isScrolled = ref(false);
const isSearchOpen = ref(false);
const showBackToTop = ref(false);
const scrollProgress = ref(0);

const typedText = ref('');
const fullText = '纪念每一段珍贵的时光';
let typingIndex = 0;

const typeEffect = () => {
  if (typingIndex < fullText.length) {
    typedText.value += fullText.charAt(typingIndex);
    typingIndex += 1;
    setTimeout(typeEffect, 200);
  } else {
    setTimeout(() => {
      typedText.value = '';
      typingIndex = 0;
      typeEffect();
    }, 3000);
  }
};

const clickEffects = ref<{ id: number; x: number; y: number; text: string }[]>([]);
let clickId = 0;
const clickTexts = ['诗意', '远方', '热爱', '自由', '纯粹', '温柔', '明亮', '星辰', '大海', '山川', '岁月', '静好'];
const route = useRoute();

const handleGlobalClick = (e: MouseEvent) => {
  const id = clickId += 1;
  const text = clickTexts[id % clickTexts.length];
  clickEffects.value.push({ id, x: e.clientX, y: e.clientY, text });
  setTimeout(() => {
    clickEffects.value = clickEffects.value.filter((effect) => effect.id !== id);
  }, 1000);
};

type NavItem = {
  name: string;
  icon: any;
  link: string;
  subItems?: NavItem[];
};

const fallbackNavItems = [
  { name: '主页', icon: Home, link: '/' },
  { name: '时间线', icon: Clock, link: '/archives' },
  { name: '标签', icon: Tag, link: '/tags' },
  { name: '分类', icon: LayoutGrid, link: '/categories' },
  {
    name: '休闲',
    icon: Music,
    link: '#',
    subItems: [
      { name: '音乐', icon: Music, link: '/music' },
      { name: '电影', icon: Film, link: '/movies' },
    ],
  },
  { name: '许愿树', icon: TreeDeciduous, link: '/wishes' },
  { name: '朋友地图', icon: Users, link: '/friends' },
  { name: '工具', icon: Wrench, link: '/tools' },
  { name: '友链', icon: LinkIcon, link: '/link' },
  { name: '关于', icon: User, link: '/about' },
];

const navItems = ref<NavItem[]>(fallbackNavItems);

const iconMap: Record<string, any> = {
  Home,
  Clock,
  Tag,
  LayoutGrid,
  Music,
  Film,
  Wrench,
  Link: LinkIcon,
  User,
  Users,
  TreeDeciduous,
  MessageSquare,
  Settings,
  Shield,
  FileText,
  PenLine,
};

const normalizeMenuItem = (menu: { name: string; path?: string; icon?: string; visible?: boolean; children?: any[] }): NavItem | null => {
  if (menu.visible === false) return null;
  const link = menu.path || '#';
  if (link.startsWith('/admin')) return null;

  const children = (menu.children || [])
    .map((child) => normalizeMenuItem(child))
    .filter((child): child is NavItem => Boolean(child));

  return {
    name: menu.name,
    icon: iconMap[menu.icon || ''] || Home,
    link,
    subItems: children.length ? children : undefined,
  };
};

const loadNavMenus = async () => {
  if (!auth.isLoggedIn.value) {
    navItems.value = fallbackNavItems;
    return;
  }
  try {
    const menus = await getUserMenus();
    const mapped = menus
      .map((menu) => normalizeMenuItem(menu))
      .filter((menu): menu is NavItem => Boolean(menu));
    navItems.value = mapped.length ? mapped : fallbackNavItems;
  } catch {
    navItems.value = fallbackNavItems;
  }
};

watch(() => auth.token.value, () => {
  loadNavMenus();
});

watch(() => route.path, () => {
  loadNavMenus();
});

const posts = ref<Array<{ title: string; date: string }>>([]);
const sidebarArchives = ref<Array<{ name: string; count: number }>>([]);
const sidebarCategories = ref<Array<{ name: string; count: number }>>([]);
const sidebarTags = ref<string[]>([]);
const articleTotal = ref(0);
const tagTotal = ref(0);
const categoryTotal = ref(0);

const formatDate = (value?: string) => {
  if (!value) return '-';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;
  return date.toLocaleDateString('zh-CN');
};

const loadSidebarData = async () => {
  try {
    const [articlePage, tagList, categoryList, archives] = await Promise.all([
      getArticles({ page: 1, size: 10, status: 'PUBLISHED' }),
      getTags(),
      getCategories(),
      getArchives(),
    ]);

    articleTotal.value = Number(articlePage.total || 0);
    tagTotal.value = tagList.length;
    categoryTotal.value = categoryList.length;

    posts.value = (articlePage.records || []).map((post) => ({
      title: post.title,
      date: formatDate(post.publishedAt || post.createdAt),
    }));

    sidebarArchives.value = (archives || []).slice(0, 6).map((item) => ({
      name: `${item[0]}-${String(item[1]).padStart(2, '0')}`,
      count: 1,
    }));

    sidebarCategories.value = categoryList.map((cat) => ({
      name: cat.name,
      count: Number(cat.articleCount || 0),
    }));

    sidebarTags.value = tagList.slice(0, 24).map((tag) => tag.name);
  } catch {
    posts.value = [];
    sidebarArchives.value = [];
    sidebarCategories.value = [];
    sidebarTags.value = [];
  }
};

const scrollToTop = () => {
  window.scrollTo({ top: 0, behavior: 'smooth' });
};

const scrollToContent = () => {
  const content = document.getElementById('content');
  if (content) {
    content.scrollIntoView({ behavior: 'smooth' });
  }
};

const isAdminRoute = computed(() => route.path.startsWith('/admin'));
const isFullscreenRoute = computed(() =>
  route.path.startsWith('/admin') || route.path === '/login'
);
const isWriteRoute = computed(() => route.path === '/write' || route.path === '/profile');

onMounted(() => {
  typeEffect();
  loadSidebarData();
  loadNavMenus();

  window.addEventListener('scroll', () => {
    isScrolled.value = window.scrollY > 50;
    showBackToTop.value = window.scrollY > 500;

    const winScroll = document.body.scrollTop || document.documentElement.scrollTop;
    const height = document.documentElement.scrollHeight - document.documentElement.clientHeight;
    scrollProgress.value = (winScroll / height) * 100;
  });

  window.addEventListener('click', handleGlobalClick);

  const observer = new IntersectionObserver((entries) => {
    entries.forEach((entry) => {
      if (entry.isIntersecting) {
        entry.target.classList.add('visible');
      }
    });
  }, { threshold: 0.1 });

  document.querySelectorAll('.reveal-animation').forEach((el) => {
    observer.observe(el);
  });
});

onUnmounted(() => {
  window.removeEventListener('click', handleGlobalClick);
});
</script>
<template>
  <div class="min-h-screen font-sans selection:bg-[#49b1f5] selection:text-white relative bg-[#f8fafc]">
    <!-- Scroll Progress Bar -->
    <div class="fixed top-0 left-0 h-1 bg-[#49b1f5] z-[100] transition-all duration-300" :style="{ width: scrollProgress + '%' }"></div>

    <!-- Decorative Background Blobs -->
    <div class="fixed top-[-10%] left-[-10%] w-[50%] h-[50%] bg-blue-100/40 rounded-full blur-[120px] animate-pulse-soft pointer-events-none"></div>
    <div class="fixed bottom-[-10%] right-[-10%] w-[50%] h-[50%] bg-indigo-100/40 rounded-full blur-[120px] animate-pulse-soft pointer-events-none" style="animation-delay: -3s"></div>

    <!-- Click Effects -->
    <TransitionGroup name="click-effect">
      <div 
        v-for="effect in clickEffects" 
        :key="effect.id"
        class="fixed z-[9999] pointer-events-none select-none text-sm font-serif italic text-[#49b1f5]"
        :style="{ left: effect.x + 'px', top: effect.y + 'px' }"
      >
        {{ effect.text }}
      </div>
    </TransitionGroup>

    <!-- Admin / Fullscreen routes bypass blog layout -->
    <template v-if="isAdminRoute">
      <router-view />
    </template>

    <template v-else>

    <!-- Navigation -->
    <nav
      :class="[
        'fixed top-0 z-50 w-full transition-all duration-700 px-4 md:px-12 py-4 md:py-5 flex items-center justify-between',
        isScrolled ? 'glass shadow-sm text-[#2c3e50]' : 'bg-transparent text-white drop-shadow-lg'
      ]"
    >
      <div class="text-2xl md:text-3xl font-serif font-bold tracking-widest cursor-pointer hover:scale-105 transition-transform duration-500">
        绾康
      </div>

      <!-- Desktop Nav -->
      <div class="hidden lg:flex items-center space-x-6">
        <div v-for="item in navItems" :key="item.name" class="relative group">
          <router-link :to="item.link" class="flex items-center space-x-2 hover:text-[#49b1f5] transition-all duration-500 py-2 relative overflow-hidden font-medium text-sm tracking-widest uppercase">
            <span>{{ item.name }}</span>
            <ChevronDown v-if="item.subItems" :size="14" class="group-hover:rotate-180 transition-transform duration-500" />
            <span class="absolute bottom-0 left-0 w-0 h-0.5 bg-[#49b1f5] group-hover:w-full transition-all duration-500 rounded-full"></span>
          </router-link>
          
          <!-- Dropdown -->
          <div v-if="item.subItems" class="absolute top-full left-0 w-44 glass shadow-2xl rounded-[2rem] opacity-0 invisible group-hover:opacity-100 group-hover:visible transition-all duration-500 transform translate-y-4 group-hover:translate-y-0 p-3 border border-white/30">
            <router-link 
              v-for="sub in item.subItems" 
              :key="sub.name" 
              :to="sub.link"
              class="flex items-center space-x-3 px-5 py-3 text-sm text-[#2c3e50] hover:bg-[#49b1f5] hover:text-white transition-all duration-500 rounded-2xl"
            >
              <span>{{ sub.name }}</span>
            </router-link>
          </div>
        </div>
        <button @click="isSearchOpen = true" class="hover:text-[#49b1f5] hover:scale-110 transition-all duration-500 p-2">
          <Search :size="22" stroke-width="1.5" />
        </button>

        <!-- 我 按钮：未登录→/login，已登录→/profile -->
        <router-link
          :to="auth.isLoggedIn.value ? '/profile' : '/login'"
          class="flex items-center hover:text-[#49b1f5] transition-all duration-300 hover:scale-110"
          title="我"
        >
          <div v-if="auth.isLoggedIn.value" class="w-9 h-9 rounded-full bg-gradient-to-br from-[#49b1f5] to-purple-400 flex items-center justify-center text-white text-sm font-bold shadow-md hover:shadow-[#49b1f5]/40 hover:shadow-lg transition-all">
            {{ (auth.user.value?.nickname || auth.user.value?.username || 'U').charAt(0).toUpperCase() }}
          </div>
          <div v-else class="flex items-center gap-1.5 px-3 py-1.5 rounded-full border border-current/30 hover:border-[#49b1f5]/50 text-sm font-medium tracking-wider uppercase transition-all">
            <User :size="16" stroke-width="1.5" />
            <span class="text-xs">我</span>
          </div>
        </router-link>
      </div>

      <!-- Mobile Menu Toggle -->
      <button @click="isMenuOpen = true" class="lg:hidden hover:scale-110 transition-transform p-2">
        <Menu :size="28" stroke-width="1.5" />
      </button>
    </nav>

    <!-- Search Modal -->
    <Transition name="fade">
      <div v-if="isSearchOpen" class="fixed inset-0 z-[100] flex items-start justify-center pt-20 px-4 bg-black/40 backdrop-blur-md" @click.self="isSearchOpen = false">
        <div class="bg-white/90 backdrop-blur-xl w-full max-w-2xl rounded-[2.5rem] shadow-2xl overflow-hidden transform transition-all duration-500 scale-100 animate-scale-up border border-white/30">
          <div class="p-10 border-b border-gray-100/50 flex items-center justify-between">
            <div class="flex items-center space-x-5 flex-1">
              <Search class="text-[#49b1f5]" :size="28" stroke-width="1.5" />
              <input type="text" placeholder="探索你的灵感..." class="w-full outline-none text-2xl font-serif italic tracking-wide bg-transparent" autofocus />
            </div>
            <button @click="isSearchOpen = false" class="text-gray-400 hover:text-red-400 transition-all duration-300 hover:rotate-90">
              <X :size="32" stroke-width="1.5" />
            </button>
          </div>
          <div class="p-20 text-center text-gray-400 font-serif italic text-xl">
            "在文字的海洋里，寻找那一抹诗意..."
          </div>
        </div>
      </div>
    </Transition>

    <!-- Mobile Sidebar -->
    <Transition name="slide">
      <div v-if="isMenuOpen" class="fixed inset-0 z-[60] bg-black/30 backdrop-blur-sm lg:hidden" @click="isMenuOpen = false"></div>
    </Transition>
    <Transition name="sidebar">
      <div v-if="isMenuOpen" class="fixed top-0 right-0 z-[70] h-full w-[85vw] max-w-[300px] shadow-2xl lg:hidden overflow-hidden">
        <!-- Background Image -->
        <div class="absolute inset-0 bg-cover bg-center bg-no-repeat" style="background-image: url('https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?auto=format&fit=crop&q=80&w=800')"></div>
        <!-- Glass Overlay -->
        <div class="absolute inset-0 bg-white/70 backdrop-blur-xl"></div>
        
        <div class="relative z-10 p-6 flex flex-col h-full overflow-y-auto">
          <div class="flex justify-between items-center mb-6">
            <div class="flex items-center space-x-3">
              <img 
                src="https://picsum.photos/seed/avatar/100/100" 
                alt="Avatar" 
                class="w-10 h-10 rounded-full border-2 border-white/80 shadow-sm"
                referrerPolicy="no-referrer"
                v-zoom
              />
              <div>
                <h2 class="text-lg font-serif font-bold text-[#2c3e50]">绾康</h2>
                <p class="text-[10px] text-gray-500 font-light uppercase tracking-wider">纪念每一段珍贵的时光</p>
              </div>
            </div>
            <button @click="isMenuOpen = false" class="text-[#2c3e50] hover:rotate-90 transition-transform duration-500 p-2">
              <X :size="24" stroke-width="1.5" />
            </button>
          </div>

          <div class="flex-1 space-y-1">
            <div v-for="item in navItems" :key="item.name">
              <router-link :to="item.link" @click="isMenuOpen = false" class="flex items-center space-x-4 text-[#2c3e50] hover:text-[#49b1f5] transition-all duration-500 p-3 rounded-xl hover:bg-white/60">
                <span class="font-medium tracking-widest uppercase text-xs">{{ item.name }}</span>
              </router-link>
              <div v-if="item.subItems" class="ml-12 mt-0.5 space-y-1">
                <router-link v-for="sub in item.subItems" :key="sub.name" :to="sub.link" @click="isMenuOpen = false" class="flex items-center space-x-3 text-[11px] text-gray-500 hover:text-[#49b1f5] transition-colors p-2">
                  <span>{{ sub.name }}</span>
                </router-link>
              </div>
            </div>

            <!-- Auth links for mobile -->
            <div class="pt-3 mt-3 border-t border-gray-200/50 space-y-1">
              <!-- 我 → profile or login -->
              <router-link
                :to="auth.isLoggedIn.value ? '/profile' : '/login'"
                @click="isMenuOpen = false"
                class="flex items-center space-x-4 text-[#2c3e50] hover:text-[#49b1f5] p-3 rounded-xl hover:bg-white/60 transition-all"
              >
                <User :size="16" stroke-width="1.5" />
                <span class="font-medium tracking-widest uppercase text-xs">我</span>
              </router-link>
            </div>
          </div>
        </div>
      </div>
    </Transition>

    <!-- Hero Section (hidden for write/login) -->
    <header v-if="!isWriteRoute && route.path !== '/login'" :class="['relative w-full flex items-center justify-center overflow-hidden transition-all duration-700', route.path === '/' ? 'h-screen' : 'h-[40vh]']">
      <div class="absolute inset-0 bg-cover bg-center bg-no-repeat transition-transform duration-[3000ms] hover:scale-110" style="background-image: url('https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?auto=format&fit=crop&q=80&w=1920')"></div>
      <div class="absolute inset-0 bg-gradient-to-b from-black/50 via-black/20 to-transparent"></div>
      
      <div class="relative z-10 text-center text-white px-6">
        <h1 v-if="route.path === '/'" class="text-5xl md:text-9xl font-serif font-bold mb-6 md:mb-10 animate-hero-title tracking-tighter drop-shadow-[0_0_30px_rgba(255,255,255,0.5)]">绾康</h1>
        <h1 v-else class="text-4xl md:text-6xl font-serif font-bold mb-4 animate-hero-title tracking-widest drop-shadow-[0_0_20px_rgba(255,255,255,0.5)]">{{ route.name }}</h1>
        
        <div class="h-px w-20 md:w-32 bg-white/40 mx-auto mb-6 md:mb-10 animate-hero-subtitle"></div>
        
        <p v-if="route.path === '/'" class="text-lg md:text-4xl font-serif italic tracking-[0.3em] md:tracking-[0.6em] opacity-90 animate-hero-subtitle min-h-[1.5em]">
          {{ typedText }}<span class="animate-cursor ml-1">|</span>
        </p>
      </div>

      <div v-if="route.path === '/'" class="absolute bottom-6 md:bottom-10 left-1/2 -translate-x-1/2 z-20 animate-bounce cursor-pointer text-white/80 hover:text-[#49b1f5] transition-colors p-4 md:p-6" @click="scrollToContent">
        <ChevronDown :size="32" class="md:w-12 md:h-12" stroke-width="1.5" />
      </div>

      <!-- Wave Divider -->
      <div class="absolute bottom-0 left-0 w-full overflow-hidden leading-[0] z-10 translate-y-[1px]">
        <svg class="relative block w-full h-[60px] md:h-[120px]" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" viewBox="0 24 150 28" preserveAspectRatio="none" shape-rendering="auto">
          <defs>
            <path id="gentle-wave" d="M-160 44c30 0 58-18 88-18s 58 18 88 18 58-18 88-18 58 18 88 18 v44h-352z" />
          </defs>
          <g class="parallax">
            <use xlink:href="#gentle-wave" x="48" y="0" fill="rgba(248, 250, 252, 0.3)" />
            <use xlink:href="#gentle-wave" x="48" y="3" fill="rgba(248, 250, 252, 0.5)" />
            <use xlink:href="#gentle-wave" x="48" y="5" fill="rgba(248, 250, 252, 0.7)" />
            <use xlink:href="#gentle-wave" x="48" y="7" fill="#f8fafc" />
          </g>
        </svg>
      </div>
    </header>

    <!-- Main Content -->
    <main
      v-if="isWriteRoute || route.path === '/login'"
      class="pt-16"
    >
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>

    <main v-else id="content" :class="[
      route.path === '/messagewall' || route.path === '/friends'
        ? 'w-full py-0'
        : 'mx-auto py-12 md:py-24',
      route.path === '/messagewall' || route.path === '/friends'
        ? 'max-w-none px-0'
        : (route.path === '/wishes'
          ? 'max-w-[1400px] px-4 md:px-8'
          : 'max-w-7xl px-4 md:px-8 grid grid-cols-1 lg:grid-cols-4 gap-6 md:gap-10')
    ]">
      <!-- Sidebar (left column) -->
      <aside v-if="route.path !== '/wishes' && route.path !== '/messagewall' && route.path !== '/friends'" class="space-y-4 lg:order-first">

        <!-- Profile Card -->
        <div class="bg-white rounded-2xl border border-gray-100 shadow-sm overflow-hidden text-center">
          <div class="h-16 bg-gradient-to-r from-cyan-100/70 to-blue-100/70 relative">
            <div class="absolute -bottom-9 left-1/2 -translate-x-1/2">
              <img
                src="https://picsum.photos/seed/avatar/150/150"
                alt="Avatar"
                class="w-[4.5rem] h-[4.5rem] rounded-full border-4 border-white shadow-md"
                referrerPolicy="no-referrer"
                v-zoom
              />
            </div>
          </div>
          <div class="pt-12 pb-5 px-5">
            <h3 class="font-bold text-gray-800 text-base tracking-widest mb-0.5">绾康</h3>
            <p class="text-[11px] text-gray-400 mb-4">纪念每一段珍贵的时光</p>
            <div class="grid grid-cols-3 mb-4 py-3 border-t border-b border-gray-50">
              <div class="text-center hover:text-[#49b1f5] transition-colors cursor-pointer">
                <div class="font-bold text-gray-800 text-base">{{ articleTotal }}</div>
                <div class="text-[10px] text-gray-400 mt-0.5">文章</div>
              </div>
              <div class="text-center border-x border-gray-50 hover:text-[#49b1f5] transition-colors cursor-pointer">
                <div class="font-bold text-gray-800 text-base">{{ categoryTotal }}</div>
                <div class="text-[10px] text-gray-400 mt-0.5">分类</div>
              </div>
              <div class="text-center hover:text-[#49b1f5] transition-colors cursor-pointer">
                <div class="font-bold text-gray-800 text-base">{{ tagTotal }}</div>
                <div class="text-[10px] text-gray-400 mt-0.5">标签</div>
              </div>
            </div>
            <router-link to="/link" class="block w-full py-1.5 border border-[#49b1f5] text-[#49b1f5] rounded-lg text-sm font-medium hover:bg-[#49b1f5] hover:text-white transition-all duration-200 text-center">
              ☆ 友站
            </router-link>
          </div>
        </div>

        <!-- Search -->
        <div class="bg-white rounded-2xl border border-gray-100 shadow-sm p-4">
          <div class="flex items-center gap-2 text-gray-500 mb-2.5">
            <Search :size="13" stroke-width="2" />
            <span class="text-xs font-medium">搜索</span>
          </div>
          <input
            type="text"
            placeholder="搜索文章..."
            class="w-full bg-gray-50 border border-gray-100 rounded-lg px-3 py-2 text-sm text-gray-600 placeholder-gray-300 outline-none focus:border-[#49b1f5] focus:bg-white transition-all duration-200"
          />
        </div>

        <!-- Recent Posts -->
        <div class="bg-white rounded-2xl border border-gray-100 shadow-sm p-4">
          <div class="flex items-center gap-2 mb-3 pb-2 border-b border-gray-50">
            <Clock :size="13" class="text-[#49b1f5]" stroke-width="2" />
            <h3 class="font-bold text-gray-700 text-sm">最新文章</h3>
          </div>
          <div class="space-y-3">
            <div v-for="post in posts.slice(0, 5)" :key="post.title" class="flex items-center gap-3 group cursor-pointer">
              <div class="w-11 h-11 rounded-lg overflow-hidden flex-shrink-0">
                <img
                  :src="`https://picsum.photos/seed/${post.title}/80/80`"
                  class="w-full h-full object-cover group-hover:scale-110 transition-transform duration-300"
                  referrerPolicy="no-referrer"
                />
              </div>
              <div class="flex-1 min-w-0">
                <div class="text-xs font-medium text-gray-700 line-clamp-1 group-hover:text-[#49b1f5] transition-colors">{{ post.title }}</div>
                <div class="text-[10px] text-gray-400 mt-0.5">{{ post.date }}</div>
              </div>
            </div>
          </div>
        </div>

        <!-- Categories -->
        <div class="bg-white rounded-2xl border border-gray-100 shadow-sm p-4">
          <div class="flex items-center gap-2 mb-3 pb-2 border-b border-gray-50">
            <LayoutGrid :size="13" class="text-[#49b1f5]" stroke-width="2" />
            <h3 class="font-bold text-gray-700 text-sm">分类</h3>
          </div>
          <div class="space-y-1">
            <div v-for="cat in sidebarCategories" :key="cat.name" class="flex justify-between items-center px-2 py-1.5 rounded-lg hover:bg-gray-50 transition-colors cursor-pointer group">
              <span class="text-xs text-gray-600 group-hover:text-[#49b1f5] transition-colors">{{ cat.name }}</span>
              <span class="text-[10px] bg-cyan-50 text-cyan-600 px-2 py-0.5 rounded-full font-medium">{{ cat.count }}</span>
            </div>
          </div>
        </div>

        <!-- Tags -->
        <div class="bg-white rounded-2xl border border-gray-100 shadow-sm p-4">
          <div class="flex items-center gap-2 mb-3 pb-2 border-b border-gray-50">
            <Tag :size="13" class="text-[#49b1f5]" stroke-width="2" />
            <h3 class="font-bold text-gray-700 text-sm">标签</h3>
          </div>
          <div class="flex flex-wrap gap-1.5">
            <span v-for="tag in sidebarTags" :key="tag" class="text-[10px] px-2.5 py-1 bg-gray-50 border border-gray-100 rounded-full text-gray-500 hover:bg-[#49b1f5] hover:text-white hover:border-[#49b1f5] transition-all duration-200 cursor-pointer">
              {{ tag }}
            </span>
          </div>
        </div>
      </aside>

      <!-- Router View (right 3 columns) -->
      <div :class="route.path === '/wishes' || route.path === '/messagewall' || route.path === '/friends' ? '' : 'lg:col-span-3'">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </div>
    </main>

    <!-- Write FAB (logged-in only) -->
    <Transition name="fade">
      <router-link
        v-if="auth.isLoggedIn.value && !isWriteRoute && route.path !== '/login'"
        to="/write"
        :class="[
          'fixed z-[99] w-12 h-12 md:w-14 md:h-14 bg-gradient-to-br from-[#49b1f5] to-[#3a9de8] text-white rounded-xl md:rounded-2xl shadow-lg shadow-[#49b1f5]/30 flex items-center justify-center hover:scale-110 hover:shadow-xl hover:shadow-[#49b1f5]/40 active:scale-95 transition-all duration-300 group',
          showBackToTop ? 'bottom-24 md:bottom-32 right-6 md:right-12' : 'bottom-6 md:bottom-12 right-6 md:right-12'
        ]"
        title="写文章"
      >
        <PenLine :size="20" class="md:w-6 md:h-6" stroke-width="2" />
        <span class="absolute right-full mr-3 bg-[#2c3e50]/80 backdrop-blur-sm text-white text-xs font-medium px-3 py-1.5 rounded-lg shadow opacity-0 group-hover:opacity-100 transition-opacity duration-200 whitespace-nowrap pointer-events-none">写文章</span>
      </router-link>
    </Transition>

    <!-- Back to Top -->
    <Transition name="fade">
      <button
        v-if="showBackToTop"
        @click="scrollToTop"
        class="fixed bottom-6 md:bottom-12 right-6 md:right-12 z-[100] w-12 h-12 md:w-16 md:h-16 glass text-[#49b1f5] rounded-xl md:rounded-[1.5rem] shadow-2xl flex items-center justify-center hover:bg-[#49b1f5] hover:text-white transition-all duration-500 hover:-translate-y-2 md:hover:-translate-y-4 active:scale-90 group border border-white/60"
      >
        <ChevronUp :size="24" class="md:w-8 md:h-8 group-hover:animate-bounce" stroke-width="2" />
      </button>
    </Transition>

    </template>
  </div>
</template>

<style>
/* Global Click Effect Animation */
.click-effect-enter-active {
  animation: click-float 1.5s cubic-bezier(0.23, 1, 0.32, 1) forwards;
}

@keyframes click-float {
  0% { opacity: 1; transform: translate(-50%, -50%) scale(0.8); }
  50% { opacity: 0.8; transform: translate(-50%, -150%) scale(1.3); }
  100% { opacity: 0; transform: translate(-50%, -250%) scale(1.8); }
}

/* Hero Animations */
@keyframes hero-title-slide {
  0% { opacity: 0; transform: translateY(-80px) scale(0.9); letter-spacing: -0.1em; }
  100% { opacity: 1; transform: translateY(0) scale(1); letter-spacing: -0.05em; }
}

@keyframes hero-subtitle-fade {
  0% { opacity: 0; letter-spacing: 1.5em; transform: translateY(40px); }
  100% { opacity: 0.9; letter-spacing: 0.6em; transform: translateY(0); }
}

.animate-hero-title {
  animation: hero-title-slide 2.2s cubic-bezier(0.23, 1, 0.32, 1) forwards;
}

.animate-hero-subtitle {
  animation: hero-subtitle-fade 2.5s cubic-bezier(0.23, 1, 0.32, 1) 1s forwards;
  opacity: 0;
}

/* Search Modal Animation */
@keyframes scale-up {
  from { opacity: 0; transform: scale(0.9) translateY(-40px); }
  to { opacity: 1; transform: scale(1) translateY(0); }
}

.animate-scale-up {
  animation: scale-up 0.6s cubic-bezier(0.34, 1.56, 0.64, 1) forwards;
}

/* Transitions */
.fade-enter-active, .fade-leave-active {
  transition: opacity 0.8s ease;
}
.fade-enter-from, .fade-leave-to {
  opacity: 0;
}

.slide-enter-active, .slide-leave-active {
  transition: opacity 0.8s ease;
}
.slide-enter-from, .slide-leave-to {
  opacity: 0;
}

.sidebar-enter-active, .sidebar-leave-active {
  transition: transform 0.8s cubic-bezier(0.23, 1, 0.32, 1);
}
.sidebar-enter-from, .sidebar-leave-to {
  transform: translateX(100%);
}

/* Reveal Animation on Scroll */
.reveal-animation {
  opacity: 0;
  transform: translateY(60px);
  animation: reveal 1.5s cubic-bezier(0.23, 1, 0.32, 1) forwards;
}

@keyframes reveal {
  to { opacity: 1; transform: translateY(0); }
}

.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.line-clamp-1 {
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* Smooth Scrolling */
html {
  scroll-behavior: smooth;
}
</style>




