<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="书名" prop="bookName">
        <el-input
          v-model="queryParams.bookName"
          placeholder="模糊搜索"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
          v-hasPermi="['system:book:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-edit"
          size="mini"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['system:book:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['system:book:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['system:book:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
      <el-button type="text" size="mini" icon="el-icon-notebook-2" @click="borrowOpen = true">借阅记录</el-button>
    </el-row>

    <el-row :gutter="16">
      <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="item in bookList" :key="item.bookId">
        <el-card shadow="hover" style="margin-bottom:16px">
          <img :src="resolveCover(item.coverUrl) || defaultCover" class="book-cover"/>
          <div style="padding: 8px;">
            <div class="book-title">{{ item.bookName }}</div>
            <div class="book-meta">作者：{{ item.author }}</div>
            <div class="book-meta">库存：{{ item.stock }}</div>
            <el-button type="primary" size="mini" icon="el-icon-shopping-cart-2" @click="handleBorrow(item)" :disabled="item.stock===0">借书</el-button>
            <el-popover placement="top" width="300" trigger="click">
              <image-upload v-model="item.coverUrl" :limit="3" :fileType="['png','jpg','jpeg']" :isShowTip="false" @input="val => onCardCoverChange(item, val)"/>
              <el-button slot="reference" type="text" icon="el-icon-picture">更换封面</el-button>
            </el-popover>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 添加或修改图书对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="480px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="封面" prop="coverUrl">
          <image-upload v-model="form.coverUrl" :limit="1" :fileType="['png','jpg','jpeg']"/>
        </el-form-item>
        <el-form-item label="书名" prop="bookName">
          <el-input v-model="form.bookName" placeholder="请输入书名" />
        </el-form-item>
        <el-form-item label="作者" prop="author">
          <el-input v-model="form.author" placeholder="请输入作者" />
        </el-form-item>
        <el-form-item label="ISBN" prop="isbn">
          <el-input v-model="form.isbn" placeholder="请输入ISBN" />
        </el-form-item>
        <el-form-item label="出版日期" prop="publishDate">
          <el-date-picker clearable
            v-model="form.publishDate"
            type="date"
            value-format="yyyy-MM-dd"
            placeholder="请选择出版日期">
          </el-date-picker>
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input v-model="form.price" placeholder="请输入价格" />
        </el-form-item>
        <el-form-item label="库存" prop="stock">
          <el-input v-model="form.stock" placeholder="请输入库存" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入内容" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 借阅记录 -->
    <el-dialog title="借阅记录" :visible.sync="borrowOpen" width="600px" append-to-body>
      <el-table :data="borrowList" size="small" style="width:100%">
        <el-table-column label="书名" prop="bookName"/>
        <el-table-column label="作者" prop="author"/>
        <el-table-column label="图书编号" prop="bookId" width="100"/>
        <el-table-column label="时间" prop="time" width="180"/>
      </el-table>
      <div slot="footer" class="dialog-footer">
        <el-button type="danger" @click="clearBorrowRecords">清空记录</el-button>
        <el-button @click="borrowOpen = false">关闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listBook, getBook, delBook, addBook, updateBook } from "@/api/system/book"
import ImageUpload from "@/components/ImageUpload"

export default {
  name: "Book",
  components: { ImageUpload },
  data() {
    return {
      // 遮罩层
      loading: true,
      // 选中数组
      ids: [],
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      bookList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 借阅记录弹窗
      borrowOpen: false,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        bookName: null,
        author: null,
        isbn: null,
        publishDate: null,
        price: null,
        stock: null,
        status: null,
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        bookName: [
          { required: true, message: "书名不能为空", trigger: "blur" }
        ],
      },
      defaultCover: require('@/assets/logo/logo.png'),
      borrowList: []
    }
  },
  created() {
    this.getList()
    this.borrowList = this.loadBorrowRecords()
  },
  methods: {
    handleBorrow(item){
      const record = {
        bookId: item.bookId,
        bookName: item.bookName,
        author: item.author,
        time: new Date().toLocaleString()
      }
      this.borrowList.unshift(record)
      localStorage.setItem('borrowRecords', JSON.stringify(this.borrowList))
      this.$message.success('借书请求已发送！已记录到借阅记录')
    },
    resolveCover(url){
      if(!url) return ''
      if(/^https?:\/\//i.test(url)) return url
      return process.env.VUE_APP_BASE_API + url
    },
    onCardCoverChange(item, val){
      const first = (val || '').split(',')[0] || ''
      item.coverUrl = first
      if(!item.bookId) return
      updateBook({ bookId: item.bookId, coverUrl: first }).then(() => {
        this.$message.success('封面已更新')
      })
    },
    loadBorrowRecords(){
      try {
        const raw = localStorage.getItem('borrowRecords')
        return raw ? JSON.parse(raw) : []
      } catch(e) {
        return []
      }
    },
    clearBorrowRecords(){
      this.borrowList = []
      localStorage.removeItem('borrowRecords')
      this.$message.success('已清空借阅记录')
    },
    /** 查询图书列表 */
    getList() {
      this.loading = true
      listBook(this.queryParams).then(response => {
        this.bookList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    // 取消按钮
    cancel() {
      this.open = false
      this.reset()
    },
    // 表单重置
    reset() {
      this.form = {
        bookId: null,
        bookName: null,
        author: null,
        isbn: null,
        publishDate: null,
        price: null,
        stock: null,
        coverUrl: null,
        status: null,
        createBy: null,
        createTime: null,
        updateBy: null,
        updateTime: null,
        remark: null
      }
      this.resetForm("form")
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm("queryForm")
      this.handleQuery()
    },
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.bookId)
      this.single = selection.length!==1
      this.multiple = !selection.length
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset()
      this.open = true
      this.title = "添加图书"
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset()
      const bookId = row.bookId || this.ids
      getBook(bookId).then(response => {
        this.form = response.data
        this.open = true
        this.title = "修改图书"
      })
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.bookId != null) {
            updateBook(this.form).then(response => {
              this.$modal.msgSuccess("修改成功")
              this.open = false
              this.getList()
            })
          } else {
            addBook(this.form).then(response => {
              this.$modal.msgSuccess("新增成功")
              this.open = false
              this.getList()
            })
          }
        }
      })
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const bookIds = row.bookId || this.ids
      this.$modal.confirm('是否确认删除图书编号为"' + bookIds + '"的数据项？').then(function() {
        return delBook(bookIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('system/book/export', {
        ...this.queryParams
      }, `book_${new Date().getTime()}.xlsx`)
    }
  }
}
</script>
<style scoped>
.book-cover { width: 100%; height: 160px; object-fit: cover; border-radius: 4px; }
.book-title { font-weight: bold; margin: 8px 0; font-size: 15px; }
.book-meta { font-size: 13px; color: #666; margin-bottom: 4px; }
</style>
