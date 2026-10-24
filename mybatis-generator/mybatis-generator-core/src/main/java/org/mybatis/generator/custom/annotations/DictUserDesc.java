package org.mybatis.generator.custom.annotations;

import com.vgosoft.core.annotation.DictUser;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2022-10-10 14:34
 * @version 3.0
 */
public class DictUserDesc extends AbstractDictTarget<DictUser>{


    public DictUserDesc() {
        super(DictUser.class);
    }

    public DictUserDesc(String value) {
        this();
        this.value = value;
    }
}
