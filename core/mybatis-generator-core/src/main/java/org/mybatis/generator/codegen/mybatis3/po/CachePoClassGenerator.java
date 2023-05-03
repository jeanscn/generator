package org.mybatis.generator.codegen.mybatis3.po;

import com.vgosoft.core.constant.GlobalConstant;
import com.vgosoft.mybatis.generate.GenerateSqlTemplate;
import com.vgosoft.mybatis.sqlbuilder.InsertSqlBuilder;
import com.vgosoft.tool.core.VMD5Util;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.codegen.AbstractJavaGenerator;
import org.mybatis.generator.codegen.mybatis3.vo.*;
import org.mybatis.generator.config.TableConfiguration;
import org.mybatis.generator.config.VOCacheGeneratorConfiguration;
import org.mybatis.generator.config.VOGeneratorConfiguration;
import org.mybatis.generator.custom.ScalableElementEnum;
import org.mybatis.generator.internal.util.JavaBeansUtil;
import org.mybatis.generator.internal.util.StringUtility;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mybatis.generator.codegen.mybatis3.vo.AbstractVOGenerator.subPackagePojo;
import static org.mybatis.generator.codegen.mybatis3.vo.CreateMappingsInterface.subPackageMaps;
import static org.mybatis.generator.internal.util.StringUtility.packageToDir;
import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;
import static org.mybatis.generator.internal.util.messages.Messages.getString;

/**
 * 生成VO抽象父类
 *
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2022-07-05 03:29
 * @version 3.0
 */
public class CachePoClassGenerator extends AbstractJavaGenerator {

    TableConfiguration tc;

    private TopLevelClass requestVoClass;
    private boolean generated = false;

    public CachePoClassGenerator(String project) {
        super(project);
    }

    @Override
    public List<CompilationUnit> getCompilationUnits() {
        List<CompilationUnit> answer = new ArrayList<>();
        progressCallback.startTask(getString("Progress.79", introspectedTable.getFullyQualifiedTable().toString()));
        tc = introspectedTable.getTableConfiguration();
        VOCacheGeneratorConfiguration voCacheGeneratorConfiguration = tc.getVoCacheGeneratorConfiguration();
        if (voCacheGeneratorConfiguration == null || !voCacheGeneratorConfiguration.isGenerate()) {
            return answer;
        }


        /*
         * 生成mappings类
         * */
        CreateMappingsInterface createMappingsInterface = new CreateMappingsInterface(introspectedTable, "project", progressCallback, warnings,"CachePo");
        Interface mappingsInterface = createMappingsInterface.generate();

        /*
         *  生成cachePo类
         *  */
        if (introspectedTable.getRules().isGenerateCachePO()) {
            String targetProject = introspectedTable.getTableConfiguration().getJavaModelGeneratorConfiguration().getTargetProject();
            TopLevelClass cachePoClass = new POCacheGenerator(introspectedTable, targetProject, progressCallback, warnings,mappingsInterface).generate();
            if (context.getPlugins().voModelCacheClassGenerated(cachePoClass, introspectedTable)) {
                generated = true;
                answer.add(cachePoClass);
            }
        }

        //生成mapstruct接口
        if (generated) {
            if (introspectedTable.getRules().isForceGenerateScalableElement(ScalableElementEnum.maps.name())
                    || VOGeneratorUtil.fileNotExist(subPackageMaps, mappingsInterface.getType().getShortName(),getProject(),context)) {
                answer.add(mappingsInterface);
            }
        }
        return answer;
    }
}
