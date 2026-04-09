<script setup lang="ts">
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { User, Lock, Mail, Eye, EyeOff, LogIn, UserPlus } from 'lucide-vue-next';
import { login, register } from '@/api/auth';
import { useAuth } from '@/stores/auth';

const router = useRouter();
const auth = useAuth();

const mode = ref<'login' | 'register'>('login');
const loading = ref(false);
const error = ref('');
const showPassword = ref(false);

const loginForm = reactive({ username: '', password: '' });
const registerForm = reactive({ username: '', password: '', email: '', nickname: '' });

const handleLogin = async () => {
  if (!loginForm.username || !loginForm.password) {
    error.value = '请填写用户名和密码';
    return;
  }
  loading.value = true;
  error.value = '';
  try {
    const res = await login({ username: loginForm.username, password: loginForm.password });
    auth.setAuth(res.token, res.refreshToken, {
      id: res.user.id,
      username: res.user.username,
      nickname: res.user.nickname,
      avatar: res.user.avatar,
      email: res.user.email,
      roles: res.user.roles || [],
    });
    const redirect = (router.currentRoute.value.query.redirect as string) || '/';
    router.push(redirect);
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : '登录失败，请检查用户名和密码';
  } finally {
    loading.value = false;
  }
};

const handleRegister = async () => {
  if (!registerForm.username || !registerForm.password || !registerForm.email) {
    error.value = '请填写必填信息';
    return;
  }
  loading.value = true;
  error.value = '';
  try {
    const res = await register({
      username: registerForm.username,
      password: registerForm.password,
      email: registerForm.email,
      nickname: registerForm.nickname || undefined,
    });
    auth.setAuth(res.token, res.refreshToken, {
      id: res.user.id,
      username: res.user.username,
      nickname: res.user.nickname,
      avatar: res.user.avatar,
      email: res.user.email,
      roles: res.user.roles || [],
    });
    router.push('/');
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : '注册失败，请稍后再试';
  } finally {
    loading.value = false;
  }
};

const switchMode = (m: 'login' | 'register') => {
  mode.value = m;
  error.value = '';
};
</script>

<template>
  <div class="min-h-screen flex items-center justify-center px-4 py-16 relative overflow-hidden">
    <!-- Background decoration -->
    <div class="absolute inset-0 pointer-events-none overflow-hidden">
      <div class="absolute -top-40 -right-40 w-96 h-96 bg-[#49b1f5]/10 rounded-full blur-3xl"></div>
      <div class="absolute -bottom-40 -left-40 w-96 h-96 bg-purple-400/10 rounded-full blur-3xl"></div>
      <div class="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-64 h-64 bg-cyan-300/5 rounded-full blur-2xl"></div>
    </div>

    <div class="w-full max-w-md relative">
      <!-- Card -->
      <div class="bg-white/80 backdrop-blur-xl rounded-3xl shadow-[0_20px_60px_rgba(73,177,245,0.15)] border border-white/80 overflow-hidden">
        <!-- Header -->
        <div class="bg-gradient-to-r from-[#49b1f5]/20 to-purple-400/10 px-8 pt-10 pb-8">
          <div class="flex items-center justify-center mb-6">
            <div class="w-16 h-16 rounded-2xl bg-gradient-to-br from-[#49b1f5] to-[#3a9de8] flex items-center justify-center shadow-lg shadow-[#49b1f5]/30">
              <component :is="mode === 'login' ? LogIn : UserPlus" :size="28" class="text-white" stroke-width="1.5" />
            </div>
          </div>
          <h1 class="text-2xl font-bold text-center text-[#2c3e50] font-sans">
            {{ mode === 'login' ? '欢迎回来' : '加入我们' }}
          </h1>
          <p class="text-sm text-center text-gray-400 mt-2 font-light">
            {{ mode === 'login' ? '登录以开始写作和互动' : '注册新账号，开启创作之旅' }}
          </p>
        </div>

        <!-- Tab switcher -->
        <div class="flex px-8 -mt-4 gap-2">
          <button
            @click="switchMode('login')"
            class="flex-1 py-2.5 text-sm font-semibold rounded-xl transition-all duration-300"
            :class="mode === 'login'
              ? 'bg-[#49b1f5] text-white shadow-md shadow-[#49b1f5]/30'
              : 'bg-white/60 text-gray-400 hover:text-[#49b1f5] border border-gray-100'"
          >登录</button>
          <button
            @click="switchMode('register')"
            class="flex-1 py-2.5 text-sm font-semibold rounded-xl transition-all duration-300"
            :class="mode === 'register'
              ? 'bg-[#49b1f5] text-white shadow-md shadow-[#49b1f5]/30'
              : 'bg-white/60 text-gray-400 hover:text-[#49b1f5] border border-gray-100'"
          >注册</button>
        </div>

        <!-- Form -->
        <div class="px-8 pb-8 pt-6">
          <!-- Error -->
          <div v-if="error" class="mb-4 px-4 py-3 rounded-xl bg-red-50 border border-red-100 text-red-500 text-sm flex items-center gap-2">
            <span class="w-1.5 h-1.5 rounded-full bg-red-400 flex-shrink-0"></span>
            {{ error }}
          </div>

          <!-- Login Form -->
          <form v-if="mode === 'login'" @submit.prevent="handleLogin" class="space-y-4">
            <div class="relative">
              <User :size="16" class="absolute left-3.5 top-1/2 -translate-y-1/2 text-gray-300" />
              <input
                v-model="loginForm.username"
                type="text"
                placeholder="用户名"
                class="w-full pl-10 pr-4 py-3 rounded-xl border border-gray-100 bg-gray-50/80 text-sm text-[#2c3e50] placeholder-gray-300 focus:outline-none focus:border-[#49b1f5] focus:bg-white transition-all"
                autocomplete="username"
              />
            </div>
            <div class="relative">
              <Lock :size="16" class="absolute left-3.5 top-1/2 -translate-y-1/2 text-gray-300" />
              <input
                v-model="loginForm.password"
                :type="showPassword ? 'text' : 'password'"
                placeholder="密码"
                class="w-full pl-10 pr-10 py-3 rounded-xl border border-gray-100 bg-gray-50/80 text-sm text-[#2c3e50] placeholder-gray-300 focus:outline-none focus:border-[#49b1f5] focus:bg-white transition-all"
                autocomplete="current-password"
              />
              <button type="button" @click="showPassword = !showPassword" class="absolute right-3.5 top-1/2 -translate-y-1/2 text-gray-300 hover:text-[#49b1f5] transition-colors">
                <component :is="showPassword ? EyeOff : Eye" :size="16" />
              </button>
            </div>
            <button
              type="submit"
              :disabled="loading"
              class="w-full py-3 rounded-xl bg-gradient-to-r from-[#49b1f5] to-[#3a9de8] text-white font-semibold text-sm shadow-md shadow-[#49b1f5]/30 hover:shadow-lg hover:shadow-[#49b1f5]/40 hover:-translate-y-0.5 transition-all duration-300 disabled:opacity-60 disabled:cursor-not-allowed disabled:transform-none"
            >
              <span v-if="loading" class="inline-flex items-center gap-2">
                <svg class="animate-spin w-4 h-4" fill="none" viewBox="0 0 24 24">
                  <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/>
                  <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
                </svg>
                登录中...
              </span>
              <span v-else>登 录</span>
            </button>
          </form>

          <!-- Register Form -->
          <form v-else @submit.prevent="handleRegister" class="space-y-4">
            <div class="relative">
              <User :size="16" class="absolute left-3.5 top-1/2 -translate-y-1/2 text-gray-300" />
              <input
                v-model="registerForm.username"
                type="text"
                placeholder="用户名 *"
                class="w-full pl-10 pr-4 py-3 rounded-xl border border-gray-100 bg-gray-50/80 text-sm text-[#2c3e50] placeholder-gray-300 focus:outline-none focus:border-[#49b1f5] focus:bg-white transition-all"
                autocomplete="username"
              />
            </div>
            <div class="relative">
              <User :size="16" class="absolute left-3.5 top-1/2 -translate-y-1/2 text-gray-300" />
              <input
                v-model="registerForm.nickname"
                type="text"
                placeholder="昵称（可选）"
                class="w-full pl-10 pr-4 py-3 rounded-xl border border-gray-100 bg-gray-50/80 text-sm text-[#2c3e50] placeholder-gray-300 focus:outline-none focus:border-[#49b1f5] focus:bg-white transition-all"
              />
            </div>
            <div class="relative">
              <Mail :size="16" class="absolute left-3.5 top-1/2 -translate-y-1/2 text-gray-300" />
              <input
                v-model="registerForm.email"
                type="email"
                placeholder="邮箱 *"
                class="w-full pl-10 pr-4 py-3 rounded-xl border border-gray-100 bg-gray-50/80 text-sm text-[#2c3e50] placeholder-gray-300 focus:outline-none focus:border-[#49b1f5] focus:bg-white transition-all"
                autocomplete="email"
              />
            </div>
            <div class="relative">
              <Lock :size="16" class="absolute left-3.5 top-1/2 -translate-y-1/2 text-gray-300" />
              <input
                v-model="registerForm.password"
                :type="showPassword ? 'text' : 'password'"
                placeholder="密码 *"
                class="w-full pl-10 pr-10 py-3 rounded-xl border border-gray-100 bg-gray-50/80 text-sm text-[#2c3e50] placeholder-gray-300 focus:outline-none focus:border-[#49b1f5] focus:bg-white transition-all"
                autocomplete="new-password"
              />
              <button type="button" @click="showPassword = !showPassword" class="absolute right-3.5 top-1/2 -translate-y-1/2 text-gray-300 hover:text-[#49b1f5] transition-colors">
                <component :is="showPassword ? EyeOff : Eye" :size="16" />
              </button>
            </div>
            <button
              type="submit"
              :disabled="loading"
              class="w-full py-3 rounded-xl bg-gradient-to-r from-[#49b1f5] to-[#3a9de8] text-white font-semibold text-sm shadow-md shadow-[#49b1f5]/30 hover:shadow-lg hover:shadow-[#49b1f5]/40 hover:-translate-y-0.5 transition-all duration-300 disabled:opacity-60 disabled:cursor-not-allowed disabled:transform-none"
            >
              <span v-if="loading" class="inline-flex items-center gap-2">
                <svg class="animate-spin w-4 h-4" fill="none" viewBox="0 0 24 24">
                  <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/>
                  <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
                </svg>
                注册中...
              </span>
              <span v-else>注 册</span>
            </button>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>
