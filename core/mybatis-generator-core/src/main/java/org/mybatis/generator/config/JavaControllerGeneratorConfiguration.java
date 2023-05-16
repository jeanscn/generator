package org.mybatis.generator.config;

import org.mybatis.generator.internal.util.StringUtility;

import java.util.ArrayList;
import java.util.List;

public class JavaControllerGeneratorConfiguration extends AbstractGeneratorConfiguration {

    private boolean noSwaggerAnnotation;

    private boolean generateUnitTest;

    private String springBootApplicationClass;

    private List<FormOptionGeneratorConfiguration> formOptionGeneratorConfigurations = new ArrayList<>();

    private List<TreeViewCateGeneratorConfiguration> treeViewCateGeneratorConfigurations = new ArrayList<>();

    public JavaControllerGeneratorConfiguration(Context context) {
        super();
        noSwaggerAnnotation = false;
        generateUnitTest = true;
        targetProject = context.getJavaModelGeneratorConfiguration().getTargetProject();
        baseTargetPackage = StringUtility.substringBeforeLast(context.getJavaModelGeneratorConfiguration().getTargetPackage(), ".");
        targetPackage = String.join(".", baseTargetPackage,"controller");
        targetPackageGen = String.join(".", baseTargetPackage,"codegen","controller");
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

    @Override
    protected void validate(List<String> errors, String contextId) {
        super.validate(errors, contextId,"JavaControllerGeneratorConfiguration");
    }
}
