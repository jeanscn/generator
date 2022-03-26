/**
 *    Copyright 2006-2018 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.mybatis.generator.config;

import java.util.List;

public class SqlMapGeneratorConfiguration extends AbstractGeneratorConfiguration {

    public SqlMapGeneratorConfiguration() {
        super();
    }

    public SqlMapGeneratorConfiguration(Context context) {
        super();
        targetProject = context.getSqlMapGeneratorConfiguration().getTargetProject();
        targetPackage = context.getSqlMapGeneratorConfiguration().getTargetPackage();
        baseTargetPackage = "";
    }


    @Override
    public void validate(List<String> errors, String contextId) {
        super.validate(errors, contextId, "SqlMapGenerator");
    }
}
