<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue';
import { MapPin, Users, Navigation, Filter, RefreshCw, Heart } from 'lucide-vue-next';

// 高德地图 key - 请替换为你自己的 key
const AMAP_KEY = '你的高德地图Key';

// 模拟用户数据
const mockUsers = ref([
  { id: 1, name: '小红', avatar: 'https://i.pravatar.cc/100?img=1', lat: 39.9042, lng: 116.4074, distance: 0, bio: '热爱旅行' },
  { id: 2, name: '小明', avatar: 'https://i.pravatar.cc/100?img=2', lat: 39.9142, lng: 116.4174, distance: 1.2, bio: '摄影师' },
  { id: 3, name: '小华', avatar: 'https://i.pravatar.cc/100?img=3', lat: 39.8942, lng: 116.3974, distance: 1.5, bio: '美食博主' },
  { id: 4, name: '小李', avatar: 'https://i.pravatar.cc/100?img=4', lat: 39.9242, lng: 116.4274, distance: 2.3, bio: '程序员' },
  { id: 5, name: '小王', avatar: 'https://i.pravatar.cc/100?img=5', lat: 39.8842, lng: 116.3874, distance: 2.8, bio: '设计师' },
  { id: 6, name: '小张', avatar: 'https://i.pravatar.cc/100?img=6', lat: 39.9342, lng: 116.4374, distance: 3.5, bio: '健身达人' },
  { id: 7, name: '小陈', avatar: 'https://i.pravatar.cc/100?img=7', lat: 39.8742, lng: 116.3774, distance: 4.2, bio: '音乐人' },
  { id: 8, name: '小周', avatar: 'https://i.pravatar.cc/100?img=8', lat: 39.9442, lng: 116.4474, distance: 5.1, bio: '旅行家' },
]);

const selectedDistance = ref(10);
const selectedUser = ref<typeof mockUsers.value[0] | null>(null);
const currentLocation = ref<{ lat: number; lng: number } | null>(null);
const isLoading = ref(true);
const mapRef = ref<any>(null);
const markersRef = ref<any[]>([]);

// 筛选后的用户列表
const filteredUsers = ref(mockUsers.value);

// 加载高德地图脚本
const loadAMapScript = (): Promise<void> => {
  return new Promise((resolve, reject) => {
    if ((window as any).AMap) {
      resolve();
      return;
    }

    const script = document.createElement('script');
    script.type = 'text/javascript';
    script.src = `https://webapi.amap.com/maps?v=2.0&key=${AMAP_KEY}`;
    script.onload = () => resolve();
    script.onerror = () => reject(new Error('加载地图失败'));
    document.head.appendChild(script);
  });
};

// 创建自定义标记
const createCustomMarker = (user: typeof mockUsers.value[0], AMap: any, map: any) => {
  // 创建标记容器
  const markerContent = document.createElement('div');
  markerContent.className = 'custom-marker';
  markerContent.style.cssText = `
    position: relative;
    cursor: pointer;
    transition: transform 0.3s ease;
  `;

  // 头像
  const avatar = document.createElement('img');
  avatar.src = user.avatar;
  avatar.style.cssText = `
    width: 48px;
    height: 48px;
    border-radius: 50%;
    border: 3px solid white;
    box-shadow: 0 4px 12px rgba(0,0,0,0.2);
    object-fit: cover;
  `;

  // 在线状态指示器
  const statusDot = document.createElement('div');
  statusDot.style.cssText = `
    position: absolute;
    bottom: 2px;
    right: 2px;
    width: 14px;
    height: 14px;
    background: #22c55e;
    border: 2px solid white;
    border-radius: 50%;
  `;

  markerContent.appendChild(avatar);
  markerContent.appendChild(statusDot);

  const marker = new AMap.Marker({
    position: [user.lng, user.lat],
    content: markerContent,
    offset: new AMap.Pixel(-24, -24),
    zIndex: 100,
  });

  // 点击事件
  marker.on('click', () => {
    selectedUser.value = user;
    map.setCenter([user.lng, user.lat]);
    map.setZoom(15);
  });

  // 悬停效果
  markerContent.addEventListener('mouseenter', () => {
    markerContent.style.transform = 'scale(1.2)';
  });
  markerContent.addEventListener('mouseleave', () => {
    markerContent.style.transform = 'scale(1)';
  });

  return marker;
};

// 初始化地图
const initMap = async () => {
  try {
    await loadAMapScript();
    const AMap = (window as any).AMap;

    // 创建地图实例
    const map = new AMap.Map('map-container', {
      zoom: 12,
      center: [116.4074, 39.9042], // 北京
      mapStyle: 'amap://styles/whitesmoke',
      viewMode: '2D',
    });

    mapRef.value = map;

    // 添加地图控件
    AMap.plugin(['AMap.ToolBar', 'AMap.Scale', 'AMap.Geolocation'], () => {
      map.addControl(new AMap.ToolBar());
      map.addControl(new AMap.Scale());

      // 定位控件
      const geolocation = new AMap.Geolocation({
        enableHighAccuracy: true,
        timeout: 10000,
        buttonPosition: 'RB',
        showButton: true,
        showMarker: true,
        showCircle: true,
      });
      map.addControl(geolocation);

      // 获取当前位置
      geolocation.getCurrentPosition((status: string, result: any) => {
        if (status === 'complete') {
          currentLocation.value = {
            lat: result.position.lat,
            lng: result.position.lng,
          };
          // 更新模拟数据距离
          updateDistances();
        }
      });
    });

    // 添加用户标记
    addMarkers();

    isLoading.value = false;
  } catch (error) {
    console.error('地图初始化失败:', error);
    isLoading.value = false;
  }
};

// 添加标记
const addMarkers = () => {
  const AMap = (window as any).AMap;
  if (!mapRef.value || !AMap) return;

  // 清除旧标记
  markersRef.value.forEach(marker => marker.setMap(null));
  markersRef.value = [];

  // 添加新标记
  filteredUsers.value.forEach(user => {
    const marker = createCustomMarker(user, AMap, mapRef.value);
    marker.setMap(mapRef.value);
    markersRef.value.push(marker);
  });
};

// 更新距离
const updateDistances = () => {
  if (!currentLocation.value) return;

  mockUsers.value.forEach(user => {
    user.distance = calculateDistance(
      currentLocation.value!.lat,
      currentLocation.value!.lng,
      user.lat,
      user.lng
    );
  });

  filterUsers();
};

// 计算两点距离（km）
const calculateDistance = (lat1: number, lng1: number, lat2: number, lng2: number): number => {
  const R = 6371;
  const dLat = (lat2 - lat1) * Math.PI / 180;
  const dLng = (lng2 - lng1) * Math.PI / 180;
  const a =
    Math.sin(dLat / 2) * Math.sin(dLat / 2) +
    Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
    Math.sin(dLng / 2) * Math.sin(dLng / 2);
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  return Math.round(R * c * 10) / 10;
};

// 筛选用户
const filterUsers = () => {
  filteredUsers.value = mockUsers.value.filter(
    user => user.distance <= selectedDistance.value
  );
  addMarkers();
};

// 刷新位置
const refreshLocation = () => {
  const AMap = (window as any).AMap;
  if (!mapRef.value || !AMap) return;

  isLoading.value = true;

  AMap.plugin('AMap.Geolocation', () => {
    const geolocation = new AMap.Geolocation({
      enableHighAccuracy: true,
      timeout: 10000,
    });

    geolocation.getCurrentPosition((status: string, result: any) => {
      if (status === 'complete') {
        currentLocation.value = {
          lat: result.position.lat,
          lng: result.position.lng,
        };
        mapRef.value.setCenter([result.position.lng, result.position.lat]);
        updateDistances();
      }
      isLoading.value = false;
    });
  });
};

// 查看用户详情
const viewUser = (user: typeof mockUsers.value[0]) => {
  selectedUser.value = user;
  if (mapRef.value) {
    mapRef.value.setCenter([user.lng, user.lat]);
    mapRef.value.setZoom(15);
  }
};

// 关闭详情面板
const closeDetail = () => {
  selectedUser.value = null;
};

onMounted(() => {
  initMap();
});

onUnmounted(() => {
  if (mapRef.value) {
    mapRef.value.destroy();
  }
});
</script>

<template>
  <div class="min-h-screen bg-gradient-to-br from-blue-50 via-white to-purple-50">
    <!-- 头部 -->
    <header class="bg-white/80 backdrop-blur-xl border-b border-gray-100 sticky top-0 z-50">
      <div class="max-w-7xl mx-auto px-6 py-4 flex items-center justify-between">
        <div class="flex items-center space-x-3">
          <div class="w-10 h-10 rounded-xl bg-gradient-to-br from-blue-500 to-purple-500 flex items-center justify-center">
            <Users class="w-5 h-5 text-white" />
          </div>
          <div>
            <h1 class="text-xl font-bold text-gray-800">附近的朋友</h1>
            <p class="text-xs text-gray-400">发现身边有趣的人</p>
          </div>
        </div>

        <div class="flex items-center space-x-4">
          <!-- 距离筛选 -->
          <div class="flex items-center space-x-2 bg-gray-50 rounded-full px-4 py-2">
            <Filter class="w-4 h-4 text-gray-400" />
            <select
              v-model="selectedDistance"
              @change="filterUsers"
              class="bg-transparent text-sm text-gray-600 outline-none cursor-pointer"
            >
              <option :value="5">5km 内</option>
              <option :value="10">10km 内</option>
              <option :value="20">20km 内</option>
              <option :value="50">50km 内</option>
              <option :value="100">100km 内</option>
            </select>
          </div>

          <!-- 刷新按钮 -->
          <button
            @click="refreshLocation"
            :disabled="isLoading"
            class="flex items-center space-x-2 bg-blue-500 hover:bg-blue-600 text-white px-4 py-2 rounded-full transition-all duration-300 disabled:opacity-50"
          >
            <RefreshCw class="w-4 h-4" :class="{ 'animate-spin': isLoading }" />
            <span class="text-sm">刷新</span>
          </button>
        </div>
      </div>
    </header>

    <!-- 主内容 -->
    <div class="flex h-[calc(100vh-73px)]">
      <!-- 左侧用户列表 -->
      <aside class="w-80 bg-white/50 backdrop-blur-xl border-r border-gray-100 overflow-y-auto">
        <div class="p-4">
          <div class="flex items-center justify-between mb-4">
            <h2 class="font-semibold text-gray-700">
              附近 {{ filteredUsers.length }} 人
            </h2>
            <span v-if="currentLocation" class="text-xs text-green-500 flex items-center">
              <span class="w-2 h-2 bg-green-500 rounded-full mr-1 animate-pulse"></span>
              已定位
            </span>
          </div>

          <!-- 用户列表 -->
          <div class="space-y-3">
            <div
              v-for="user in filteredUsers"
              :key="user.id"
              @click="viewUser(user)"
              :class="[
                'p-4 rounded-2xl cursor-pointer transition-all duration-300',
                selectedUser?.id === user.id
                  ? 'bg-blue-50 border-2 border-blue-200 shadow-lg'
                  : 'bg-white hover:bg-gray-50 border border-gray-100 hover:shadow-md'
              ]"
            >
              <div class="flex items-center space-x-3">
                <img
                  :src="user.avatar"
                  :alt="user.name"
                  class="w-12 h-12 rounded-full object-cover border-2 border-white shadow-md"
                />
                <div class="flex-1 min-w-0">
                  <h3 class="font-semibold text-gray-800 truncate">{{ user.name }}</h3>
                  <p class="text-xs text-gray-400 truncate">{{ user.bio }}</p>
                </div>
              </div>
              <div class="flex items-center justify-between mt-3 pt-3 border-t border-gray-50">
                <div class="flex items-center text-gray-400 text-xs">
                  <Navigation class="w-3 h-3 mr-1" />
                  {{ user.distance }} km
                </div>
                <Heart class="w-4 h-4 text-gray-300 hover:text-red-400 transition-colors cursor-pointer" />
              </div>
            </div>
          </div>

          <!-- 空状态 -->
          <div v-if="filteredUsers.length === 0" class="text-center py-12">
            <Users class="w-16 h-16 text-gray-200 mx-auto mb-4" />
            <p class="text-gray-400">附近暂无朋友</p>
            <p class="text-xs text-gray-300 mt-1">尝试扩大搜索范围</p>
          </div>
        </div>
      </aside>

      <!-- 地图区域 -->
      <main class="flex-1 relative">
        <!-- 加载状态 -->
        <div v-if="isLoading" class="absolute inset-0 bg-white/80 backdrop-blur-sm flex items-center justify-center z-10">
          <div class="text-center">
            <div class="w-16 h-16 border-4 border-blue-200 border-t-blue-500 rounded-full animate-spin mx-auto mb-4"></div>
            <p class="text-gray-500">加载地图中...</p>
          </div>
        </div>

        <!-- 地图容器 -->
        <div id="map-container" class="w-full h-full"></div>

        <!-- 用户详情浮窗 -->
        <Transition name="slide-up">
          <div
            v-if="selectedUser"
            class="absolute bottom-6 left-1/2 -translate-x-1/2 bg-white rounded-3xl shadow-2xl p-6 w-96 z-20 border border-gray-100"
          >
            <button
              @click="closeDetail"
              class="absolute top-4 right-4 text-gray-400 hover:text-gray-600 transition-colors"
            >
              ✕
            </button>

            <div class="flex items-center space-x-4 mb-4">
              <img
                :src="selectedUser.avatar"
                :alt="selectedUser.name"
                class="w-16 h-16 rounded-full object-cover border-4 border-white shadow-lg"
              />
              <div>
                <h3 class="text-lg font-bold text-gray-800">{{ selectedUser.name }}</h3>
                <p class="text-sm text-gray-400">{{ selectedUser.bio }}</p>
              </div>
            </div>

            <div class="flex items-center justify-between text-sm text-gray-500 mb-4 pb-4 border-b border-gray-100">
              <div class="flex items-center">
                <Navigation class="w-4 h-4 mr-1 text-blue-500" />
                距离你 {{ selectedUser.distance }} km
              </div>
              <div class="flex items-center text-green-500">
                <span class="w-2 h-2 bg-green-500 rounded-full mr-1"></span>
                在线
              </div>
            </div>

            <div class="flex space-x-3">
              <button class="flex-1 bg-blue-500 hover:bg-blue-600 text-white py-3 rounded-xl transition-all duration-300 font-medium">
                打招呼
              </button>
              <button class="flex-1 bg-gray-100 hover:bg-gray-200 text-gray-700 py-3 rounded-xl transition-all duration-300 font-medium">
                关注
              </button>
            </div>
          </div>
        </Transition>
      </main>
    </div>
  </div>
</template>

<style scoped>
/* 地图样式覆盖 */
:deep(.amap-logo),
:deep(.amap-copyright) {
  display: none !important;
}

/* 自定义标记悬停效果 */
.custom-marker:hover {
  transform: scale(1.2);
}

/* 滑入动画 */
.slide-up-enter-active,
.slide-up-leave-active {
  transition: all 0.3s ease;
}

.slide-up-enter-from,
.slide-up-leave-to {
  opacity: 0;
  transform: translate(-50%, 20px);
}
</style>