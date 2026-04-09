import { apiGet, apiPost, apiPut, apiDelete, apiPutParams } from './http';
import type { PageResponse, Article, Category, Tag, MenuResponse } from './blog';

// ===== Stats =====
export interface AdminStats {
  userCount: number;
  articleCount: number;
  publishedCount: number;
  draftCount: number;
  commentCount: number;
  pendingCommentCount: number;
  messageCount: number;
  wishCount: number;
  linkCount: number;
}

export const getAdminStats = () => apiGet<AdminStats>('/admin/stats/overview');

// ===== Users =====
export interface AdminUser {
  id: number;
  username: string;
  nickname?: string;
  email?: string;
  avatar?: string;
  bio?: string;
  status: string;
  roles: string[];
  createdAt?: string;
  lastLoginAt?: string;
}

export const getAdminUsers = (params: { page?: number; size?: number; keyword?: string } = {}) => {
  const q = new URLSearchParams();
  if (params.page) q.set('page', String(params.page));
  if (params.size) q.set('size', String(params.size));
  if (params.keyword) q.set('keyword', params.keyword);
  return apiGet<PageResponse<AdminUser>>(`/admin/users${q.toString() ? `?${q}` : ''}`);
};

export const updateUserStatus = (id: number, status: string) =>
  apiPutParams<void>(`/admin/users/${id}/status`, { status });

export const assignUserRoles = (id: number, roleIds: number[]) =>
  apiPut<void, number[]>(`/admin/users/${id}/roles`, roleIds);

export const deleteUser = (id: number) => apiDelete<void>(`/admin/users/${id}`);

// ===== Roles =====
export interface AdminRole {
  id: number;
  name: string;
  code: string;
  description?: string;
}

export const getAdminRoles = () => apiGet<AdminRole[]>('/admin/roles');

export const createRole = (role: Partial<AdminRole>) =>
  apiPost<AdminRole, Partial<AdminRole>>('/admin/roles', role);

export const updateRole = (id: number, role: Partial<AdminRole>) =>
  apiPut<AdminRole, Partial<AdminRole>>(`/admin/roles/${id}`, role);

export const deleteRole = (id: number) => apiDelete<void>(`/admin/roles/${id}`);
export const getRoleMenus = (roleId: number) => apiGet<number[]>(`/admin/roles/${roleId}/menus`);
export const updateRoleMenus = (roleId: number, menuIds: number[]) =>
  apiPut<void, number[]>(`/admin/roles/${roleId}/menus`, menuIds);

// ===== Articles =====
export interface AdminArticle {
  id: number;
  title: string;
  slug?: string;
  excerpt?: string;
  coverImage?: string;
  status: string;
  isTop?: boolean;
  views?: number;
  likes?: number;
  comments?: number;
  createdAt?: string;
  publishedAt?: string;
  category?: Category;
  tags?: Tag[];
  author?: { id: number; nickname: string; avatar?: string };
}

export const getAdminArticles = (params: {
  page?: number;
  size?: number;
  status?: string;
  categoryId?: number;
  keyword?: string;
} = {}) => {
  const q = new URLSearchParams();
  if (params.page) q.set('page', String(params.page));
  if (params.size) q.set('size', String(params.size));
  if (params.status) q.set('status', params.status);
  if (params.categoryId) q.set('categoryId', String(params.categoryId));
  if (params.keyword) q.set('keyword', params.keyword);
  return apiGet<PageResponse<AdminArticle>>(`/admin/articles${q.toString() ? `?${q}` : ''}`);
};

export const updateArticleStatus = (id: number, status: string) =>
  apiPutParams<void>(`/admin/articles/${id}/status`, { status });

export const reviewArticle = (id: number, action: 'APPROVE' | 'REJECT') =>
  apiPut<void, { action: string }>(`/admin/articles/${id}/review`, { action });

export const toggleArticleTop = (id: number, isTop: boolean) =>
  apiPutParams<void>(`/admin/articles/${id}/top`, { isTop });

export const deleteAdminArticle = (id: number) => apiDelete<void>(`/admin/articles/${id}`);

// ===== Menus =====
export const getAdminMenus = () => apiGet<MenuResponse[]>('/admin/menus');

// ===== Categories =====
export const getAdminCategories = (params: { page?: number; size?: number; keyword?: string } = {}) => {
  const q = new URLSearchParams();
  if (params.page) q.set('page', String(params.page));
  if (params.size) q.set('size', String(params.size));
  if (params.keyword) q.set('keyword', params.keyword);
  return apiGet<PageResponse<Category>>(`/admin/categories${q.toString() ? `?${q}` : ''}`);
};

export const getAllAdminCategories = () => apiGet<Category[]>('/admin/categories/all');

export const createCategory = (cat: Partial<Category>) =>
  apiPost<Category, Partial<Category>>('/admin/categories', cat);

export const updateCategory = (id: number, cat: Partial<Category>) =>
  apiPut<Category, Partial<Category>>(`/admin/categories/${id}`, cat);

export const deleteCategory = (id: number) => apiDelete<void>(`/admin/categories/${id}`);

// ===== Tags =====
export const getAdminTags = (params: { page?: number; size?: number; keyword?: string } = {}) => {
  const q = new URLSearchParams();
  if (params.page) q.set('page', String(params.page));
  if (params.size) q.set('size', String(params.size));
  if (params.keyword) q.set('keyword', params.keyword);
  return apiGet<PageResponse<Tag>>(`/admin/tags${q.toString() ? `?${q}` : ''}`);
};

export const getAllAdminTags = () => apiGet<Tag[]>('/admin/tags/all');

export const createTag = (tag: Partial<Tag>) =>
  apiPost<Tag, Partial<Tag>>('/admin/tags', tag);

export const updateTag = (id: number, tag: Partial<Tag>) =>
  apiPut<Tag, Partial<Tag>>(`/admin/tags/${id}`, tag);

export const deleteTag = (id: number) => apiDelete<void>(`/admin/tags/${id}`);

// ===== Comments =====
export interface AdminComment {
  id: number;
  content: string;
  articleId?: number;
  userId?: number;
  guestName?: string;
  guestEmail?: string;
  status: string;
  likes?: number;
  createdAt?: string;
}

export const getAdminComments = (params: {
  page?: number;
  size?: number;
  status?: string;
  articleId?: number;
  keyword?: string;
} = {}) => {
  const q = new URLSearchParams();
  if (params.page) q.set('page', String(params.page));
  if (params.size) q.set('size', String(params.size));
  if (params.status) q.set('status', params.status);
  if (params.articleId) q.set('articleId', String(params.articleId));
  if (params.keyword) q.set('keyword', params.keyword);
  return apiGet<PageResponse<AdminComment>>(`/admin/comments${q.toString() ? `?${q}` : ''}`);
};

export const updateCommentStatus = (id: number, status: string) =>
  apiPut<void, { status: string }>(`/admin/comments/${id}/status`, { status });

export const deleteComment = (id: number) => apiDelete<void>(`/admin/comments/${id}`);

// ===== Article creation for users =====
export interface ArticleCreatePayload {
  title: string;
  slug?: string;
  excerpt?: string;
  content: string;
  coverImage?: string;
  status?: string;
  categoryId?: number;
  tagIds?: number[];
  allowComment?: boolean;
}

export const createArticle = (payload: ArticleCreatePayload) =>
  apiPost<Article, ArticleCreatePayload>('/articles', payload);

export const updateArticle = (id: number, payload: ArticleCreatePayload) =>
  apiPut<Article, ArticleCreatePayload>(`/articles/${id}`, payload);
