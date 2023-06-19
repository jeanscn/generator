package org.mybatis.generator.custom.annotations;

import com.vgosoft.core.annotation.DictModule;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2022-10-10 14:34
 * @version 3.0
 */
public class DictModuleDesc extends AbstractDictTarget<DictModule>{


    public DictModuleDesc() {
        super(DictModule.class);
    }

    public DictModuleDesc(String value) {
        this();
        this.value = value;
    }
}
