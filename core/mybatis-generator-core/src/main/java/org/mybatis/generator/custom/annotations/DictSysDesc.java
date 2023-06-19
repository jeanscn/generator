package org.mybatis.generator.custom.annotations;

import com.vgosoft.core.annotation.DictSys;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2022-10-10 14:34
 * @version 3.0
 */
public class DictSysDesc extends AbstractDictTarget<DictSys>{


    public DictSysDesc() {
        super(DictSys.class);
    }

    public DictSysDesc(String value) {
        this();
        this.value = value;
    }
}
