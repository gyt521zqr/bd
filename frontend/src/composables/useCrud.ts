/**
 * useCrud - 通用 CRUD 管理逻辑
 * 统一处理列表加载、分页、搜索、增删改查
 * 列表数据仅来自后端接口；接口失败或返回空数据时显示空态，不注入任何假数据
 */
import { ref, computed, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/api'

export interface CrudOptions<T extends Record<string, any>> {
  /** API 基础路径，如 /admin/student */
  apiBase: string
  /**
   * @deprecated 仅保留参数兼容性，禁止用假数据兜底；传入的值不再参与任何渲染或赋值
   */
  mockData?: T[]
  /** 主键字段名，默认 id */
  idField?: string
  /** 搜索字段列表（用于本地过滤） */
  searchFields?: string[]
  /** 默认每页条数 */
  defaultPageSize?: number
  /** 新建时生成默认对象的函数 */
  createDefault?: () => Partial<T>
}

export function useCrud<T extends Record<string, any>>(options: CrudOptions<T>) {
  const {
    apiBase,
    idField = 'id',
    searchFields = [],
    defaultPageSize = 20,
    createDefault,
  } = options

  // ==================== 状态 ====================
  const loading = ref(false)
  const submitting = ref(false)
  const dataList = ref<T[]>([]) as any
  const pageNum = ref(1)
  const pageSize = ref(defaultPageSize)
  const total = ref(0)
  const searchForm = reactive<Record<string, any>>({})
  const selectedRows = ref<T[]>([])

  // 弹窗状态
  const dialogVisible = ref(false)
  const isEdit = ref(false)
  const editId = ref<any>(null)
  const form = ref<Partial<T>>({})

  // ==================== 计算属性 ====================
  /** 过滤后的列表（本地搜索） */
  const filteredList = computed(() => {
    let list = dataList.value
    // 按搜索字段过滤
    for (const field of searchFields) {
      const val = searchForm[field]
      if (val !== undefined && val !== null && val !== '') {
        list = list.filter((item: any) => {
          const itemVal = item[field]
          if (itemVal === undefined || itemVal === null) return false
          return String(itemVal).toLowerCase().includes(String(val).toLowerCase())
        })
      }
    }
    return list
  })

  /** 当前页数据（分页切片） */
  const pagedList = computed(() => {
    const start = (pageNum.value - 1) * pageSize.value
    return filteredList.value.slice(start, start + pageSize.value)
  })

  // ==================== 数据加载 ====================
  async function fetchList() {
    loading.value = true
    try {
      const res = await request.get(`${apiBase}/list`, {
        params: {
          ...searchForm,
          pageNum: pageNum.value,
          pageSize: pageSize.value,
        },
        silent: true,
      }) as any
      // 兼容后端分页信封的多种形态：
      // res.data.list / res.data.records（分页对象），或 res.data 本身为数组
      const payload = res?.data
      const isPayloadArray = Array.isArray(payload)
      const list: T[] = isPayloadArray ? payload : (payload?.list || payload?.records || [])
      // API 成功但返回空数组时同样赋值，正常显示空态，避免残留旧数据
      dataList.value = list
      total.value = isPayloadArray ? list.length : (payload?.total ?? list.length)
    } catch {
      // 请求失败：清空列表并明确提示，禁止回填 mockData 或静默吞掉错误
      dataList.value = []
      total.value = 0
      ElMessage.error('数据加载失败，请检查网络或稍后重试')
    } finally {
      loading.value = false
    }
  }

  // ==================== 搜索 ====================
  function handleSearch() {
    pageNum.value = 1
    total.value = filteredList.value.length
    // 尝试从 API 获取
    fetchList()
  }

  function resetSearch() {
    Object.keys(searchForm).forEach(key => {
      searchForm[key] = ''
    })
    pageNum.value = 1
    total.value = filteredList.value.length
    fetchList()
  }

  // ==================== 分页 ====================
  function handlePageChange() {
    fetchList()
  }

  // ==================== 选择 ====================
  function handleSelectionChange(rows: T[]) {
    selectedRows.value = rows
  }

  // ==================== 增删改查 ====================
  function handleCreate() {
    isEdit.value = false
    editId.value = null
    form.value = createDefault ? createDefault() : {}
    dialogVisible.value = true
  }

  function handleEdit(row: T) {
    isEdit.value = true
    editId.value = row[idField as keyof T]
    form.value = { ...row }
    dialogVisible.value = true
  }

  async function submitForm(validateFn?: () => Promise<boolean>) {
    if (validateFn) {
      const valid = await validateFn()
      if (!valid) return
    }
    submitting.value = true
    try {
      if (isEdit.value && editId.value !== null) {
        // 编辑
        try {
          await request.put(`${apiBase}/${editId.value}`, form.value, { silent: true })
        } catch {
          ElMessage.error('编辑失败，请检查网络或稍后重试')
          return
        }
        ElMessage.success('编辑成功')
      } else {
        // 新建
        try {
          await request.post(`${apiBase}/create`, form.value, { silent: true })
        } catch {
          ElMessage.error('添加失败，请检查网络或稍后重试')
          return
        }
        ElMessage.success('添加成功')
      }
      dialogVisible.value = false
      // 写操作成功后从后端重新拉取真实列表，不再本地伪造/修改数据
      await fetchList()
    } catch {
      ElMessage.error('操作失败')
    } finally {
      submitting.value = false
    }
  }

  function handleDelete(row: T, confirmMessage?: string) {
    const name = row.name || row.title || `ID:${row[idField as keyof T]}`
    ElMessageBox.confirm(confirmMessage || `确定删除 "${name}" 吗？`, '警告', { type: 'warning' })
      .then(async () => {
        try {
          await request.delete(`${apiBase}/${row[idField as keyof T]}`, { silent: true })
        } catch {
          ElMessage.error('删除失败，请检查网络或稍后重试')
          return
        }
        ElMessage.success('删除成功')
        await fetchList()
      })
      .catch(() => {})
  }

  function handleBatchDelete() {
    if (selectedRows.value.length === 0) {
      ElMessage.warning('请先选择要删除的数据')
      return
    }
    ElMessageBox.confirm(`确定删除选中的 ${selectedRows.value.length} 项吗？`, '警告', { type: 'warning' })
      .then(async () => {
        const ids = selectedRows.value.map((r: any) => r[idField])
        try {
          await request.post(`${apiBase}/batch-delete`, { ids }, { silent: true })
        } catch {
          ElMessage.error('批量删除失败，请检查网络或稍后重试')
          return
        }
        selectedRows.value = []
        ElMessage.success('批量删除成功')
        await fetchList()
      })
      .catch(() => {})
  }

  // ==================== 状态切换 ====================
  async function handleToggleStatus(row: any, statusField = 'status') {
    const current = row[statusField]
    const next = current === 'enabled' ? 'disabled' : 'enabled'
    const label = next === 'enabled' ? '启用' : '禁用'
    try {
      await request.put(`${apiBase}/${row[idField]}`, { [statusField]: next }, { silent: true })
    } catch {
      ElMessage.error(`${label}失败，请检查网络或稍后重试`)
      return
    }
    ElMessage.success(`${label}成功`)
    await fetchList()
  }

  // ==================== 初始化 ====================
  function initSearchForm(fields: string[]) {
    fields.forEach(field => {
      if (!(field in searchForm)) {
        searchForm[field] = ''
      }
    })
  }

  return {
    // 状态
    loading,
    submitting,
    dataList,
    pageNum,
    pageSize,
    total,
    searchForm,
    selectedRows,
    dialogVisible,
    isEdit,
    editId,
    form,
    // 计算属性
    filteredList,
    pagedList,
    // 方法
    fetchList,
    handleSearch,
    resetSearch,
    handlePageChange,
    handleSelectionChange,
    handleCreate,
    handleEdit,
    submitForm,
    handleDelete,
    handleBatchDelete,
    handleToggleStatus,
    initSearchForm,
  }
}
