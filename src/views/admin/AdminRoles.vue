<script setup lang="ts">
import { ref, onMounted, reactive, computed } from 'vue';
import { Plus, Pencil, Trash2, X, Shield, Settings, KeyRound } from 'lucide-vue-next';
import {
  getAdminRoles, createRole, updateRole, deleteRole,
  getAdminMenus, getRoleMenus, updateRoleMenus,
  getAdminPermissions, getRolePermissions, updateRolePermissions,
  type AdminPermission,
} from '@/api/admin';
import type { AdminRole } from '@/api/admin';
import type { MenuResponse } from '@/api/blog';

const roles = ref<AdminRole[]>([]);
const loading = ref(false);
const showModal = ref(false);
const editing = ref<AdminRole | null>(null);
const showMenusModal = ref(false);
const menuLoading = ref(false);
const savingMenus = ref(false);
const menuRole = ref<AdminRole | null>(null);
const menuTree = ref<MenuResponse[]>([]);
const selectedMenuIds = ref<Set<number>>(new Set());
const menuParentMap = ref<Record<number, number | null>>({});
// 功能权限 tab
const permTab = ref<'menus' | 'features'>('menus');
const allPermissions = ref<AdminPermission[]>([]);
const selectedPermIds = ref<Set<number>>(new Set());
const savingPerms = ref(false);

const form = reactive({ name: '', code: '', description: '' });

const flattenedMenus = computed(() => {
  const result: Array<MenuResponse & { depth: number }> = [];

  const walk = (menus: MenuResponse[], depth: number) => {
    menus.forEach((menu) => {
      result.push({ ...menu, depth });
      if (menu.children?.length) {
        walk(menu.children, depth + 1);
      }
    });
  };

  walk(menuTree.value || [], 0);
  return result;
});

const collectAncestorIds = (menuId: number, visited = new Set<number>()) => {
  const parentId = menuParentMap.value[menuId];
  if (parentId == null || visited.has(parentId)) return visited;
  visited.add(parentId);
  return collectAncestorIds(parentId, visited);
};

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

const loadRoleMenus = async (role: AdminRole) => {
  menuLoading.value = true;
  try {
    const [menus, roleMenuIds] = await Promise.all([
      getAdminMenus(),
      getRoleMenus(role.id),
    ]);

    menuTree.value = menus || [];
    selectedMenuIds.value = new Set(roleMenuIds || []);

    const parentMap: Record<number, number | null> = {};
    const walk = (items: MenuResponse[], parentId: number | null) => {
      items.forEach((item) => {
        parentMap[item.id] = parentId;
        if (item.children?.length) {
          walk(item.children, item.id);
        }
      });
    };
    walk(menuTree.value, null);
    menuParentMap.value = parentMap;

    const expandedIds = new Set<number>(roleMenuIds || []);
    (roleMenuIds || []).forEach((menuId) => {
      collectAncestorIds(menuId).forEach((ancestorId) => expandedIds.add(ancestorId));
    });
    selectedMenuIds.value = expandedIds;
  } catch {
    menuTree.value = [];
    selectedMenuIds.value = new Set();
    menuParentMap.value = {};
  } finally {
    menuLoading.value = false;
  }
};

const openMenuPermissions = async (role: AdminRole) => {
  menuRole.value = role;
  permTab.value = 'menus';
  showMenusModal.value = true;
  await loadRoleMenus(role);
};

const loadRoleFeaturePerms = async (role: AdminRole) => {
  menuLoading.value = true;
  try {
    const [perms, rolePermIds] = await Promise.all([
      getAdminPermissions(),
      getRolePermissions(role.id),
    ]);
    allPermissions.value = perms;
    selectedPermIds.value = new Set(rolePermIds);
  } catch {
    allPermissions.value = [];
    selectedPermIds.value = new Set();
  } finally {
    menuLoading.value = false;
  }
};

const switchTab = async (tab: 'menus' | 'features') => {
  permTab.value = tab;
  if (tab === 'features' && menuRole.value) {
    await loadRoleFeaturePerms(menuRole.value);
  }
};

const togglePerm = (permId: number, checked: boolean) => {
  const next = new Set(selectedPermIds.value);
  if (checked) next.add(permId);
  else next.delete(permId);
  selectedPermIds.value = next;
};

const saveRoleFeaturePerms = async () => {
  if (!menuRole.value) return;
  savingPerms.value = true;
  try {
    await updateRolePermissions(menuRole.value.id, Array.from(selectedPermIds.value));
    showMenusModal.value = false;
  } catch {
    // ignore
  } finally {
    savingPerms.value = false;
  }
};

const toggleMenu = (menuId: number, checked: boolean) => {
  const next = new Set(selectedMenuIds.value);
  if (checked) {
    next.add(menuId);
    collectAncestorIds(menuId).forEach((id) => next.add(id));
  } else {
    next.delete(menuId);
  }
  selectedMenuIds.value = next;
};

const handleMenuToggle = (menuId: number, event: Event) => {
  toggleMenu(menuId, (event.target as HTMLInputElement).checked);
};

const saveRoleMenus = async () => {
  if (!menuRole.value) return;
  savingMenus.value = true;
  try {
    await updateRoleMenus(menuRole.value.id, Array.from(selectedMenuIds.value));
    showMenusModal.value = false;
    fetchRoles();
  } catch {
    // ignore
  } finally {
    savingMenus.value = false;
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
            <button @click="openMenuPermissions(role)" class="p-1.5 rounded-lg hover:bg-blue-50 text-gray-400 hover:text-[#49b1f5] transition-all" title="权限配置">
              <Settings :size="13" />
            </button>
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

    <!-- Role permissions modal (menus + feature perms) -->
    <div v-if="showMenusModal" class="fixed inset-0 z-50 flex items-center justify-center p-4">
      <div class="absolute inset-0 bg-black/20 backdrop-blur-sm" @click="showMenusModal = false"></div>
      <div class="relative bg-white rounded-2xl shadow-2xl border border-gray-100 p-6 w-full max-w-2xl max-h-[85vh] overflow-hidden flex flex-col">
        <!-- Header -->
        <div class="flex items-center justify-between mb-4">
          <div>
            <h3 class="text-base font-bold text-[#2c3e50]">角色权限配置</h3>
            <p class="text-xs text-gray-400 mt-1">{{ menuRole?.name }} / {{ menuRole?.code }}</p>
          </div>
          <button @click="showMenusModal = false" class="p-1.5 rounded-lg hover:bg-gray-100 text-gray-400 transition-all">
            <X :size="16" />
          </button>
        </div>

        <!-- Tabs -->
        <div class="flex gap-1 p-1 bg-gray-100/80 rounded-xl mb-4">
          <button
            @click="switchTab('menus')"
            class="flex-1 flex items-center justify-center gap-1.5 py-2 rounded-lg text-xs font-semibold transition-all"
            :class="permTab === 'menus' ? 'bg-white text-[#49b1f5] shadow-sm' : 'text-gray-400 hover:text-gray-600'"
          >
            <Settings :size="13" /> 菜单路由
          </button>
          <button
            @click="switchTab('features')"
            class="flex-1 flex items-center justify-center gap-1.5 py-2 rounded-lg text-xs font-semibold transition-all"
            :class="permTab === 'features' ? 'bg-white text-[#49b1f5] shadow-sm' : 'text-gray-400 hover:text-gray-600'"
          >
            <KeyRound :size="13" /> 功能权限
          </button>
        </div>

        <!-- Tab content -->
        <div class="flex-1 overflow-y-auto pr-1">
          <div v-if="menuLoading" class="space-y-3">
            <div v-for="i in 6" :key="i" class="h-12 rounded-xl bg-gray-100 animate-pulse"></div>
          </div>

          <!-- Menus tab -->
          <div v-else-if="permTab === 'menus'" class="space-y-2">
            <label
              v-for="menu in flattenedMenus"
              :key="menu.id"
              class="flex items-center gap-3 px-3 py-2.5 rounded-xl hover:bg-gray-50 transition-colors cursor-pointer"
              :style="{ paddingLeft: `${12 + menu.depth * 20}px` }"
            >
              <input
                type="checkbox"
                class="w-4 h-4 rounded border-gray-300 text-[#49b1f5] focus:ring-[#49b1f5]"
                :checked="selectedMenuIds.has(menu.id)"
                @change="handleMenuToggle(menu.id, $event)"
              />
              <span class="text-sm font-medium text-[#2c3e50]">{{ menu.name }}</span>
              <span class="text-[10px] text-gray-400 font-mono">{{ menu.code }}</span>
              <span v-if="menu.path" class="ml-auto text-[10px] text-gray-300 font-mono">{{ menu.path }}</span>
            </label>
          </div>

          <!-- Feature permissions tab -->
          <div v-else class="space-y-2">
            <label
              v-for="perm in allPermissions"
              :key="perm.id"
              class="flex items-center gap-3 px-4 py-3 rounded-xl border transition-all cursor-pointer"
              :class="selectedPermIds.has(perm.id)
                ? 'border-[#49b1f5]/30 bg-[#49b1f5]/5'
                : 'border-gray-100 hover:bg-gray-50'"
            >
              <input
                type="checkbox"
                :checked="selectedPermIds.has(perm.id)"
                @change="togglePerm(perm.id, ($event.target as HTMLInputElement).checked)"
                class="w-4 h-4 accent-[#49b1f5]"
              />
              <div class="flex-1 min-w-0">
                <div class="text-sm font-medium text-[#2c3e50]">{{ perm.name }}</div>
                <div class="text-[10px] font-mono text-gray-400">{{ perm.code }}</div>
              </div>
              <span v-if="perm.description" class="text-[10px] text-gray-300 truncate max-w-[140px]">{{ perm.description }}</span>
            </label>
          </div>
        </div>

        <!-- Footer -->
        <div class="flex gap-3 mt-6 pt-4 border-t border-gray-100">
          <button @click="showMenusModal = false" class="flex-1 py-2.5 rounded-xl border border-gray-100 text-sm text-gray-500 hover:bg-gray-50 transition-colors">取消</button>
          <button
            v-if="permTab === 'menus'"
            @click="saveRoleMenus"
            :disabled="savingMenus || menuLoading"
            class="flex-1 py-2.5 rounded-xl bg-[#49b1f5] text-white text-sm font-medium hover:bg-[#3a9de8] transition-colors disabled:opacity-50"
          >
            {{ savingMenus ? '保存中...' : '保存菜单' }}
          </button>
          <button
            v-else
            @click="saveRoleFeaturePerms"
            :disabled="savingPerms || menuLoading"
            class="flex-1 py-2.5 rounded-xl bg-[#49b1f5] text-white text-sm font-medium hover:bg-[#3a9de8] transition-colors disabled:opacity-50"
          >
            {{ savingPerms ? '保存中...' : '保存权限' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
