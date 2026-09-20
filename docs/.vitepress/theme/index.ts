// https://vitepress.dev/guide/custom-theme
import { h } from 'vue'
import type { Theme } from 'vitepress'
import DefaultTheme from 'vitepress/theme'
import ControllerCompatibility from './components/ControllerCompatibility.vue'
import ControlifyHome from './components/ControlifyHome.vue'
import './style.css'
import './docs.css'
import './home.css'

export default {
  extends: DefaultTheme,
  Layout: () => {
    return h(DefaultTheme.Layout, null, {
      // https://vitepress.dev/guide/extending-default-theme#layout-slots
    })
  },
  enhanceApp({ app, router, siteData }) {
    app.component('ControllerCompatibility', ControllerCompatibility)
    app.component('ControlifyHome', ControlifyHome)
  }
} satisfies Theme
