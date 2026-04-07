<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue';
import { Send } from 'lucide-vue-next';

interface Danmu {
  id: number;
  text: string;
  avatar: string;
  top: string;
  left?: string;
  duration: string;
  delay: string;
  fontSize: string;
  opacity: number;
  glowColor: string;
  zIndex: number;
  animationClass: string;
  wobbleIntensity: number;
  timingFunction: string;
}

const danmuList = ref<Danmu[]>([]);
const nextId = ref(0);
const userInput = ref('');

const messages = [
  "没有源码我很难分析啊",
  "我在哪",
  "大佬真厉害",
  "牛牛牛",
  "这个页面真的棒",
  "歪比巴卜",
  "网页真漂亮",
  "👍",
  "好看的紧",
  "牛",
  "监控",
  "加油！",
  "牛啊",
  "好强，属实好看",
  "真的很牛逼，2026.04.07",
  "有点意思",
  "hahaha",
  "厉害！",
  "网站",
  "弹幕功能很强！",
  "大佬页",
  "这个弹幕真的很好玩",
  "我超",
  "漂亮",
  "路过",
  "666666",
  "omg",
  "你好啊",
  "云想衣裳花想容，春风拂槛露华浓。",
  "两情若是久长时，又岂在朝朝暮暮。",
  "人生若只如初见，何事秋风悲画扇。",
  "众里寻他千百度，蓦然回首，那人却在，灯火阑珊处。",
  "落霞与孤鹜齐飞，秋水共长天一色。",
  "星垂平野阔，月涌大江流。",
  "海上生明月，天涯共此时。",
  "大漠孤烟直，长河落日圆。"
];

const glowColors = [
  'rgba(255, 255, 255, 0.2)',
  'rgba(147, 197, 253, 0.3)', // blue
  'rgba(167, 243, 208, 0.3)', // green
  'rgba(253, 164, 175, 0.3)', // rose
  'rgba(216, 180, 254, 0.3)', // purple
];

const animationClasses = [
  'danmu-scroll', 
  'danmu-float', 
  'danmu-zoom', 
  'danmu-swing',
  'danmu-up',
  'danmu-down',
  'danmu-diagonal',
  'danmu-bounce',
  'danmu-spiral',
  'danmu-flip',
  'danmu-pulse',
  'danmu-wavy'
];
const timingFunctions = ['linear', 'ease-in-out', 'cubic-bezier(0.4, 0, 0.2, 1)'];

const createDanmu = (customText?: string) => {
  const text = customText || messages[Math.floor(Math.random() * messages.length)];
  const avatar = `https://picsum.photos/seed/${Math.random()}/100/100`;
  
  // Randomize animation type
  const animationClass = animationClasses[Math.floor(Math.random() * animationClasses.length)];
  const timingFunction = timingFunctions[Math.floor(Math.random() * timingFunctions.length)];
  
  // Logic for starting positions based on animation type
  let top = `${Math.random() * 90 + 5}%`;
  let left = 'auto';
  
  if (animationClass === 'danmu-up' || animationClass === 'danmu-down') {
    left = `${Math.random() * 90 + 5}%`;
    top = 'auto';
  }
  
  // Adjust duration based on type
  let durationBase = 12;
  if (animationClass === 'danmu-float') durationBase = 20;
  if (animationClass === 'danmu-zoom') durationBase = 15;
  if (animationClass === 'danmu-swing') durationBase = 14;
  if (animationClass === 'danmu-bounce') durationBase = 10;
  if (animationClass === 'danmu-spiral') durationBase = 18;
  if (animationClass === 'danmu-flip') durationBase = 12;
  if (animationClass === 'danmu-pulse') durationBase = 16;
  if (animationClass === 'danmu-wavy') durationBase = 14;
  if (animationClass.includes('danmu-up') || animationClass.includes('danmu-down')) durationBase = 10;
  
  const duration = `${Math.random() * 15 + durationBase}s`;
  const delay = `0s`;
  
  const fontSize = customText ? '1.2rem' : `${Math.random() * 0.3 + 0.9}rem`;
  const opacity = customText ? 1 : Math.random() * 0.2 + 0.6;
  const glowColor = customText ? 'rgba(255, 255, 255, 0.5)' : glowColors[Math.floor(Math.random() * glowColors.length)];
  const zIndex = customText ? 50 : Math.floor(Math.random() * 10) + 10;
  const wobbleIntensity = Math.random() * 30 - 15;
  
  const id = nextId.value++;
  danmuList.value.push({ 
    id, text, avatar, top, left, duration, delay, 
    fontSize, opacity, glowColor, zIndex,
    animationClass, wobbleIntensity, timingFunction
  });
  
  // Cleanup after animation
  setTimeout(() => {
    danmuList.value = danmuList.value.filter(d => d.id !== id);
  }, 50000);
};

const sendDanmu = () => {
  if (!userInput.value.trim()) return;
  createDanmu(userInput.value);
  userInput.value = '';
};

let interval: any;

onMounted(() => {
  // Initial batch - smaller
  for (let i = 0; i < 5; i++) {
    setTimeout(createDanmu, i * 1000);
  }
  
  // Continuous generation - slower interval
  interval = setInterval(() => {
    if (danmuList.value.length < 20) {
      createDanmu();
    }
  }, 2500);
});

onUnmounted(() => {
  clearInterval(interval);
});
</script>

<template>
  <div class="relative w-full h-[calc(100vh-200px)] min-h-[500px] overflow-hidden font-sans">

    <!-- Danmu Container -->
    <div class="absolute inset-0 z-10 pointer-events-none">
      <div 
        v-for="danmu in danmuList" 
        :key="danmu.id"
        :class="['danmu-item absolute flex items-center gap-3 px-4 py-2 bg-black/50 backdrop-blur-lg rounded-full text-white shadow-2xl whitespace-nowrap', danmu.animationClass]"
        :style="{
          top: danmu.top,
          left: danmu.left,
          animationDuration: danmu.duration,
          animationDelay: danmu.delay,
          animationTimingFunction: danmu.timingFunction,
          fontSize: danmu.fontSize,
          opacity: danmu.opacity,
          boxShadow: `0 0 20px ${danmu.glowColor}`,
          zIndex: danmu.zIndex,
          '--wobble': `${danmu.wobbleIntensity}px`
        }"
      >
        <img :src="danmu.avatar" class="w-8 h-8 rounded-full border border-white/20 object-cover" alt="avatar" />
        <span class="font-medium tracking-wide drop-shadow-sm">{{ danmu.text }}</span>
      </div>
    </div>

    <!-- Input Area -->
    <div class="absolute bottom-12 left-1/2 -translate-x-1/2 z-30 w-full max-w-md px-4">
      <div class="relative group">
        <input 
          v-model="userInput"
          @keyup.enter="sendDanmu"
          type="text" 
          placeholder="发送一条弹幕..." 
          class="w-full px-6 py-4 bg-black/50 backdrop-blur-2xl rounded-full text-white placeholder-white/40 outline-none focus:ring-2 focus:ring-white/30 transition-all shadow-2xl"
        />
        <button 
          @click="sendDanmu"
          class="absolute right-2 top-1/2 -translate-y-1/2 p-3 bg-white/20 hover:bg-white/30 rounded-full text-white transition-all active:scale-90"
        >
          <Send class="w-5 h-5" />
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* Base animation logic */
.danmu-item {
  animation-fill-mode: forwards;
  will-change: transform, opacity;
}

/* 1. Standard Scroll with Wave */
@keyframes danmuScroll {
  0% {
    transform: translateX(100vw) translateY(0) scale(1);
    opacity: 0;
  }
  5% { opacity: 1; }
  25% { transform: translateX(75vw) translateY(var(--wobble)) scale(1.05); }
  50% { transform: translateX(50vw) translateY(calc(var(--wobble) * -1)) scale(1.1); }
  75% { transform: translateX(25vw) translateY(var(--wobble)) scale(1.05); }
  95% { opacity: 1; }
  100% {
    transform: translateX(-100vw) translateY(0) scale(1);
    opacity: 0;
  }
}
.danmu-scroll { animation-name: danmuScroll; }

/* 2. Floating Drift */
@keyframes danmuFloat {
  0% {
    transform: translateX(100vw) translateY(0) rotate(0deg);
    opacity: 0;
  }
  10% { opacity: 0.9; }
  30% { transform: translateX(70vw) translateY(calc(var(--wobble) * 2)) rotate(3deg); }
  50% { transform: translateX(50vw) translateY(calc(var(--wobble) * -2)) rotate(-3deg); }
  70% { transform: translateX(30vw) translateY(calc(var(--wobble) * 2)) rotate(2deg); }
  100% {
    transform: translateX(-100vw) translateY(calc(var(--wobble) * -1)) rotate(0deg);
    opacity: 0;
  }
}
.danmu-float { animation-name: danmuFloat; }

/* 3. Zooming Stream */
@keyframes danmuZoom {
  0% {
    transform: translateX(100vw) scale(0.3) rotate(-10deg);
    opacity: 0;
  }
  20% { opacity: 1; transform: translateX(80vw) scale(1.2) rotate(5deg); }
  40% { transform: translateX(60vw) scale(0.9) rotate(-3deg); }
  60% { transform: translateX(40vw) scale(1.1) rotate(3deg); }
  80% { opacity: 1; transform: translateX(20vw) scale(0.8) rotate(-2deg); }
  100% {
    transform: translateX(-100vw) scale(0.3) rotate(0deg);
    opacity: 0;
  }
}
.danmu-zoom { animation-name: danmuZoom; }

/* 4. Swinging Path */
@keyframes danmuSwing {
  0% {
    transform: translateX(100vw) rotate(-15deg) translateY(0);
    opacity: 0;
  }
  15% { opacity: 1; transform: translateX(85vw) rotate(10deg) translateY(20px); }
  30% { transform: translateX(70vw) rotate(-12deg) translateY(-15px); }
  45% { transform: translateX(55vw) rotate(8deg) translateY(25px); }
  60% { transform: translateX(40vw) rotate(-10deg) translateY(-20px); }
  75% { transform: translateX(25vw) rotate(5deg) translateY(15px); }
  90% { opacity: 1; transform: translateX(10vw) rotate(-3deg) translateY(-10px); }
  100% {
    transform: translateX(-100vw) rotate(0deg) translateY(0);
    opacity: 0;
  }
}
.danmu-swing { animation-name: danmuSwing; }

/* 5. Bottom to Top */
@keyframes danmuUp {
  0% {
    transform: translateY(100vh) translateX(0) scale(0.5);
    opacity: 0;
  }
  10% { opacity: 1; transform: translateY(80vh) translateX(var(--wobble)) scale(1.1); }
  30% { transform: translateY(60vh) translateX(calc(var(--wobble) * -1)) scale(0.95); }
  50% { transform: translateY(40vh) translateX(var(--wobble)) scale(1.05); }
  70% { transform: translateY(20vh) translateX(calc(var(--wobble) * -0.5)) scale(0.98); }
  90% { opacity: 1; }
  100% {
    transform: translateY(-20vh) translateX(0) scale(0.5);
    opacity: 0;
  }
}
.danmu-up { animation-name: danmuUp; }

/* 6. Top to Bottom */
@keyframes danmuDown {
  0% {
    transform: translateY(-20vh) translateX(0) scale(0.5);
    opacity: 0;
  }
  10% { opacity: 1; transform: translateY(0vh) translateX(calc(var(--wobble) * -1)) scale(1.1); }
  30% { transform: translateY(20vh) translateX(var(--wobble)) scale(0.95); }
  50% { transform: translateY(40vh) translateX(calc(var(--wobble) * -1)) scale(1.05); }
  70% { transform: translateY(60vh) translateX(var(--wobble)) scale(0.98); }
  90% { opacity: 1; }
  100% {
    transform: translateY(120vh) translateX(0) scale(0.5);
    opacity: 0;
  }
}
.danmu-down { animation-name: danmuDown; }

/* 7. Diagonal Drift */
@keyframes danmuDiagonal {
  0% {
    transform: translate(100vw, 100vh) rotate(-15deg) scale(0.6);
    opacity: 0;
  }
  15% { opacity: 1; transform: translate(70vw, 70vh) rotate(-5deg) scale(1.1); }
  40% { transform: translate(40vw, 40vh) rotate(5deg) scale(0.95); }
  60% { transform: translate(20vw, 20vh) rotate(-3deg) scale(1.05); }
  85% { opacity: 1; transform: translate(0vw, 0vh) rotate(2deg) scale(1); }
  100% {
    transform: translate(-100vw, -100vh) rotate(10deg) scale(0.6);
    opacity: 0;
  }
}
.danmu-diagonal { animation-name: danmuDiagonal; }

/* 8. Bouncing Ball */
@keyframes danmuBounce {
  0% {
    transform: translateX(100vw) translateY(0) scale(0.5);
    opacity: 0;
  }
  10% { opacity: 1; transform: translateX(90vw) translateY(-30px) scale(1.1); }
  20% { transform: translateX(80vw) translateY(0) scale(1); }
  30% { transform: translateX(70vw) translateY(-25px) scale(1.05); }
  40% { transform: translateX(60vw) translateY(0) scale(1); }
  50% { transform: translateX(50vw) translateY(-20px) scale(1.05); }
  60% { transform: translateX(40vw) translateY(0) scale(1); }
  70% { transform: translateX(30vw) translateY(-15px) scale(1.03); }
  80% { transform: translateX(20vw) translateY(0) scale(1); }
  90% { opacity: 1; transform: translateX(10vw) translateY(-10px) scale(1.02); }
  100% {
    transform: translateX(-100vw) translateY(0) scale(0.5);
    opacity: 0;
  }
}
.danmu-bounce { animation-name: danmuBounce; }

/* 9. Spiral Motion */
@keyframes danmuSpiral {
  0% {
    transform: translateX(100vw) rotate(0deg) scale(0.6);
    opacity: 0;
  }
  10% { opacity: 1; }
  20% { transform: translateX(80vw) rotate(72deg) scale(1.1) translateY(30px); }
  40% { transform: translateX(60vw) rotate(144deg) scale(0.9) translateY(-30px); }
  60% { transform: translateX(40vw) rotate(216deg) scale(1.05) translateY(20px); }
  80% { transform: translateX(20vw) rotate(288deg) scale(0.95) translateY(-15px); }
  90% { opacity: 1; }
  100% {
    transform: translateX(-100vw) rotate(360deg) scale(0.6) translateY(0);
    opacity: 0;
  }
}
.danmu-spiral { animation-name: danmuSpiral; }

/* 10. 3D Flip */
@keyframes danmuFlip {
  0% {
    transform: translateX(100vw) rotateY(-90deg) rotateX(0deg) scale(0.5);
    opacity: 0;
  }
  15% { opacity: 1; transform: translateX(85vw) rotateY(0deg) rotateX(20deg) scale(1.1); }
  30% { transform: translateX(70vw) rotateY(45deg) rotateX(-10deg) scale(0.95); }
  50% { transform: translateX(50vw) rotateY(0deg) rotateX(15deg) scale(1.05); }
  70% { transform: translateX(30vw) rotateY(-30deg) rotateX(-5deg) scale(0.98); }
  85% { opacity: 1; transform: translateX(15vw) rotateY(0deg) rotateX(0deg) scale(1); }
  100% {
    transform: translateX(-100vw) rotateY(90deg) rotateX(0deg) scale(0.5);
    opacity: 0;
  }
}
.danmu-flip { animation-name: danmuFlip; perspective: 1000px; }

/* 11. Pulse Glow */
@keyframes danmuPulse {
  0% {
    transform: translateX(100vw) scale(0.6);
    opacity: 0;
    filter: brightness(0.8);
  }
  10% { opacity: 1; transform: translateX(90vw) scale(1.2); filter: brightness(1.3); }
  25% { transform: translateX(75vw) scale(0.9); filter: brightness(1); }
  40% { transform: translateX(60vw) scale(1.15); filter: brightness(1.2); }
  55% { transform: translateX(45vw) scale(0.95); filter: brightness(1); }
  70% { transform: translateX(30vw) scale(1.1); filter: brightness(1.15); }
  85% { opacity: 1; transform: translateX(15vw) scale(0.95); filter: brightness(1); }
  100% {
    transform: translateX(-100vw) scale(0.6);
    opacity: 0;
    filter: brightness(0.8);
  }
}
.danmu-pulse { animation-name: danmuPulse; }

/* 12. Wavy Sine Motion */
@keyframes danmuWavy {
  0% {
    transform: translateX(100vw) translateY(0) rotate(0deg);
    opacity: 0;
  }
  5% { opacity: 1; }
  12.5% { transform: translateX(87.5vw) translateY(-40px) rotate(-8deg); }
  25% { transform: translateX(75vw) translateY(0) rotate(0deg); }
  37.5% { transform: translateX(62.5vw) translateY(40px) rotate(8deg); }
  50% { transform: translateX(50vw) translateY(0) rotate(0deg); }
  62.5% { transform: translateX(37.5vw) translateY(-40px) rotate(-8deg); }
  75% { transform: translateX(25vw) translateY(0) rotate(0deg); }
  87.5% { transform: translateX(12.5vw) translateY(40px) rotate(8deg); }
  95% { opacity: 1; }
  100% {
    transform: translateX(-100vw) translateY(0) rotate(0deg);
    opacity: 0;
  }
}
.danmu-wavy { animation-name: danmuWavy; }
</style>
