<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue';
import * as THREE from 'three';

const container = ref<HTMLElement | null>(null);
let scene: THREE.Scene, camera: THREE.PerspectiveCamera, renderer: THREE.WebGLRenderer;
let animationId: number;
let treeGroup: THREE.Group;
let leaves: THREE.Points;
let resizeObserver: ResizeObserver;

onMounted(() => {
  if (!container.value) return;

  // 1. Setup Scene
  scene = new THREE.Scene();
  
  camera = new THREE.PerspectiveCamera(45, container.value.clientWidth / container.value.clientHeight, 0.1, 1000);
  camera.position.set(0, 15, 45);
  camera.lookAt(0, 10, 0);

  renderer = new THREE.WebGLRenderer({ alpha: true, antialias: true });
  renderer.setSize(container.value.clientWidth, container.value.clientHeight);
  renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2));
  container.value.appendChild(renderer.domElement);

  // 2. Lights
  const ambientLight = new THREE.AmbientLight(0xffffff, 0.8);
  scene.add(ambientLight);
  
  const pointLight1 = new THREE.PointLight(0x49b1f5, 2, 100);
  pointLight1.position.set(20, 20, 20);
  scene.add(pointLight1);

  const pointLight2 = new THREE.PointLight(0xff00ff, 1.5, 100);
  pointLight2.position.set(-20, 10, -20);
  scene.add(pointLight2);

  // 3. Tree Group
  treeGroup = new THREE.Group();
  scene.add(treeGroup);
  treeGroup.position.y = -10;

  // Trunk Material (Glowing cyan/blue)
  const trunkMat = new THREE.MeshStandardMaterial({ 
    color: 0x112233,
    emissive: 0x004466,
    emissiveIntensity: 0.5,
    roughness: 0.3,
    metalness: 0.8
  });

  const wireMat = new THREE.MeshBasicMaterial({
    color: 0x49b1f5,
    wireframe: true,
    transparent: true,
    opacity: 0.3,
    blending: THREE.AdditiveBlending
  });

  // Build Tree (Recursive with TubeGeometry for organic look)
  const buildBranch = (parent: THREE.Object3D, startPt: THREE.Vector3, dir: THREE.Vector3, length: number, radius: number, depth: number, maxDepth: number) => {
    if (depth > maxDepth) return;

    const points = [];
    let currPt = startPt.clone();
    let currDir = dir.clone().normalize();
    
    const segments = 4;
    for (let i = 0; i <= segments; i++) {
      points.push(currPt.clone());
      if (i < segments) {
        const twist = new THREE.Vector3((Math.random() - 0.5), (Math.random() - 0.5) * 0.2, (Math.random() - 0.5)).normalize();
        currDir.lerp(twist, 0.15).normalize();
        currPt.add(currDir.clone().multiplyScalar(length / segments));
      }
    }

    const curve = new THREE.CatmullRomCurve3(points);
    const geo = new THREE.TubeGeometry(curve, segments * 2, radius, 6, false);
    const branch = new THREE.Mesh(geo, trunkMat);
    const wireBranch = new THREE.Mesh(geo, wireMat);
    branch.add(wireBranch);
    
    parent.add(branch);

    const endPt = points[points.length - 1];

    if (depth < maxDepth) {
      const numBranches = depth === 0 ? 3 : 2;
      for (let i = 0; i < numBranches; i++) {
        const angleX = (Math.random() - 0.5) * 1.5;
        const angleZ = (Math.random() - 0.5) * 1.5;
        const angleY = (Math.PI * 2 / numBranches) * i;
        
        const newDir = new THREE.Vector3(
          Math.sin(angleY) * Math.cos(angleX), 
          Math.abs(Math.cos(angleX)) + 0.5, 
          Math.cos(angleY) * Math.cos(angleZ)
        ).normalize();
        
        buildBranch(parent, endPt, newDir, length * (0.7 + Math.random() * 0.2), radius * 0.6, depth + 1, maxDepth);
      }
    }
  };

  buildBranch(treeGroup, new THREE.Vector3(0, 0, 0), new THREE.Vector3(0, 1, 0), 12, 1.5, 0, 4);

  // 4. Leaves (Glowing particles)
  const leafGeo = new THREE.BufferGeometry();
  const leafCount = 3000;
  const leafPos = new Float32Array(leafCount * 3);
  const leafColors = new Float32Array(leafCount * 3);
  const leafSizes = new Float32Array(leafCount);
  
  const color = new THREE.Color();
  for(let i=0; i<leafCount; i++) {
    const r = 8 + Math.random() * 15;
    const theta = Math.random() * Math.PI * 2;
    const phi = Math.acos((Math.random() * 1.5) - 0.5);
    
    leafPos[i*3] = r * Math.sin(phi) * Math.cos(theta);
    leafPos[i*3+1] = 15 + r * Math.cos(phi) * 0.8;
    leafPos[i*3+2] = r * Math.sin(phi) * Math.sin(theta);

    color.setHSL(0.5 + Math.random() * 0.1, 0.9, 0.6); // Cyan to Blue
    leafColors[i*3] = color.r;
    leafColors[i*3+1] = color.g;
    leafColors[i*3+2] = color.b;
    
    leafSizes[i] = Math.random() * 2 + 0.5;
  }
  
  leafGeo.setAttribute('position', new THREE.BufferAttribute(leafPos, 3));
  leafGeo.setAttribute('color', new THREE.BufferAttribute(leafColors, 3));
  leafGeo.setAttribute('size', new THREE.BufferAttribute(leafSizes, 1));

  const canvasTex = document.createElement('canvas');
  canvasTex.width = 32; canvasTex.height = 32;
  const ctx = canvasTex.getContext('2d')!;
  const gradient = ctx.createRadialGradient(16, 16, 0, 16, 16, 16);
  gradient.addColorStop(0, 'rgba(255,255,255,1)');
  gradient.addColorStop(0.2, 'rgba(255,255,255,0.8)');
  gradient.addColorStop(1, 'rgba(255,255,255,0)');
  ctx.fillStyle = gradient;
  ctx.fillRect(0, 0, 32, 32);
  const tex = new THREE.CanvasTexture(canvasTex);

  const leafMat = new THREE.PointsMaterial({
    size: 1.5,
    vertexColors: true,
    map: tex,
    blending: THREE.AdditiveBlending,
    depthWrite: false,
    transparent: true,
    opacity: 0.8
  });

  leaves = new THREE.Points(leafGeo, leafMat);
  treeGroup.add(leaves);

  // 5. Floating Orbs
  const orbGeo = new THREE.BufferGeometry();
  const orbCount = 50;
  const orbPos = new Float32Array(orbCount * 3);
  for(let i=0; i<orbCount; i++) {
    orbPos[i*3] = (Math.random() - 0.5) * 40;
    orbPos[i*3+1] = Math.random() * 30;
    orbPos[i*3+2] = (Math.random() - 0.5) * 40;
  }
  orbGeo.setAttribute('position', new THREE.BufferAttribute(orbPos, 3));
  const orbMat = new THREE.PointsMaterial({
    size: 2,
    color: 0xffffff,
    map: tex,
    blending: THREE.AdditiveBlending,
    transparent: true,
    opacity: 0.6,
    depthWrite: false
  });
  const orbs = new THREE.Points(orbGeo, orbMat);
  scene.add(orbs);

  // 6. Animation
  let time = 0;
  const animate = () => {
    animationId = requestAnimationFrame(animate);
    time += 0.005;
    
    treeGroup.rotation.y = Math.sin(time) * 0.1;
    leaves.rotation.y = time * 0.5;
    
    const positions = orbs.geometry.attributes.position.array as Float32Array;
    for(let i=0; i<orbCount; i++) {
      positions[i*3+1] += Math.sin(time * 2 + i) * 0.02;
      if (positions[i*3+1] > 35) positions[i*3+1] = -5;
    }
    orbs.geometry.attributes.position.needsUpdate = true;
    
    renderer.render(scene, camera);
  };
  animate();

  // 7. Resize
  resizeObserver = new ResizeObserver(() => {
    if (container.value && camera && renderer) {
      camera.aspect = container.value.clientWidth / container.value.clientHeight;
      camera.updateProjectionMatrix();
      renderer.setSize(container.value.clientWidth, container.value.clientHeight);
    }
  });
  resizeObserver.observe(container.value);
});

onUnmounted(() => {
  cancelAnimationFrame(animationId);
  if (resizeObserver && container.value) {
    resizeObserver.unobserve(container.value);
  }
  if (renderer) {
    renderer.dispose();
  }
});
</script>

<template>
  <div ref="container" class="w-full h-full min-h-[300px] absolute inset-0 z-0 pointer-events-none"></div>
</template>
