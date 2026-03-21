import { defineComponent, h } from 'vue'

export function createPagePlaceholder(options: { title: string; description: string; accent?: string }) {
  return defineComponent({
    name: `${options.title.replace(/\s+/g, '')}Placeholder`,
    setup() {
      return () =>
        h('div', { class: 'placeholder-page' }, [
          h('div', { class: 'placeholder-card' }, [
            h('div', { class: 'placeholder-badge' }, options.accent ?? 'Base'),
            h('h2', options.title),
            h('p', options.description),
          ]),
        ])
    },
  })
}
