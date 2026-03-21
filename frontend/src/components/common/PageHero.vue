<template>
  <section class="page-hero">
    <div class="hero-copy">
      <div class="eyebrow" v-if="eyebrow">{{ eyebrow }}</div>
      <h1 class="title">{{ title }}</h1>
      <p v-if="description" class="description">{{ description }}</p>
      <div v-if="$slots.meta" class="meta">
        <slot name="meta" />
      </div>
    </div>
    <div v-if="$slots.actions" class="actions">
      <slot name="actions" />
    </div>
  </section>
</template>

<script setup lang="ts">
defineProps<{
  title: string
  description?: string
  eyebrow?: string
}>()
</script>

<style scoped>
.page-hero {
  display: flex;
  justify-content: space-between;
  gap: 18px;
  align-items: flex-start;
  margin-bottom: 18px;
  padding: 24px 26px;
  border-radius: 24px;
  background:
    radial-gradient(circle at top left, rgba(227, 107, 44, 0.16), transparent 32%),
    radial-gradient(circle at right top, rgba(40, 88, 255, 0.14), transparent 28%),
    linear-gradient(135deg, rgba(255, 255, 255, 0.94), rgba(246, 248, 255, 0.98));
  border: 1px solid rgba(28, 39, 72, 0.08);
  box-shadow: 0 24px 44px rgba(28, 39, 72, 0.08);
}

.eyebrow {
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.08em;
  color: #2858ff;
  margin-bottom: 10px;
  text-transform: uppercase;
}

.title {
  margin: 0;
  color: #15203c;
  font-size: 30px;
  line-height: 1.15;
  font-weight: 800;
}

.description {
  margin: 12px 0 0;
  color: #63708d;
  line-height: 1.7;
  max-width: 760px;
}

.meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 16px;
}

.actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

@media (max-width: 960px) {
  .page-hero {
    flex-direction: column;
  }

  .actions {
    justify-content: flex-start;
  }
}
</style>
