package org.mybatis.generator.codegen.mybatis3.htmlmapper;

import org.mybatis.generator.api.GeneratedHtmlFile;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.html.Document;
import org.mybatis.generator.codegen.AbstractHtmlGenerator;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.config.HtmlGeneratorConfiguration;

import java.util.ArrayList;
import java.util.List;

public class GenerateHtmlFiles {

    private final AbstractHtmlGenerator htmlMapperGenerator;
    private final Context context;
    private final IntrospectedTable introspectedTable;

    public GenerateHtmlFiles(Context context,IntrospectedTable introspectedTable,AbstractHtmlGenerator htmlMapperGenerator) {
       this.context = context;
       this.htmlMapperGenerator = htmlMapperGenerator;
       this.introspectedTable = introspectedTable;
    }
    public List<GeneratedHtmlFile> getGeneratedHtmlFiles(){
        List<GeneratedHtmlFile> answer = new ArrayList<>();
        for (HtmlGeneratorConfiguration htmlGeneratorConfiguration : introspectedTable.getTableConfiguration().getHtmlMapGeneratorConfigurations()) {
            if (htmlGeneratorConfiguration.isGenerate()) {
                String targetProject = htmlGeneratorConfiguration.getTargetProject();
                Document document = htmlMapperGenerator.getDocument(htmlGeneratorConfiguration);
                GeneratedHtmlFile ghf = new GeneratedHtmlFile(document,
                        htmlGeneratorConfiguration.getHtmlFileName(),
                        htmlGeneratorConfiguration.getTargetPackage(),
                        targetProject,
                        false,
                        context.getHtmlFormatter(),introspectedTable);
                ghf.setOverwriteFile(htmlGeneratorConfiguration.isOverWriteFile());
                if (context.getPlugins().htmlMapGenerated(ghf, introspectedTable, htmlGeneratorConfiguration)) {
                    answer.add(ghf);
                }
            }
        }
        return answer;
    }

}
