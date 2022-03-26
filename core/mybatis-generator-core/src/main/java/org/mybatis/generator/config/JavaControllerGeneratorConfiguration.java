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

import org.mybatis.generator.internal.util.StringUtility;

import java.util.List;

public class JavaControllerGeneratorConfiguration extends AbstractGeneratorConfiguration {

    private boolean noSwaggerAnnotation;

    public JavaControllerGeneratorConfiguration(Context context) {
        super();
        noSwaggerAnnotation = false;
        targetProject = context.getJavaModelGeneratorConfiguration().getTargetProject();
        baseTargetPackage = StringUtility.substringBeforeLast(context.getJavaModelGeneratorConfiguration().getTargetPackage(), ".");
        targetPackage = String.join(".", baseTargetPackage,"controller");
    }

    public boolean isNoSwaggerAnnotation() {
        return noSwaggerAnnotation;
    }

    public void setNoSwaggerAnnotation(boolean noSwaggerAnnotation) {
        this.noSwaggerAnnotation = noSwaggerAnnotation;
    }

    @Override
    protected void validate(List<String> errors, String contextId) {
        super.validate(errors, contextId,"JavaControllerGeneratorConfiguration");
    }
}
