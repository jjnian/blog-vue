<script setup lang="ts">
import { onMounted, onUnmounted, ref } from 'vue';

const canvasRef = ref<HTMLCanvasElement | null>(null);

class Petal {
  x: number;
  y: number;
  s: number;
  r: number;
  fnx: number;
  fny: number;
  fnr: number;
  canvasWidth: number;
  canvasHeight: number;
  opacity: number;

  constructor(canvasWidth: number, canvasHeight: number) {
    this.canvasWidth = canvasWidth;
    this.canvasHeight = canvasHeight;
    this.x = Math.random() * canvasWidth;
    this.y = Math.random() * canvasHeight;
    this.s = Math.random() * 1.2 + 0.3; // Smaller, more delicate
    this.r = Math.random() * Math.PI * 2;
    this.opacity = Math.random() * 0.4 + 0.4;
    
    // Movement parameters - gentle drifting
    this.fnx = Math.random() * 1.5 - 0.75;
    this.fny = Math.random() * 1.0 + 0.5;
    this.fnr = Math.random() * 0.01;
  }

  update() {
    this.x += this.fnx;
    this.y += this.fny;
    this.r += this.fnr;

    // Reset if out of bounds
    if (this.y > this.canvasHeight + 20) {
      this.y = -20;
      this.x = Math.random() * this.canvasWidth;
    }
    if (this.x > this.canvasWidth + 20) {
      this.x = -20;
    } else if (this.x < -20) {
      this.x = this.canvasWidth + 20;
    }
  }

  draw(ctx: CanvasRenderingContext2D) {
    ctx.save();
    ctx.translate(this.x, this.y);
    ctx.rotate(this.r);
    ctx.scale(this.s, this.s);

    ctx.beginPath();
    ctx.fillStyle = '#ffc0cb';
    ctx.globalAlpha = this.opacity;
    
    // Classic Sakura Petal Shape with a small notch at the top
    ctx.moveTo(0, 0);
    ctx.bezierCurveTo(-6, -6, -12, 4, 0, 16);
    ctx.bezierCurveTo(12, 4, 6, -6, 0, 0);
    
    // Add the notch
    ctx.moveTo(0, 0);
    ctx.lineTo(0, 3);
    
    ctx.fill();
    
    // Subtle vein
    ctx.beginPath();
    ctx.strokeStyle = 'rgba(255, 255, 255, 0.4)';
    ctx.lineWidth = 0.5;
    ctx.moveTo(0, 3);
    ctx.lineTo(0, 10);
    ctx.stroke();
    
    ctx.restore();
  }
}

let animationFrameId: number;
const petals: Petal[] = [];
const petalCount = 80; // More petals for a fuller effect

const init = () => {
  const canvas = canvasRef.value;
  if (!canvas) return;

  const ctx = canvas.getContext('2d');
  if (!ctx) return;

  canvas.width = window.innerWidth;
  canvas.height = window.innerHeight;

  for (let i = 0; i < petalCount; i++) {
    petals.push(new Petal(canvas.width, canvas.height));
  }

  const render = () => {
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    petals.forEach((petal) => {
      petal.update();
      petal.draw(ctx);
    });
    animationFrameId = requestAnimationFrame(render);
  };

  render();
};

const handleResize = () => {
  if (canvasRef.value) {
    canvasRef.value.width = window.innerWidth;
    canvasRef.value.height = window.innerHeight;
  }
};

onMounted(() => {
  init();
  window.addEventListener('resize', handleResize);
});

onUnmounted(() => {
  cancelAnimationFrame(animationFrameId);
  window.removeEventListener('resize', handleResize);
});
</script>

<template>
  <canvas
    ref="canvasRef"
    class="fixed inset-0 pointer-events-none z-0 opacity-60"
  ></canvas>
</template>

<style scoped>
canvas {
  filter: blur(0.5px);
}
</style>
