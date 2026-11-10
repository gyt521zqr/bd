<template>
  <!-- 分页组件封装 -->
  <div class="pagination-wrapper" v-if="total > 0">
    <el-pagination
      v-model:current-page="currentPage"
      v-model:page-size="currentPageSize"
      :page-sizes="[10, 20, 50, 100]"
      :total="total"
      layout="total, sizes, prev, pager, next, jumper"
      background
      @size-change="handleSizeChange"
      @current-change="handlePageChange"
    />
  </div>
</template>

<script setup lang="ts">
/**
 * Pagination - 分页组件
 */
const currentPage = defineModel<number>('page', { default: 1 })
const currentPageSize = defineModel<number>('pageSize', { default: 20 })

defineProps<{ total: number }>()
const emit = defineEmits<{ change: [] }>()

function handleSizeChange() { emit('change') }
function handlePageChange() { emit('change') }
</script>

<style scoped lang="scss">
.pagination-wrapper { display: flex; justify-content: center; padding: 20px 0; }
</style>
