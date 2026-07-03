<template>
  <div class="app-container mod-customer page-accent">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="客户名称" prop="customerName">
        <el-input v-model="queryParams.customerName" placeholder="请输入客户名称" clearable style="width: 240px" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="手机号码" prop="phone">
        <el-input v-model="queryParams.phone" placeholder="请输入手机号码" clearable style="width: 240px" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="客户来源" prop="source">
        <el-select v-model="queryParams.source" placeholder="客户来源" clearable style="width: 240px">
          <el-option v-for="dict in dict.type.crm_customer_source" :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="客户等级" prop="level">
        <el-select v-model="queryParams.level" placeholder="客户等级" clearable style="width: 240px">
          <el-option v-for="dict in dict.type.crm_customer_level" :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="跟进状态" prop="followStatus">
        <el-select v-model="queryParams.followStatus" placeholder="跟进状态" clearable style="width: 240px">
          <el-option v-for="dict in dict.type.crm_follow_status" :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="负责人" prop="belongUserId">
        <el-select v-model="queryParams.belongUserId" placeholder="负责人" clearable style="width: 240px">
          <el-option v-for="item in userOptions" :key="item.userId" :label="item.label || item.nickName" :value="item.userId" />
        </el-select>
      </el-form-item>
      <el-form-item label="负责部门" prop="belongDeptId">
        <el-select v-model="queryParams.belongDeptId" placeholder="负责部门" clearable style="width: 240px">
          <el-option v-for="item in flatDeptOptions" :key="item.id" :label="item.label" :value="item.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="标签" prop="tag">
        <el-select v-model="queryParams.tag" placeholder="选择标签" clearable filterable style="width: 240px">
          <el-option v-for="t in tagOptions" :key="t.tagId" :label="t.tagName" :value="t.tagName" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['crm:customer:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="el-icon-edit" size="mini" :disabled="single" @click="handleUpdate" v-hasPermi="['crm:customer:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete" v-hasPermi="['crm:customer:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="el-icon-share" size="mini" @click="handleMerge" v-hasPermi="['crm:customer:merge']">合并</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="info" plain icon="el-icon-s-home" size="mini" @click="handleDeptShare" v-hasPermi="['crm:customer:list']">共享待分配</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport" v-hasPermi="['crm:customer:export']">导出</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="info" plain icon="el-icon-upload2" size="mini" @click="handleImport" v-hasPermi="['crm:customer:import']">导入</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-s-management" size="mini" :disabled="multiple" @click="handleBatchAssign" v-hasPermi="['crm:customer:assign']">批量分配</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="customerList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column label="客户名称" align="center" min-width="180">
        <template slot-scope="scope">
          <div>{{ scope.row.customerName }}</div>
          <div v-if="scope.row.shareActionDesc" style="margin-top: 2px; line-height: 1.6;">
            <el-tag size="mini" :type="scope.row.shareActionDesc.indexOf('已分配') !== -1 ? 'success' : 'warning'">{{ scope.row.shareActionDesc }}</el-tag>
            <span style="font-size: 11px; color: #909399; margin-left: 4px;">{{ parseTime(scope.row.shareActionTime) }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="手机号码" align="center" prop="phone" width="120" />
      <el-table-column label="客户来源" align="center" prop="source" width="100">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.crm_customer_source" :value="scope.row.source" />
        </template>
      </el-table-column>
      <el-table-column label="客户等级" align="center" prop="level" width="100">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.crm_customer_level" :value="scope.row.level" />
        </template>
      </el-table-column>
      <el-table-column label="跟进状态" align="center" prop="followStatus" width="100">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.crm_follow_status" :value="scope.row.followStatus" />
        </template>
      </el-table-column>
      <el-table-column label="标签" align="center" min-width="160">
        <template slot-scope="scope">
          <el-tag v-for="t in (scope.row.tags ? scope.row.tags.split(',') : [])" :key="t" size="mini" style="margin:1px 2px" effect="plain">{{ t }}</el-tag>
          <span v-if="!scope.row.tags">-</span>
        </template>
      </el-table-column>
      <el-table-column label="负责人" align="center" prop="belongUserName" width="100" />
      <el-table-column label="负责部门" align="center" prop="belongDeptName" width="120" />
      <el-table-column label="来源" align="center" width="120">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.shareFromDeptName" type="warning" size="small">{{ scope.row.shareFromDeptName }}共享</el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="下次联系时间" align="center" prop="nextContactTime" width="140">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.nextContactTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="140">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="220" class-name="small-padding fixed-width" fixed="right">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleFollowup(scope.row)" v-hasPermi="['crm:followup:list']">跟进</el-button>
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)" v-hasPermi="['crm:customer:edit']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)" v-hasPermi="['crm:customer:remove']">删除</el-button>
          <el-dropdown size="mini" @command="(command) => handleCommand(command, scope.row)">
            <el-button size="mini" type="text" icon="el-icon-d-arrow-right">更多</el-button>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item command="handleAssign" icon="el-icon-user" v-hasPermi="['crm:customer:edit']">分配</el-dropdown-item>
              <el-dropdown-item command="handleShare" icon="el-icon-share" v-hasPermi="['crm:customer:edit']">共享</el-dropdown-item>
              <el-dropdown-item command="handlePutPool" icon="el-icon-remove" v-hasPermi="['crm:customer:pool']">放入公海</el-dropdown-item>
              <el-dropdown-item command="handleCreateContract" icon="el-icon-document" v-hasPermi="['crm:contract:add']">创建合同</el-dropdown-item>
              <el-dropdown-item command="handleCreateOrder" icon="el-icon-s-order" v-hasPermi="['crm:order:add']">创建直接订单</el-dropdown-item>
              <el-dropdown-item command="handleDetail" icon="el-icon-view">详情</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" :visible.sync="open" width="700px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="客户名称" prop="customerName">
              <el-input v-model="form.customerName" placeholder="请输入客户名称" maxlength="50" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="手机号码" prop="phone">
              <el-input v-model="form.phone" placeholder="请输入手机号码" maxlength="20" @blur="handleCheckPhoneUnique" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="客户来源" prop="source">
              <el-select v-model="form.source" placeholder="请选择客户来源" clearable style="width: 100%">
                <el-option v-for="dict in dict.type.crm_customer_source" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="客户等级" prop="level">
              <el-select v-model="form.level" placeholder="请选择客户等级" clearable style="width: 100%">
                <el-option v-for="dict in dict.type.crm_customer_level" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="所属部门" prop="belongDeptId">
              <el-select v-model="form.belongDeptId" placeholder="请选择所属部门" clearable filterable style="width: 100%">
                <el-option v-for="item in flatDeptOptions" :key="item.id" :label="item.label" :value="item.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="负责人" prop="belongUserId">
              <el-select v-model="form.belongUserId" placeholder="负责人（默认自己）" clearable filterable style="width: 100%">
                <el-option v-for="item in userOptions" :key="item.userId" :label="item.label || item.nickName" :value="item.userId" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="电子邮件" prop="email">
              <el-input v-model="form.email" placeholder="请输入电子邮件" maxlength="50" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="公司名称" prop="company">
              <el-input v-model="form.company" placeholder="请输入公司名称" maxlength="100" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="职位" prop="position">
              <el-input v-model="form.position" placeholder="请输入职位" maxlength="50" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="地址" prop="address">
              <el-input v-model="form.address" placeholder="请输入地址" maxlength="200" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="下次联系时间" prop="nextContactTime">
              <el-date-picker v-model="form.nextContactTime" type="datetime" placeholder="请选择日期" value-format="yyyy-MM-dd HH:mm:ss" style="width: 100%"></el-date-picker>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="跟进状态" prop="followStatus">
              <el-select v-model="form.followStatus" placeholder="请选择跟进状态" clearable style="width: 100%">
                <el-option v-for="dict in dict.type.crm_follow_status" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="标签" prop="tagList">
              <el-select v-model="form.tagList" multiple placeholder="选择标签" style="width: 100%">
                <el-option v-for="t in tagOptions" :key="t.tagName" :label="t.tagName" :value="t.tagName" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="备注" prop="remark">
              <el-input v-model="form.remark" type="textarea" placeholder="请输入备注" maxlength="500" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <el-dialog :title="upload.title" :visible.sync="upload.open" width="400px" append-to-body>
      <el-upload ref="upload" :limit="1" accept=".xlsx, .xls" :headers="upload.headers" :action="upload.url + '?updateSupport=' + upload.updateSupport" :disabled="upload.isUploading" :on-progress="handleFileUploadProgress" :on-success="handleFileSuccess" :auto-upload="false" drag>
        <i class="el-icon-upload"></i>
        <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
        <div class="el-upload__tip text-center" slot="tip">
          <span>仅允许导入xls、xlsx格式文件。</span>
          <el-link type="primary" :underline="false" style="font-size: 12px; vertical-align: baseline" @click="importTemplate">下载模板</el-link>
        </div>
      </el-upload>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitFileForm">确 定</el-button>
        <el-button @click="upload.open = false">取 消</el-button>
      </div>
    </el-dialog>

    <el-dialog title="分配客户" :visible.sync="assignOpen" width="400px" append-to-body>
      <el-form ref="assignForm" :model="assignForm" label-width="80px">
        <el-form-item label="客户名称">
          <span>{{ assignForm.customerName }}</span>
        </el-form-item>
        <el-form-item label="客户等级">
          <dict-tag :options="dict.type.crm_customer_level" :value="assignForm.level" />
        </el-form-item>
        <el-form-item label="负责人" prop="userId">
          <el-select v-model="assignForm.userId" placeholder="请选择负责人" clearable style="width: 100%">
            <el-option v-for="item in userOptions" :key="item.userId" :label="item.label || item.nickName" :value="item.userId" />
          </el-select>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitAssignForm">确 定</el-button>
        <el-button @click="assignOpen = false">取 消</el-button>
      </div>
    </el-dialog>

    <el-dialog title="放入公海" :visible.sync="poolOpen" width="400px" append-to-body>
      <el-form ref="poolForm" :model="poolForm" label-width="80px">
        <el-form-item label="客户名称">
          <span>{{ poolForm.customerName }}</span>
        </el-form-item>
        <el-form-item label="原因" prop="reason">
          <el-input v-model="poolForm.reason" type="textarea" placeholder="请输入放入公海的原因" maxlength="200" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitPoolForm">确 定</el-button>
        <el-button @click="poolOpen = false">取 消</el-button>
      </div>
    </el-dialog>

    <el-dialog title="客户详情" :visible.sync="detailOpen" width="700px" append-to-body>
      <el-form ref="detailForm" :model="detailForm" label-width="100px" size="mini">
        <el-row>
          <el-col :span="12">
            <el-form-item label="客户名称：">{{ detailForm.customerName }}</el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="手机号码：">{{ detailForm.phone }}</el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="客户来源：">
              <dict-tag :options="dict.type.crm_customer_source" :value="detailForm.source" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="客户等级：">
              <dict-tag :options="dict.type.crm_customer_level" :value="detailForm.level" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="电子邮件：">{{ detailForm.email }}</el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="公司名称：">{{ detailForm.company }}</el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="职位：">{{ detailForm.position }}</el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="地址：">{{ detailForm.address }}</el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="负责人：">{{ detailForm.belongUserName }}</el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="负责部门：">{{ detailForm.belongDeptName }}</el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="跟进状态：">
              <dict-tag :options="dict.type.crm_follow_status" :value="detailForm.followStatus" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="下次联系时间：">{{ parseTime(detailForm.nextContactTime) }}</el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="创建时间：">{{ parseTime(detailForm.createTime) }}</el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="备注：">{{ detailForm.remark }}</el-form-item>
          </el-col>
        </el-row>
        <el-divider />
        <el-row v-if="detailForm.shareActionDesc">
          <el-col :span="24">
            <el-form-item label="共享状态：">
              <el-tag size="mini" :type="detailForm.shareActionDesc.indexOf('已分配') !== -1 ? 'success' : 'warning'">{{ detailForm.shareActionDesc }}</el-tag>
              <span style="font-size: 12px; color: #909399; margin-left: 6px;">{{ parseTime(detailForm.shareActionTime) }}</span>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="关联合同：">
              <span v-if="detailForm.contractNos">{{ detailForm.contractNos }}</span>
              <span v-else style="color: #999">无</span>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="关联订单：">
              <span v-if="detailForm.orderNos">{{ detailForm.orderNos }}</span>
              <span v-else style="color: #999">无</span>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24" style="text-align: center; margin-top: 8px;">
            <el-button type="primary" size="mini" icon="el-icon-document" @click="handleCreateContract" v-hasPermi="['crm:contract:add']">创建合同</el-button>
            <el-button type="success" size="mini" icon="el-icon-s-order" @click="handleCreateOrder" v-hasPermi="['crm:order:add']">创建直接订单</el-button>
          </el-col>
        </el-row>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="detailOpen = false">关 闭</el-button>
      </div>
    </el-dialog>

    <el-dialog title="批量分配客户" :visible.sync="batchAssignOpen" width="400px" append-to-body>
      <el-form ref="batchAssignForm" :model="batchAssignForm" label-width="80px">
        <el-form-item label="选中客户">
          <span>{{ batchAssignForm.count }} 个客户</span>
        </el-form-item>
        <el-form-item label="目标负责人" prop="userId">
          <el-select v-model="batchAssignForm.userId" placeholder="请选择负责人" clearable style="width: 100%">
            <el-option v-for="item in userOptions" :key="item.userId" :label="item.label || item.nickName" :value="item.userId" />
          </el-select>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitBatchAssignForm">确 定</el-button>
        <el-button @click="batchAssignOpen = false">取 消</el-button>
      </div>
    </el-dialog>

    <el-dialog title="共享客户" :visible.sync="shareOpen" width="500px" append-to-body>
      <el-form ref="shareForm" :model="shareForm" label-width="80px">
        <el-form-item label="客户名称">
          <span>{{ shareForm.customerName }}</span>
        </el-form-item>
        <el-form-item label="共享方式" prop="shareType">
          <el-radio-group v-model="shareForm.shareType">
            <el-radio label="user">共享给用户</el-radio>
            <el-radio label="dept">共享给部门</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="目标" prop="targetId">
          <el-select v-if="shareForm.shareType === 'user'" v-model="shareForm.targetId" placeholder="请选择用户" clearable filterable style="width: 100%">
            <el-option v-for="item in userOptions" :key="item.userId" :label="item.label || item.nickName" :value="item.userId" />
          </el-select>
          <el-select v-else v-model="shareForm.targetId" placeholder="请选择部门" clearable filterable style="width: 100%">
              <el-option v-for="item in flatDeptOptions" :key="item.id" :label="item.label" :value="item.id" />
            </el-select>
        </el-form-item>
        <el-divider />
        <div class="share-history" v-if="shareHistory.length > 0">
          <span style="font-weight: bold; font-size: 13px;">共享记录</span>
          <el-table :data="shareHistory" size="mini" style="margin-top: 8px">
            <el-table-column label="类型" width="80">
              <template slot-scope="s">
                <dict-tag :options="[{value:'manual',label:'手动'},{value:'auto',label:'自动'},{value:'temp',label:'临时'}]" :value="s.row.shareType" />
              </template>
            </el-table-column>
            <el-table-column label="目标" prop="toUserId" width="80" />
            <el-table-column label="时间" prop="createTime" width="80">
              <template slot-scope="s">{{ parseTime(s.row.createTime) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="60">
              <template slot-scope="s">
                <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDeleteShare(s.row.shareId)"></el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitShareForm">确 定</el-button>
        <el-button @click="shareOpen = false">取 消</el-button>
      </div>
    </el-dialog>
    <div class="page-tag">
      <el-tag size="mini" type="info" effect="plain">当前页面：客户管理 — 第 {{ queryParams.pageNum }} / {{ Math.ceil(total / queryParams.pageSize) || 1 }} 页，共 {{ total }} 条</el-tag>
    </div>

    <el-dialog :title="mergeTitle" :visible.sync="mergeOpen" width="500px" append-to-body>
      <el-form ref="mergeForm" :model="mergeForm" :rules="mergeRules" label-width="100px">
        <el-form-item label="保留客户" prop="keepCustomerId">
          <el-select v-model="mergeForm.keepCustomerId" filterable remote
            :remote-method="searchKeepCustomer" placeholder="搜索并选择保留的客户"
            @change="onKeepCustomerChange">
            <el-option v-for="item in keepCustomerOptions" :key="item.customerId"
              :label="item.customerName" :value="item.customerId" />
          </el-select>
        </el-form-item>
        <el-form-item label="被合并客户" prop="mergeCustomerId">
          <el-select v-model="mergeForm.mergeCustomerId" filterable placeholder="请选择要合并的客户">
            <el-option v-for="item in mergeCustomerOptions" :key="item.customerId"
              :label="item.customerName" :value="item.customerId" />
          </el-select>
          <div v-if="mergeCustomerOptions.length === 0 && mergeForm.keepCustomerId" style="color: #909399; font-size: 12px; margin-top: 4px;">
            未找到与保留客户同手机号的重复客户，可更换保留客户
          </div>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="mergeOpen = false">取 消</el-button>
        <el-button type="primary" @click="submitMerge">确 定</el-button>
      </div>
    </el-dialog>

    <el-dialog title="发现重复客户" :visible.sync="dedupOpen" width="600px" append-to-body>
      <el-alert title="以下客户与您输入的信息重复，请确认是否继续创建" type="warning" show-icon style="margin-bottom: 16px" />
      <el-table :data="duplicateList" max-height="300" border>
        <el-table-column prop="customerName" label="客户名称" min-width="120" />
        <el-table-column prop="phone" label="手机号" min-width="120" />
        <el-table-column prop="email" label="邮箱" min-width="120" />
        <el-table-column label="重复字段" min-width="100">
          <template slot-scope="scope">
            <el-tag v-if="scope.row.customerName === dedupForm.customerName" size="small">名称</el-tag>
            <el-tag v-if="scope.row.phone === dedupForm.phone" type="warning" size="small">电话</el-tag>
            <el-tag v-if="scope.row.email === dedupForm.email" type="success" size="small">邮箱</el-tag>
          </template>
        </el-table-column>
      </el-table>
      <div slot="footer">
        <el-button @click="dedupOpen = false; reset()">取消创建</el-button>
        <el-button type="primary" @click="submitForce">继续创建</el-button>
        <el-button type="warning" @click="openDispute">发起争议</el-button>
      </div>
    </el-dialog>

    <el-dialog title="发起争议" :visible.sync="disputeOpen" width="400px" append-to-body>
      <el-form ref="disputeForm" :model="disputeForm" :rules="disputeRules" label-width="100px">
        <el-form-item label="目标负责人" prop="targetUserId">
          <el-select v-model="disputeForm.targetUserId" filterable placeholder="请选择">
            <el-option v-for="item in userOptions" :key="item.userId"
              :label="item.userName" :value="item.userId" />
          </el-select>
        </el-form-item>
        <el-form-item label="原因" prop="reason">
          <el-input type="textarea" v-model="disputeForm.reason" placeholder="请说明争议原因" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="disputeOpen = false">取 消</el-button>
        <el-button type="primary" @click="submitDispute">提 交</el-button>
      </div>
    </el-dialog>

    <el-dialog title="部门共享待分配" :visible.sync="deptShareOpen" width="700px" append-to-body>
      <el-table v-loading="deptShareLoading" :data="deptShareList" empty-text="暂无待分配的共享客户" border>
        <el-table-column prop="customerName" label="客户名称" min-width="140" />
        <el-table-column label="共享人" min-width="120">
          <template slot-scope="scope">
            {{ getFromUserName(scope.row.fromUserId) }}
          </template>
        </el-table-column>
        <el-table-column prop="shareType" label="类型" width="80">
          <template slot-scope="scope">
            <dict-tag :options="[{value:'manual',label:'手动'},{value:'auto',label:'自动'},{value:'dept_assign',label:'分配'}]" :value="scope.row.shareType" />
          </template>
        </el-table-column>
        <el-table-column label="共享时间" width="150">
          <template slot-scope="scope">{{ parseTime(scope.row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="100" align="center">
          <template slot-scope="scope">
            <el-button size="mini" type="primary" @click="openAssignDeptUser(scope.row)">分配</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <el-dialog title="分配部门共享客户" :visible.sync="assignDeptDialog" width="400px" append-to-body>
      <el-form ref="assignDeptForm" :model="assignDeptForm" label-width="80px">
        <el-form-item label="目标用户" prop="targetUserId">
          <el-select v-model="assignDeptForm.targetUserId" filterable placeholder="请选择用户" style="width: 100%">
            <el-option v-for="item in userOptions" :key="item.userId"
              :label="item.label || item.nickName" :value="item.userId" />
          </el-select>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="assignDeptDialog = false">取 消</el-button>
        <el-button type="primary" @click="submitAssignDept">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listCustomer, getCustomer, delCustomer, addCustomer, updateCustomer, exportCustomer, importTemplate, assignCustomer, assignCheck, putToPool, claimFromPool, checkPhoneUnique, batchAssignCustomers, batchAssignCheck, shareToUser, shareToDept, getShares, deleteShare, getCrmUsers, getCrmDepts, getCrmShareDepts, checkDuplicate, mergeCustomer, getPendingDeptShares, assignFromDept } from "@/api/crm/customer"
import { listActiveTag } from "@/api/crm/tag"
import { getToken } from "@/utils/auth"
import { addDispute } from '@/api/crm/dispute'
import { listUser } from '@/api/system/user'
import '@/assets/styles/crm-table.scss'

export default {
  name: "CrmCustomer",
  dicts: ['crm_customer_source', 'crm_customer_level', 'crm_follow_status'],
  data() {
    return {
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      customerList: [],
      title: "",
      open: false,
      assignOpen: false,
      poolOpen: false,
      detailOpen: false,
      batchAssignOpen: false,
      shareOpen: false,
      userOptions: [],
      deptOptions: [],
      flatDeptOptions: [],
      tagOptions: [],
      assignForm: {},
      poolForm: {},
      detailForm: {},
      batchAssignForm: { count: 0, userId: undefined },
      shareForm: { customerId: undefined, customerName: '', shareType: 'user', targetId: undefined },
      shareHistory: [],
      upload: {
        open: false,
        title: "",
        isUploading: false,
        updateSupport: 0,
        headers: { Authorization: "Bearer " + getToken() },
        url: process.env.VUE_APP_BASE_API + "/crm/customer/importData"
      },
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        customerName: undefined,
        phone: undefined,
        source: undefined,
        level: undefined,
        followStatus: undefined,
        belongUserId: undefined,
        belongDeptId: undefined,
        tag: undefined
      },
      form: {},
      rules: {
        customerName: [
          { required: true, message: "客户名称不能为空", trigger: "blur" }
        ],
        phone: [
          { required: true, message: "手机号码不能为空", trigger: "blur" }
        ]
      },
      mergeOpen: false,
      mergeTitle: '合并客户',
      mergeForm: { keepCustomerId: null, mergeCustomerId: null },
      mergeRules: {
        keepCustomerId: [{ required: true, message: '请选择保留客户', trigger: 'change' }],
        mergeCustomerId: [{ required: true, message: '请选择被合并客户', trigger: 'change' }]
      },
      customerOptions: [],
      keepCustomerOptions: [],
      mergeCustomerOptions: [],
      selectedKeepCustomer: null,
      dedupOpen: false,
      duplicateList: [],
      dedupForm: {},
      disputeOpen: false,
      disputeForm: { targetUserId: null, reason: '' },
      disputeRules: {
        targetUserId: [{ required: true, message: '请选择目标负责人', trigger: 'change' }],
        reason: [{ required: true, message: '请输入争议原因', trigger: 'blur' }]
      },
      // Stored dedup form data for force-submit
      _dedupFormData: null,
      deptShareOpen: false,
      deptShareList: [],
      deptShareLoading: false,
      assignDeptDialog: false,
      assignDeptForm: { targetUserId: null, customerId: null, fromUserId: null }
    }
  },
  created() {
    this.getDeptList()
    this.getUserList()
    this.getTagList()
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listCustomer(this.queryParams).then(response => {
        this.customerList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    cancel() {
      this.open = false
      this.reset()
    },
    reset() {
      this.form = {
        customerId: undefined,
        customerName: undefined,
        phone: undefined,
        source: undefined,
        level: undefined,
        email: undefined,
        company: undefined,
        position: undefined,
        address: undefined,
        nextContactTime: undefined,
        followStatus: undefined,
        belongDeptId: undefined,
        belongUserId: undefined,
        tags: undefined,
        tagList: [],
        remark: undefined
      }
      this.resetForm("form")
    },
    getTagList() {
      listActiveTag().then(response => { this.tagOptions = response.data || [] })
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.resetForm("queryForm")
      this.handleQuery()
    },
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.customerId)
      this.single = selection.length != 1
      this.multiple = !selection.length
    },
    handleCommand(command, row) {
      switch (command) {
        case "handleAssign":
          this.handleAssign(row)
          break
        case "handleShare":
          this.handleShare(row)
          break
        case "handlePutPool":
          this.handlePutPool(row)
          break
        case "handleCreateContract":
          this.handleCreateContract(row)
          break
        case "handleCreateOrder":
          this.handleCreateOrder(row)
          break
        case "handleDetail":
          this.handleDetail(row)
          break
        default:
          break
      }
    },
    handleAdd() {
      this.reset()
      this.getDeptList()
      this.getUserList()
      this.open = true
      this.title = "添加客户"
    },
    handleUpdate(row) {
      this.reset()
      const customerId = row.customerId || this.ids
      getCustomer(customerId).then(response => {
        const data = response.data
        if (data.tags) data.tagList = data.tags.split(',')
        this.form = data
        this.open = true
        this.title = "修改客户"
      })
      this.getDeptList()
      this.getUserList()
    },
    handleDelete(row) {
      const customerIds = row.customerId || this.ids
      this.$modal.confirm('是否确认删除客户编号为"' + customerIds + '"的数据项？').then(function() {
        return delCustomer(customerIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    handleExport() {
      this.download('crm/customer/export', {
        ...this.queryParams
      }, `customer_${new Date().getTime()}.xlsx`)
    },
    handleImport() {
      this.upload.title = "客户导入"
      this.upload.open = true
    },
    importTemplate() {
      this.download('crm/customer/importTemplate', {}, `customer_template_${new Date().getTime()}.xlsx`)
    },
    handleFileUploadProgress(event, file, fileList) {
      this.upload.isUploading = true
    },
    handleFileSuccess(response, file, fileList) {
      this.upload.open = false
      this.upload.isUploading = false
      this.$refs.upload.clearFiles()
      this.$alert("<div style='overflow: auto;overflow-x: hidden;max-height: 70vh;padding: 10px 20px 0;'>" + response.msg + "</div>", "导入结果", { dangerouslyUseHTMLString: true })
      this.getList()
    },
    submitFileForm() {
      const file = this.$refs.upload.uploadFiles
      if (!file || file.length === 0) {
        this.$modal.msgError("请选择文件")
        return
      }
      this.$refs.upload.submit()
    },
    handleCheckPhoneUnique() {
      if (this.form.phone) {
        checkPhoneUnique(this.form.phone).then(response => {
          if (response.code === 200 && response.data === "true") {
            // 手机号可用，不做任何事
          }
        })
      }
    },
    handleFollowup(row) {
      this.$router.push({ path: '/crm/followup', query: { customerId: row.customerId, customerName: row.customerName } })
    },
    getAssignRoleKey() {
      const roles = this.$store.getters.roles || []
      if (roles.includes('admin')) return 'manager'
      if (roles.includes('manager')) return 'employee'
      return undefined
    },
    handleAssign(row) {
      this.assignForm = {
        customerId: row.customerId,
        customerName: row.customerName,
        level: row.level,
        userId: undefined
      }
      this.assignOpen = true
      this.getUserList(this.getAssignRoleKey())
    },
    getUserList(roleKey) {
      getCrmUsers(roleKey).then(response => {
        this.userOptions = response.data || []
      }).catch(() => {
        this.userOptions = []
      })
    },
    submitAssignForm() {
      if (!this.assignForm.userId) {
        this.$modal.msgError("请选择负责人")
        return
      }
      assignCheck(this.assignForm.customerId).then(response => {
        const info = response.data || {}
        if (info.belongUserId) {
          this.$modal.confirm('该客户已被分配至「' + (info.belongUserName || info.belongUserId) + '」，是否重新分配？').then(() => {
            this.doAssign()
          }).catch(() => {})
        } else {
          this.doAssign()
        }
      }).catch(() => {})
    },
    doAssign() {
      assignCustomer(this.assignForm.customerId, this.assignForm.userId).then(() => {
        this.$modal.msgSuccess("分配成功")
        this.assignOpen = false
        this.getList()
      }).catch(() => {})
    },
    handlePutPool(row) {
      this.poolForm = {
        customerId: row.customerId,
        customerName: row.customerName,
        reason: ''
      }
      this.poolOpen = true
    },
    submitPoolForm() {
      putToPool(this.poolForm.customerId, this.poolForm.reason).then(response => {
        this.$modal.msgSuccess("已放入公海")
        this.poolOpen = false
        this.getList()
      })
    },
    handleCreateContract(row) {
      this.$router.push({ path: '/crm/contract', query: { customerId: row.customerId, customerName: row.customerName } })
    },
    handleCreateOrder(row) {
      this.$router.push({ path: '/crm/order', query: { customerId: row.customerId, customerName: row.customerName } })
    },
    handleDetail(row) {
      getCustomer(row.customerId).then(response => {
        this.detailForm = response.data
        this.detailOpen = true
      })
    },
    handleShare(row) {
      this.shareForm = {
        customerId: row.customerId,
        customerName: row.customerName,
        shareType: 'user',
        targetId: undefined
      }
      this.shareOpen = true
      this.getUserList()
      this.getShareDeptList()
      this.loadShareHistory(row.customerId)
    },
    flattenDepts(tree, prefix) {
      let result = []
      if (!tree) return result
      for (const node of tree) {
        const label = prefix ? prefix + ' / ' + node.label : node.label
        result.push({ id: node.id, label: label })
        if (node.children && node.children.length > 0) {
          result = result.concat(this.flattenDepts(node.children, label))
        }
      }
      return result
    },
    getDeptList() {
      getCrmDepts().then(response => {
        this.deptOptions = response.data || []
        this.flatDeptOptions = this.flattenDepts(response.data, '')
      }).catch(() => {
        this.deptOptions = []
        this.flatDeptOptions = []
      })
    },
    getShareDeptList() {
      getCrmShareDepts().then(response => {
        this.flatDeptOptions = this.flattenDepts(response.data, '')
      }).catch(() => {
        this.flatDeptOptions = []
      })
    },
    loadShareHistory(customerId) {
      getShares(customerId).then(response => {
        this.shareHistory = response.data || []
      }).catch(() => {
        this.shareHistory = []
      })
    },
    submitShareForm() {
      if (!this.shareForm.targetId) {
        this.$modal.msgError(this.shareForm.shareType === 'user' ? '请选择用户' : '请选择部门')
        return
      }
      const api = this.shareForm.shareType === 'user'
        ? shareToUser({ customerId: this.shareForm.customerId, toUserId: this.shareForm.targetId })
        : shareToDept({ customerId: this.shareForm.customerId, toDeptId: this.shareForm.targetId })
      api.then(response => {
        this.$modal.msgSuccess("共享成功")
        this.loadShareHistory(this.shareForm.customerId)
      }).catch(() => {})
    },
    handleDeleteShare(shareId) {
      deleteShare(shareId).then(response => {
        this.$modal.msgSuccess("已取消共享")
        this.loadShareHistory(this.shareForm.customerId)
      }).catch(() => {})
    },
    handleBatchAssign() {
      if (this.ids.length === 0) {
        this.$modal.msgError("请先选择要分配的客户")
        return
      }
      this.batchAssignForm = {
        count: this.ids.length,
        userId: undefined
      }
      this.batchAssignOpen = true
      this.getUserList(this.getAssignRoleKey())
    },
    submitBatchAssignForm() {
      if (!this.batchAssignForm.userId) {
        this.$modal.msgError("请选择目标负责人")
        return
      }
      batchAssignCheck({ customerIds: this.ids }).then(response => {
        const assigned = response.data || []
        const remaining = this.ids.length - assigned.length
        if (assigned.length > 0 && remaining > 0) {
          const names = assigned.map(item => item.customerName).join('、')
          this.$modal.confirm('以下 ' + assigned.length + ' 个客户已有负责人：' + names + '，剩余 ' + remaining + ' 个客户可分配。<br>是否跳过这些已分配的客户，将剩余客户分配给目标用户？').then(() => {
            this.doBatchAssign()
          }).catch(() => {})
        } else if (assigned.length > 0 && remaining === 0) {
          this.$modal.confirm('所有选中的客户（' + assigned.length + ' 个）已有负责人，是否强制重新分配所有客户？').then(() => {
            this.doBatchAssignForce()
          }).catch(() => {})
        } else {
          this.doBatchAssign()
        }
      }).catch(() => {})
    },
    doBatchAssign() {
      batchAssignCustomers({ customerIds: this.ids, targetUserId: this.batchAssignForm.userId }).then(response => {
        const data = response.data || {}
        let msg = "成功：" + (data.success || 0) + " 条"
        if (data.skipped > 0) {
          msg += "，跳过（已有负责人）：" + data.skipped + " 条"
        }
        if (data.failed > 0) {
          msg += "，失败：" + data.failed + " 条"
        }
        this.$modal.msgSuccess(msg)
        this.batchAssignOpen = false
        this.getList()
      }).catch(() => {})
    },
    doBatchAssignForce() {
      batchAssignCustomers({ customerIds: this.ids, targetUserId: this.batchAssignForm.userId, force: true }).then(response => {
        const data = response.data || {}
        let msg = "成功：" + (data.success || 0) + " 条"
        if (data.failed > 0) {
          msg += "，失败：" + data.failed + " 条"
        }
        this.$modal.msgSuccess(msg)
        this.batchAssignOpen = false
        this.getList()
      }).catch(() => {})
    },
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          const data = { ...this.form }
          if (Array.isArray(data.tagList)) {
            data.tags = data.tagList.join(',')
          }
          delete data.tagList
          if (data.customerId != undefined) {
            updateCustomer(data).then(response => {
              this.$modal.msgSuccess("修改成功")
              this.open = false
              this.getList()
            })
          } else {
            checkDuplicate(data).then(res => {
              if (res.data && res.data.length > 0) {
                this.duplicateList = res.data
                this.dedupForm = { ...this.form }
                this._dedupFormData = data
                this.dedupOpen = true
              } else {
                addCustomer(data).then(response => {
                  this.$modal.msgSuccess("新增成功")
                  this.open = false
                  this.getList()
                })
              }
            })
          }
        }
      })
    },
    handleMerge() {
      this.mergeOpen = true
      this.mergeTitle = '合并客户'
      this.mergeForm = { keepCustomerId: null, mergeCustomerId: null }
      this.keepCustomerOptions = []
      this.mergeCustomerOptions = []
      this.selectedKeepCustomer = null
    },
    searchKeepCustomer(query) {
      listCustomer({ customerName: query }).then(res => {
        this.keepCustomerOptions = res.rows || []
      })
    },
    onKeepCustomerChange(val) {
      this.mergeForm.mergeCustomerId = null
      this.mergeCustomerOptions = []
      this.selectedKeepCustomer = this.keepCustomerOptions.find(c => c.customerId === val)
      if (!this.selectedKeepCustomer || !this.selectedKeepCustomer.phone) return
      checkDuplicate({ phone: this.selectedKeepCustomer.phone }).then(res => {
        if (res.data && res.data.length > 0) {
          this.mergeCustomerOptions = res.data.filter(c => c.customerId !== val)
        }
      })
    },
    submitMerge() {
      this.$refs.mergeForm.validate(valid => {
        if (!valid) return
        mergeCustomer(this.mergeForm).then(() => {
          this.$modal.msgSuccess('合并成功')
          this.mergeOpen = false
          this.getList()
        })
      })
    },
    getFromUserName(userId) {
      const user = this.userOptions.find(u => u.userId === userId)
      return user ? (user.label || user.nickName) : ('用户#' + userId)
    },
    handleDeptShare() {
      this.deptShareLoading = true
      this.deptShareOpen = true
      this.getUserList()
      getPendingDeptShares().then(res => {
        this.deptShareList = res.data || []
        this.deptShareLoading = false
      }).catch(() => {
        this.deptShareList = []
        this.deptShareLoading = false
      })
    },
    openAssignDeptUser(row) {
      this.getUserList()
      this.assignDeptForm = {
        targetUserId: null,
        customerId: row.customerId,
        fromUserId: row.fromUserId
      }
      this.assignDeptDialog = true
    },
    submitAssignDept() {
      if (!this.assignDeptForm.targetUserId) {
        this.$modal.msgError('请选择目标用户')
        return
      }
      assignFromDept({
        customerId: this.assignDeptForm.customerId,
        fromUserId: this.assignDeptForm.fromUserId,
        toUserId: this.assignDeptForm.targetUserId
      }).then(() => {
        this.$modal.msgSuccess('分配成功')
        this.assignDeptDialog = false
        this.handleDeptShare()
      }).catch(() => {})
    },
    submitForce() {
      this.dedupOpen = false
      if (this._dedupFormData) {
        addCustomer(this._dedupFormData).then(response => {
          this.$modal.msgSuccess("新增成功")
          this.open = false
          this.getList()
        })
      }
    },
    openDispute() {
      this.dedupOpen = false
      this.disputeOpen = true
      listUser().then(res => {
        this.userOptions = res.rows || []
      })
    },
    submitDispute() {
      this.$refs.disputeForm.validate(valid => {
        if (!valid) return
        addDispute({
          customerId: this.duplicateList[0]?.customerId,
          targetUserId: this.disputeForm.targetUserId,
          reason: this.disputeForm.reason
        }).then(() => {
          this.$modal.msgSuccess('争议已提交')
          this.disputeOpen = false
          this.reset()
        })
      })
    }
  }
}
</script>

<style>
.page-tag {
  margin-top: 16px;
  text-align: right;
  padding-right: 8px;
}
</style>
