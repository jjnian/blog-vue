<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue';
import {
  Search, RefreshCw, Shield, UserCheck, UserX, Trash2,
  ChevronLeft, ChevronRight, KeyRound, CheckCircle, XCircle, MinusCircle
} from 'lucide-vue-next';
import {
  getAdminUsers, updateUserStatus, assignUserRoles, deleteUser, getAdminRoles,
  getAdminPermissions, getUserPermissionOverrides, setUserPermission, deleteUserPermission,
  type AdminPermission, type UserPermissionOverride,
} from '@/api/admin';
import type { AdminUser, AdminRole } from '@/api/admin';

const users = ref<AdminUser[]>([]);
const roles = ref<AdminRole[]>([]);
const loading = ref(false);
const total = ref(0);
const pages = ref(1);

const filter = reactive({ page: 1, size: 10, keyword: '' });

const editingRoles = ref<{ userId: number; roleIds: number[] } | null>(null);
const showRoleModal = ref(false);

// 用户专属权限覆盖
const showPermModal = ref(false);
const permUser = ref<AdminUser | null>(null);
const allPermissions = ref<AdminPermission[]>([]);
const userOverrides = ref<UserPermissionOverride[]>([]);
const permLoading = ref(false);
const savingPerm = ref(false);

const fetchUsers = async () => {
  loading.value = true;
  try {
    const res = await getAdminUsers(filter);
    users.value = res.records;
    total.value = Number(res.total);
    pages.value = Number(res.pages);
  } catch {
    // ignore
  } finally {
    loading.value = false;
  }
};

const fetchRoles = async () => {
  try {
    roles.value = await getAdminRoles();
  } catch {
    // ignore
  }
};

onMounted(() => {
  fetchUsers();
  fetchRoles();
});

const handleSearch = () => {
  filter.page = 1;
  fetchUsers();
};

const toggleStatus = async (user: AdminUser) => {
  const newStatus = user.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE';
  try {
    await updateUserStatus(user.id, newStatus);
    user.status = newStatus;
  } catch {
    // ignore
  }
};

const openRoleModal = (user: AdminUser) => {
  const roleCodeToId = (code: string) => roles.value.find((r) => r.code === code)?.id;
  editingRoles.value = {
    userId: user.id,
    roleIds: user.roles.map(roleCodeToId).filter((id): id is number => id !== undefined),
  };
  showRoleModal.value = true;
};

const toggleRoleId = (roleId: number) => {
  if (!editingRoles.value) return;
  const idx = editingRoles.value.roleIds.indexOf(roleId);
  if (idx > -1) editingRoles.value.roleIds.splice(idx, 1);
  else editingRoles.value.roleIds.push(roleId);
};

const saveRoles = async () => {
  if (!editingRoles.value) return;
  try {
    await assignUserRoles(editingRoles.value.userId, editingRoles.value.roleIds);
    showRoleModal.value = false;
    fetchUsers();
  } catch {
    // ignore
  }
};

const openPermModal = async (user: AdminUser) => {
  permUser.value = user;
  showPermModal.value = true;
  permLoading.value = true;
  try {
    const [perms, overrides] = await Promise.all([
      getAdminPermissions(),
      getUserPermissionOverrides(user.id),
    ]);
    allPermissions.value = perms;
    userOverrides.value = overrides;
  } catch {
    allPermissions.value = [];
    userOverrides.value = [];
  } finally {
    permLoading.value = false;
  }
};

// override state: 'grant' | 'deny' | 'inherit'
const getOverrideState = (permId: number): 'grant' | 'deny' | 'inherit' => {
  const found = userOverrides.value.find((o) => o.permissionId === permId);
  if (!found) return 'inherit';
  return found.granted ? 'grant' : 'deny';
};

const setOverride = async (permId: number, state: 'grant' | 'deny' | 'inherit') => {
  if (!permUser.value) return;
  savingPerm.value = true;
  try {
    if (state === 'inherit') {
      await deleteUserPermission(permUser.value.id, permId);
      userOverrides.value = userOverrides.value.filter((o) => o.permissionId !== permId);
    } else {
      const granted = state === 'grant';
      await setUserPermission(permUser.value.id, permId, granted);
      const existing = userOverrides.value.find((o) => o.permissionId === permId);
      if (existing) {
        existing.granted = granted;
      } else {
        userOverrides.value.push({ id: Date.now(), userId: permUser.value.id, permissionId: permId, granted });
      }
    }
  } catch {
    // ignore
  } finally {
    savingPerm.value = false;
  }
};

const handleDelete = async (user: AdminUser) => {
  if (!confirm(`确定要删除用户 "${user.username}" 吗？此操作不可恢复。`)) return;
  try {
    await deleteUser(user.id);
    fetchUsers();
  } catch {
    // ignore
  }
};

const formatDate = (v?: string) => {
  if (!v) return '-';
  return new Date(v).toLocaleDateString('zh-CN');
};

const statusBadge = (status: string) =>
  status === 'ACTIVE'
    ? 'bg-green-100 text-green-600'
    : 'bg-red-100 text-red-400';
</script>

<template>
  <div class="space-y-4">
    <!-- Toolbar -->
    <div class="bg-white/80 backdrop-blur-xl rounded-2xl border border-white/80 shadow-sm p-4 flex flex-wrap items-center gap-3">
      <div class="relative flex-1 min-w-48">
        <Search :size="15" class="absolute left-3 top-1/2 -translate-y-1/2 text-gray-300" />
        <input
          v-model="filter.keyword"
          @keydown.enter="handleSearch"
          type="text"
          placeholder="搜索用户名/昵称/邮箱"
          class="w-full pl-9 pr-4 py-2 text-sm bg-gray-50 border border-gray-100 rounded-xl focus:outline-none focus:border-[#49b1f5] transition-colors"
        />
      </div>
      <button @click="handleSearch" class="flex items-center gap-2 px-4 py-2 rounded-xl bg-[#49b1f5] text-white text-sm font-medium hover:bg-[#3a9de8] transition-colors">
        <Search :size="14" /> 搜索
      </button>
      <button @click="fetchUsers" class="flex items-center gap-2 px-3 py-2 rounded-xl border border-gray-100 text-gray-400 text-sm hover:text-[#49b1f5] hover:border-[#49b1f5]/30 transition-colors">
        <RefreshCw :size="14" :class="{ 'animate-spin': loading }" />
      </button>
    </div>

    <!-- Table -->
    <div class="bg-white/80 backdrop-blur-xl rounded-2xl border border-white/80 shadow-sm overflow-hidden">
      <div class="overflow-x-auto">
        <table class="w-full text-sm">
          <thead>
            <tr class="border-b border-gray-100">
              <th class="text-left px-5 py-3.5 text-xs font-semibold text-gray-400 uppercase tracking-wider">用户</th>
              <th class="text-left px-5 py-3.5 text-xs font-semibold text-gray-400 uppercase tracking-wider">邮箱</th>
              <th class="text-left px-5 py-3.5 text-xs font-semibold text-gray-400 uppercase tracking-wider">角色</th>
              <th class="text-left px-5 py-3.5 text-xs font-semibold text-gray-400 uppercase tracking-wider">状态</th>
              <th class="text-left px-5 py-3.5 text-xs font-semibold text-gray-400 uppercase tracking-wider">注册时间</th>
              <th class="text-right px-5 py-3.5 text-xs font-semibold text-gray-400 uppercase tracking-wider">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="loading">
              <td colspan="6" class="px-5 py-10 text-center text-gray-300 text-sm">
                <div class="flex items-center justify-center gap-2">
                  <RefreshCw :size="16" class="animate-spin" /> 加载中...
                </div>
              </td>
            </tr>
            <tr v-else-if="users.length === 0">
              <td colspan="6" class="px-5 py-10 text-center text-gray-300 text-sm">暂无用户数据</td>
            </tr>
            <tr
              v-for="user in users"
              :key="user.id"
              class="border-b border-gray-50 hover:bg-gray-50/50 transition-colors"
            >
              <td class="px-5 py-4">
                <div class="flex items-center gap-3">
                  <div class="w-8 h-8 rounded-full bg-gradient-to-br from-[#49b1f5]/20 to-purple-400/20 flex items-center justify-center text-xs font-bold text-[#49b1f5] flex-shrink-0">
                    {{ (user.nickname || user.username).charAt(0).toUpperCase() }}
                  </div>
                  <div>
                    <div class="font-medium text-[#2c3e50]">{{ user.nickname || user.username }}</div>
                    <div class="text-xs text-gray-400">@{{ user.username }}</div>
                  </div>
                </div>
              </td>
              <td class="px-5 py-4 text-gray-500">{{ user.email || '-' }}</td>
              <td class="px-5 py-4">
                <div class="flex flex-wrap gap-1">
                  <span
                    v-for="role in user.roles"
                    :key="role"
                    class="px-2 py-0.5 rounded-lg text-xs font-medium"
                    :class="role.includes('SUPER') ? 'bg-purple-100 text-purple-600' : role.includes('ADMIN') ? 'bg-blue-100 text-blue-600' : 'bg-gray-100 text-gray-500'"
                  >{{ role }}</span>
                  <span v-if="!user.roles?.length" class="text-gray-300 text-xs">无角色</span>
                </div>
              </td>
              <td class="px-5 py-4">
                <span class="px-2.5 py-1 rounded-lg text-xs font-medium" :class="statusBadge(user.status)">
                  {{ user.status === 'ACTIVE' ? '正常' : '禁用' }}
                </span>
              </td>
              <td class="px-5 py-4 text-gray-400 text-xs">{{ formatDate(user.createdAt) }}</td>
              <td class="px-5 py-4">
                <div class="flex items-center justify-end gap-2">
                  <button
                    @click="openRoleModal(user)"
                    class="p-2 rounded-lg hover:bg-blue-50 text-gray-300 hover:text-blue-500 transition-all"
                    title="分配角色"
                  >
                    <Shield :size="15" />
                  </button>
                  <button
                    @click="openPermModal(user)"
                    class="p-2 rounded-lg hover:bg-purple-50 text-gray-300 hover:text-purple-500 transition-all"
                    title="专属权限"
                  >
                    <KeyRound :size="15" />
                  </button>
                  <button
                    @click="toggleStatus(user)"
                    class="p-2 rounded-lg transition-all"
                    :class="user.status === 'ACTIVE' ? 'hover:bg-red-50 text-gray-300 hover:text-red-400' : 'hover:bg-green-50 text-gray-300 hover:text-green-500'"
                    :title="user.status === 'ACTIVE' ? '禁用用户' : '启用用户'"
                  >
                    <component :is="user.status === 'ACTIVE' ? UserX : UserCheck" :size="15" />
                  </button>
                  <button
                    @click="handleDelete(user)"
                    class="p-2 rounded-lg hover:bg-red-50 text-gray-300 hover:text-red-400 transition-all"
                    title="删除用户"
                  >
                    <Trash2 :size="15" />
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Pagination -->
      <div class="px-5 py-3 border-t border-gray-50 flex items-center justify-between">
        <span class="text-xs text-gray-400">共 {{ total }} 条记录</span>
        <div class="flex items-center gap-2">
          <button
            @click="filter.page--; fetchUsers()"
            :disabled="filter.page <= 1"
            class="p-1.5 rounded-lg border border-gray-100 text-gray-400 disabled:opacity-40 hover:border-[#49b1f5]/30 hover:text-[#49b1f5] transition-all"
          >
            <ChevronLeft :size="14" />
          </button>
          <span class="text-xs text-gray-500">{{ filter.page }} / {{ pages }}</span>
          <button
            @click="filter.page++; fetchUsers()"
            :disabled="filter.page >= pages"
            class="p-1.5 rounded-lg border border-gray-100 text-gray-400 disabled:opacity-40 hover:border-[#49b1f5]/30 hover:text-[#49b1f5] transition-all"
          >
            <ChevronRight :size="14" />
          </button>
        </div>
      </div>
    </div>

    <!-- Role Assignment Modal -->
    <div v-if="showRoleModal" class="fixed inset-0 z-50 flex items-center justify-center p-4">
      <div class="absolute inset-0 bg-black/20 backdrop-blur-sm" @click="showRoleModal = false"></div>
      <div class="relative bg-white rounded-2xl shadow-2xl border border-gray-100 p-6 w-full max-w-sm">
        <h3 class="text-base font-bold text-[#2c3e50] mb-4">分配角色</h3>
        <div class="space-y-2 mb-6">
          <label
            v-for="role in roles"
            :key="role.id"
            class="flex items-center gap-3 px-4 py-3 rounded-xl border cursor-pointer transition-all"
            :class="editingRoles?.roleIds.includes(role.id) ? 'border-[#49b1f5]/30 bg-[#49b1f5]/5' : 'border-gray-100 hover:bg-gray-50'"
          >
            <input
              type="checkbox"
              :checked="editingRoles?.roleIds.includes(role.id)"
              @change="toggleRoleId(role.id)"
              class="w-4 h-4 accent-[#49b1f5]"
            />
            <div>
              <div class="text-sm font-medium text-[#2c3e50]">{{ role.name }}</div>
              <div class="text-xs text-gray-400">{{ role.code }}</div>
            </div>
          </label>
        </div>
        <div class="flex gap-3">
          <button @click="showRoleModal = false" class="flex-1 py-2.5 rounded-xl border border-gray-100 text-sm text-gray-500 hover:bg-gray-50 transition-colors">取消</button>
          <button @click="saveRoles" class="flex-1 py-2.5 rounded-xl bg-[#49b1f5] text-white text-sm font-medium hover:bg-[#3a9de8] transition-colors">保存</button>
        </div>
      </div>
    </div>

    <!-- User Permission Override Modal -->
    <div v-if="showPermModal" class="fixed inset-0 z-50 flex items-center justify-center p-4">
      <div class="absolute inset-0 bg-black/20 backdrop-blur-sm" @click="showPermModal = false"></div>
      <div class="relative bg-white rounded-2xl shadow-2xl border border-gray-100 p-6 w-full max-w-lg max-h-[85vh] overflow-hidden flex flex-col">
        <div class="flex items-center justify-between mb-1">
          <div>
            <h3 class="text-base font-bold text-[#2c3e50]">专属权限配置</h3>
            <p class="text-xs text-gray-400 mt-0.5">
              @{{ permUser?.username }} — 覆盖角色默认权限（继承 = 遵循角色设置）
            </p>
          </div>
          <button @click="showPermModal = false" class="p-1.5 rounded-lg hover:bg-gray-100 text-gray-400">
            <XCircle :size="18" />
          </button>
        </div>

        <!-- Legend -->
        <div class="flex items-center gap-4 py-3 mb-2 border-b border-gray-100 text-[10px] text-gray-400">
          <span class="flex items-center gap-1"><CheckCircle :size="12" class="text-green-500" /> 明确授权</span>
          <span class="flex items-center gap-1"><XCircle :size="12" class="text-red-400" /> 明确拒绝</span>
          <span class="flex items-center gap-1"><MinusCircle :size="12" class="text-gray-300" /> 继承角色</span>
          <span v-if="savingPerm" class="ml-auto text-[#49b1f5]">保存中...</span>
        </div>

        <div v-if="permLoading" class="space-y-3 overflow-y-auto flex-1">
          <div v-for="i in 6" :key="i" class="h-12 rounded-xl bg-gray-100 animate-pulse"></div>
        </div>

        <div v-else class="overflow-y-auto flex-1 space-y-1.5 pr-1">
          <div
            v-for="perm in allPermissions"
            :key="perm.id"
            class="flex items-center gap-3 px-4 py-3 rounded-xl border border-gray-100 hover:border-gray-200 transition-all"
          >
            <div class="flex-1 min-w-0">
              <div class="text-sm font-medium text-[#2c3e50]">{{ perm.name }}</div>
              <div class="text-[10px] font-mono text-gray-400">{{ perm.code }}</div>
            </div>
            <!-- Three-way toggle -->
            <div class="flex items-center gap-1 flex-shrink-0">
              <button
                @click="setOverride(perm.id, 'grant')"
                :disabled="savingPerm"
                class="p-1.5 rounded-lg transition-all disabled:opacity-40"
                :class="getOverrideState(perm.id) === 'grant'
                  ? 'bg-green-100 text-green-600'
                  : 'hover:bg-green-50 text-gray-300 hover:text-green-500'"
                title="明确授权"
              >
                <CheckCircle :size="16" />
              </button>
              <button
                @click="setOverride(perm.id, 'inherit')"
                :disabled="savingPerm"
                class="p-1.5 rounded-lg transition-all disabled:opacity-40"
                :class="getOverrideState(perm.id) === 'inherit'
                  ? 'bg-gray-200 text-gray-500'
                  : 'hover:bg-gray-100 text-gray-200 hover:text-gray-400'"
                title="继承角色"
              >
                <MinusCircle :size="16" />
              </button>
              <button
                @click="setOverride(perm.id, 'deny')"
                :disabled="savingPerm"
                class="p-1.5 rounded-lg transition-all disabled:opacity-40"
                :class="getOverrideState(perm.id) === 'deny'
                  ? 'bg-red-100 text-red-500'
                  : 'hover:bg-red-50 text-gray-300 hover:text-red-400'"
                title="明确拒绝"
              >
                <XCircle :size="16" />
              </button>
            </div>
          </div>
        </div>

        <div class="mt-4 pt-4 border-t border-gray-100">
          <button @click="showPermModal = false" class="w-full py-2.5 rounded-xl border border-gray-100 text-sm text-gray-500 hover:bg-gray-50 transition-colors">关闭</button>
        </div>
      </div>
    </div>
  </div>
</template>
