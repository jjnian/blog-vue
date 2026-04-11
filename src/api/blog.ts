import { apiGet, apiPost, apiDelete } from './http';

export interface PageResponse<T> {
  records: T[];
  total: number;
  pages: number;
  current: number;
  size: number;
}

export interface Category {
  id: number;
  name: string;
  slug?: string;
  description?: string;
  color?: string;
  articleCount?: number;
}

export interface Tag {
  id: number;
  name: string;
  slug?: string;
  color?: string;
  articleCount?: number;
}

export interface Article {
  id: number;
  title: string;
  slug?: string;
  excerpt?: string;
  content?: string;
  coverImage?: string;
  views?: number;
  likes?: number;
  comments?: number;
  status?: string;
  isTop?: boolean;
  createdAt?: string;
  publishedAt?: string;
  category?: Category;
  tags?: Tag[];
}

export interface CommentUser {
  id?: number;
  name?: string;
  email?: string;
  avatar?: string;
}

export interface Comment {
  id: number;
  content: string;
  articleId?: number;
  parentId?: number;
  user?: CommentUser;
  status?: string;
  likes?: number;
  createdAt?: string;
  children?: Comment[];
}

export interface LinkItem {
  id: number;
  name: string;
  url: string;
  description?: string;
  avatar?: string;
}

export interface MessageItem {
  id: number;
  content: string;
  guestName?: string;
  guestAvatar?: string;
  animationType?: string;
  color?: string;
  fontSize?: string;
  createdAt?: string;
}

export interface WishItem {
  id: number;
  content: string;
  guestName?: string;
  guestAvatar?: string;
  color?: string;
  positionX?: number;
  positionY?: number;
  createdAt?: string;
}

export interface MenuResponse {
  id: number;
  parentId?: number | null;
  name: string;
  code: string;
  path?: string;
  icon?: string;
  component?: string;
  sortOrder?: number;
  visible?: boolean;
  children?: MenuResponse[];
}

export const getArticles = (params?: {
  page?: number;
  size?: number;
  status?: string;
  categoryId?: number;
  tagId?: number;
}) => {
  const query = new URLSearchParams();
  if (params?.page) query.set('page', String(params.page));
  if (params?.size) query.set('size', String(params.size));
  if (params?.status) query.set('status', params.status);
  if (params?.categoryId) query.set('categoryId', String(params.categoryId));
  if (params?.tagId) query.set('tagId', String(params.tagId));

  const search = query.toString();
  return apiGet<PageResponse<Article>>(`/articles${search ? `?${search}` : ''}`);
};

export const getArchives = () => apiGet<Array<[number, number]>>('/articles/archives');
export const getCategories = () => apiGet<Category[]>('/categories');
export const getTags = () => apiGet<Tag[]>('/tags');
export const getLinks = () => apiGet<LinkItem[]>('/links');

export const getPublicMenus = () => apiGet<MenuResponse[]>('/menus/public');
export const getUserMenus = () => apiGet<MenuResponse[]>('/menus/user');

export const getMessages = (page = 1, size = 20) =>
  apiGet<PageResponse<MessageItem>>(`/messages?page=${page}&size=${size}`);

export const createMessage = (payload: {
  content: string;
  guestName?: string;
  animationType?: string;
  color?: string;
  fontSize?: string;
  guestAvatar?: string;
}) => apiPost<MessageItem, typeof payload>('/messages', payload);

export const getWishes = (page = 1, size = 50) =>
  apiGet<PageResponse<WishItem>>(`/wishes?page=${page}&size=${size}`);

export const createWish = (payload: {
  content: string;
  guestName?: string;
  color?: string;
  positionX?: number;
  positionY?: number;
}) => apiPost<WishItem, typeof payload>('/wishes', payload);

export const getArticleBySlug = (slug: string) =>
  apiGet<Article>(`/articles/slug/${encodeURIComponent(slug)}`);

export const getArticleComments = (articleId: number) =>
  apiGet<Comment[]>(`/comments/article/${articleId}`);

export const createComment = (payload: {
  content: string;
  articleId: number;
  parentId?: number;
  guestName?: string;
  guestEmail?: string;
}) => apiPost<Comment, typeof payload>('/comments', payload);

export const likeComment = (id: number) =>
  apiPost<void, Record<string, never>>(`/comments/${id}/like`, {});

export const deleteComment = (id: number) =>
  apiDelete<void>(`/comments/${id}`);
