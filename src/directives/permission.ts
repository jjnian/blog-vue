import type { Directive } from 'vue';
import { useAuth } from '@/stores/auth';

/**
 * v-permission 指令 - 根据权限控制元素显示/隐藏
 *
 * 用法:
 *   v-permission="'ARTICLE:DELETE'"          - 单个权限
 *   v-permission="['ARTICLE:DELETE', 'ARTICLE:DELETE:ANY']"  - 任意一个满足即显示
 */
export const permissionDirective: Directive<HTMLElement, string | string[]> = {
  mounted(el, binding) {
    applyPermission(el, binding.value);
  },
  updated(el, binding) {
    applyPermission(el, binding.value);
  },
};

function applyPermission(el: HTMLElement, value: string | string[]) {
  const auth = useAuth();
  const codes = Array.isArray(value) ? value : [value];
  const allowed = codes.some((code) => auth.hasPermission(code));
  if (!allowed) {
    el.style.display = 'none';
  } else {
    el.style.display = '';
  }
}
