<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue';
import * as THREE from 'three';
import { Sparkles } from 'lucide-vue-next';

const canvasContainer = ref<HTMLElement | null>(null);
const newWish = ref('');
const activeWish = ref('');

let scene: THREE.Scene, camera: THREE.PerspectiveCamera, renderer: THREE.WebGLRenderer;
let treeGroup: THREE.Group;
let leaves: THREE.Points;
let fireflies: THREE.Points;
let magicCircle: THREE.Group;
let animationId: number;
const wishesGroups: THREE.Group[] = [];
const wishColors = [0xff00ff, 0x00ffff, 0x00ff00, 0xffff00, 0xff5500, 0xaa00ff];

let resizeObserver: ResizeObserver;
let introProgress = 0;

const initThree = () => {
  if (!canvasContainer.value) return;
  
  // Use fallback dimensions if container is not fully rendered yet
  let width = canvasContainer.value.clientWidth || 800;
  let height = canvasContainer.value.clientHeight || 600;

  // 1. Scene Setup
  scene = new THREE.Scene();
  scene.background = new THREE.Color(0x05020a); // Deep magical dark background
  scene.fog = new THREE.FogExp2(0x05020a, 0.008);

  camera = new THREE.PerspectiveCamera(60, width / height, 0.1, 1000);
  camera.position.set(0, 40, 90);
  camera.lookAt(0, 25, 0);

  renderer = new THREE.WebGLRenderer({ alpha: true, antialias: true });
  renderer.setSize(width, height);
  renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2));
  // Enhance colors
  renderer.toneMapping = THREE.ACESFilmicToneMapping;
  renderer.toneMappingExposure = 1.2;
  canvasContainer.value.appendChild(renderer.domElement);

  // Setup ResizeObserver
  resizeObserver = new ResizeObserver(entries => {
    for (let entry of entries) {
      const { width, height } = entry.contentRect;
      if (width > 0 && height > 0 && camera && renderer) {
        camera.aspect = width / height;
        camera.updateProjectionMatrix();
        renderer.setSize(width, height);
      }
    }
  });
  resizeObserver.observe(canvasContainer.value);

  // 2. Lighting (Magical Cyan/Blue)
  scene.add(new THREE.AmbientLight(0xffffff, 0.3));
  
  const light1 = new THREE.PointLight(0x49b1f5, 2, 150);
  light1.position.set(30, 20, 30);
  scene.add(light1);

  const light2 = new THREE.PointLight(0x00ffff, 2, 150);
  light2.position.set(-30, 40, -30);
  scene.add(light2);

  const light3 = new THREE.PointLight(0x004466, 1.5, 100);
  light3.position.set(0, 10, 40);
  scene.add(light3);

  // 3. Magical Base (Magic Circle)
  magicCircle = new THREE.Group();
  scene.add(magicCircle);
  magicCircle.position.y = -15;

  const ringGeo1 = new THREE.TorusGeometry(25, 0.2, 16, 100);
  const ringMat1 = new THREE.MeshBasicMaterial({ color: 0x00ffff, transparent: true, opacity: 0.8, blending: THREE.AdditiveBlending });
  const ringMesh1 = new THREE.Mesh(ringGeo1, ringMat1);
  ringMesh1.rotation.x = Math.PI / 2;
  magicCircle.add(ringMesh1);

  const ringGeo2 = new THREE.TorusGeometry(20, 0.5, 16, 100);
  const ringMat2 = new THREE.MeshBasicMaterial({ color: 0x49b1f5, transparent: true, opacity: 0.4, blending: THREE.AdditiveBlending, wireframe: true });
  const ringMesh2 = new THREE.Mesh(ringGeo2, ringMat2);
  ringMesh2.rotation.x = Math.PI / 2;
  magicCircle.add(ringMesh2);

  const basePlane = new THREE.Mesh(
    new THREE.CircleGeometry(24, 64),
    new THREE.MeshBasicMaterial({ color: 0x004466, transparent: true, opacity: 0.3, blending: THREE.AdditiveBlending, side: THREE.DoubleSide })
  );
  basePlane.rotation.x = Math.PI / 2;
  magicCircle.add(basePlane);

  // 4. The Flashy Tree Trunk
  treeGroup = new THREE.Group();
  scene.add(treeGroup);
  treeGroup.position.y = -15;
  
  // Luxurious Cyan/Blue Material for the trunk
  const trunkMat = new THREE.MeshStandardMaterial({ 
    color: 0x112233,
    emissive: 0x004466,
    emissiveIntensity: 0.5,
    roughness: 0.3,
    metalness: 0.8,
  });

  const trunkWireMat = new THREE.MeshBasicMaterial({
    color: 0x49b1f5,
    wireframe: true,
    transparent: true,
    opacity: 0.3,
    blending: THREE.AdditiveBlending
  });

  // Create an organic, twisted tree using TubeGeometry
  const buildOrganicBranch = (parent: THREE.Object3D, startPt: THREE.Vector3, dir: THREE.Vector3, length: number, radius: number, depth: number, maxDepth: number) => {
    if (depth > maxDepth) return;

    const points = [];
    let currPt = startPt.clone();
    let currDir = dir.clone().normalize();
    
    const segments = 5;
    for (let i = 0; i <= segments; i++) {
      points.push(currPt.clone());
      if (i < segments) {
        // Twist direction slightly
        const twist = new THREE.Vector3((Math.random() - 0.5) * 2, (Math.random() - 0.5) * 0.5, (Math.random() - 0.5) * 2).normalize();
        currDir.lerp(twist, 0.2).normalize();
        currPt.add(currDir.clone().multiplyScalar(length / segments));
      }
    }

    const curve = new THREE.CatmullRomCurve3(points);
    const geo = new THREE.TubeGeometry(curve, segments * 2, radius, 8, false);
    const branch = new THREE.Mesh(geo, trunkMat);
    
    // Add glowing veins
    const wireBranch = new THREE.Mesh(geo, trunkWireMat);
    branch.add(wireBranch);
    
    parent.add(branch);

    const endPt = points[points.length - 1];

    if (depth === maxDepth) {
      if (Math.random() > 0.3) {
        const orb = createWishOrb(parent, 0, "星空下的祈愿");
        orb.position.copy(endPt);
      }
    } else {
      const numBranches = depth === 0 ? 4 : (Math.random() > 0.2 ? 3 : 2);
      for (let i = 0; i < numBranches; i++) {
        // Branch off from the upper half of the current branch
        const branchPointIdx = Math.floor(segments * (0.6 + Math.random() * 0.4));
        const branchPt = points[branchPointIdx];
        
        const angleX = (Math.random() - 0.5) * 2.0;
        const angleZ = (Math.random() - 0.5) * 2.0;
        const angleY = (Math.PI * 2 / numBranches) * i;
        
        const newDir = new THREE.Vector3(Math.sin(angleY) * Math.cos(angleX), Math.abs(Math.cos(angleX)) + 0.5, Math.cos(angleY) * Math.cos(angleZ)).normalize();
        
        buildOrganicBranch(parent, branchPt, newDir, length * (0.7 + Math.random() * 0.2), radius * 0.6, depth + 1, maxDepth);
      }
    }
  };

  buildOrganicBranch(treeGroup, new THREE.Vector3(0, 0, 0), new THREE.Vector3(0, 1, 0), 25, 3.5, 0, 4);

  // 5. The Magical Canopy (Thousands of colorful glowing leaves)
  const leafGeo = new THREE.BufferGeometry();
  const leafCount = 15000;
  const leafPos = new Float32Array(leafCount * 3);
  const leafColors = new Float32Array(leafCount * 3);
  const leafSizes = new Float32Array(leafCount);
  
  const tempColor = new THREE.Color();
  for(let i=0; i<leafCount; i++) {
    // Spherical distribution concentrated at the top
    const r = 10 + Math.random() * 35;
    const theta = Math.random() * Math.PI * 2;
    const phi = Math.acos((Math.random() * 1.5) - 0.5); // Bias towards upper hemisphere
    
    leafPos[i*3] = r * Math.sin(phi) * Math.cos(theta);
    leafPos[i*3+1] = 35 + r * Math.cos(phi) * 0.8; // Height offset and squash
    leafPos[i*3+2] = r * Math.sin(phi) * Math.sin(theta);

    // Cyan to Blue colors
    tempColor.setHSL(0.5 + Math.random() * 0.1, 0.9, 0.6);
    leafColors[i*3] = tempColor.r;
    leafColors[i*3+1] = tempColor.g;
    leafColors[i*3+2] = tempColor.b;
    
    leafSizes[i] = Math.random() * 2 + 0.5;
  }
  
  leafGeo.setAttribute('position', new THREE.BufferAttribute(leafPos, 3));
  leafGeo.setAttribute('color', new THREE.BufferAttribute(leafColors, 3));
  leafGeo.setAttribute('size', new THREE.BufferAttribute(leafSizes, 1));

  // Create a glowing particle texture
  const canvas = document.createElement('canvas');
  canvas.width = 64; canvas.height = 64;
  const ctx = canvas.getContext('2d')!;
  const gradient = ctx.createRadialGradient(32, 32, 0, 32, 32, 32);
  gradient.addColorStop(0, 'rgba(255,255,255,1)');
  gradient.addColorStop(0.2, 'rgba(255,255,255,0.8)');
  gradient.addColorStop(0.5, 'rgba(255,255,255,0.2)');
  gradient.addColorStop(1, 'rgba(0,0,0,0)');
  ctx.fillStyle = gradient;
  ctx.fillRect(0, 0, 64, 64);
  const particleTexture = new THREE.CanvasTexture(canvas);

  // Custom shader material for leaves to support varying sizes and colors
  const leafMat = new THREE.PointsMaterial({
    size: 1.5,
    vertexColors: true,
    map: particleTexture,
    blending: THREE.AdditiveBlending,
    depthWrite: false,
    transparent: true,
    opacity: 0.9
  });

  leaves = new THREE.Points(leafGeo, leafMat);
  treeGroup.add(leaves);

  // 6. Floating Fireflies & Starry Sky & Galaxy Swirl
  const ffGeo = new THREE.BufferGeometry();
  const ffCount = 800;
  const ffPos = new Float32Array(ffCount * 3);
  const ffColors = new Float32Array(ffCount * 3);
  for(let i=0; i<ffCount; i++) {
     ffPos[i*3] = (Math.random() - 0.5) * 150;
     ffPos[i*3+1] = Math.random() * 100 - 20; // Y axis
     ffPos[i*3+2] = (Math.random() - 0.5) * 150;
     
     const c = new THREE.Color().setHSL(0.5 + Math.random() * 0.1, 0.8, 0.6);
     ffColors[i*3] = c.r; ffColors[i*3+1] = c.g; ffColors[i*3+2] = c.b;
  }
  ffGeo.setAttribute('position', new THREE.BufferAttribute(ffPos, 3));
  ffGeo.setAttribute('color', new THREE.BufferAttribute(ffColors, 3));
  
  const ffMat = new THREE.PointsMaterial({
    size: 3.0,
    vertexColors: true,
    map: particleTexture,
    transparent: true,
    blending: THREE.AdditiveBlending,
    depthWrite: false
  });
  fireflies = new THREE.Points(ffGeo, ffMat);
  scene.add(fireflies);

  // Galaxy Swirl
  const swirlGeo = new THREE.BufferGeometry();
  const swirlCount = 3000;
  const swirlPos = new Float32Array(swirlCount * 3);
  const swirlColors = new Float32Array(swirlCount * 3);
  for(let i=0; i<swirlCount; i++) {
    const radius = 10 + Math.random() * 60;
    const angle = radius * 0.1 + Math.random() * Math.PI * 2;
    swirlPos[i*3] = Math.cos(angle) * radius;
    swirlPos[i*3+1] = (Math.random() - 0.5) * 10 - 10 + (radius * 0.2); // Swirl goes up slightly
    swirlPos[i*3+2] = Math.sin(angle) * radius;
    
    const c = new THREE.Color().setHSL(0.5 + Math.random() * 0.1, 0.9, 0.6); // Cyan to Blue
    swirlColors[i*3] = c.r; swirlColors[i*3+1] = c.g; swirlColors[i*3+2] = c.b;
  }
  swirlGeo.setAttribute('position', new THREE.BufferAttribute(swirlPos, 3));
  swirlGeo.setAttribute('color', new THREE.BufferAttribute(swirlColors, 3));
  const swirlMat = new THREE.PointsMaterial({
    size: 1.5,
    vertexColors: true,
    map: particleTexture,
    transparent: true,
    blending: THREE.AdditiveBlending,
    depthWrite: false,
    opacity: 0.6
  });
  const galaxySwirl = new THREE.Points(swirlGeo, swirlMat);
  galaxySwirl.userData = { isSwirl: true };
  scene.add(galaxySwirl);

  // Intro Animation State
  treeGroup.scale.setScalar(0.01);
  magicCircle.scale.setScalar(0.01);
  if (leaves.material instanceof THREE.Material) {
    leaves.material.opacity = 0;
  }
  
  introProgress = 0;

  // 7. Interaction
  const raycaster = new THREE.Raycaster();
  const mouse = new THREE.Vector2();

  const onClick = (event: MouseEvent) => {
    if (!canvasContainer.value) return;
    const rect = canvasContainer.value.getBoundingClientRect();
    mouse.x = ((event.clientX - rect.left) / rect.width) * 2 - 1;
    mouse.y = -((event.clientY - rect.top) / rect.height) * 2 + 1;
    
    raycaster.setFromCamera(mouse, camera);
    
    // Check intersection with all children of wishesGroups
    const wishMeshes = wishesGroups.flatMap(g => g.children);
    const intersects = raycaster.intersectObjects(wishMeshes);
    
    if (intersects.length > 0) {
      // Find the parent group which holds the userData
      let object: THREE.Object3D | null = intersects[0].object;
      while (object && !object.userData.isWish) {
        object = object.parent;
      }
      
      if (object && object.userData.isWish) {
        const wishText = object.userData.text;
        activeWish.value = wishText;
        
        // Smooth zoom-in effect on the group
        const group = object as THREE.Group;
        const originalScale = group.userData.originalScale || group.scale.x;
        group.userData.originalScale = originalScale;
        
        let scalePhase = 0;
        const animateScale = () => {
          scalePhase += 0.15;
          if (scalePhase <= Math.PI) {
            const scale = originalScale * (1 + 0.8 * Math.sin(scalePhase));
            group.scale.setScalar(scale);
            requestAnimationFrame(animateScale);
          } else {
            group.scale.setScalar(originalScale);
          }
        };
        animateScale();

        setTimeout(() => {
          if (activeWish.value === wishText) activeWish.value = '';
        }, 4000);
      }
    }
  };

  canvasContainer.value.addEventListener('click', onClick);
  window.addEventListener('resize', onWindowResize);

  animate();
};

const createWishOrb = (parent: THREE.Object3D, yPos: number, text: string) => {
  const colorHex = wishColors[Math.floor(Math.random() * wishColors.length)];
  
  const group = new THREE.Group();
  group.position.y = yPos;
  group.userData = { isWish: true, text: text };

  // Core Crystal (Diamond shape)
  const coreGeo = new THREE.OctahedronGeometry(1.5, 0);
  const coreMat = new THREE.MeshPhysicalMaterial({ 
    color: 0xffffff, 
    emissive: colorHex,
    emissiveIntensity: 1.5,
    roughness: 0.0,
    metalness: 0.1,
    transmission: 0.9,
    thickness: 1.0,
    clearcoat: 1.0
  });
  const core = new THREE.Mesh(coreGeo, coreMat);
  core.scale.set(1, 1.5, 1); // Elongate
  group.add(core);

  // Outer Glowing Sphere
  const haloGeo = new THREE.SphereGeometry(2.5, 32, 32);
  const haloMat = new THREE.MeshBasicMaterial({
    color: colorHex,
    transparent: true,
    opacity: 0.2,
    blending: THREE.AdditiveBlending,
    depthWrite: false
  });
  const halo = new THREE.Mesh(haloGeo, haloMat);
  group.add(halo);
  
  // Orbiting rings
  const ringGeo = new THREE.TorusGeometry(3, 0.1, 8, 32);
  const ringMat = new THREE.MeshBasicMaterial({ color: 0xffffff, transparent: true, opacity: 0.8, blending: THREE.AdditiveBlending });
  const ring1 = new THREE.Mesh(ringGeo, ringMat);
  ring1.rotation.x = Math.PI / 2;
  const ring2 = new THREE.Mesh(ringGeo, ringMat);
  ring2.rotation.y = Math.PI / 2;
  
  const ringsGroup = new THREE.Group();
  ringsGroup.add(ring1);
  ringsGroup.add(ring2);
  group.add(ringsGroup);

  // Point Light
  const light = new THREE.PointLight(colorHex, 2, 25);
  group.add(light);

  parent.add(group);
  wishesGroups.push(group);
  return group;
};

const onWindowResize = () => {
  if (!canvasContainer.value || !camera || !renderer) return;
  const width = canvasContainer.value.clientWidth;
  const height = canvasContainer.value.clientHeight;
  camera.aspect = width / height;
  camera.updateProjectionMatrix();
  renderer.setSize(width, height);
};

const animate = () => {
  animationId = requestAnimationFrame(animate);
  
  const time = Date.now() * 0.001;
  
  // Intro Animation
  if (introProgress < 1) {
    introProgress += 0.01;
    const easeOutElastic = (x: number): number => {
      const c4 = (2 * Math.PI) / 3;
      return x === 0 ? 0 : x === 1 ? 1 : Math.pow(2, -10 * x) * Math.sin((x * 10 - 0.75) * c4) + 1;
    };
    const scale = easeOutElastic(introProgress);
    if (treeGroup) treeGroup.scale.setScalar(scale);
    if (magicCircle) magicCircle.scale.setScalar(Math.min(1, introProgress * 2)); // Scale up faster
    if (leaves && leaves.material instanceof THREE.PointsMaterial) {
      leaves.material.opacity = Math.min(1, introProgress * 1.5);
    }
  }
  
  if (treeGroup) {
    treeGroup.rotation.y = Math.sin(time * 0.1) * 0.2; // Gentle sway
  }

  if (leaves) {
    leaves.rotation.y = time * 0.05; // Slowly rotate the canopy
    
    // Pulsate colors of leaves
    const colors = leaves.geometry.attributes.color.array as Float32Array;
    const tempColor = new THREE.Color();
    for(let i=0; i<colors.length/3; i+=10) { // Update a subset for performance/shimmer effect
       tempColor.setHSL((time * 0.1 + i * 0.001) % 1, 0.9, 0.6);
       colors[i*3] = tempColor.r;
       colors[i*3+1] = tempColor.g;
       colors[i*3+2] = tempColor.b;
    }
    leaves.geometry.attributes.color.needsUpdate = true;
  }
  
  if (fireflies) {
    const positions = fireflies.geometry.attributes.position.array as Float32Array;
    for(let i=0; i<positions.length; i+=3) {
      positions[i+1] += Math.sin(time + i) * 0.05 + 0.08; // Float up faster
      if (positions[i+1] > 100) positions[i+1] = -20; // Reset
      
      positions[i] += Math.sin(time * 2 + i) * 0.03; // Sway X
      positions[i+2] += Math.cos(time * 2 + i) * 0.03; // Sway Z
    }
    fireflies.geometry.attributes.position.needsUpdate = true;
  }

  // Rotate galaxy swirl
  scene.children.forEach(child => {
    if (child.userData.isSwirl) {
      child.rotation.y = time * 0.05;
    }
  });

  if (magicCircle) {
    magicCircle.rotation.y = -time * 0.2;
    magicCircle.children[0].rotation.z = time * 0.5;
    magicCircle.children[1].rotation.z = -time * 0.3;
  }

  // Animate wishes
  wishesGroups.forEach((group, i) => {
    group.position.y += Math.sin(time * 2 + i) * 0.01; // Bobbing
    group.children[0].rotation.y += 0.02; // Core spinning
    
    // Rotate rings
    if (group.children[2]) {
      group.children[2].rotation.x += 0.01;
      group.children[2].rotation.y += 0.015;
      group.children[2].rotation.z += 0.005;
    }
  });

  renderer.render(scene, camera);
};

const handleAddWish = () => {
  if (!newWish.value.trim() || wishesGroups.length === 0) return;
  
  // Pick a random existing wish to branch off from
  const randomBase = wishesGroups[Math.floor(Math.random() * wishesGroups.length)];
  
  const newOrb = createWishOrb(randomBase.parent!, 0, newWish.value);
  
  // Position it near the random base but slightly offset
  newOrb.position.copy(randomBase.position);
  newOrb.position.x += (Math.random() - 0.5) * 8;
  newOrb.position.y += (Math.random() - 0.5) * 8;
  newOrb.position.z += (Math.random() - 0.5) * 8;
  
  // Animate scale in
  newOrb.scale.setScalar(0);
  let scale = 0;
  const grow = () => {
    scale += 0.05;
    if (scale <= 1) {
      newOrb.scale.setScalar(scale);
      requestAnimationFrame(grow);
    }
  };
  grow();

  activeWish.value = `许愿成功: ${newWish.value}`;
  setTimeout(() => activeWish.value = '', 4000);
  newWish.value = '';
};

onMounted(() => {
  initThree();
});

onUnmounted(() => {
  if (animationId) cancelAnimationFrame(animationId);
  window.removeEventListener('resize', onWindowResize);
  if (resizeObserver) resizeObserver.disconnect();
  if (renderer) {
    renderer.dispose();
  }
});
</script>

<template>
  <div class="relative w-full group/wrapper">
    <!-- Gorgeous Outer Glow -->
    <div class="absolute -inset-1 bg-gradient-to-r from-blue-600 via-cyan-500 to-teal-400 rounded-[3.5rem] blur-2xl opacity-40 group-hover/wrapper:opacity-60 transition-opacity duration-700 animate-pulse-slow pointer-events-none"></div>
    
    <div class="relative bg-white/40 dark:bg-[#0a0510]/60 backdrop-blur-3xl rounded-[3rem] p-6 md:p-12 border border-white/60 dark:border-white/10 shadow-2xl overflow-hidden reveal-animation">
      
      <!-- Luxury Top Decoration -->
      <div class="absolute top-0 left-0 w-full h-full overflow-hidden pointer-events-none">
        <div class="absolute -top-32 -right-32 w-96 h-96 bg-blue-500/20 rounded-full blur-[100px]"></div>
        <div class="absolute -bottom-32 -left-32 w-96 h-96 bg-cyan-500/20 rounded-full blur-[100px]"></div>
      </div>

      <!-- Header Section -->
      <div class="relative z-10 flex flex-col items-center mb-10 md:mb-14">
        <!-- Magical Runes Background -->
        <div class="absolute inset-0 flex items-center justify-center opacity-10 pointer-events-none overflow-hidden">
          <span class="text-9xl font-serif tracking-[1em] text-blue-300 transform -rotate-6">✧ ✦ ✧</span>
        </div>
        
        <div class="flex items-center justify-center space-x-4 md:space-x-8 mb-6 relative">
          <div class="h-[3px] w-16 md:w-40 bg-gradient-to-r from-transparent via-blue-500 to-cyan-500 rounded-full shadow-[0_0_10px_rgba(59,130,246,0.8)]"></div>
          <Sparkles :size="40" class="text-cyan-400 animate-pulse drop-shadow-[0_0_15px_rgba(6,182,212,0.8)]" stroke-width="1.5" />
          <h1 class="font-serif font-bold text-5xl md:text-7xl text-center tracking-widest text-transparent bg-clip-text bg-gradient-to-r from-blue-400 via-cyan-300 to-teal-300 drop-shadow-[0_0_20px_rgba(255,255,255,0.5)] animate-gradient-x py-4">
            幻彩星空许愿树
          </h1>
          <Sparkles :size="40" class="text-teal-400 animate-pulse drop-shadow-[0_0_15px_rgba(45,212,191,0.8)]" stroke-width="1.5" />
          <div class="h-[3px] w-16 md:w-40 bg-gradient-to-l from-transparent via-teal-500 to-cyan-500 rounded-full shadow-[0_0_10px_rgba(20,184,166,0.8)]"></div>
        </div>
        <p class="text-center text-cyan-100 font-serif text-xl md:text-2xl tracking-[0.25em] opacity-90 drop-shadow-[0_2px_4px_rgba(0,0,0,0.8)]">
          "在流光溢彩的魔法阵中，每一颗璀璨的钻石，都是一个闪耀的愿望。"
        </p>
      </div>

      <!-- Magical Portal (Canvas Container) -->
      <div class="relative w-full h-[65vh] md:h-[75vh] rounded-[2.5rem] overflow-hidden group/canvas shadow-[0_0_80px_rgba(200,50,255,0.15)]">
        <!-- Portal Border Glow -->
        <div class="absolute inset-0 rounded-[2.5rem] border-[4px] border-transparent bg-clip-padding pointer-events-none z-10" style="background-image: linear-gradient(#05020a, #05020a), linear-gradient(135deg, rgba(255,0,255,0.5), rgba(0,255,255,0.5)); background-origin: border-box; background-clip: padding-box, border-box;"></div>
        
        <!-- Inner Shadow for depth -->
        <div class="absolute inset-0 shadow-[inset_0_0_120px_rgba(0,0,0,0.9)] z-10 pointer-events-none rounded-[2.5rem]"></div>

        <div ref="canvasContainer" class="absolute inset-0 cursor-pointer rounded-[2.5rem] overflow-hidden bg-[#05020a]"></div>
        
        <!-- UI Overlay (Input & Button) -->
        <div class="absolute bottom-8 left-1/2 -translate-x-1/2 z-20 flex flex-col items-center w-full max-w-3xl px-4 transition-all duration-700 transform group-hover/canvas:-translate-y-4">
          <!-- Ornate Magical Container -->
          <div class="relative w-full p-[3px] rounded-[2.5rem] bg-gradient-to-r from-blue-400 via-cyan-400 to-teal-400 backdrop-blur-xl shadow-[0_20px_80px_rgba(0,0,0,0.8)] hover:shadow-[0_20px_100px_rgba(0,255,255,0.5)] transition-shadow duration-500">
            <!-- Decorative Corner Ornaments -->
            <div class="absolute -top-4 -left-4 w-12 h-12 border-t-4 border-l-4 border-cyan-400 rounded-tl-2xl shadow-[0_0_15px_rgba(34,211,238,0.8)]"></div>
            <div class="absolute -top-4 -right-4 w-12 h-12 border-t-4 border-r-4 border-teal-400 rounded-tr-2xl shadow-[0_0_15px_rgba(45,212,191,0.8)]"></div>
            <div class="absolute -bottom-4 -left-4 w-12 h-12 border-b-4 border-l-4 border-blue-400 rounded-bl-2xl shadow-[0_0_15px_rgba(59,130,246,0.8)]"></div>
            <div class="absolute -bottom-4 -right-4 w-12 h-12 border-b-4 border-r-4 border-cyan-400 rounded-br-2xl shadow-[0_0_15px_rgba(34,211,238,0.8)]"></div>

            <div class="bg-[#05020a]/90 backdrop-blur-3xl p-8 md:p-10 rounded-[2.3rem] w-full flex flex-col gap-6 relative overflow-hidden">
              
              <!-- Glass reflection & Magical Runes -->
              <div class="absolute top-0 left-0 w-full h-1/2 bg-gradient-to-b from-white/15 to-transparent pointer-events-none"></div>
              <div class="absolute inset-0 flex items-center justify-between px-4 opacity-5 pointer-events-none">
                <span class="text-6xl font-serif text-white">✧</span>
                <span class="text-6xl font-serif text-white">✦</span>
              </div>
              
              <div class="relative group/input flex items-center">
                <div class="absolute left-6 z-20 text-cyan-400 animate-pulse">
                  <Sparkles :size="28" />
                </div>
                <input 
                  v-model="newWish" 
                  type="text" 
                  placeholder="写下你最绚烂的期盼..." 
                  class="w-full bg-white/5 border-2 border-white/10 rounded-3xl pl-16 pr-6 py-6 text-white placeholder-cyan-200/50 outline-none focus:border-transparent focus:ring-0 transition-all duration-300 font-serif text-xl md:text-2xl shadow-[inset_0_4px_20px_rgba(0,0,0,0.5)] relative z-10" 
                  @keyup.enter="handleAddWish" 
                />
                <!-- Animated Focus Border -->
                <div class="absolute inset-0 rounded-3xl border-2 border-transparent pointer-events-none transition-all duration-500 z-20" :class="newWish.length > 0 ? 'border-cyan-400 shadow-[0_0_40px_rgba(34,211,238,0.6)]' : 'group-focus-within/input:border-teal-400/50 group-focus-within/input:shadow-[0_0_30px_rgba(45,212,191,0.4)]'"></div>
              </div>

              <button 
                @click="handleAddWish" 
                class="relative w-full group/btn overflow-hidden rounded-3xl shadow-[0_0_40px_rgba(0,255,255,0.4)] hover:shadow-[0_0_60px_rgba(0,255,255,0.6)] transition-all duration-500 hover:-translate-y-2 active:translate-y-0 border border-white/20"
              >
                <div class="absolute inset-0 bg-gradient-to-r from-blue-600 via-cyan-500 to-teal-500 opacity-90 group-hover/btn:opacity-100 transition-opacity duration-300"></div>
                <!-- Shimmer effect -->
                <div class="absolute inset-0 -translate-x-full bg-gradient-to-r from-transparent via-white/50 to-transparent group-hover/btn:animate-shimmer"></div>
                
                <div class="relative px-8 py-6 flex items-center justify-center gap-4">
                  <Sparkles :size="28" class="text-white animate-spin-slow" />
                  <span class="text-white font-bold tracking-[0.4em] text-xl md:text-2xl drop-shadow-[0_2px_4px_rgba(0,0,0,0.5)]">
                    缔结契约 · 挂上许愿树
                  </span>
                  <Sparkles :size="28" class="text-white animate-spin-slow" />
                </div>
              </button>
            </div>
          </div>
        </div>

        <!-- Wish Tooltip -->
        <Transition name="fade-scale">
          <div 
            v-if="activeWish" 
            class="absolute top-1/4 left-1/2 -translate-x-1/2 z-30 pointer-events-none w-11/12 max-w-2xl"
          >
            <!-- Ornate Tooltip Container -->
            <div class="relative px-10 py-12 md:px-16 md:py-16 rounded-[3rem] bg-[#05020a]/80 backdrop-blur-3xl border-2 border-cyan-400/50 shadow-[0_0_100px_rgba(34,211,238,0.6)] overflow-hidden">
              <!-- Glowing aura -->
              <div class="absolute inset-0 bg-gradient-to-br from-blue-500/30 via-cyan-500/30 to-teal-500/30 animate-pulse-slow"></div>
              <!-- Glass highlight -->
              <div class="absolute top-0 left-0 w-full h-1/2 bg-gradient-to-b from-white/20 to-transparent"></div>
              
              <!-- Decorative elements -->
              <div class="absolute top-6 left-1/2 -translate-x-1/2 text-cyan-400 opacity-80">
                <Sparkles :size="32" class="animate-pulse" />
              </div>
              
              <p class="relative z-10 font-serif text-3xl md:text-5xl text-center tracking-widest text-transparent bg-clip-text bg-gradient-to-b from-white via-cyan-100 to-blue-200 drop-shadow-[0_5px_15px_rgba(0,0,0,0.8)] leading-relaxed mt-4">
                "{{ activeWish }}"
              </p>
              
              <div class="absolute bottom-6 left-1/2 -translate-x-1/2 text-cyan-400 opacity-80">
                <Sparkles :size="32" class="animate-pulse" />
              </div>
            </div>
          </div>
        </Transition>
      </div>
    </div>
  </div>
</template>

<style scoped>
.animate-gradient-x {
  background-size: 200% 200%;
  animation: gradient-x 4s ease infinite;
}

@keyframes gradient-x {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}

.animate-pulse-slow {
  animation: pulse-slow 4s cubic-bezier(0.4, 0, 0.6, 1) infinite;
}

@keyframes pulse-slow {
  0%, 100% { opacity: 0.4; }
  50% { opacity: 0.8; }
}

.animate-shimmer {
  animation: shimmer 2.5s infinite;
}

@keyframes shimmer {
  100% { transform: translateX(100%); }
}

.fade-scale-enter-active,
.fade-scale-leave-active {
  transition: all 0.5s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.fade-scale-enter-from,
.fade-scale-leave-to {
  opacity: 0;
  transform: translate(-50%, 20px) scale(0.9);
}
</style>
