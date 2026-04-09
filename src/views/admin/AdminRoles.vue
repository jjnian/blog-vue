<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue';
import { Plus, Pencil, Trash2, X, Shield } from 'lucide-vue-next';
import { getAdminRoles, createRole, updateRole, deleteRole } from '@/api/admin';
import type { AdminRole } from '@/api/admin';

const roles = ref<AdminRole[]>([]);
const loading = ref(false);
const showModal = ref(false);
const editing = ref<AdminRole | null>(null);

const form = reactive({ name: '', code: '', description: '' });

const fetchRoles = async () => {
  loading.value = true;
  try {
    roles.value = await getAdminRoles();
  } catch {
    // ignore
  } finally {
    loading.value = false;
  }
};

onMounted(fetchRoles);

const openCreate = () => {
  editing.value = null;
  form.name = '';
  form.code = '';
  form.description = '';
  showModal.value = true;
};

const openEdit = (role: AdminRole) => {
  editing.value = role;
  form.name = role.name;
  form.code = role.code;
  form.description = role.description || '';
  showModal.value = true;
};

const handleSave = async () => {
  if (!form.name || !form.code) return;
  try {
    if (editing.value) {
      await updateRole(editing.value.id, { name: form.name, code: form.code, description: form.description });
    } else {
      await createRole({ name: form.name, code: form.code, description: form.description });
    }
    showModal.value = false;
    fetchRoles();
  } catch {
    // ignore
  }
};

const handleDelete = async (role: AdminRole) => {
  if (!confirm(`确定要删除角色 "${role.name}" 吗？`)) return;
  try {
    await deleteRole(role.id);
    fetchRoles();
  } catch {
    // ignore
  }
};

const roleColorClass = (code: string) => {
  if (code.includes('SUPER')) return 'bg-purple-100 text-purple-600';
  if (code.includes('ADMIN')) return 'bg-blue-100 text-blue-600';
  return 'bg-gray-100 text-gray-500';
};
</script>

<template>
  <div class="space-y-4">
    <div class="flex items-center justify-between">
      <div></div>
      <button
        @click="openCreate"
        class="flex items-center gap-2 px-4 py-2 rounded-xl bg-[#49b1f5] text-white text-sm font-medium hover:bg-[#3a9de8] transition-colors shadow-md shadow-[#49b1f5]/30"
      >
        <Plus :size="16" /> 新建角色
      </button>
    </div>

    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
      <div v-if="loading" v-for="i in 3" :key="i" class="bg-white/80 rounded-2xl border border-white/80 shadow-sm p-5 animate-pulse">
        <div class="h-5 bg-gray-100 rounded w-1/2 mb-3"></div>
        <div class="h-3 bg-gray-100 rounded w-1/3 mb-2"></div>
        <div class="h-3 bg-gray-100 rounded w-3/4"></div>
      </div>

      <div
        v-for="role in roles"
        :key="role.id"
        class="bg-white/80 backdrop-blur-xl rounded-2xl border border-white/80 shadow-sm p-5 hover:shadow-md hover:-translate-y-0.5 transition-all group"
      >
        <div class="flex items-start justify-between mb-3">
          <div class="w-10 h-10 rounded-xl flex items-center justify-center" :class="roleColorClass(role.code)">
            <Shield :size="18" />
          </div>
          <div class="flex items-center gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
            <button @click="openEdit(role)" class="p-1.5 rounded-lg hover:bg-gray-100 text-gray-400 hover:text-[#49b1f5] transition-all">
              <Pencil :size="13" />
            </button>
            <button @click="handleDelete(role)" class="p-1.5 rounded-lg hover:bg-red-50 text-gray-400 hover:text-red-400 transition-all">
              <Trash2 :size="13" />
            </button>
          </div>
        </div>
        <h3 class="font-semibold text-[#2c3e50] mb-1">{{ role.name }}</h3>
        <span class="inline-block px-2 py-0.5 rounded-lg text-xs font-mono font-medium mb-2" :class="roleColorClass(role.code)">{{ role.code }}</span>
        <p v-if="role.description" class="text-xs text-gray-400 leading-relaxed">{{ role.description }}</p>
      </div>
    </div>

    <!-- Modal -->
    <div v-if="showModal" class="fixed inset-0 z-50 flex items-center justify-center p-4">
      <div class="absolute inset-0 bg-black/20 backdrop-blur-sm" @click="showModal = false"></div>
      <div class="relative bg-white rounded-2xl shadow-2xl border border-gray-100 p-6 w-full max-w-md">
        <div class="flex items-center justify-between mb-5">
          <h3 class="text-base font-bold text-[#2c3e50]">{{ editing ? '编辑角色' : '新建角色' }}</h3>
          <button @click="showModal = false" class="p-1.5 rounded-lg hover:bg-gray-100 text-gray-400 transition-all">
            <X :size="16" />
          </button>
        </div>

        <div class="space-y-4">
          <div>
            <label class="text-xs font-semibold text-gray-400 uppercase tracking-wider block mb-1.5">角色名称 *</label>
            <input
              v-model="form.name"
              type="text"
              placeholder="例如：普通用户"
              class="w-full px-4 py-2.5 rounded-xl border border-gray-100 bg-gray-50 text-sm text-[#2c3e50] focus:outline-none focus:border-[#49b1f5] transition-colors"
            />
          </div>
          <div>
            <label class="text-xs font-semibold text-gray-400 uppercase tracking-wider block mb-1.5">角色代码 *</label>
            <input
              v-model="form.code"
              type="text"
              placeholder="例如：USER"
              class="w-full px-4 py-2.5 rounded-xl border border-gray-100 bg-gray-50 text-sm text-[#2c3e50] font-mono focus:outline-none focus:border-[#49b1f5] transition-colors"
            />
          </div>
          <div>
            <label class="text-xs font-semibold text-gray-400 uppercase tracking-wider block mb-1.5">描述</label>
            <textarea
              v-model="form.description"
              placeholder="角色描述（可选）"
              rows="2"
              class="w-full px-4 py-2.5 rounded-xl border border-gray-100 bg-gray-50 text-sm text-[#2c3e50] focus:outline-none focus:border-[#49b1f5] transition-colors resize-none"
            ></textarea>
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
