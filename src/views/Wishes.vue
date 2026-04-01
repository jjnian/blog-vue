<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue';
import * as THREE from 'three';
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls.js';
import { Sparkles, Send, Star } from 'lucide-vue-next';

const canvasContainer = ref<HTMLElement | null>(null);
const newWish = ref('');
const activeWish = ref('');
const wishes = ref<string[]>([
  '愿世界和平 🕊️',
  '希望家人健康平安 ❤️',
  '愿所有梦想都能实现 ✨',
  '希望每天都开心 🌟',
  '愿代码永无 Bug 🖥️',
  '希望能环游世界 🌍',
]);
const showWishPanel = ref(false);
const wishCount = ref(6);

let scene: THREE.Scene;
let camera: THREE.PerspectiveCamera;
let renderer: THREE.WebGLRenderer;
let controls: OrbitControls;
let animationId: number;
let clock: THREE.Clock;
let resizeObserver: ResizeObserver;

// Tree components
let treeGroup: THREE.Group;
let petalSystem: THREE.Points;
let fireflySystem: THREE.Points;
let auroraGroup: THREE.Group;
let starField: THREE.Points;
let groundMirror: THREE.Mesh;
let magicCircle: THREE.Group;
const lanternGroups: THREE.Group[] = [];
const branchEndPoints: THREE.Vector3[] = [];

// Petal animation data
let petalVelocities: Float32Array;
let petalRotations: Float32Array;

const initScene = () => {
  if (!canvasContainer.value) return;

  const width = canvasContainer.value.clientWidth || 800;
  const height = canvasContainer.value.clientHeight || 600;

  clock = new THREE.Clock();

  // Scene
  scene = new THREE.Scene();
  scene.fog = new THREE.FogExp2(0x0a0015, 0.006);

  // Camera
  camera = new THREE.PerspectiveCamera(55, width / height, 0.1, 1500);
  camera.position.set(0, 35, 80);
  camera.lookAt(0, 20, 0);

  // Renderer
  renderer = new THREE.WebGLRenderer({ antialias: true, alpha: false });
  renderer.setSize(width, height);
  renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2));
  renderer.toneMapping = THREE.ACESFilmicToneMapping;
  renderer.toneMappingExposure = 1.3;
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

  createSkyAndStars();
  createAurora();
  createGround();
  createMagicCircle();
  createTree();
  createPetals();
  createFireflies();
  createLighting();

  animate();
};

// ========== SKY & STARS ==========
const createSkyAndStars = () => {
  // Gradient sky dome
  const skyGeo = new THREE.SphereGeometry(500, 32, 32);
  const skyMat = new THREE.ShaderMaterial({
    uniforms: {
      topColor: { value: new THREE.Color(0x000510) },
      midColor: { value: new THREE.Color(0x0a0030) },
      bottomColor: { value: new THREE.Color(0x150040) },
    },
    vertexShader: `
      varying vec3 vWorldPosition;
      void main() {
        vec4 worldPosition = modelMatrix * vec4(position, 1.0);
        vWorldPosition = worldPosition.xyz;
        gl_Position = projectionMatrix * modelViewMatrix * vec4(position, 1.0);
      }
    `,
    fragmentShader: `
      uniform vec3 topColor;
      uniform vec3 midColor;
      uniform vec3 bottomColor;
      varying vec3 vWorldPosition;
      void main() {
        float h = normalize(vWorldPosition).y;
        vec3 color;
        if (h > 0.0) {
          color = mix(midColor, topColor, h);
        } else {
          color = mix(midColor, bottomColor, -h);
        }
        gl_FragColor = vec4(color, 1.0);
      }
    `,
    side: THREE.BackSide,
    depthWrite: false,
  });
  scene.add(new THREE.Mesh(skyGeo, skyMat));

  // Stars
  const starGeo = new THREE.BufferGeometry();
  const starCount = 5000;
  const starPos = new Float32Array(starCount * 3);
  const starSizes = new Float32Array(starCount);
  for (let i = 0; i < starCount; i++) {
    const r = 400 + Math.random() * 80;
    const theta = Math.random() * Math.PI * 2;
    const phi = Math.acos(Math.random() * 0.8 + 0.2);
    starPos[i * 3] = r * Math.sin(phi) * Math.cos(theta);
    starPos[i * 3 + 1] = r * Math.cos(phi);
    starPos[i * 3 + 2] = r * Math.sin(phi) * Math.sin(theta);
    starSizes[i] = Math.random() * 2 + 0.5;
  }
  starGeo.setAttribute('position', new THREE.BufferAttribute(starPos, 3));
  starGeo.setAttribute('size', new THREE.BufferAttribute(starSizes, 1));

  const starTex = createGlowTexture(64, 'rgba(255,255,255,1)', 'rgba(200,220,255,0)');
  const starMat = new THREE.PointsMaterial({
    size: 1.5,
    map: starTex,
    transparent: true,
    blending: THREE.AdditiveBlending,
    depthWrite: false,
    opacity: 0.9,
  });
  starField = new THREE.Points(starGeo, starMat);
  scene.add(starField);
};

// ========== AURORA ==========
const createAurora = () => {
  auroraGroup = new THREE.Group();
  scene.add(auroraGroup);

  const colors = [
    [0x00ff88, 0x00ffcc],
    [0x4488ff, 0x00ccff],
    [0xff44ff, 0x8844ff],
  ];

  for (let band = 0; band < 3; band++) {
    const curve = new THREE.CatmullRomCurve3([
      new THREE.Vector3(-200 + band * 40, 200 + band * 30, -200 + band * 20),
      new THREE.Vector3(-80 + band * 30, 220 + band * 20, -180 + band * 15),
      new THREE.Vector3(0, 240 + band * 15, -160 + band * 10),
      new THREE.Vector3(80 - band * 30, 220 + band * 20, -180 + band * 15),
      new THREE.Vector3(200 - band * 40, 200 + band * 30, -200 + band * 20),
    ]);

    const points = curve.getPoints(80);
    const geo = new THREE.BufferGeometry();
    const positions: number[] = [];
    const alphas: number[] = [];

    for (let i = 0; i < points.length; i++) {
      const p = points[i];
      const heightVariation = 30 + Math.random() * 20;
      // Create vertical strips
      for (let j = 0; j < 5; j++) {
        const yOff = j * heightVariation * 0.2;
        positions.push(p.x, p.y + yOff, p.z);
        alphas.push(1.0 - j * 0.2);
      }
    }

    geo.setAttribute('position', new THREE.BufferAttribute(new Float32Array(positions), 3));
    geo.setAttribute('alpha', new THREE.BufferAttribute(new Float32Array(alphas), 1));

    const mat = new THREE.PointsMaterial({
      size: 4,
      color: colors[band][0],
      transparent: true,
      opacity: 0.15,
      blending: THREE.AdditiveBlending,
      depthWrite: false,
      map: createGlowTexture(32, 'rgba(255,255,255,1)', 'rgba(255,255,255,0)'),
    });

    const aurora = new THREE.Points(geo, mat);
    aurora.userData = { basePositions: new Float32Array(positions), band };
    auroraGroup.add(aurora);
  }
};

// ========== GROUND ==========
const createGround = () => {
  // Reflective dark water surface
  const groundGeo = new THREE.PlaneGeometry(600, 600, 128, 128);
  const groundMat = new THREE.MeshStandardMaterial({
    color: 0x050510,
    metalness: 0.95,
    roughness: 0.05,
    transparent: true,
    opacity: 0.85,
  });
  groundMirror = new THREE.Mesh(groundGeo, groundMat);
  groundMirror.rotation.x = -Math.PI / 2;
  groundMirror.position.y = -1;
  groundMirror.receiveShadow = true;
  scene.add(groundMirror);

  // Ripple rings on ground
  for (let i = 0; i < 5; i++) {
    const ringGeo = new THREE.RingGeometry(8 + i * 12, 8.3 + i * 12, 64);
    const ringMat = new THREE.MeshBasicMaterial({
      color: 0x2244aa,
      transparent: true,
      opacity: 0.08 - i * 0.012,
      side: THREE.DoubleSide,
      blending: THREE.AdditiveBlending,
    });
    const ring = new THREE.Mesh(ringGeo, ringMat);
    ring.rotation.x = -Math.PI / 2;
    ring.position.y = -0.5;
    scene.add(ring);
  }
};

// ========== MAGIC CIRCLE ==========
const createMagicCircle = () => {
  magicCircle = new THREE.Group();
  magicCircle.position.y = 0;
  scene.add(magicCircle);

  // Outer ring
  const ring1 = new THREE.Mesh(
    new THREE.TorusGeometry(18, 0.15, 16, 128),
    new THREE.MeshBasicMaterial({ color: 0x44aaff, transparent: true, opacity: 0.6, blending: THREE.AdditiveBlending })
  );
  ring1.rotation.x = Math.PI / 2;
  magicCircle.add(ring1);

  // Inner ring
  const ring2 = new THREE.Mesh(
    new THREE.TorusGeometry(14, 0.1, 16, 128),
    new THREE.MeshBasicMaterial({ color: 0xff88cc, transparent: true, opacity: 0.4, blending: THREE.AdditiveBlending })
  );
  ring2.rotation.x = Math.PI / 2;
  magicCircle.add(ring2);

  // Rune symbols (small diamonds around the circle)
  for (let i = 0; i < 12; i++) {
    const angle = (i / 12) * Math.PI * 2;
    const runeGeo = new THREE.OctahedronGeometry(0.4, 0);
    const runeMat = new THREE.MeshBasicMaterial({
      color: 0x88ccff,
      transparent: true,
      opacity: 0.8,
      blending: THREE.AdditiveBlending,
    });
    const rune = new THREE.Mesh(runeGeo, runeMat);
    rune.position.set(Math.cos(angle) * 16, 0.5, Math.sin(angle) * 16);
    magicCircle.add(rune);
  }

  // Glow disc
  const discMat = new THREE.MeshBasicMaterial({
    color: 0x2244aa,
    transparent: true,
    opacity: 0.15,
    blending: THREE.AdditiveBlending,
    side: THREE.DoubleSide,
  });
  const disc = new THREE.Mesh(new THREE.CircleGeometry(17, 64), discMat);
  disc.rotation.x = -Math.PI / 2;
  disc.position.y = 0.1;
  magicCircle.add(disc);
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

    const endPt = pts[pts.length - 1];

    if (depth >= maxDepth - 1) {
      branchEndPoints.push(endPt.clone());
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

  // Place initial lanterns
  placeInitialLanterns();
};

// ========== LANTERNS (WISHES) ==========
const createLantern = (position: THREE.Vector3, text: string, colorHue: number) => {
  const group = new THREE.Group();
  group.position.copy(position);
  group.userData = { text, isLantern: true, phase: Math.random() * Math.PI * 2 };

  // Lantern body (glowing sphere)
  const bodyColor = new THREE.Color().setHSL(colorHue, 0.9, 0.6);
  const bodyGeo = new THREE.SphereGeometry(1.2, 16, 16);
  const bodyMat = new THREE.MeshPhysicalMaterial({
    color: bodyColor,
    emissive: bodyColor,
    emissiveIntensity: 2,
    roughness: 0.1,
    metalness: 0.0,
    transmission: 0.6,
    thickness: 0.5,
    clearcoat: 1.0,
  });
  group.add(new THREE.Mesh(bodyGeo, bodyMat));

  // Outer glow
  const glowGeo = new THREE.SphereGeometry(2.0, 16, 16);
  const glowMat = new THREE.MeshBasicMaterial({
    color: bodyColor,
    transparent: true,
    opacity: 0.15,
    blending: THREE.AdditiveBlending,
    depthWrite: false,
  });
  group.add(new THREE.Mesh(glowGeo, glowMat));

  // Light source
  const light = new THREE.PointLight(bodyColor.getHex(), 2.5, 15);
  group.add(light);

  // Ribbon (hanging string from branch to lantern)
  const ribbonColor = new THREE.Color().setHSL(colorHue, 0.7, 0.8);
  const ribbonGeo = new THREE.CylinderGeometry(0.03, 0.03, 4, 4);
  const ribbonMat = new THREE.MeshBasicMaterial({
    color: ribbonColor,
    transparent: true,
    opacity: 0.6,
  });
  const ribbon = new THREE.Mesh(ribbonGeo, ribbonMat);
  ribbon.position.y = 2.5;
  group.add(ribbon);

  // Small tag at bottom
  const tagGeo = new THREE.PlaneGeometry(1.5, 0.8);
  const tagMat = new THREE.MeshBasicMaterial({
    color: ribbonColor,
    transparent: true,
    opacity: 0.4,
    side: THREE.DoubleSide,
    blending: THREE.AdditiveBlending,
  });
  const tag = new THREE.Mesh(tagGeo, tagMat);
  tag.position.y = -1.8;
  group.add(tag);

  treeGroup.add(group);
  lanternGroups.push(group);
  return group;
};

const placeInitialLanterns = () => {
  if (branchEndPoints.length === 0) return;

  const defaultWishes = wishes.value;
  for (let i = 0; i < Math.min(defaultWishes.length, branchEndPoints.length); i++) {
    const pt = branchEndPoints[i % branchEndPoints.length].clone();
    pt.y -= 3 + Math.random() * 2;
    pt.x += (Math.random() - 0.5) * 3;
    pt.z += (Math.random() - 0.5) * 3;
    createLantern(pt, defaultWishes[i], Math.random());
  }
};

// ========== PETALS ==========
const createPetals = () => {
  const petalCount = 3000;
  const geo = new THREE.BufferGeometry();
  const positions = new Float32Array(petalCount * 3);
  const colors = new Float32Array(petalCount * 3);
  const sizes = new Float32Array(petalCount);
  petalVelocities = new Float32Array(petalCount * 3);
  petalRotations = new Float32Array(petalCount);

  const petalColors = [
    new THREE.Color(0xff88aa), // Pink
    new THREE.Color(0xffaacc), // Light pink
    new THREE.Color(0xffccdd), // Pale pink
    new THREE.Color(0xff6699), // Hot pink
    new THREE.Color(0xffeedd), // Cream
    new THREE.Color(0xffdd88), // Gold
    new THREE.Color(0xddaaff), // Lavender
    new THREE.Color(0xaaddff), // Sky blue
  ];

  for (let i = 0; i < petalCount; i++) {
    // Start scattered around the canopy
    const r = 5 + Math.random() * 30;
    const theta = Math.random() * Math.PI * 2;
    const phi = Math.acos(Math.random() * 1.2 - 0.2);

    positions[i * 3] = r * Math.sin(phi) * Math.cos(theta);
    positions[i * 3 + 1] = 25 + Math.random() * 40;
    positions[i * 3 + 2] = r * Math.sin(phi) * Math.sin(theta);

    const c = petalColors[Math.floor(Math.random() * petalColors.length)];
    colors[i * 3] = c.r;
    colors[i * 3 + 1] = c.g;
    colors[i * 3 + 2] = c.b;

    sizes[i] = 0.5 + Math.random() * 1.5;

    petalVelocities[i * 3] = (Math.random() - 0.5) * 0.3;
    petalVelocities[i * 3 + 1] = -(Math.random() * 0.15 + 0.05);
    petalVelocities[i * 3 + 2] = (Math.random() - 0.5) * 0.3;
    petalRotations[i] = Math.random() * Math.PI * 2;
  }

  geo.setAttribute('position', new THREE.BufferAttribute(positions, 3));
  geo.setAttribute('color', new THREE.BufferAttribute(colors, 3));
  geo.setAttribute('size', new THREE.BufferAttribute(sizes, 1));

  const petalTex = createPetalTexture();
  const mat = new THREE.PointsMaterial({
    size: 1.2,
    vertexColors: true,
    map: petalTex,
    transparent: true,
    opacity: 0.85,
    blending: THREE.AdditiveBlending,
    depthWrite: false,
  });

  petalSystem = new THREE.Points(geo, mat);
  scene.add(petalSystem);
};

// ========== FIREFLIES ==========
const createFireflies = () => {
  const ffCount = 500;
  const geo = new THREE.BufferGeometry();
  const positions = new Float32Array(ffCount * 3);
  const colors = new Float32Array(ffCount * 3);

  const ffColors = [0x44ffaa, 0xffdd44, 0x44aaff, 0xff88ff, 0xaaffdd];
  for (let i = 0; i < ffCount; i++) {
    positions[i * 3] = (Math.random() - 0.5) * 120;
    positions[i * 3 + 1] = Math.random() * 80;
    positions[i * 3 + 2] = (Math.random() - 0.5) * 120;

    const c = new THREE.Color(ffColors[Math.floor(Math.random() * ffColors.length)]);
    colors[i * 3] = c.r;
    colors[i * 3 + 1] = c.g;
    colors[i * 3 + 2] = c.b;
  }

  geo.setAttribute('position', new THREE.BufferAttribute(positions, 3));
  geo.setAttribute('color', new THREE.BufferAttribute(colors, 3));

  const mat = new THREE.PointsMaterial({
    size: 1.5,
    vertexColors: true,
    map: createGlowTexture(64, 'rgba(255,255,255,1)', 'rgba(255,255,255,0)'),
    transparent: true,
    blending: THREE.AdditiveBlending,
    depthWrite: false,
    opacity: 0.7,
  });

  fireflySystem = new THREE.Points(geo, mat);
  scene.add(fireflySystem);
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
  moonLight.shadow.camera.left = -50;
  moonLight.shadow.camera.right = 50;
  moonLight.shadow.camera.top = 50;
  moonLight.shadow.camera.bottom = -50;
  scene.add(moonLight);

  // Rim lights for tree
  const rimLight1 = new THREE.PointLight(0xff4488, 3, 80);
  rimLight1.position.set(-30, 30, -20);
  scene.add(rimLight1);

  const rimLight2 = new THREE.PointLight(0x4488ff, 3, 80);
  rimLight2.position.set(30, 30, 20);
  scene.add(rimLight2);

  // Ground accent light
  const groundLight = new THREE.PointLight(0x2244aa, 2, 40);
  groundLight.position.set(0, 1, 0);
  scene.add(groundLight);
};

// ========== TEXTURES ==========
const createGlowTexture = (size: number, innerColor: string, outerColor: string) => {
  const canvas = document.createElement('canvas');
  canvas.width = size;
  canvas.height = size;
  const ctx = canvas.getContext('2d')!;
  const half = size / 2;
  const gradient = ctx.createRadialGradient(half, half, 0, half, half, half);
  gradient.addColorStop(0, innerColor);
  gradient.addColorStop(0.3, innerColor.replace('1)', '0.6)'));
  gradient.addColorStop(1, outerColor);
  ctx.fillStyle = gradient;
  ctx.fillRect(0, 0, size, size);
  return new THREE.CanvasTexture(canvas);
};

const createPetalTexture = () => {
  const canvas = document.createElement('canvas');
  canvas.width = 64;
  canvas.height = 64;
  const ctx = canvas.getContext('2d')!;

  // Draw petal shape
  ctx.beginPath();
  ctx.moveTo(32, 8);
  ctx.bezierCurveTo(20, 8, 8, 24, 16, 40);
  ctx.bezierCurveTo(20, 50, 28, 56, 32, 56);
  ctx.bezierCurveTo(36, 56, 44, 50, 48, 40);
  ctx.bezierCurveTo(56, 24, 44, 8, 32, 8);
  ctx.closePath();

  const gradient = ctx.createRadialGradient(32, 32, 0, 32, 32, 28);
  gradient.addColorStop(0, 'rgba(255,255,255,1)');
  gradient.addColorStop(0.5, 'rgba(255,255,255,0.6)');
  gradient.addColorStop(1, 'rgba(255,255,255,0)');
  ctx.fillStyle = gradient;
  ctx.fill();

  return new THREE.CanvasTexture(canvas);
};

// ========== ANIMATION ==========
const animate = () => {
  animationId = requestAnimationFrame(animate);
  const time = clock.getElapsedTime();
  const delta = clock.getDelta();

  controls.update();

  // Tree gentle sway
  if (treeGroup) {
    treeGroup.rotation.y = Math.sin(time * 0.15) * 0.03;
  }

  // Magic circle rotation
  if (magicCircle) {
    magicCircle.rotation.y = time * 0.15;
    // Pulsing runes
    magicCircle.children.forEach((child, i) => {
      if (i >= 2 && i < 14) {
        child.position.y = 0.5 + Math.sin(time * 2 + i) * 0.3;
        (child as THREE.Mesh).scale.setScalar(0.8 + Math.sin(time * 3 + i * 0.5) * 0.3);
      }
    });
  }

  // Petal animation
  if (petalSystem) {
    const pos = petalSystem.geometry.attributes.position.array as Float32Array;
    for (let i = 0; i < pos.length / 3; i++) {
      // Apply velocity
      pos[i * 3] += petalVelocities[i * 3] + Math.sin(time + i * 0.1) * 0.05;
      pos[i * 3 + 1] += petalVelocities[i * 3 + 1];
      pos[i * 3 + 2] += petalVelocities[i * 3 + 2] + Math.cos(time + i * 0.1) * 0.05;

      // Spiral motion
      petalRotations[i] += 0.01;
      pos[i * 3] += Math.sin(petalRotations[i]) * 0.03;
      pos[i * 3 + 2] += Math.cos(petalRotations[i]) * 0.03;

      // Reset when below ground
      if (pos[i * 3 + 1] < -2) {
        const r = 5 + Math.random() * 25;
        const theta = Math.random() * Math.PI * 2;
        pos[i * 3] = Math.cos(theta) * r;
        pos[i * 3 + 1] = 40 + Math.random() * 30;
        pos[i * 3 + 2] = Math.sin(theta) * r;
      }
    }
    petalSystem.geometry.attributes.position.needsUpdate = true;
  }

  // Firefly animation
  if (fireflySystem) {
    const pos = fireflySystem.geometry.attributes.position.array as Float32Array;
    for (let i = 0; i < pos.length / 3; i++) {
      pos[i * 3] += Math.sin(time * 0.5 + i * 7.3) * 0.08;
      pos[i * 3 + 1] += Math.sin(time * 0.7 + i * 3.1) * 0.04 + 0.02;
      pos[i * 3 + 2] += Math.cos(time * 0.5 + i * 5.7) * 0.08;

      if (pos[i * 3 + 1] > 90) pos[i * 3 + 1] = -2;
    }
    fireflySystem.geometry.attributes.position.needsUpdate = true;

    // Pulsing opacity
    if (fireflySystem.material instanceof THREE.PointsMaterial) {
      fireflySystem.material.opacity = 0.5 + Math.sin(time * 2) * 0.2;
    }
  }

  // Aurora animation
  if (auroraGroup) {
    auroraGroup.children.forEach((aurora) => {
      const base = aurora.userData.basePositions;
      const band = aurora.userData.band;
      if (base) {
        const pos = (aurora as THREE.Points).geometry.attributes.position.array as Float32Array;
        for (let i = 0; i < pos.length; i += 3) {
          pos[i] = base[i] + Math.sin(time * 0.3 + i * 0.01 + band) * 15;
          pos[i + 1] = base[i + 1] + Math.sin(time * 0.5 + i * 0.02) * 8;
          pos[i + 2] = base[i + 2] + Math.cos(time * 0.2 + i * 0.01) * 5;
        }
        (aurora as THREE.Points).geometry.attributes.position.needsUpdate = true;
      }
      if ((aurora as THREE.Points).material instanceof THREE.PointsMaterial) {
        ((aurora as THREE.Points).material as THREE.PointsMaterial).opacity = 0.1 + Math.sin(time * 0.5 + band * 2) * 0.05;
      }
    });
  }

  // Star twinkle
  if (starField && starField.material instanceof THREE.PointsMaterial) {
    starField.material.opacity = 0.7 + Math.sin(time * 0.8) * 0.2;
  }

  // Lantern animation
  lanternGroups.forEach((group, i) => {
    const phase = group.userData.phase || 0;
    group.position.y += Math.sin(time * 1.5 + phase) * 0.005;
    group.rotation.y = Math.sin(time * 0.5 + i) * 0.1;

    // Pulsing glow
    const light = group.children.find(c => c instanceof THREE.PointLight) as THREE.PointLight;
    if (light) {
      light.intensity = 2 + Math.sin(time * 3 + phase) * 0.8;
    }
  });

  // Ground water ripple effect
  if (groundMirror) {
    const geoAttrs = groundMirror.geometry.attributes.position;
    const arr = geoAttrs.array as Float32Array;
    for (let i = 0; i < arr.length; i += 3) {
      const x = arr[i];
      const z = arr[i + 2] || arr[i + 1];
      const dist = Math.sqrt(x * x + z * z);
      // Only original Y (which was 0 for a flat plane), we add subtle wave
      arr[i + 2] = Math.sin(dist * 0.1 - time * 0.8) * 0.3 + Math.sin(x * 0.05 + time * 0.5) * 0.2;
    }
    geoAttrs.needsUpdate = true;
    groundMirror.geometry.computeVertexNormals();
  }

  renderer.render(scene, camera);
};

// ========== WISH SUBMISSION ==========
const handleAddWish = () => {
  const text = newWish.value.trim();
  if (!text || branchEndPoints.length === 0) return;

  wishes.value.push(text);
  wishCount.value++;

  // Pick random branch endpoint
  const idx = Math.floor(Math.random() * branchEndPoints.length);
  const pt = branchEndPoints[idx].clone();
  pt.y -= 2 + Math.random() * 3;
  pt.x += (Math.random() - 0.5) * 5;
  pt.z += (Math.random() - 0.5) * 5;

  const lantern = createLantern(pt, text, Math.random());

  // Birth animation
  lantern.scale.setScalar(0.01);
  let scale = 0.01;
  const grow = () => {
    scale += 0.03;
    if (scale < 1) {
      // Elastic ease
      const t = scale;
      const elastic = Math.pow(2, -10 * t) * Math.sin((t * 10 - 0.75) * (2 * Math.PI / 3)) + 1;
      lantern.scale.setScalar(elastic);
      requestAnimationFrame(grow);
    } else {
      lantern.scale.setScalar(1);
    }
  };
  grow();

  // Burst particles effect
  createWishBurst(pt);

  activeWish.value = `✨ "${text}" 已被星辰铭记 ✨`;
  setTimeout(() => (activeWish.value = ''), 4000);
  newWish.value = '';
};

const createWishBurst = (pos: THREE.Vector3) => {
  const burstCount = 100;
  const geo = new THREE.BufferGeometry();
  const positions = new Float32Array(burstCount * 3);
  const colors = new Float32Array(burstCount * 3);
  const velocities: { x: number; y: number; z: number }[] = [];

  for (let i = 0; i < burstCount; i++) {
    positions[i * 3] = pos.x;
    positions[i * 3 + 1] = pos.y;
    positions[i * 3 + 2] = pos.z;

    const c = new THREE.Color().setHSL(Math.random(), 0.9, 0.7);
    colors[i * 3] = c.r;
    colors[i * 3 + 1] = c.g;
    colors[i * 3 + 2] = c.b;

    velocities.push({
      x: (Math.random() - 0.5) * 2,
      y: Math.random() * 2,
      z: (Math.random() - 0.5) * 2,
    });
  }

  geo.setAttribute('position', new THREE.BufferAttribute(positions, 3));
  geo.setAttribute('color', new THREE.BufferAttribute(colors, 3));

  const mat = new THREE.PointsMaterial({
    size: 1.5,
    vertexColors: true,
    map: createGlowTexture(32, 'rgba(255,255,255,1)', 'rgba(255,255,255,0)'),
    transparent: true,
    blending: THREE.AdditiveBlending,
    depthWrite: false,
    opacity: 1,
  });

  const burst = new THREE.Points(geo, mat);
  scene.add(burst);

  let life = 60;
  const animBurst = () => {
    life--;
    if (life <= 0) {
      scene.remove(burst);
      geo.dispose();
      mat.dispose();
      return;
    }

    const posArr = burst.geometry.attributes.position.array as Float32Array;
    for (let i = 0; i < burstCount; i++) {
      posArr[i * 3] += velocities[i].x * 0.3;
      posArr[i * 3 + 1] += velocities[i].y * 0.3;
      posArr[i * 3 + 2] += velocities[i].z * 0.3;
      velocities[i].y -= 0.02; // Gravity
    }
    burst.geometry.attributes.position.needsUpdate = true;
    mat.opacity = life / 60;

    requestAnimationFrame(animBurst);
  };
  animBurst();
};

// Cleanup
onMounted(() => {
  initScene();
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
  <div class="relative w-full">
    <!-- Soft accent glow behind card -->
    <div
      class="absolute -inset-2 rounded-[3.5rem] blur-3xl opacity-20 pointer-events-none animate-glow-pulse"
      style="background: radial-gradient(ellipse at center, #49b1f5 0%, transparent 70%)"
    ></div>

    <div
      class="relative glass rounded-[3rem] overflow-hidden shadow-2xl hover:shadow-2xl hover:shadow-[#49b1f5]/20 transition-all duration-700 border border-white/60 hover:border-[#49b1f5]/30"
    >
      <!-- Header -->
      <div class="relative z-20 pt-10 pb-5 text-center px-4">
        <div class="flex items-center justify-center gap-4 mb-3">
          <div
            class="h-[2px] w-12 md:w-28 rounded-full"
            style="background: linear-gradient(90deg, transparent, #49b1f5, #a78bfa)"
          ></div>
          <Sparkles :size="24" class="text-[#49b1f5] animate-pulse" />
          <h1
            class="text-3xl md:text-5xl font-serif font-bold tracking-wider text-[#2c3e50]"
          >
            许愿树
          </h1>
          <Sparkles :size="24" class="text-[#49b1f5] animate-pulse" />
          <div
            class="h-[2px] w-12 md:w-28 rounded-full"
            style="background: linear-gradient(90deg, #a78bfa, #49b1f5, transparent)"
          ></div>
        </div>
        <p class="text-gray-400 font-serif text-sm md:text-base tracking-widest italic">
          拖拽旋转 · 滚轮缩放 · 点亮星辰间的愿望
        </p>
      </div>

      <!-- 3D Canvas -->
      <div
        class="relative w-full mx-4 md:mx-6 rounded-[2rem] overflow-hidden border border-gray-200/50 shadow-inner"
        style="height: 65vh; min-height: 480px; width: calc(100% - 2rem); margin-left: auto; margin-right: auto;"
      >
        <div
          ref="canvasContainer"
          class="absolute inset-0 cursor-grab active:cursor-grabbing"
        ></div>

        <!-- Wish Tooltip -->
        <Transition name="wish-toast">
          <div
            v-if="activeWish"
            class="absolute top-6 left-1/2 -translate-x-1/2 z-30 pointer-events-none"
          >
            <div
              class="px-8 py-4 rounded-[1.5rem] glass border border-white/60 text-center shadow-xl"
            >
              <p class="text-[#2c3e50] font-serif text-lg md:text-xl tracking-wider">
                {{ activeWish }}
              </p>
            </div>
          </div>
        </Transition>

        <!-- Bottom Stats Bar -->
        <div class="absolute bottom-4 left-4 z-20 flex items-center gap-3">
          <div
            class="flex items-center gap-2 px-5 py-2.5 rounded-[1.25rem] backdrop-blur-xl border border-white/30 shadow-lg"
            style="background: rgba(255,255,255,0.7)"
          >
            <Star :size="14" class="text-[#49b1f5]" />
            <span class="text-[#2c3e50] text-xs font-bold tracking-wider">{{ wishCount }} 个愿望</span>
          </div>
        </div>
      </div>

      <!-- Wish Input Section -->
      <div class="relative z-20 p-6 md:p-10">
        <div class="max-w-2xl mx-auto">
          <div class="flex gap-3">
            <div class="relative flex-1">
              <Sparkles :size="18" class="absolute left-4 top-1/2 -translate-y-1/2 text-[#49b1f5]/50" />
              <input
                v-model="newWish"
                type="text"
                placeholder="写下你的心愿，挂上许愿树..."
                class="w-full bg-white/60 border border-gray-200/60 rounded-[1.5rem] pl-12 pr-5 py-4 text-[#2c3e50] placeholder-gray-400/60 outline-none focus:border-[#49b1f5]/50 focus:shadow-[0_0_20px_rgba(73,177,245,0.15)] transition-all duration-500 font-serif text-base md:text-lg backdrop-blur-sm"
                @keyup.enter="handleAddWish"
              />
            </div>
            <button
              @click="handleAddWish"
              class="group px-6 md:px-8 py-4 rounded-[1.5rem] bg-[#49b1f5] hover:bg-[#3a9fe0] text-white transition-all duration-500 hover:shadow-2xl hover:shadow-[#49b1f5]/30 active:scale-95 hover:scale-105"
            >
              <div class="flex items-center gap-2 font-bold tracking-wider text-sm md:text-base">
                <Send :size="18" />
                <span class="hidden md:inline">许愿</span>
              </div>
            </button>
          </div>

          <!-- Recent wishes -->
          <div class="mt-6 flex flex-wrap gap-2">
            <span
              v-for="(wish, i) in wishes.slice(-5).reverse()"
              :key="i"
              class="text-[10px] font-bold tracking-widest px-5 py-2 glass rounded-full text-gray-400 hover:text-[#49b1f5] hover:scale-105 transition-all duration-500 cursor-default truncate max-w-[200px]"
            >
              {{ wish }}
            </span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
@keyframes glow-pulse {
  0%,
  100% {
    opacity: 0.15;
    filter: blur(30px);
  }
  50% {
    opacity: 0.25;
    filter: blur(40px);
  }
}

.animate-glow-pulse {
  animation: glow-pulse 5s ease-in-out infinite;
}

.wish-toast-enter-active {
  transition: all 0.6s cubic-bezier(0.23, 1, 0.32, 1);
}

.wish-toast-leave-active {
  transition: all 0.5s ease;
}

.wish-toast-enter-from {
  opacity: 0;
  transform: translate(-50%, -20px) scale(0.9);
}

.wish-toast-leave-to {
  opacity: 0;
  transform: translate(-50%, -10px);
}
</style>
