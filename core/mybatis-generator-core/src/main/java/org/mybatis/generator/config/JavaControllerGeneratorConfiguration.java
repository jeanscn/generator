package org.mybatis.generator.config;

import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.internal.util.StringUtility;

import java.util.ArrayList;
import java.util.List;

import static com.vgosoft.tool.core.VStringUtil.*;

public class JavaControllerGeneratorConfiguration extends AbstractGeneratorConfiguration {

    private boolean noSwaggerAnnotation;

    private boolean generateUnitTest;

    private String springBootApplicationClass;

    private final TableConfiguration tableConfiguration;

    private final List<FormOptionGeneratorConfiguration> formOptionGeneratorConfigurations = new ArrayList<>();

    private List<TreeViewCateGeneratorConfiguration> treeViewCateGeneratorConfigurations = new ArrayList<>();

    public JavaControllerGeneratorConfiguration(Context context,TableConfiguration tc) {
        super(context);
        noSwaggerAnnotation = false;
        generateUnitTest = true;
        tableConfiguration = tc;
        //targetProject = context.getJavaModelGeneratorConfiguration().getTargetProject();
        //baseTargetPackage = StringUtility.substringBeforeLast(context.getJavaModelGeneratorConfiguration().getTargetPackage(), ".");
        //targetPackage = String.join(".", baseTargetPackage,"controller");
        //targetPackageGen = String.join(".", baseTargetPackage,"codegen","controller");
    }

    @Override
    public String getTargetProject() {
        if (stringHasValue(targetProject)) {
            return targetProject;
        }else{
            return targetProject = context.getJavaModelGeneratorConfiguration().getTargetProject();
        }
    }

    @Override
    public String getTargetPackage() {
        if (stringHasValue(targetPackage)) {
            return targetPackage;
        }else{
            return targetPackage = String.join(".", getBaseTargetPackage(),"controller");
        }
    }

    @Override
    public String getTargetPackageGen() {
        if (stringHasValue(targetPackageGen)) {
            return targetPackageGen;
        }else{
            return targetPackageGen = String.join(".", getBaseTargetPackage(),"codegen","controller");
        }
    }

    @Override
    public String getBaseTargetPackage() {
        if (stringHasValue(baseTargetPackage)) {
            return baseTargetPackage;
        }else{
            if (stringHasValue(targetPackage))
                return baseTargetPackage = StringUtility.substringBeforeLast(targetPackage, ".");
            else
                return baseTargetPackage = StringUtility.substringBeforeLast(context.getJavaModelGeneratorConfiguration().getTargetPackage(), ".");
        }
    }

    public boolean isNoSwaggerAnnotation() {
        return noSwaggerAnnotation;
    }

    public void setNoSwaggerAnnotation(boolean noSwaggerAnnotation) {
        this.noSwaggerAnnotation = noSwaggerAnnotation;
    }

    public boolean isGenerateUnitTest() {
        return generateUnitTest;
    }

    public void setGenerateUnitTest(boolean generateUnitTest) {
        this.generateUnitTest = generateUnitTest;
    }

    public String getSpringBootApplicationClass() {
        return springBootApplicationClass;
    }

    public void setSpringBootApplicationClass(String springBootApplicationClass) {
        this.springBootApplicationClass = springBootApplicationClass;
    }

    public List<FormOptionGeneratorConfiguration> getFormOptionGeneratorConfigurations() {
        return formOptionGeneratorConfigurations;
    }

    public void addFormOptionGeneratorConfigurations(FormOptionGeneratorConfiguration formOptionGeneratorConfigurations) {
        this.formOptionGeneratorConfigurations.add(formOptionGeneratorConfigurations);
    }

    public List<TreeViewCateGeneratorConfiguration> getTreeViewCateGeneratorConfigurations() {
        return treeViewCateGeneratorConfigurations;
    }

    public void setTreeViewCateGeneratorConfigurations(List<TreeViewCateGeneratorConfiguration> treeViewCateGeneratorConfigurations) {
        this.treeViewCateGeneratorConfigurations = treeViewCateGeneratorConfigurations;
    }

    public TableConfiguration getTableConfiguration() {
        return tableConfiguration;
    }

    @Override
    protected void validate(List<String> errors, String contextId) {
        super.validate(errors, contextId,"JavaControllerGeneratorConfiguration");
    }
}
