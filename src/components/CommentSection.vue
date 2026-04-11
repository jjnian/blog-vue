<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { Heart, MessageCircle, Send, ChevronDown, User, X, Loader2 } from 'lucide-vue-next';
import { getArticleComments, createComment, likeComment, deleteComment } from '@/api/blog';
import type { Comment } from '@/api/blog';
import { useAuth } from '@/stores/auth';

const props = defineProps<{
  articleId: number;
  allowComment?: boolean;
}>();

const auth = useAuth();
const comments = ref<Comment[]>([]);
const loading = ref(true);
const submitting = ref(false);
const error = ref('');
const successMsg = ref('');

// Main comment form
const form = ref({
  content: '',
  guestName: '',
  guestEmail: '',
});

// Reply state: commentId -> open, replyTo name
const replyingTo = ref<{ id: number; name: string } | null>(null);
const replyForm = ref({
  content: '',
  guestName: '',
  guestEmail: '',
});

// Like tracking (optimistic UI)
const likedIds = ref<Set<number>>(new Set());

const isLoggedIn = computed(() => auth.isLoggedIn.value);
const currentUser = computed(() => auth.user.value);
const isAdmin = computed(() => auth.isAdmin.value);

// Relative time
function timeAgo(dateStr?: string): string {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  const now = Date.now();
  const diff = now - date.getTime();
  const mins = Math.floor(diff / 60000);
  if (mins < 1) return '刚刚';
  if (mins < 60) return `${mins} 分钟前`;
  const hours = Math.floor(mins / 60);
  if (hours < 24) return `${hours} 小时前`;
  const days = Math.floor(hours / 24);
  if (days < 30) return `${days} 天前`;
  const months = Math.floor(days / 30);
  if (months < 12) return `${months} 个月前`;
  const pad = (n: number) => String(n).padStart(2, '0');
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`;
}

// Avatar color based on name
const AVATAR_COLORS = [
  '#49b1f5', '#ff6b6b', '#ffa94d', '#69db7c', '#f06595',
  '#74c0fc', '#a9e34b', '#e599f7', '#66d9e8', '#ffd43b',
];
function avatarColor(name?: string): string {
  if (!name) return AVATAR_COLORS[0];
  let hash = 0;
  for (let i = 0; i < name.length; i++) hash = name.charCodeAt(i) + ((hash << 5) - hash);
  return AVATAR_COLORS[Math.abs(hash) % AVATAR_COLORS.length];
}

function getDisplayName(comment: Comment): string {
  return comment.user?.name || (comment.user?.id ? `用户${comment.user.id}` : '匿名用户');
}

function getAvatarLetter(comment: Comment): string {
  const name = getDisplayName(comment);
  return name.charAt(0).toUpperCase();
}

const totalCount = computed(() => {
  let count = 0;
  const walk = (list: Comment[]) => {
    count += list.length;
    list.forEach(c => c.children && walk(c.children));
  };
  walk(comments.value);
  return count;
});

async function loadComments() {
  loading.value = true;
  try {
    comments.value = await getArticleComments(props.articleId);
  } catch {
    comments.value = [];
  } finally {
    loading.value = false;
  }
}

async function submitComment() {
  const content = form.value.content.trim();
  if (!content) return;
  if (!isLoggedIn.value && !form.value.guestName.trim()) {
    error.value = '请填写昵称';
    return;
  }
  submitting.value = true;
  error.value = '';
  try {
    await createComment({
      content,
      articleId: props.articleId,
      guestName: isLoggedIn.value ? undefined : form.value.guestName.trim(),
      guestEmail: isLoggedIn.value ? undefined : form.value.guestEmail.trim() || undefined,
    });
    form.value.content = '';
    form.value.guestName = '';
    form.value.guestEmail = '';
    successMsg.value = isLoggedIn.value ? '评论发表成功！' : '评论已提交，等待审核通过后显示。';
    setTimeout(() => { successMsg.value = ''; }, 4000);
    await loadComments();
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : '发表失败，请重试';
  } finally {
    submitting.value = false;
  }
}

async function submitReply() {
  if (!replyingTo.value) return;
  const content = replyForm.value.content.trim();
  if (!content) return;
  if (!isLoggedIn.value && !replyForm.value.guestName.trim()) {
    error.value = '请填写昵称';
    return;
  }
  submitting.value = true;
  error.value = '';
  try {
    await createComment({
      content,
      articleId: props.articleId,
      parentId: replyingTo.value.id,
      guestName: isLoggedIn.value ? undefined : replyForm.value.guestName.trim(),
      guestEmail: isLoggedIn.value ? undefined : replyForm.value.guestEmail.trim() || undefined,
    });
    replyForm.value = { content: '', guestName: '', guestEmail: '' };
    replyingTo.value = null;
    successMsg.value = isLoggedIn.value ? '回复发表成功！' : '回复已提交，等待审核通过后显示。';
    setTimeout(() => { successMsg.value = ''; }, 4000);
    await loadComments();
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : '回复失败，请重试';
  } finally {
    submitting.value = false;
  }
}

async function handleLike(comment: Comment) {
  if (likedIds.value.has(comment.id)) return;
  likedIds.value.add(comment.id);
  comment.likes = (comment.likes || 0) + 1;
  try {
    await likeComment(comment.id);
  } catch {
    likedIds.value.delete(comment.id);
    comment.likes = Math.max(0, (comment.likes || 1) - 1);
  }
}

async function handleDelete(comment: Comment) {
  if (!confirm('确认删除这条评论？')) return;
  try {
    await deleteComment(comment.id);
    await loadComments();
  } catch {
    // ignore
  }
}

function startReply(comment: Comment) {
  const name = getDisplayName(comment);
  if (replyingTo.value?.id === comment.id) {
    replyingTo.value = null;
  } else {
    replyingTo.value = { id: comment.id, name };
    replyForm.value = { content: '', guestName: '', guestEmail: '' };
  }
}

function cancelReply() {
  replyingTo.value = null;
  replyForm.value = { content: '', guestName: '', guestEmail: '' };
}

onMounted(loadComments);
</script>

<template>
  <section class="mt-12">
    <!-- Header -->
    <div class="flex items-center gap-3 mb-8">
      <div class="w-1 h-6 bg-[#49b1f5] rounded-full"></div>
      <h2 class="text-lg font-bold text-gray-800">
        评论
        <span class="ml-2 text-sm font-normal text-gray-400">({{ totalCount }})</span>
      </h2>
    </div>

    <!-- Comment Form -->
    <div v-if="allowComment !== false" class="bg-white rounded-2xl border border-gray-100 shadow-sm p-5 mb-8">
      <!-- User avatar row -->
      <div class="flex gap-3">
        <!-- Avatar -->
        <div class="flex-shrink-0">
          <div
            v-if="currentUser?.avatar"
            class="w-9 h-9 rounded-full overflow-hidden"
          >
            <img :src="currentUser.avatar" :alt="currentUser.nickname || currentUser.username" class="w-full h-full object-cover" />
          </div>
          <div
            v-else
            class="w-9 h-9 rounded-full flex items-center justify-center text-white text-sm font-bold"
            :style="{ background: avatarColor(isLoggedIn ? (currentUser?.nickname || currentUser?.username) : '游') }"
          >
            {{ isLoggedIn ? (currentUser?.nickname || currentUser?.username || '我').charAt(0).toUpperCase() : '游' }}
          </div>
        </div>

        <!-- Textarea -->
        <div class="flex-1">
          <textarea
            v-model="form.content"
            placeholder="说点什么吧..."
            rows="3"
            maxlength="1000"
            class="w-full px-4 py-3 rounded-xl border border-gray-200 focus:border-[#49b1f5] focus:ring-2 focus:ring-[#49b1f5]/20 outline-none resize-none text-sm text-gray-700 placeholder-gray-300 transition-all duration-200"
          ></textarea>

          <!-- Guest fields -->
          <div v-if="!isLoggedIn" class="flex gap-2 mt-2">
            <input
              v-model="form.guestName"
              type="text"
              placeholder="昵称（必填）"
              maxlength="30"
              class="flex-1 px-3 py-2 rounded-lg border border-gray-200 focus:border-[#49b1f5] focus:ring-2 focus:ring-[#49b1f5]/20 outline-none text-xs text-gray-700 placeholder-gray-300 transition-all duration-200"
            />
            <input
              v-model="form.guestEmail"
              type="email"
              placeholder="邮箱（选填）"
              maxlength="100"
              class="flex-1 px-3 py-2 rounded-lg border border-gray-200 focus:border-[#49b1f5] focus:ring-2 focus:ring-[#49b1f5]/20 outline-none text-xs text-gray-700 placeholder-gray-300 transition-all duration-200"
            />
          </div>

          <!-- Actions row -->
          <div class="flex items-center justify-between mt-3">
            <span class="text-[11px] text-gray-300">{{ form.content.length }} / 1000</span>
            <button
              @click="submitComment"
              :disabled="submitting || !form.content.trim()"
              class="flex items-center gap-1.5 px-4 py-1.5 bg-[#49b1f5] text-white text-xs font-medium rounded-lg hover:bg-[#36a3e8] disabled:opacity-50 disabled:cursor-not-allowed transition-all duration-200"
            >
              <Loader2 v-if="submitting" :size="12" class="animate-spin" />
              <Send v-else :size="12" stroke-width="2" />
              发表
            </button>
          </div>
        </div>
      </div>

      <!-- Success / Error messages -->
      <p v-if="successMsg" class="mt-3 text-xs text-green-600 bg-green-50 rounded-lg px-3 py-2">{{ successMsg }}</p>
      <p v-if="error" class="mt-3 text-xs text-red-500 bg-red-50 rounded-lg px-3 py-2">{{ error }}</p>
    </div>

    <!-- Disabled -->
    <div v-else class="bg-gray-50 rounded-2xl border border-gray-100 p-5 mb-8 text-center text-sm text-gray-400">
      本文章已关闭评论
    </div>

    <!-- Loading -->
    <div v-if="loading" class="space-y-4">
      <div v-for="i in 3" :key="i" class="bg-white rounded-2xl border border-gray-100 p-5 animate-pulse">
        <div class="flex gap-3">
          <div class="w-9 h-9 rounded-full bg-gray-100 flex-shrink-0"></div>
          <div class="flex-1 space-y-2">
            <div class="h-3 bg-gray-100 rounded w-24"></div>
            <div class="h-3 bg-gray-100 rounded w-full"></div>
            <div class="h-3 bg-gray-100 rounded w-3/4"></div>
          </div>
        </div>
      </div>
    </div>

    <!-- Empty state -->
    <div v-else-if="!loading && comments.length === 0" class="text-center py-12">
      <div class="w-16 h-16 mx-auto mb-4 rounded-full bg-gray-50 flex items-center justify-center">
        <MessageCircle :size="28" class="text-gray-200" stroke-width="1.5" />
      </div>
      <p class="text-sm text-gray-400">暂无评论，快来抢沙发吧！</p>
    </div>

    <!-- Comment list -->
    <div v-else class="space-y-4">
      <div
        v-for="comment in comments"
        :key="comment.id"
        class="bg-white rounded-2xl border border-gray-100 shadow-sm overflow-hidden"
      >
        <!-- Top-level comment -->
        <div class="p-5">
          <div class="flex gap-3">
            <!-- Avatar -->
            <div class="flex-shrink-0">
              <div
                v-if="comment.user?.avatar"
                class="w-9 h-9 rounded-full overflow-hidden"
              >
                <img :src="comment.user.avatar" alt="" class="w-full h-full object-cover" />
              </div>
              <div
                v-else
                class="w-9 h-9 rounded-full flex items-center justify-center text-white text-sm font-bold"
                :style="{ background: avatarColor(getDisplayName(comment)) }"
              >
                {{ getAvatarLetter(comment) }}
              </div>
            </div>

            <div class="flex-1 min-w-0">
              <!-- Name + time -->
              <div class="flex items-center justify-between gap-2 mb-1.5">
                <div class="flex items-center gap-2 flex-wrap">
                  <span class="text-sm font-semibold text-gray-800">{{ getDisplayName(comment) }}</span>
                  <span
                    v-if="comment.user?.id && isLoggedIn && currentUser?.id === comment.user.id"
                    class="text-[10px] px-1.5 py-0.5 bg-[#49b1f5]/10 text-[#49b1f5] rounded-full"
                  >我</span>
                </div>
                <span class="text-[11px] text-gray-300 whitespace-nowrap">{{ timeAgo(comment.createdAt) }}</span>
              </div>

              <!-- Content -->
              <p class="text-sm text-gray-700 leading-relaxed break-words whitespace-pre-wrap">{{ comment.content }}</p>

              <!-- Actions -->
              <div class="flex items-center gap-4 mt-3">
                <button
                  @click="handleLike(comment)"
                  :class="[
                    'flex items-center gap-1 text-[11px] transition-all duration-200',
                    likedIds.has(comment.id) ? 'text-red-400' : 'text-gray-400 hover:text-red-400'
                  ]"
                >
                  <Heart
                    :size="13"
                    :fill="likedIds.has(comment.id) ? 'currentColor' : 'none'"
                    stroke-width="2"
                  />
                  {{ comment.likes || 0 }}
                </button>
                <button
                  v-if="allowComment !== false"
                  @click="startReply(comment)"
                  class="flex items-center gap-1 text-[11px] text-gray-400 hover:text-[#49b1f5] transition-colors duration-200"
                >
                  <MessageCircle :size="13" stroke-width="2" />
                  回复
                </button>
                <button
                  v-if="isAdmin || (isLoggedIn && comment.user?.id === currentUser?.id)"
                  @click="handleDelete(comment)"
                  class="flex items-center gap-1 text-[11px] text-gray-300 hover:text-red-400 transition-colors duration-200 ml-auto"
                >
                  <X :size="12" stroke-width="2" />
                  删除
                </button>
              </div>

              <!-- Reply form for this comment -->
              <div
                v-if="replyingTo?.id === comment.id"
                class="mt-4 pl-4 border-l-2 border-[#49b1f5]/30"
              >
                <div class="flex gap-2">
                  <div
                    class="w-7 h-7 rounded-full flex items-center justify-center text-white text-xs font-bold flex-shrink-0"
                    :style="{ background: avatarColor(isLoggedIn ? (currentUser?.nickname || currentUser?.username) : replyForm.guestName || '游') }"
                  >
                    {{ isLoggedIn ? (currentUser?.nickname || currentUser?.username || '我').charAt(0).toUpperCase() : (replyForm.guestName || '游').charAt(0).toUpperCase() }}
                  </div>
                  <div class="flex-1">
                    <textarea
                      v-model="replyForm.content"
                      :placeholder="`回复 @${replyingTo.name}...`"
                      rows="2"
                      maxlength="500"
                      class="w-full px-3 py-2 rounded-lg border border-gray-200 focus:border-[#49b1f5] focus:ring-2 focus:ring-[#49b1f5]/20 outline-none resize-none text-xs text-gray-700 placeholder-gray-300 transition-all duration-200"
                    ></textarea>
                    <div v-if="!isLoggedIn" class="flex gap-2 mt-1.5">
                      <input
                        v-model="replyForm.guestName"
                        type="text"
                        placeholder="昵称（必填）"
                        maxlength="30"
                        class="flex-1 px-2.5 py-1.5 rounded-lg border border-gray-200 focus:border-[#49b1f5] outline-none text-xs text-gray-700 placeholder-gray-300"
                      />
                      <input
                        v-model="replyForm.guestEmail"
                        type="email"
                        placeholder="邮箱（选填）"
                        maxlength="100"
                        class="flex-1 px-2.5 py-1.5 rounded-lg border border-gray-200 focus:border-[#49b1f5] outline-none text-xs text-gray-700 placeholder-gray-300"
                      />
                    </div>
                    <div class="flex items-center justify-end gap-2 mt-2">
                      <button
                        @click="cancelReply"
                        class="px-3 py-1 text-xs text-gray-400 hover:text-gray-600 transition-colors duration-200"
                      >
                        取消
                      </button>
                      <button
                        @click="submitReply"
                        :disabled="submitting || !replyForm.content.trim()"
                        class="flex items-center gap-1 px-3 py-1 bg-[#49b1f5] text-white text-xs rounded-lg hover:bg-[#36a3e8] disabled:opacity-50 disabled:cursor-not-allowed transition-all duration-200"
                      >
                        <Loader2 v-if="submitting" :size="10" class="animate-spin" />
                        <Send v-else :size="10" stroke-width="2" />
                        回复
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Nested replies -->
        <div
          v-if="comment.children && comment.children.length > 0"
          class="border-t border-gray-50 bg-gray-50/50"
        >
          <div class="px-5 py-3 space-y-4">
            <div
              v-for="child in comment.children"
              :key="child.id"
              class="flex gap-3"
            >
              <!-- Reply avatar -->
              <div class="flex-shrink-0">
                <div
                  v-if="child.user?.avatar"
                  class="w-7 h-7 rounded-full overflow-hidden"
                >
                  <img :src="child.user.avatar" alt="" class="w-full h-full object-cover" />
                </div>
                <div
                  v-else
                  class="w-7 h-7 rounded-full flex items-center justify-center text-white text-[11px] font-bold"
                  :style="{ background: avatarColor(getDisplayName(child)) }"
                >
                  {{ getAvatarLetter(child) }}
                </div>
              </div>

              <div class="flex-1 min-w-0">
                <div class="flex items-center justify-between gap-2 mb-1">
                  <span class="text-xs font-semibold text-gray-700">{{ getDisplayName(child) }}</span>
                  <span class="text-[10px] text-gray-300 whitespace-nowrap">{{ timeAgo(child.createdAt) }}</span>
                </div>
                <p class="text-xs text-gray-600 leading-relaxed break-words whitespace-pre-wrap">
                  <span class="text-[#49b1f5] font-medium mr-1">
                    @{{ getDisplayName(comment) }}
                  </span>
                  {{ child.content }}
                </p>
                <div class="flex items-center gap-3 mt-2">
                  <button
                    @click="handleLike(child)"
                    :class="[
                      'flex items-center gap-1 text-[10px] transition-all duration-200',
                      likedIds.has(child.id) ? 'text-red-400' : 'text-gray-400 hover:text-red-400'
                    ]"
                  >
                    <Heart
                      :size="11"
                      :fill="likedIds.has(child.id) ? 'currentColor' : 'none'"
                      stroke-width="2"
                    />
                    {{ child.likes || 0 }}
                  </button>
                  <button
                    v-if="allowComment !== false"
                    @click="startReply(comment)"
                    class="flex items-center gap-1 text-[10px] text-gray-400 hover:text-[#49b1f5] transition-colors duration-200"
                  >
                    <MessageCircle :size="11" stroke-width="2" />
                    回复
                  </button>
                  <button
                    v-if="isAdmin || (isLoggedIn && child.user?.id === currentUser?.id)"
                    @click="handleDelete(child)"
                    class="flex items-center gap-1 text-[10px] text-gray-300 hover:text-red-400 transition-colors duration-200 ml-auto"
                  >
                    <X :size="11" stroke-width="2" />
                    删除
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Load more hint -->
    <div v-if="totalCount > 0" class="flex justify-center mt-6">
      <span class="text-xs text-gray-300">· 共 {{ totalCount }} 条评论 ·</span>
    </div>
  </section>
</template>
