import { useAuth } from '@/stores/auth';

/**
 * 权限检查 composable
 *
 * 用法:
 *   const { can, canAny } = usePermission();
 *   if (can('ARTICLE:DELETE:ANY')) { ... }
 *
 * 权限码示例:
 *   ARTICLE:CREATE       发布文章
 *   ARTICLE:EDIT         编辑文章
 *   ARTICLE:DELETE       删除自己的文章
 *   ARTICLE:DELETE:ANY   删除任意文章（管理员）
 *   COMMENT:CREATE       发表评论
 *   COMMENT:DELETE       删除自己的评论
 *   COMMENT:DELETE:ANY   删除任意评论（管理员）
 *   USER:MANAGE          管理用户
 *   ROLE:MANAGE          管理角色权限
 *   CONTENT:MANAGE       管理内容
 *   SYSTEM:MANAGE        管理系统设置
 */
export function usePermission() {
  const auth = useAuth();

  /** 是否拥有指定权限码 */
  const can = (code: string): boolean => auth.hasPermission(code);

  /** 是否拥有任意一个权限码 */
  const canAny = (...codes: string[]): boolean => auth.hasAnyPermission(...codes);

  /** 是否同时拥有所有权限码 */
  const canAll = (...codes: string[]): boolean => codes.every((c) => auth.hasPermission(c));

  return { can, canAny, canAll };
}

/** 所有功能权限码常量 */
export const PERMISSIONS = {
  ARTICLE_CREATE: 'ARTICLE:CREATE',
  ARTICLE_EDIT: 'ARTICLE:EDIT',
  ARTICLE_DELETE: 'ARTICLE:DELETE',
  ARTICLE_DELETE_ANY: 'ARTICLE:DELETE:ANY',
  COMMENT_CREATE: 'COMMENT:CREATE',
  COMMENT_DELETE: 'COMMENT:DELETE',
  COMMENT_DELETE_ANY: 'COMMENT:DELETE:ANY',
  USER_MANAGE: 'USER:MANAGE',
  ROLE_MANAGE: 'ROLE:MANAGE',
  CONTENT_MANAGE: 'CONTENT:MANAGE',
  SYSTEM_MANAGE: 'SYSTEM:MANAGE',
} as const;
