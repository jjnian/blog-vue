<script setup lang="ts">
import { onMounted, ref, watch } from 'vue';
import * as d3 from 'd3';

interface PetalData {
  label: string;
  value: number;
  color: string;
}

const props = defineProps<{
  data?: PetalData[];
}>();

const chartRef = ref<HTMLElement | null>(null);

const defaultData: PetalData[] = [
  { label: '数据库', value: 85, color: '#49b1f5' },
  { label: '后端', value: 90, color: '#00d2ff' },
  { label: 'DevOps', value: 70, color: '#3a7bd5' },
  { label: '随笔', value: 95, color: '#00c6ff' },
  { label: '前端', value: 80, color: '#0072ff' },
  { label: '算法', value: 75, color: '#4facfe' },
];

const drawChart = () => {
  if (!chartRef.value) return;

  const data = props.data || defaultData;
  const width = chartRef.value.clientWidth;
  const height = 400;
  const centerX = width / 2;
  const centerY = height / 2;
  const maxRadius = Math.min(width, height) / 2.5;

  // Clear previous chart
  d3.select(chartRef.value).selectAll('*').remove();

  const svg = d3.select(chartRef.value)
    .append('svg')
    .attr('width', width)
    .attr('height', height)
    .attr('viewBox', `0 0 ${width} ${height}`)
    .style('overflow', 'visible');

  const g = svg.append('g')
    .attr('transform', `translate(${centerX}, ${centerY})`);

  // Create petals
  const numPetals = data.length;
  const angleStep = (Math.PI * 2) / numPetals;

  const petals = g.selectAll('.petal')
    .data(data)
    .enter()
    .append('g')
    .attr('class', 'petal')
    .attr('transform', (_, i) => `rotate(${(i * angleStep * 180) / Math.PI})`);

  // Petal shape (using path for more control)
  // We'll draw a petal-like shape using cubic bezier curves
  petals.append('path')
    .attr('d', d => {
      const r = (d.value / 100) * maxRadius;
      const w = r * 0.4; // width of petal
      return `M 0,0 C ${w},${-r/2} ${w},${-r} 0,${-r} C ${-w},${-r} ${-w},${-r/2} 0,0`;
    })
    .attr('fill', d => d.color)
    .attr('fill-opacity', 0.6)
    .attr('stroke', d => d.color)
    .attr('stroke-width', 2)
    .style('cursor', 'pointer')
    .on('mouseover', function(event, d) {
      d3.select(this)
        .transition()
        .duration(300)
        .attr('fill-opacity', 0.9)
        .attr('transform', 'scale(1.1)');
      
      // Show tooltip or label
      d3.select(this.parentNode as any).select('text')
        .transition()
        .duration(300)
        .attr('opacity', 1)
        .attr('transform', `translate(0, ${-(d.value / 100) * maxRadius - 20})`);
    })
    .on('mouseout', function(event, d) {
      d3.select(this)
        .transition()
        .duration(300)
        .attr('fill-opacity', 0.6)
        .attr('transform', 'scale(1)');
      
      d3.select(this.parentNode as any).select('text')
        .transition()
        .duration(300)
        .attr('opacity', 0)
        .attr('transform', `translate(0, ${-(d.value / 100) * maxRadius - 10})`);
    });

  // Labels
  petals.append('text')
    .attr('text-anchor', 'middle')
    .attr('dy', '0.35em')
    .attr('font-size', '12px')
    .attr('font-weight', 'bold')
    .attr('fill', '#2c3e50')
    .attr('opacity', 0)
    .attr('transform', d => `translate(0, ${-(d.value / 100) * maxRadius - 10})`)
    .text(d => `${d.label}: ${d.value}%`);

  // Center circle
  g.append('circle')
    .attr('r', 10)
    .attr('fill', '#fff')
    .attr('stroke', '#49b1f5')
    .attr('stroke-width', 3)
    .attr('class', 'animate-pulse');

  // Static labels around the petals
  petals.append('text')
    .attr('transform', d => {
      const r = (d.value / 100) * maxRadius + 30;
      return `translate(0, ${-r}) rotate(${-(0)})`; // Keep text upright? No, rotate with petal but adjust
    })
    .attr('text-anchor', 'middle')
    .attr('font-size', '10px')
    .attr('fill', '#94a3b8')
    .attr('font-family', 'serif')
    .attr('letter-spacing', '0.1em')
    .text(d => d.label)
    .attr('transform', d => {
      const r = (d.value / 100) * maxRadius + 30;
      // We need to counter-rotate the text so it's readable
      return `translate(0, ${-r})`;
    })
    .each(function(_, i) {
      const angle = (i * angleStep * 180) / Math.PI;
      d3.select(this).attr('transform', `translate(0, ${-((data[i].value / 100) * maxRadius + 30)}) rotate(${-angle})`);
    });
};

onMounted(() => {
  drawChart();
  window.addEventListener('resize', drawChart);
});

watch(() => props.data, drawChart, { deep: true });
</script>

<template>
  <div class="petal-chart-container w-full py-10 flex flex-col items-center">
    <div class="text-center mb-8">
      <h2 class="text-3xl font-serif font-bold text-[#2c3e50] mb-2">技能图谱</h2>
      <div class="h-1 w-12 bg-[#49b1f5] mx-auto rounded-full"></div>
    </div>
    <div ref="chartRef" class="w-full max-w-2xl h-[400px]"></div>
  </div>
</template>

<style scoped>
.petal-chart-container {
  background: transparent;
}

@keyframes pulse {
  0%, 100% { transform: scale(1); opacity: 1; }
  50% { transform: scale(1.2); opacity: 0.8; }
}

.animate-pulse {
  animation: pulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite;
}
</style>
