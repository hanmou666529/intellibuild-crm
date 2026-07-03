package com.ruoyi.crm.ai.context;

import com.ruoyi.crm.domain.CrmContract;
import com.ruoyi.crm.domain.CrmCustomer;
import com.ruoyi.crm.domain.CrmOrder;
import com.ruoyi.crm.mapper.*;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.util.*;

@Component
public class AiContextFetcher {
    @Resource private CrmCustomerMapper customerMapper;

    public CrmCustomer customerParam() {
        return new CrmCustomer();
    }

    public CrmOrder orderParam() {
        return new CrmOrder();
    }

    public CrmContract contractParam() {
        return new CrmContract();
    }
}
