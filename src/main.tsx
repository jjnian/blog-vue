import { createApp } from 'vue';
import App from './App.vue';
import router from './router';
import './index.css';
import mediumZoom from 'medium-zoom';

const app = createApp(App);

const zoom = mediumZoom({
  margin: 24,
  background: 'rgba(255, 255, 255, 0.9)',
  scrollOffset: 0,
});

app.directive('zoom', {
  mounted(el) {
    zoom.attach(el);
  },
  unmounted(el) {
    zoom.detach(el);
  }
});

app.use(router);
app.mount('#root');
