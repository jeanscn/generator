/**
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
package org.mybatis.generator.codegen.mybatis3.htmlmapper;

import org.mybatis.generator.api.GeneratedHtmlFile;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.html.Document;
import org.mybatis.generator.codegen.AbstractHtmlGenerator;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.internal.rules.BaseRules;

import java.util.ArrayList;
import java.util.List;

public class GenerateHtmlFiles {

    private AbstractHtmlGenerator htmlMapperGenerator;
    private Context context;
    private IntrospectedTable introspectedTable;

    public GenerateHtmlFiles(Context context,IntrospectedTable introspectedTable,AbstractHtmlGenerator htmlMapperGenerator) {
       this.context = context;
       this.htmlMapperGenerator = htmlMapperGenerator;
       this.introspectedTable = introspectedTable;
    }
    public List<GeneratedHtmlFile> getGeneratedHtmlFiles(){
        List<GeneratedHtmlFile> answer = new ArrayList<>();
        String tagertProject;
        if (context.getHtmlMapGeneratorConfiguration()!=null) {
            tagertProject = context.getHtmlMapGeneratorConfiguration().getTargetProject();
        }else{
            tagertProject = "src/main/resources/templates";
        }
        BaseRules rules = introspectedTable.getRules();
        if (htmlMapperGenerator != null && rules.generateHtml()) {
            Document document = htmlMapperGenerator.getDocument();
            GeneratedHtmlFile ghf = new GeneratedHtmlFile(document,
                    introspectedTable.getMyBatis3HtmlMapperFileName(),
                    introspectedTable.getMyBatis3HtmlMapperPackage(),
                    tagertProject,
                    false,
                    context.getHtmlFormatter());
            if (context.getPlugins().htmlMapGenerated(ghf, introspectedTable)) {
                answer.add(ghf);
            }
        }
        return answer;
    }

}
