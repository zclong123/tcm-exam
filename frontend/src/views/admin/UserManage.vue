<template>
  <el-card>
    <template #header>
      <div class="card-head">
        <span class="card-title">👥 用户管理</span>
        <el-button type="primary" @click="openCreate">新增用户</el-button>
      </div>
    </template>

    <div class="filters">
      <el-input v-model="keyword" placeholder="搜索用户名/姓名" clearable style="width:200px" @change="load" @clear="load" />
      <el-select v-model="roleCode" placeholder="角色" clearable style="width:130px" @change="load">
        <el-option label="学生" value="STUDENT" />
        <el-option label="教师" value="TEACHER" />
        <el-option label="管理员" value="ADMIN" />
      </el-select>
      <el-select v-model="clazzFilter" placeholder="班级" clearable style="width:160px" @change="load">
        <el-option v-for="c in clazzes" :key="c.id" :label="c.name" :value="c.id" />
      </el-select>
    </div>

    <el-table :data="rows" v-loading="loading" stripe>
      <el-table-column prop="username" label="用户名" width="120" />
      <el-table-column prop="realName" label="姓名" width="120" />
      <el-table-column label="角色" width="90">
        <template #default="{ row }">
          <el-tag :type="roleType(row.roleCode)" size="small">{{ roleName(row.roleCode) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="email" label="邮箱" width="160" />
      <el-table-column prop="phone" label="手机" width="140" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-switch :model-value="row.status === 1" @change="toggleStatus(row)" />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="140">
        <template #default="{ row }">
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="del(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="current"
      :page-size="pageSize"
      :total="total"
      layout="total, prev, pager, next"
      @current-change="load"
      style="margin-top:16px; justify-content: flex-end"
    />

    <el-dialog v-model="dialog" :title="isEdit ? '编辑用户' : '新增用户'" width="480px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="用户名">
          <el-input v-model="form.username" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="form.realName" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" :placeholder="isEdit ? '留空则不修改' : '默认123456'" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="form.roleCode" style="width:100%">
            <el-option label="学生" value="STUDENT" />
            <el-option label="教师" value="TEACHER" />
            <el-option label="管理员" value="ADMIN" />
          </el-select>
        </el-form-item>
        <el-form-item label="班级">
          <el-select v-model="form.clazzId" placeholder="选择班级" clearable style="width:100%">
            <el-option v-for="c in clazzes" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUsers, createUser, updateUser, deleteUser, updateUserStatus, getClazzes } from '../../api/stats'

const rows = ref<any[]>([])
const clazzes = ref<any[]>([])
const loading = ref(false)
const current = ref(1)
const pageSize = 10
const total = ref(0)
const keyword = ref('')
const roleCode = ref('')
const clazzFilter = ref<number | undefined>()
const dialog = ref(false)
const isEdit = ref(false)
const form = ref<any>({ username: '', realName: '', password: '', roleCode: 'STUDENT', clazzId: undefined })

function roleName(r: string) { return r === 'ADMIN' ? '管理员' : r === 'TEACHER' ? '教师' : '学生' }
function roleType(r: string) { return r === 'ADMIN' ? 'danger' : r === 'TEACHER' ? 'warning' : 'success' }

async function load() {
  loading.value = true
  try {
    const data = await getUsers({ current: current.value, size: pageSize, keyword: keyword.value, roleCode: roleCode.value, clazzId: clazzFilter.value }) as any
    rows.value = data.records
    total.value = data.total
  } finally { loading.value = false }
}

function openCreate() {
  isEdit.value = false
  form.value = { username: '', realName: '', password: '', roleCode: 'STUDENT', clazzId: undefined }
  dialog.value = true
}
function openEdit(row: any) {
  isEdit.value = true
  form.value = { ...row, password: '' }
  dialog.value = true
}
async function save() {
  if (isEdit.value) {
    await updateUser({ ...form.value, id: form.value.id })
  } else {
    await createUser(form.value)
  }
  ElMessage.success('保存成功')
  dialog.value = false
  load()
}
function del(row: any) {
  ElMessageBox.confirm(`确定删除用户「${row.username}」吗？`, '提示', { type: 'warning' }).then(async () => {
    await deleteUser(row.id)
    ElMessage.success('已删除')
    load()
  }).catch(() => {})
}
async function toggleStatus(row: any) {
  await updateUserStatus(row.id, row.status === 1 ? 0 : 1)
  row.status = row.status === 1 ? 0 : 1
  ElMessage.success('状态已更新')
}

onMounted(async () => {
  clazzes.value = (await getClazzes()) as any
  load()
})
</script>

<style scoped>
.card-title { font-weight: 700; color: #9b3d2f; }
.card-head { display: flex; justify-content: space-between; align-items: center; }
.filters { display: flex; gap: 10px; margin-bottom: 16px; }
</style>
