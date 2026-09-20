<script setup lang="ts">
import { computed, ref } from 'vue'
import {
  Connection,
  Controller,
  OperatingSystem,
  SupportStatus,
  compatibilityFeatures,
  compatibilityMatrix,
  controllerGuides
} from '../../data/controllerCompatibility'

const props = withDefaults(defineProps<{
  controller?: Controller
  guide?: string
  searchable?: boolean
}>(), {
  controller: undefined,
  guide: undefined,
  searchable: false
})

const selectedController = ref<Controller | ''>('')
const selectedConnection = ref<Connection | ''>('')
const selectedOperatingSystem = ref<OperatingSystem | ''>('')

const statusDetails: Record<SupportStatus, { symbol: string, label: string }> = {
  [SupportStatus.Works]: { symbol: '✅', label: 'Works out of the box' },
  [SupportStatus.SetupRequired]: { symbol: '🛠️', label: 'Additional setup required' },
  [SupportStatus.Partial]: { symbol: '🟡', label: 'Partial support' },
  [SupportStatus.Unsupported]: { symbol: '❌', label: 'Unsupported' },
  [SupportStatus.NotApplicable]: { symbol: '—', label: 'Not applicable' }
}

const applicableRows = computed(() => compatibilityMatrix.filter((entry) =>
  (!props.controller || entry.controller === props.controller)
  && (!props.guide || controllerGuides[entry.controller] === props.guide)
))

const filteredRows = computed(() => applicableRows.value.filter((entry) =>
  (!selectedController.value || entry.controller === selectedController.value)
  && (!selectedConnection.value || entry.connection === selectedConnection.value)
  && (!selectedOperatingSystem.value || entry.operatingSystem === selectedOperatingSystem.value)
))

const controllers = computed(() => unique(applicableRows.value.map(({ controller }) => controller)))
const connections = computed(() => unique(applicableRows.value.map(({ connection }) => connection)))
const operatingSystems = computed(() => unique(applicableRows.value.map(({ operatingSystem }) => operatingSystem)))

const hasFilters = computed(() => Boolean(
  selectedController.value || selectedConnection.value || selectedOperatingSystem.value
))

function unique<T>(values: readonly T[]): T[] {
  return [...new Set(values)]
}

function clearFilters(): void {
  selectedController.value = ''
  selectedConnection.value = ''
  selectedOperatingSystem.value = ''
}
</script>

<template>
  <div class="compatibility">
    <form v-if="searchable" class="compatibility__filters" aria-label="Filter controller compatibility" @submit.prevent>
      <label>
        <span>Controller</span>
        <select v-model="selectedController">
          <option value="">All controllers</option>
          <option v-for="option in controllers" :key="option" :value="option">{{ option }}</option>
        </select>
      </label>

      <label>
        <span>Connection</span>
        <select v-model="selectedConnection">
          <option value="">All connections</option>
          <option v-for="option in connections" :key="option" :value="option">{{ option }}</option>
        </select>
      </label>

      <label>
        <span>Operating system</span>
        <select v-model="selectedOperatingSystem">
          <option value="">All operating systems</option>
          <option v-for="option in operatingSystems" :key="option" :value="option">{{ option }}</option>
        </select>
      </label>

      <button type="button" :disabled="!hasFilters" @click="clearFilters">Clear filters</button>
    </form>

    <p v-if="searchable" class="compatibility__count" aria-live="polite">
      {{ filteredRows.length }} {{ filteredRows.length === 1 ? 'combination' : 'combinations' }}
    </p>

    <div v-if="filteredRows.length" class="compatibility__table-wrap">
      <table>
        <caption class="sr-only">Controller compatibility combinations</caption>
        <thead>
          <tr>
            <th scope="col">Controller</th>
            <th scope="col">Connection</th>
            <th scope="col">Operating system</th>
            <th v-for="feature in compatibilityFeatures" :key="feature" scope="col">{{ feature }}</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="entry in filteredRows" :key="`${entry.controller}:${entry.connection}:${entry.operatingSystem}`">
            <th scope="row">
              <a v-if="controllerGuides[entry.controller]" :href="controllerGuides[entry.controller]">
                {{ entry.controller }}
              </a>
              <template v-else>{{ entry.controller }}</template>
            </th>
            <td>{{ entry.connection === Connection.NotApplicable ? '—' : entry.connection }}</td>
            <td>{{ entry.operatingSystem }}</td>
            <td v-for="(status, index) in entry.support" :key="compatibilityFeatures[index]" class="compatibility__status">
              <span :aria-label="statusDetails[status].label" :title="statusDetails[status].label">
                {{ statusDetails[status].symbol }}
              </span>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <p v-else class="compatibility__empty">
      No compatibility combinations have been recorded for this selection yet.
    </p>
  </div>
</template>

<style scoped>
.compatibility {
  margin: 24px 0;
}

.compatibility__filters {
  display: grid;
  grid-template-columns: repeat(3, minmax(150px, 1fr)) auto;
  align-items: end;
  gap: 12px;
  margin-bottom: 10px;
  padding: 16px;
  border: 1px solid var(--vp-c-divider);
  border-radius: 12px;
  background: var(--vp-c-bg-soft);
}

.compatibility__filters label,
.compatibility__filters label span {
  display: block;
}

.compatibility__filters label span {
  margin-bottom: 6px;
  color: var(--vp-c-text-2);
  font-size: 0.8rem;
  font-weight: 600;
}

.compatibility__filters select,
.compatibility__filters button {
  width: 100%;
  min-height: 40px;
  border: 1px solid var(--vp-c-divider);
  border-radius: 8px;
  color: var(--vp-c-text-1);
  background: var(--vp-c-bg);
  font: inherit;
}

.compatibility__filters select {
  padding: 0 32px 0 10px;
}

.compatibility__filters button {
  padding: 0 12px;
  cursor: pointer;
  font-weight: 600;
}

.compatibility__filters button:disabled {
  cursor: default;
  opacity: 0.5;
}

.compatibility__count {
  margin: 0 0 8px;
  color: var(--vp-c-text-2);
  font-size: 0.85rem;
}

.compatibility__table-wrap {
  overflow-x: auto;
}

.compatibility table {
  display: table;
  width: 100%;
  min-width: 1050px;
  margin: 0;
}

.compatibility th,
.compatibility td {
  white-space: nowrap;
}

.compatibility tbody th {
  text-align: left;
}

.compatibility__status {
  text-align: center;
}

.compatibility__empty {
  padding: 16px;
  border: 1px solid var(--vp-c-divider);
  border-radius: 10px;
  color: var(--vp-c-text-2);
  background: var(--vp-c-bg-soft);
}

.sr-only {
  position: absolute;
  width: 1px;
  height: 1px;
  padding: 0;
  margin: -1px;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
  white-space: nowrap;
  border: 0;
}

@media (max-width: 760px) {
  .compatibility__filters {
    grid-template-columns: 1fr;
  }
}
</style>
