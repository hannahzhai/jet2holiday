import { createRouter, createWebHistory } from 'vue-router'
import DashboardView from '../views/DashboardView.vue'
import HoldingsView from '../views/HoldingsView.vue'
import MarketView from '../views/MarketView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: '/dashboard' },
    { path: '/dashboard', name: 'dashboard', component: DashboardView },
    { path: '/holdings', name: 'holdings', component: HoldingsView },
    { path: '/market', name: 'market', component: MarketView }
  ]
})

export default router
