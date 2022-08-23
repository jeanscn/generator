/*
 *    Copyright 2006-2020 the original author or authors.
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

import org.mybatis.generator.internal.util.StringUtility;

import java.util.List;

public class JavaClientGeneratorConfiguration  extends AbstractGeneratorConfiguration {

    public JavaClientGeneratorConfiguration() {
        super();
    }

    public JavaClientGeneratorConfiguration(Context context) {
        super();
        targetProject = context.getJavaClientGeneratorConfiguration().getTargetProject();
        baseTargetPackage = StringUtility.substringBeforeLast(context.getJavaClientGeneratorConfiguration().getTargetPackage(), ".");
        targetPackage = String.join(".", baseTargetPackage,"dao");
        targetPackageGen = String.join(".", baseTargetPackage,"codegen.dao");
    }

    @Override
    public void validate(List<String> errors, String contextId) {
        super.validate(errors, contextId, "JavaClientGeneratorConfiguration");
    }
}
