package org.mybatis.generator.custom.annotations;

import com.vgosoft.core.annotation.DictDepartment;

public class DictDepartmentDesc extends AbstractDictTarget<DictDepartment>{


    public DictDepartmentDesc() {
        super(DictDepartment.class);
    }

    public DictDepartmentDesc(String value) {
        this();
        this.value = value;
    }
}
