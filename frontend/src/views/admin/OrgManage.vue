<template>
  <el-row :gutter="16">
    <el-col :span="10">
      <el-card>
        <template #header><span class="card-title">🏫 院系管理</span></template>
        <el-table :data="departments" stripe>
          <el-table-column prop="id" label="ID" width="60" />
          <el-table-column prop="name" label="院系名称" />
        </el-table>
        <el-empty v-if="!departments.length" description="暂无院系" :image-size="60" />
      </el-card>
    </el-col>
    <el-col :span="14">
      <el-card>
        <template #header><span class="card-title">👨‍🎓 班级管理</span></template>
        <el-table :data="clazzes" stripe>
          <el-table-column prop="id" label="ID" width="60" />
          <el-table-column prop="name" label="班级名称" />
          <el-table-column prop="grade" label="年级" width="80" />
        </el-table>
        <el-empty v-if="!clazzes.length" description="暂无班级" :image-size="60" />
      </el-card>
    </el-col>
  </el-row>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getDepartments, getClazzes } from '../../api/stats'

const departments = ref<any[]>([])
const clazzes = ref<any[]>([])

onMounted(async () => {
  departments.value = (await getDepartments()) as any
  clazzes.value = (await getClazzes()) as any
})
</script>

<style scoped>
.card-title { font-weight: 700; color: #9b3d2f; }
</style>
