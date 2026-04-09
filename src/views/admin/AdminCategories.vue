<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue';
import { Plus, Pencil, Trash2, X, RefreshCw, Search, ChevronLeft, ChevronRight } from 'lucide-vue-next';
import { getAdminCategories, createCategory, updateCategory, deleteCategory } from '@/api/admin';
import type { Category } from '@/api/blog';

const items = ref<Category[]>([]);
const loading = ref(false);
const total = ref(0);
const pages = ref(1);
const filter = reactive({ page: 1, size: 10, keyword: '' });
const showModal = ref(false);
const editing = ref<Category | null>(null);

const form = reactive({
  name: '',
  slug: '',
  description: '',
  color: '#49b1f5',
  icon: '',
  sortOrder: 0,
});

const colorPresets = ['#49b1f5', '#f59e0b', '#10b981', '#8b5cf6', '#ef4444', '#ec4899', '#06b6d4', '#84cc16'];

const fetchItems = async () => {
  loading.value = true;
  try {
    const res = await getAdminCategories(filter);
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
  Object.assign(form, { name: '', slug: '', description: '', color: '#49b1f5', icon: '', sortOrder: 0 });
  showModal.value = true;
};

const openEdit = (item: Category) => {
  editing.value = item;
  Object.assign(form, {
    name: item.name,
    slug: item.slug || '',
    description: item.description || '',
    color: item.color || '#49b1f5',
    icon: '',
    sortOrder: 0,
  });
  showModal.value = true;
};

const handleSave = async () => {
  if (!form.name) return;
  try {
    if (editing.value) {
      await updateCategory(editing.value.id, { ...form });
    } else {
      await createCategory({ ...form });
    }
    showModal.value = false;
    fetchItems();
  } catch {
    // ignore
  }
};

const handleDelete = async (item: Category) => {
  if (!confirm(`确定要删除分类 "${item.name}" 吗？`)) return;
  try {
    await deleteCategory(item.id);
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
          placeholder="搜索分类名称"
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
        <Plus :size="15" /> 新建分类
      </button>
    </div>

    <!-- Table -->
    <div class="bg-white/80 backdrop-blur-xl rounded-2xl border border-white/80 shadow-sm overflow-hidden">
      <table class="w-full text-sm">
        <thead>
          <tr class="border-b border-gray-100">
            <th class="text-left px-5 py-3.5 text-xs font-semibold text-gray-400 uppercase tracking-wider">分类</th>
            <th class="text-left px-5 py-3.5 text-xs font-semibold text-gray-400 uppercase tracking-wider">别名</th>
            <th class="text-left px-5 py-3.5 text-xs font-semibold text-gray-400 uppercase tracking-wider">文章数</th>
            <th class="text-left px-5 py-3.5 text-xs font-semibold text-gray-400 uppercase tracking-wider">描述</th>
            <th class="text-right px-5 py-3.5 text-xs font-semibold text-gray-400 uppercase tracking-wider">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading"><td colspan="5" class="px-5 py-10 text-center text-gray-300 text-sm"><RefreshCw :size="16" class="animate-spin mx-auto" /></td></tr>
          <tr v-else-if="items.length === 0"><td colspan="5" class="px-5 py-10 text-center text-gray-300 text-sm">暂无分类数据</td></tr>
          <tr
            v-for="item in items"
            :key="item.id"
            class="border-b border-gray-50 hover:bg-gray-50/50 transition-colors"
          >
            <td class="px-5 py-4">
              <div class="flex items-center gap-3">
                <div class="w-3 h-3 rounded-full flex-shrink-0" :style="{ background: item.color || '#49b1f5' }"></div>
                <span class="font-medium text-[#2c3e50]">{{ item.name }}</span>
              </div>
            </td>
            <td class="px-5 py-4 text-gray-400 text-xs font-mono">{{ item.slug || '-' }}</td>
            <td class="px-5 py-4 text-gray-500">{{ item.articleCount ?? 0 }}</td>
            <td class="px-5 py-4 text-gray-400 text-xs max-w-xs truncate">{{ item.description || '-' }}</td>
            <td class="px-5 py-4">
              <div class="flex items-center justify-end gap-2">
                <button @click="openEdit(item)" class="p-2 rounded-lg hover:bg-blue-50 text-gray-300 hover:text-blue-500 transition-all"><Pencil :size="14" /></button>
                <button @click="handleDelete(item)" class="p-2 rounded-lg hover:bg-red-50 text-gray-300 hover:text-red-400 transition-all"><Trash2 :size="14" /></button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>

      <div class="px-5 py-3 border-t border-gray-50 flex items-center justify-between">
        <span class="text-xs text-gray-400">共 {{ total }} 个分类</span>
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
      <div class="relative bg-white rounded-2xl shadow-2xl border border-gray-100 p-6 w-full max-w-md">
        <div class="flex items-center justify-between mb-5">
          <h3 class="text-base font-bold text-[#2c3e50]">{{ editing ? '编辑分类' : '新建分类' }}</h3>
          <button @click="showModal = false" class="p-1.5 rounded-lg hover:bg-gray-100 text-gray-400 transition-all"><X :size="16" /></button>
        </div>
        <div class="space-y-4">
          <div>
            <label class="text-xs font-semibold text-gray-400 uppercase tracking-wider block mb-1.5">名称 *</label>
            <input v-model="form.name" type="text" placeholder="分类名称" class="w-full px-4 py-2.5 rounded-xl border border-gray-100 bg-gray-50 text-sm text-[#2c3e50] focus:outline-none focus:border-[#49b1f5] transition-colors" />
          </div>
          <div>
            <label class="text-xs font-semibold text-gray-400 uppercase tracking-wider block mb-1.5">别名 (Slug)</label>
            <input v-model="form.slug" type="text" placeholder="url-friendly-name" class="w-full px-4 py-2.5 rounded-xl border border-gray-100 bg-gray-50 text-sm text-[#2c3e50] font-mono focus:outline-none focus:border-[#49b1f5] transition-colors" />
          </div>
          <div>
            <label class="text-xs font-semibold text-gray-400 uppercase tracking-wider block mb-1.5">颜色</label>
            <div class="flex items-center gap-2 flex-wrap">
              <div
                v-for="color in colorPresets"
                :key="color"
                @click="form.color = color"
                class="w-7 h-7 rounded-full cursor-pointer transition-all border-2"
                :style="{ background: color }"
                :class="form.color === color ? 'border-gray-400 scale-110' : 'border-transparent hover:scale-110'"
              ></div>
              <input v-model="form.color" type="color" class="w-7 h-7 rounded-full cursor-pointer border-0 p-0 bg-transparent" />
            </div>
          </div>
          <div>
            <label class="text-xs font-semibold text-gray-400 uppercase tracking-wider block mb-1.5">描述</label>
            <textarea v-model="form.description" placeholder="分类描述（可选）" rows="2" class="w-full px-4 py-2.5 rounded-xl border border-gray-100 bg-gray-50 text-sm text-[#2c3e50] focus:outline-none focus:border-[#49b1f5] transition-colors resize-none"></textarea>
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
