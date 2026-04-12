<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { KeyRound, Shield, RefreshCw } from 'lucide-vue-next';
import {
  getAdminPermissions,
  getAdminRoles,
  getRolePermissions,
  updateRolePermissions,
  type AdminPermission,
  type AdminRole,
} from '@/api/admin';

const permissions = ref<AdminPermission[]>([]);
const roles = ref<AdminRole[]>([]);
const loading = ref(false);
const saving = ref(false);

// 矩阵：roleId -> Set<permissionId>
const matrix = ref<Map<number, Set<number>>>(new Map());
// 是否有未保存的修改
const dirty = ref<Set<number>>(new Set());

const fetchData = async () => {
  loading.value = true;
  try {
    const [perms, roleList] = await Promise.all([getAdminPermissions(), getAdminRoles()]);
    permissions.value = perms;
    roles.value = roleList;
    matrix.value = new Map();
    dirty.value = new Set();

    await Promise.all(
      roleList.map(async (role) => {
        const ids = await getRolePermissions(role.id);
        matrix.value.set(role.id, new Set(ids));
      })
    );
  } catch {
    // ignore
  } finally {
    loading.value = false;
  }
};

onMounted(fetchData);

const isChecked = (roleId: number, permId: number) =>
  matrix.value.get(roleId)?.has(permId) ?? false;

const toggleCell = (roleId: number, permId: number) => {
  const set = matrix.value.get(roleId) ?? new Set<number>();
  if (set.has(permId)) {
    set.delete(permId);
  } else {
    set.add(permId);
  }
  matrix.value.set(roleId, set);
  dirty.value.add(roleId);
};

const saveAll = async () => {
  if (dirty.value.size === 0) return;
  saving.value = true;
  try {
    await Promise.all(
      Array.from(dirty.value).map((roleId) =>
        updateRolePermissions(roleId, Array.from(matrix.value.get(roleId) ?? []))
      )
    );
    dirty.value = new Set();
  } catch {
    // ignore
  } finally {
    saving.value = false;
  }
};

const roleColorClass = (code: string) => {
  if (code.includes('SUPER')) return 'bg-purple-100 text-purple-600';
  if (code.includes('ADMIN')) return 'bg-blue-100 text-blue-600';
  if (code.includes('EDITOR')) return 'bg-green-100 text-green-600';
  return 'bg-gray-100 text-gray-500';
};

const permGroupLabel: Record<string, string> = {
  'ARTICLE': '文章',
  'COMMENT': '评论',
  'USER': '用户',
  'ROLE': '角色',
  'CONTENT': '内容',
  'SYSTEM': '系统',
};

const groupedPermissions = computed(() => {
  const groups: Record<string, AdminPermission[]> = {};
  for (const p of permissions.value) {
    const prefix = p.code.split(':')[0];
    if (!groups[prefix]) groups[prefix] = [];
    groups[prefix].push(p);
  }
  return groups;
});
</script>

<template>
  <div class="space-y-4">
    <div class="flex items-center justify-between">
      <div class="flex items-center gap-2 text-sm text-gray-400">
        <KeyRound :size="15" />
        <span>勾选矩阵中的格子来配置各角色拥有的功能权限，修改后点击保存</span>
      </div>
      <div class="flex items-center gap-3">
        <button
          @click="fetchData"
          class="p-2 rounded-xl border border-gray-100 text-gray-400 hover:text-[#49b1f5] hover:border-[#49b1f5]/30 transition-all"
        >
          <RefreshCw :size="14" :class="{ 'animate-spin': loading }" />
        </button>
        <button
          @click="saveAll"
          :disabled="dirty.size === 0 || saving"
          class="flex items-center gap-2 px-4 py-2 rounded-xl bg-[#49b1f5] text-white text-sm font-medium hover:bg-[#3a9de8] transition-colors shadow-md shadow-[#49b1f5]/30 disabled:opacity-40 disabled:cursor-not-allowed"
        >
          {{ saving ? '保存中...' : `保存修改${dirty.size > 0 ? ` (${dirty.size})` : ''}` }}
        </button>
      </div>
    </div>

    <div v-if="loading" class="bg-white/80 rounded-2xl border border-white/80 shadow-sm p-10 flex items-center justify-center text-gray-300 gap-2">
      <RefreshCw :size="16" class="animate-spin" /> 加载中...
    </div>

    <div v-else class="bg-white/80 backdrop-blur-xl rounded-2xl border border-white/80 shadow-sm overflow-hidden">
      <div class="overflow-x-auto">
        <table class="w-full text-sm">
          <thead>
            <tr class="border-b border-gray-100 bg-gray-50/50">
              <th class="text-left px-5 py-3 text-xs font-semibold text-gray-400 uppercase tracking-wider w-52">权限</th>
              <th
                v-for="role in roles"
                :key="role.id"
                class="px-4 py-3 text-center min-w-[90px]"
              >
                <div class="flex flex-col items-center gap-1">
                  <div class="w-7 h-7 rounded-lg flex items-center justify-center" :class="roleColorClass(role.code)">
                    <Shield :size="13" />
                  </div>
                  <span class="text-[10px] font-mono font-semibold" :class="roleColorClass(role.code).split(' ')[1]">
                    {{ role.code }}
                  </span>
                </div>
              </th>
            </tr>
          </thead>
          <tbody>
            <template v-for="(perms, group) in groupedPermissions" :key="group">
              <!-- Group header -->
              <tr class="bg-gray-50/30">
                <td :colspan="roles.length + 1" class="px-5 py-1.5">
                  <span class="text-[10px] font-bold text-gray-300 uppercase tracking-widest">
                    {{ permGroupLabel[group] || group }}
                  </span>
                </td>
              </tr>
              <!-- Permission rows -->
              <tr
                v-for="perm in perms"
                :key="perm.id"
                class="border-b border-gray-50 hover:bg-blue-50/20 transition-colors"
              >
                <td class="px-5 py-3">
                  <div class="font-medium text-[#2c3e50] text-xs">{{ perm.name }}</div>
                  <div class="text-[10px] text-gray-300 font-mono mt-0.5">{{ perm.code }}</div>
                </td>
                <td
                  v-for="role in roles"
                  :key="role.id"
                  class="px-4 py-3 text-center"
                >
                  <input
                    type="checkbox"
                    :checked="isChecked(role.id, perm.id)"
                    @change="toggleCell(role.id, perm.id)"
                    class="w-4 h-4 rounded accent-[#49b1f5] cursor-pointer"
                  />
                </td>
              </tr>
            </template>
          </tbody>
        </table>
      </div>
    </div>

    <p class="text-xs text-gray-400 text-center">
      SUPER_ADMIN 与 ADMIN 角色在前端自动拥有所有权限，此矩阵仅影响其他角色及用户专属覆盖设置
    </p>
  </div>
</template>
