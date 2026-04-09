<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import {
  Save, Send, Eye, EyeOff, Image, Tag as TagIcon, LayoutGrid,
  ChevronDown, X, ArrowLeft, FileText, Bold, Italic, Code, List, Link as LinkIcon
} from 'lucide-vue-next';
import { getCategories, getTags } from '@/api/blog';
import type { Category, Tag } from '@/api/blog';
import { createArticle, updateArticle } from '@/api/admin';
import { useAuth } from '@/stores/auth';

const router = useRouter();
const route = useRoute();
const auth = useAuth();

const loading = ref(false);
const saving = ref(false);
const error = ref('');
const success = ref('');
const showPreview = ref(false);
const showCategoryDropdown = ref(false);
const showTagDropdown = ref(false);

const categories = ref<Category[]>([]);
const tags = ref<Tag[]>([]);

const form = ref({
  title: '',
  excerpt: '',
  content: '',
  coverImage: '',
  categoryId: null as number | null,
  tagIds: [] as number[],
  status: 'PENDING_REVIEW' as 'DRAFT' | 'PENDING_REVIEW' | 'PUBLISHED',
  allowComment: true,
});

const selectedCategory = computed(() =>
  categories.value.find((c) => c.id === form.value.categoryId)
);

const selectedTags = computed(() =>
  tags.value.filter((t) => form.value.tagIds.includes(t.id))
);

const wordCount = computed(() => form.value.content.length);

const isAdmin = auth.isAdmin;

const statusOptions = computed(() => {
  const base = [
    { value: 'DRAFT', label: '保存草稿', icon: '📝' },
    { value: 'PENDING_REVIEW', label: '提交审核', icon: '⏳' },
  ];
  if (isAdmin.value) {
    base.push({ value: 'PUBLISHED', label: '直接发布', icon: '✅' });
  }
  return base;
});

onMounted(async () => {
  try {
    const [cats, tagList] = await Promise.all([getCategories(), getTags()]);
    categories.value = cats;
    tags.value = tagList;
  } catch {
    // ignore
  }
});

const toggleTag = (tagId: number) => {
  const idx = form.value.tagIds.indexOf(tagId);
  if (idx > -1) {
    form.value.tagIds.splice(idx, 1);
  } else {
    form.value.tagIds.push(tagId);
  }
};

const insertMarkdown = (prefix: string, suffix = '', placeholder = '文字') => {
  const textarea = document.getElementById('content-editor') as HTMLTextAreaElement;
  if (!textarea) return;
  const start = textarea.selectionStart;
  const end = textarea.selectionEnd;
  const selected = form.value.content.substring(start, end) || placeholder;
  const replacement = `${prefix}${selected}${suffix}`;
  form.value.content = form.value.content.substring(0, start) + replacement + form.value.content.substring(end);
  setTimeout(() => {
    textarea.focus();
    textarea.setSelectionRange(start + prefix.length, start + prefix.length + selected.length);
  }, 10);
};

const handleSave = async (submitStatus?: typeof form.value.status) => {
  if (!form.value.title.trim()) {
    error.value = '请填写文章标题';
    return;
  }
  if (!form.value.content.trim()) {
    error.value = '请填写文章内容';
    return;
  }

  saving.value = true;
  error.value = '';
  success.value = '';

  try {
    const payload = {
      title: form.value.title,
      excerpt: form.value.excerpt || form.value.content.slice(0, 150),
      content: form.value.content,
      coverImage: form.value.coverImage || undefined,
      categoryId: form.value.categoryId || undefined,
      tagIds: form.value.tagIds,
      status: submitStatus || form.value.status,
      allowComment: form.value.allowComment,
    };

    await createArticle(payload);

    const statusMsg: Record<string, string> = {
      DRAFT: '草稿已保存',
      PENDING_REVIEW: '文章已提交，等待管理员审核',
      PUBLISHED: '文章发布成功',
    };
    success.value = statusMsg[payload.status] || '保存成功';

    setTimeout(() => {
      router.push('/');
    }, 2000);
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : '保存失败，请重试';
  } finally {
    saving.value = false;
  }
};

// Simple markdown preview
const renderMarkdown = (text: string) => {
  return text
    .replace(/^### (.+)$/gm, '<h3 class="text-xl font-bold mt-4 mb-2">$1</h3>')
    .replace(/^## (.+)$/gm, '<h2 class="text-2xl font-bold mt-6 mb-3">$1</h2>')
    .replace(/^# (.+)$/gm, '<h1 class="text-3xl font-bold mt-8 mb-4">$1</h1>')
    .replace(/\*\*(.+?)\*\*/g, '<strong class="font-bold">$1</strong>')
    .replace(/\*(.+?)\*/g, '<em class="italic">$1</em>')
    .replace(/`(.+?)`/g, '<code class="bg-gray-100 px-1 py-0.5 rounded text-sm font-mono">$1</code>')
    .replace(/\[(.+?)\]\((.+?)\)/g, '<a href="$2" class="text-[#49b1f5] hover:underline">$1</a>')
    .replace(/^- (.+)$/gm, '<li class="ml-4 list-disc">$1</li>')
    .replace(/^(\d+)\. (.+)$/gm, '<li class="ml-4 list-decimal">$2</li>')
    .replace(/\n\n/g, '</p><p class="mb-4">')
    .replace(/\n/g, '<br/>');
};
</script>

<template>
  <div class="min-h-screen pb-20">
    <!-- Top Bar -->
    <div class="sticky top-0 z-40 bg-white/80 backdrop-blur-xl border-b border-gray-100 px-4 md:px-8 py-3 flex items-center justify-between gap-4">
      <div class="flex items-center gap-3">
        <button @click="router.back()" class="p-2 rounded-xl hover:bg-gray-100 text-gray-400 hover:text-[#49b1f5] transition-all">
          <ArrowLeft :size="18" />
        </button>
        <div class="flex items-center gap-2">
          <FileText :size="18" class="text-[#49b1f5]" />
          <span class="text-sm font-semibold text-[#2c3e50]">写文章</span>
        </div>
      </div>

      <div class="flex items-center gap-2">
        <button
          @click="showPreview = !showPreview"
          class="flex items-center gap-1.5 px-3 py-2 rounded-xl text-sm font-medium border border-gray-100 hover:border-[#49b1f5]/50 text-gray-500 hover:text-[#49b1f5] transition-all"
        >
          <component :is="showPreview ? EyeOff : Eye" :size="15" />
          <span class="hidden sm:inline">{{ showPreview ? '编辑' : '预览' }}</span>
        </button>

        <button
          @click="handleSave('DRAFT')"
          :disabled="saving"
          class="flex items-center gap-1.5 px-3 py-2 rounded-xl text-sm font-medium border border-gray-100 hover:border-[#49b1f5]/50 text-gray-500 hover:text-[#49b1f5] transition-all disabled:opacity-50"
        >
          <Save :size="15" />
          <span class="hidden sm:inline">存草稿</span>
        </button>

        <button
          @click="handleSave()"
          :disabled="saving"
          class="flex items-center gap-1.5 px-4 py-2 rounded-xl text-sm font-semibold bg-gradient-to-r from-[#49b1f5] to-[#3a9de8] text-white shadow-md shadow-[#49b1f5]/30 hover:shadow-lg hover:-translate-y-0.5 transition-all disabled:opacity-50 disabled:transform-none"
        >
          <Send :size="15" />
          <span>{{ isAdmin ? '发布' : '提交审核' }}</span>
        </button>
      </div>
    </div>

    <!-- Messages -->
    <div class="max-w-5xl mx-auto px-4 md:px-8 pt-4">
      <div v-if="error" class="mb-4 px-4 py-3 rounded-xl bg-red-50 border border-red-100 text-red-500 text-sm">{{ error }}</div>
      <div v-if="success" class="mb-4 px-4 py-3 rounded-xl bg-green-50 border border-green-100 text-green-600 text-sm font-medium">{{ success }}</div>
    </div>

    <div class="max-w-5xl mx-auto px-4 md:px-8 pt-2">
      <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <!-- Main Editor -->
        <div class="lg:col-span-2 space-y-4">
          <!-- Title -->
          <div class="bg-white/80 backdrop-blur-xl rounded-2xl border border-white/80 shadow-sm p-6">
            <input
              v-model="form.title"
              type="text"
              placeholder="输入文章标题..."
              class="w-full text-2xl md:text-3xl font-bold text-[#2c3e50] placeholder-gray-200 border-none outline-none bg-transparent leading-tight"
            />
            <input
              v-model="form.excerpt"
              type="text"
              placeholder="文章摘要（可选，留空则自动截取正文）"
              class="w-full mt-3 text-sm text-gray-400 placeholder-gray-200 border-none outline-none bg-transparent"
            />
          </div>

          <!-- Editor / Preview -->
          <div class="bg-white/80 backdrop-blur-xl rounded-2xl border border-white/80 shadow-sm overflow-hidden">
            <!-- Toolbar -->
            <div v-if="!showPreview" class="flex items-center gap-1 px-4 py-2 border-b border-gray-100 flex-wrap">
              <button @click="insertMarkdown('**', '**', '粗体')" class="p-2 rounded-lg hover:bg-gray-100 text-gray-400 hover:text-[#49b1f5] transition-all" title="粗体">
                <Bold :size="15" />
              </button>
              <button @click="insertMarkdown('*', '*', '斜体')" class="p-2 rounded-lg hover:bg-gray-100 text-gray-400 hover:text-[#49b1f5] transition-all" title="斜体">
                <Italic :size="15" />
              </button>
              <button @click="insertMarkdown('`', '`', '代码')" class="p-2 rounded-lg hover:bg-gray-100 text-gray-400 hover:text-[#49b1f5] transition-all" title="行内代码">
                <Code :size="15" />
              </button>
              <button @click="insertMarkdown('\n- ', '', '列表项')" class="p-2 rounded-lg hover:bg-gray-100 text-gray-400 hover:text-[#49b1f5] transition-all" title="列表">
                <List :size="15" />
              </button>
              <button @click="insertMarkdown('[', '](url)', '链接文字')" class="p-2 rounded-lg hover:bg-gray-100 text-gray-400 hover:text-[#49b1f5] transition-all" title="链接">
                <LinkIcon :size="15" />
              </button>
              <div class="ml-auto text-xs text-gray-300 font-mono">{{ wordCount }} 字</div>
            </div>

            <!-- Editor -->
            <div v-if="!showPreview" class="p-4">
              <textarea
                id="content-editor"
                v-model="form.content"
                placeholder="开始写作...（支持 Markdown 语法）

# 标题
## 二级标题

**粗体** *斜体* `代码`

- 列表项
- 另一项

[链接文字](url)"
                class="w-full min-h-[400px] text-sm text-[#2c3e50] placeholder-gray-200 border-none outline-none bg-transparent resize-none leading-relaxed font-mono"
              ></textarea>
            </div>

            <!-- Preview -->
            <div v-else class="p-6 min-h-[400px]">
              <div
                v-if="form.content"
                class="prose prose-sm max-w-none text-[#2c3e50] leading-relaxed"
                v-html="renderMarkdown(form.content)"
              ></div>
              <div v-else class="text-gray-300 text-sm text-center pt-20">暂无内容可预览</div>
            </div>
          </div>
        </div>

        <!-- Sidebar -->
        <div class="space-y-4">
          <!-- Status -->
          <div class="bg-white/80 backdrop-blur-xl rounded-2xl border border-white/80 shadow-sm p-5">
            <h3 class="text-xs font-bold tracking-widest uppercase text-gray-400 mb-3">发布设置</h3>
            <div class="space-y-2">
              <button
                v-for="opt in statusOptions"
                :key="opt.value"
                @click="form.status = opt.value as typeof form.status"
                class="w-full flex items-center gap-3 px-4 py-3 rounded-xl text-sm transition-all border"
                :class="form.status === opt.value
                  ? 'bg-[#49b1f5]/10 border-[#49b1f5]/30 text-[#49b1f5] font-medium'
                  : 'bg-gray-50/50 border-transparent text-gray-500 hover:bg-gray-100'"
              >
                <span>{{ opt.icon }}</span>
                <span>{{ opt.label }}</span>
              </button>
            </div>

            <div class="mt-4 flex items-center justify-between">
              <span class="text-sm text-gray-500">允许评论</span>
              <button
                @click="form.allowComment = !form.allowComment"
                class="w-11 h-6 rounded-full transition-all duration-300 relative"
                :class="form.allowComment ? 'bg-[#49b1f5]' : 'bg-gray-200'"
              >
                <span class="absolute top-0.5 w-5 h-5 bg-white rounded-full shadow transition-all duration-300"
                  :class="form.allowComment ? 'left-5.5' : 'left-0.5'"
                ></span>
              </button>
            </div>
          </div>

          <!-- Cover Image -->
          <div class="bg-white/80 backdrop-blur-xl rounded-2xl border border-white/80 shadow-sm p-5">
            <h3 class="text-xs font-bold tracking-widest uppercase text-gray-400 mb-3">封面图片</h3>
            <div v-if="form.coverImage" class="mb-3 rounded-xl overflow-hidden relative group">
              <img :src="form.coverImage" class="w-full h-32 object-cover" alt="封面" />
              <button @click="form.coverImage = ''" class="absolute top-2 right-2 p-1 rounded-lg bg-white/80 text-red-400 opacity-0 group-hover:opacity-100 transition-all">
                <X :size="14" />
              </button>
            </div>
            <div class="flex items-center gap-2">
              <Image :size="15" class="text-gray-300" />
              <input
                v-model="form.coverImage"
                type="url"
                placeholder="封面图片 URL"
                class="flex-1 text-sm text-[#2c3e50] placeholder-gray-200 border-b border-gray-100 focus:border-[#49b1f5] outline-none pb-1 bg-transparent transition-colors"
              />
            </div>
          </div>

          <!-- Category -->
          <div class="bg-white/80 backdrop-blur-xl rounded-2xl border border-white/80 shadow-sm p-5">
            <h3 class="text-xs font-bold tracking-widest uppercase text-gray-400 mb-3 flex items-center gap-2">
              <LayoutGrid :size="13" /> 分类
            </h3>
            <div class="relative">
              <button
                @click="showCategoryDropdown = !showCategoryDropdown"
                class="w-full flex items-center justify-between px-3 py-2.5 rounded-xl border border-gray-100 bg-gray-50/50 text-sm text-gray-500 hover:border-[#49b1f5]/30 transition-all"
              >
                <span :class="selectedCategory ? 'text-[#2c3e50]' : 'text-gray-300'">
                  {{ selectedCategory?.name || '选择分类' }}
                </span>
                <ChevronDown :size="14" class="text-gray-300" :class="{ 'rotate-180': showCategoryDropdown }" />
              </button>
              <div v-if="showCategoryDropdown" class="absolute top-full left-0 right-0 mt-1 bg-white rounded-xl border border-gray-100 shadow-lg z-20 max-h-48 overflow-y-auto">
                <button
                  @click="form.categoryId = null; showCategoryDropdown = false"
                  class="w-full text-left px-3 py-2.5 text-sm text-gray-300 hover:bg-gray-50 hover:text-[#49b1f5] transition-all"
                >不选择分类</button>
                <button
                  v-for="cat in categories"
                  :key="cat.id"
                  @click="form.categoryId = cat.id; showCategoryDropdown = false"
                  class="w-full text-left px-3 py-2.5 text-sm hover:bg-gray-50 transition-all flex items-center gap-2"
                  :class="form.categoryId === cat.id ? 'text-[#49b1f5] font-medium' : 'text-[#2c3e50]'"
                >
                  <span v-if="cat.color" class="w-2 h-2 rounded-full flex-shrink-0" :style="{ background: cat.color }"></span>
                  {{ cat.name }}
                </button>
              </div>
            </div>
          </div>

          <!-- Tags -->
          <div class="bg-white/80 backdrop-blur-xl rounded-2xl border border-white/80 shadow-sm p-5">
            <h3 class="text-xs font-bold tracking-widest uppercase text-gray-400 mb-3 flex items-center gap-2">
              <TagIcon :size="13" /> 标签
            </h3>
            <div class="flex flex-wrap gap-2 mb-3">
              <span
                v-for="tag in selectedTags"
                :key="tag.id"
                class="inline-flex items-center gap-1 px-3 py-1 rounded-full text-xs font-medium bg-[#49b1f5]/10 text-[#49b1f5] cursor-pointer hover:bg-red-50 hover:text-red-400 transition-colors"
                @click="toggleTag(tag.id)"
              >
                # {{ tag.name }} <X :size="10" />
              </span>
              <span v-if="selectedTags.length === 0" class="text-xs text-gray-300">暂无选中标签</span>
            </div>
            <div class="relative">
              <button
                @click="showTagDropdown = !showTagDropdown"
                class="w-full flex items-center justify-between px-3 py-2.5 rounded-xl border border-gray-100 bg-gray-50/50 text-sm text-gray-400 hover:border-[#49b1f5]/30 transition-all"
              >
                <span>添加标签</span>
                <ChevronDown :size="14" class="text-gray-300" :class="{ 'rotate-180': showTagDropdown }" />
              </button>
              <div v-if="showTagDropdown" class="absolute top-full left-0 right-0 mt-1 bg-white rounded-xl border border-gray-100 shadow-lg z-20 max-h-40 overflow-y-auto">
                <button
                  v-for="tag in tags"
                  :key="tag.id"
                  @click="toggleTag(tag.id)"
                  class="w-full text-left px-3 py-2 text-sm hover:bg-gray-50 transition-all flex items-center gap-2"
                  :class="form.tagIds.includes(tag.id) ? 'text-[#49b1f5] font-medium bg-[#49b1f5]/5' : 'text-[#2c3e50]'"
                >
                  <span v-if="tag.color" class="w-2 h-2 rounded-full flex-shrink-0" :style="{ background: tag.color }"></span>
                  # {{ tag.name }}
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
