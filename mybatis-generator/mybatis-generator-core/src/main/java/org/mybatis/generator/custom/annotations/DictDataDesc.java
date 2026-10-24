package org.mybatis.generator.custom.annotations;

import com.vgosoft.core.annotation.DictData;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2022-10-10 14:34
 * @version 3.0
 */
public class DictDataDesc extends AbstractDictTarget<DictData>{


    public DictDataDesc() {
        super(DictData.class);
    }

    public DictDataDesc(String value) {
        this();
        this.value = value;
    }
}
