<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue';
import { Plus, Pencil, Trash2, X, RefreshCw, Search, ChevronLeft, ChevronRight, Tag as TagIcon } from 'lucide-vue-next';
import { getAdminTags, createTag, updateTag, deleteTag } from '@/api/admin';
import type { Tag } from '@/api/blog';

const items = ref<Tag[]>([]);
const loading = ref(false);
const total = ref(0);
const pages = ref(1);
const filter = reactive({ page: 1, size: 10, keyword: '' });
const showModal = ref(false);
const editing = ref<Tag | null>(null);

const form = reactive({ name: '', slug: '', color: '#49b1f5' });
const colorPresets = ['#49b1f5', '#f59e0b', '#10b981', '#8b5cf6', '#ef4444', '#ec4899', '#06b6d4', '#84cc16'];

const fetchItems = async () => {
  loading.value = true;
  try {
    const res = await getAdminTags(filter);
    items.value = res.records;
    total.value = Number(res.total);
    pages.value = Number(res.pages);
  } catch {
    // ignore
  } finally {
    loading.value = false;
  }
};

onMounted(fetchItems);

const openCreate = () => {
  editing.value = null;
  Object.assign(form, { name: '', slug: '', color: '#49b1f5' });
  showModal.value = true;
};

const openEdit = (item: Tag) => {
  editing.value = item;
  Object.assign(form, { name: item.name, slug: item.slug || '', color: item.color || '#49b1f5' });
  showModal.value = true;
};

const handleSave = async () => {
  if (!form.name) return;
  try {
    if (editing.value) {
      await updateTag(editing.value.id, { ...form });
    } else {
      await createTag({ ...form });
    }
    showModal.value = false;
    fetchItems();
  } catch {
    // ignore
  }
};

const handleDelete = async (item: Tag) => {
  if (!confirm(`确定要删除标签 "${item.name}" 吗？`)) return;
  try {
    await deleteTag(item.id);
    fetchItems();
  } catch {
    // ignore
  }
};
</script>

<template>
  <div class="space-y-4">
    <!-- Toolbar -->
    <div class="bg-white/80 backdrop-blur-xl rounded-2xl border border-white/80 shadow-sm p-4 flex flex-wrap items-center gap-3">
      <div class="relative flex-1 min-w-40">
        <Search :size="15" class="absolute left-3 top-1/2 -translate-y-1/2 text-gray-300" />
        <input
          v-model="filter.keyword"
          @keydown.enter="filter.page = 1; fetchItems()"
          type="text"
          placeholder="搜索标签名称"
          class="w-full pl-9 pr-4 py-2 text-sm bg-gray-50 border border-gray-100 rounded-xl focus:outline-none focus:border-[#49b1f5] transition-colors"
        />
      </div>
      <button @click="fetchItems" class="p-2 rounded-xl border border-gray-100 text-gray-400 hover:text-[#49b1f5] hover:border-[#49b1f5]/30 transition-colors">
        <RefreshCw :size="14" :class="{ 'animate-spin': loading }" />
      </button>
      <button
        @click="openCreate"
        class="flex items-center gap-2 px-4 py-2 rounded-xl bg-[#49b1f5] text-white text-sm font-medium hover:bg-[#3a9de8] transition-colors shadow-md shadow-[#49b1f5]/30"
      >
        <Plus :size="15" /> 新建标签
      </button>
    </div>

    <!-- Tags Grid -->
    <div class="bg-white/80 backdrop-blur-xl rounded-2xl border border-white/80 shadow-sm p-5">
      <div v-if="loading" class="flex items-center justify-center py-10 text-gray-300">
        <RefreshCw :size="20" class="animate-spin" />
      </div>
      <div v-else-if="items.length === 0" class="text-center py-10 text-gray-300">
        <TagIcon :size="32" class="mx-auto mb-2 opacity-40" />
        暂无标签数据
      </div>
      <div v-else class="flex flex-wrap gap-3">
        <div
          v-for="item in items"
          :key="item.id"
          class="group flex items-center gap-2 px-4 py-2 rounded-full border transition-all hover:shadow-md"
          :style="{ borderColor: `${item.color || '#49b1f5'}40`, background: `${item.color || '#49b1f5'}10` }"
        >
          <span class="text-sm font-medium" :style="{ color: item.color || '#49b1f5' }"># {{ item.name }}</span>
          <span class="text-xs text-gray-400">({{ item.articleCount ?? 0 }})</span>
          <div class="flex items-center gap-1 ml-1 opacity-0 group-hover:opacity-100 transition-opacity">
            <button @click="openEdit(item)" class="p-0.5 rounded hover:text-[#49b1f5] text-gray-400 transition-colors"><Pencil :size="11" /></button>
            <button @click="handleDelete(item)" class="p-0.5 rounded hover:text-red-400 text-gray-400 transition-colors"><Trash2 :size="11" /></button>
          </div>
        </div>
      </div>

      <!-- Pagination -->
      <div v-if="pages > 1" class="mt-4 pt-4 border-t border-gray-100 flex items-center justify-between">
        <span class="text-xs text-gray-400">共 {{ total }} 个标签</span>
        <div class="flex items-center gap-2">
          <button @click="filter.page--; fetchItems()" :disabled="filter.page <= 1" class="p-1.5 rounded-lg border border-gray-100 text-gray-400 disabled:opacity-40 hover:border-[#49b1f5]/30 hover:text-[#49b1f5] transition-all"><ChevronLeft :size="14" /></button>
          <span class="text-xs text-gray-500">{{ filter.page }} / {{ pages }}</span>
          <button @click="filter.page++; fetchItems()" :disabled="filter.page >= pages" class="p-1.5 rounded-lg border border-gray-100 text-gray-400 disabled:opacity-40 hover:border-[#49b1f5]/30 hover:text-[#49b1f5] transition-all"><ChevronRight :size="14" /></button>
        </div>
      </div>
    </div>

    <!-- Modal -->
    <div v-if="showModal" class="fixed inset-0 z-50 flex items-center justify-center p-4">
      <div class="absolute inset-0 bg-black/20 backdrop-blur-sm" @click="showModal = false"></div>
      <div class="relative bg-white rounded-2xl shadow-2xl border border-gray-100 p-6 w-full max-w-sm">
        <div class="flex items-center justify-between mb-5">
          <h3 class="text-base font-bold text-[#2c3e50]">{{ editing ? '编辑标签' : '新建标签' }}</h3>
          <button @click="showModal = false" class="p-1.5 rounded-lg hover:bg-gray-100 text-gray-400 transition-all"><X :size="16" /></button>
        </div>
        <div class="space-y-4">
          <div>
            <label class="text-xs font-semibold text-gray-400 uppercase tracking-wider block mb-1.5">标签名称 *</label>
            <input v-model="form.name" type="text" placeholder="标签名称" class="w-full px-4 py-2.5 rounded-xl border border-gray-100 bg-gray-50 text-sm text-[#2c3e50] focus:outline-none focus:border-[#49b1f5] transition-colors" />
          </div>
          <div>
            <label class="text-xs font-semibold text-gray-400 uppercase tracking-wider block mb-1.5">别名 (Slug)</label>
            <input v-model="form.slug" type="text" placeholder="url-friendly-name" class="w-full px-4 py-2.5 rounded-xl border border-gray-100 bg-gray-50 text-sm text-[#2c3e50] font-mono focus:outline-none focus:border-[#49b1f5] transition-colors" />
          </div>
          <div>
            <label class="text-xs font-semibold text-gray-400 uppercase tracking-wider block mb-1.5">颜色</label>
            <div class="flex items-center gap-2 flex-wrap">
              <div v-for="c in colorPresets" :key="c" @click="form.color = c" class="w-7 h-7 rounded-full cursor-pointer transition-all border-2" :style="{ background: c }" :class="form.color === c ? 'border-gray-400 scale-110' : 'border-transparent hover:scale-110'"></div>
              <input v-model="form.color" type="color" class="w-7 h-7 rounded-full cursor-pointer border-0 p-0" />
            </div>
          </div>
        </div>
        <div class="flex gap-3 mt-6">
          <button @click="showModal = false" class="flex-1 py-2.5 rounded-xl border border-gray-100 text-sm text-gray-500 hover:bg-gray-50 transition-colors">取消</button>
          <button @click="handleSave" class="flex-1 py-2.5 rounded-xl bg-[#49b1f5] text-white text-sm font-medium hover:bg-[#3a9de8] transition-colors">保存</button>
        </div>
      </div>
    </div>
  </div>
</template>
