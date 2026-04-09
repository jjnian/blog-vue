<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue';
import * as THREE from 'three';
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls.js';
import { getWishes } from '@/api/blog';

const canvasContainer = ref<HTMLElement | null>(null);
const wishesCount = ref(0);

let scene: THREE.Scene;
let camera: THREE.PerspectiveCamera;
let renderer: THREE.WebGLRenderer;
let controls: OrbitControls;
let animationId: number;
let clock: THREE.Clock;
let resizeObserver: ResizeObserver;

// Tree components
let treeGroup: THREE.Group;

const initScene = () => {
  if (!canvasContainer.value) return;

  const width = canvasContainer.value.clientWidth || 800;
  const height = canvasContainer.value.clientHeight || 600;

  clock = new THREE.Clock();

  // Scene
  scene = new THREE.Scene();
  scene.fog = new THREE.FogExp2(0xffffff, 0.008);

  // Camera
  camera = new THREE.PerspectiveCamera(55, width / height, 0.1, 1500);
  camera.position.set(0, 35, 80);
  camera.lookAt(0, 20, 0);

  // Renderer
  renderer = new THREE.WebGLRenderer({ antialias: true, alpha: true });
  renderer.setSize(width, height);
  renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2));
  renderer.toneMapping = THREE.ACESFilmicToneMapping;
  renderer.toneMappingExposure = 1.0;
  renderer.shadowMap.enabled = true;
  renderer.shadowMap.type = THREE.PCFSoftShadowMap;
  canvasContainer.value.appendChild(renderer.domElement);

  // Controls
  controls = new OrbitControls(camera, renderer.domElement);
  controls.enableDamping = true;
  controls.dampingFactor = 0.05;
  controls.minDistance = 30;
  controls.maxDistance = 150;
  controls.maxPolarAngle = Math.PI / 2 + 0.1;
  controls.minPolarAngle = 0.2;
  controls.target.set(0, 20, 0);
  controls.autoRotate = true;
  controls.autoRotateSpeed = 0.3;
  controls.update();

  // Resize
  resizeObserver = new ResizeObserver(entries => {
    for (const entry of entries) {
      const { width: w, height: h } = entry.contentRect;
      if (w > 0 && h > 0) {
        camera.aspect = w / h;
        camera.updateProjectionMatrix();
        renderer.setSize(w, h);
      }
    }
  });
  resizeObserver.observe(canvasContainer.value);

  createTree();
  createLighting();

  animate();
};

// ========== TREE ==========
const createTree = () => {
  treeGroup = new THREE.Group();
  treeGroup.position.y = 0;
  scene.add(treeGroup);

  // Bark material - rich dark wood with emissive veins
  const barkMat = new THREE.MeshStandardMaterial({
    color: 0x1a0a05,
    emissive: 0x331100,
    emissiveIntensity: 0.3,
    roughness: 0.8,
    metalness: 0.1,
  });

  // Glowing vein overlay
  const veinMat = new THREE.MeshBasicMaterial({
    color: 0xff6633,
    wireframe: true,
    transparent: true,
    opacity: 0.08,
    blending: THREE.AdditiveBlending,
  });

  // Inner glow material
  const innerGlowMat = new THREE.MeshBasicMaterial({
    color: 0xffaa44,
    transparent: true,
    opacity: 0.05,
    blending: THREE.AdditiveBlending,
  });

  const buildBranch = (
    parent: THREE.Object3D,
    start: THREE.Vector3,
    direction: THREE.Vector3,
    length: number,
    radius: number,
    depth: number,
    maxDepth: number
  ) => {
    if (depth > maxDepth) return;

    const pts: THREE.Vector3[] = [];
    let curr = start.clone();
    let dir = direction.clone().normalize();
    const segs = 6;

    for (let i = 0; i <= segs; i++) {
      pts.push(curr.clone());
      if (i < segs) {
        const twist = new THREE.Vector3(
          (Math.random() - 0.5) * 1.2,
          (Math.random() - 0.3) * 0.4,
          (Math.random() - 0.5) * 1.2
        ).normalize();
        dir.lerp(twist, 0.15 + depth * 0.05).normalize();
        curr.add(dir.clone().multiplyScalar(length / segs));
      }
    }

    const curve = new THREE.CatmullRomCurve3(pts);
    const radialSegs = depth < 2 ? 12 : 8;
    const geo = new THREE.TubeGeometry(curve, segs * 3, radius, radialSegs, false);

    const branch = new THREE.Mesh(geo, barkMat);
    branch.castShadow = true;
    branch.receiveShadow = true;
    parent.add(branch);

    // Vein overlay
    const vein = new THREE.Mesh(geo, veinMat);
    parent.add(vein);

    // Inner glow for trunk
    if (depth < 2) {
      const innerGeo = new THREE.TubeGeometry(curve, segs * 2, radius * 0.6, 6, false);
      parent.add(new THREE.Mesh(innerGeo, innerGlowMat));
    }

    if (depth < maxDepth) {
      const numBranches = depth === 0 ? 5 : depth === 1 ? 4 : (Math.random() > 0.3 ? 3 : 2);
      for (let i = 0; i < numBranches; i++) {
        const splitPtIdx = Math.floor(segs * (0.5 + Math.random() * 0.5));
        const splitPt = pts[splitPtIdx];

        const angleY = (Math.PI * 2 / numBranches) * i + (Math.random() - 0.5) * 0.5;
        const spread = 0.8 + depth * 0.3;
        const newDir = new THREE.Vector3(
          Math.sin(angleY) * spread,
          0.8 - depth * 0.1 + Math.random() * 0.3,
          Math.cos(angleY) * spread
        ).normalize();

        buildBranch(
          parent,
          splitPt,
          newDir,
          length * (0.65 + Math.random() * 0.15),
          radius * (depth === 0 ? 0.55 : 0.5),
          depth + 1,
          maxDepth
        );
      }
    }
  };

  buildBranch(treeGroup, new THREE.Vector3(0, 0, 0), new THREE.Vector3(0, 1, 0), 22, 3.5, 0, 5);

  // Add some root-like structures at the base
  for (let i = 0; i < 8; i++) {
    const angle = (i / 8) * Math.PI * 2;
    const rootDir = new THREE.Vector3(Math.cos(angle) * 2, -0.3, Math.sin(angle) * 2).normalize();
    const rootCurve = new THREE.CatmullRomCurve3([
      new THREE.Vector3(0, 2, 0),
      new THREE.Vector3(Math.cos(angle) * 5, -0.5, Math.sin(angle) * 5),
      new THREE.Vector3(Math.cos(angle) * 10, -1, Math.sin(angle) * 10),
    ]);
    const rootGeo = new THREE.TubeGeometry(rootCurve, 8, 1.2 - i * 0.08, 8, false);
    const root = new THREE.Mesh(rootGeo, barkMat);
    root.castShadow = true;
    treeGroup.add(root);
    treeGroup.add(new THREE.Mesh(rootGeo, veinMat));
  }

  // Canopy glow (core bloom at center of canopy)
  const canopyGlow = new THREE.PointLight(0xffaa66, 3, 60);
  canopyGlow.position.set(0, 45, 0);
  treeGroup.add(canopyGlow);

  // Add glowing leaf clusters (point lights scattered through canopy)
  const clusterColors = [0xff6699, 0xffaa44, 0x44ddff, 0xaa66ff, 0x66ff99];
  for (let i = 0; i < 8; i++) {
    const light = new THREE.PointLight(
      clusterColors[i % clusterColors.length],
      1.5,
      25
    );
    const angle = (i / 8) * Math.PI * 2;
    const r = 10 + Math.random() * 10;
    light.position.set(
      Math.cos(angle) * r,
      35 + Math.random() * 20,
      Math.sin(angle) * r
    );
    treeGroup.add(light);
  }
};

// ========== LIGHTING ==========
const createLighting = () => {
  // Ambient
  scene.add(new THREE.AmbientLight(0x111133, 0.6));

  // Moon light
  const moonLight = new THREE.DirectionalLight(0x6688cc, 0.8);
  moonLight.position.set(100, 200, -100);
  moonLight.castShadow = true;
  moonLight.shadow.mapSize.width = 2048;
  moonLight.shadow.mapSize.height = 2048;
  moonLight.shadow.camera.near = 0.5;
  moonLight.shadow.camera.far = 500;
  moonLight.shadow.camera.left = -100;
  moonLight.shadow.camera.right = 100;
  moonLight.shadow.camera.top = 100;
  moonLight.shadow.camera.bottom = -100;
  scene.add(moonLight);

  // Additional fill lights
  const fillLight1 = new THREE.DirectionalLight(0x4466aa, 0.3);
  fillLight1.position.set(-50, 50, 50);
  scene.add(fillLight1);

  const fillLight2 = new THREE.DirectionalLight(0xaa6644, 0.2);
  fillLight2.position.set(50, -50, -50);
  scene.add(fillLight2);
};

// ========== ANIMATION ==========
const animate = () => {
  animationId = requestAnimationFrame(animate);
  const time = clock.getElapsedTime();

  controls.update();

  // Tree gentle sway
  if (treeGroup) {
    treeGroup.rotation.y = Math.sin(time * 0.15) * 0.03;
  }

  renderer.render(scene, camera);
};

// Cleanup
onMounted(() => {
  initScene();
  getWishes(1, 50)
    .then((page) => {
      wishesCount.value = page.records.length;
    })
    .catch(() => {
      wishesCount.value = 0;
    });
});

onUnmounted(() => {
  if (animationId) cancelAnimationFrame(animationId);
  if (resizeObserver) resizeObserver.disconnect();
  if (controls) controls.dispose();
  if (renderer) {
    renderer.dispose();
    renderer.forceContextLoss();
  }
});
</script>

<template>
  <div class="relative w-full h-screen overflow-hidden">
    <div ref="canvasContainer" class="absolute inset-0"></div>
  </div>
</template>
