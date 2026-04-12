import { reactive, computed } from 'vue';

export interface UserInfo {
  id: number;
  username: string;
  nickname?: string;
  avatar?: string;
  email?: string;
  roles: string[];
  /** 用户有效功能权限码列表 */
  permissions?: string[];
}

const state = reactive({
  token: localStorage.getItem('token') || '',
  refreshToken: localStorage.getItem('refreshToken') || '',
  user: null as UserInfo | null,
});

// Try to restore user from localStorage
try {
  const stored = localStorage.getItem('user');
  if (stored) {
    state.user = JSON.parse(stored);
  }
} catch {
  // ignore
}

export const useAuth = () => {
  const isLoggedIn = computed(() => !!state.token);
  const isAdmin = computed(() =>
    state.user?.roles?.some((r) => r === 'ADMIN' || r === 'SUPER_ADMIN') ?? false
  );

  const hasPermission = (code: string): boolean => {
    if (isAdmin.value) return true; // admin 拥有所有权限
    return state.user?.permissions?.includes(code) ?? false;
  };

  const hasAnyPermission = (...codes: string[]): boolean =>
    codes.some((c) => hasPermission(c));

  const setAuth = (token: string, refreshToken: string, user: UserInfo) => {
    state.token = token;
    state.refreshToken = refreshToken;
    state.user = user;
    localStorage.setItem('token', token);
    localStorage.setItem('refreshToken', refreshToken);
    localStorage.setItem('user', JSON.stringify(user));
  };

  const clearAuth = () => {
    state.token = '';
    state.refreshToken = '';
    state.user = null;
    localStorage.removeItem('token');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('user');
  };

  const getToken = () => state.token;

  return {
    isLoggedIn,
    isAdmin,
    hasPermission,
    hasAnyPermission,
    user: computed(() => state.user),
    token: computed(() => state.token),
    setAuth,
    clearAuth,
    getToken,
  };
};
