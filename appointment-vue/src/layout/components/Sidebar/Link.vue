<template>
  <component :is="type" v-bind="linkProps" @click="handleClick">
    <slot />
  </component>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { isExternal } from '@/utils/validate'

const router = useRouter()
const props = defineProps({
  to: {
    type: String,
    required: true
  }
})

const isExternalLink = computed(() => isExternal(props.to))

const type = computed(() => {
  if (isExternalLink.value) {
    return 'a'
  }
  return 'div'
})

const linkProps = computed(() => {
  if (isExternalLink.value) {
    return {
      href: props.to,
      target: '_blank',
      rel: 'noopener'
    }
  }
  return {
    class: 'router-link'
  }
})

const handleClick = (e) => {
  if (isExternalLink.value) return
  
  e.preventDefault()
  let path = props.to
  
  if (path === '/forum/list') {
    path = '/forum'
  }
  
  router.push(path)
}
</script>

<style scoped>
.router-link {
  cursor: pointer;
}
</style>