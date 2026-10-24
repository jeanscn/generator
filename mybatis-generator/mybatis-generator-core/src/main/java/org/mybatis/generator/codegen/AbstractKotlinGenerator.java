package org.mybatis.generator.codegen;

import java.util.Arrays;
import java.util.List;

import org.mybatis.generator.api.dom.kotlin.KotlinFile;

public abstract class AbstractKotlinGenerator extends AbstractGenerator {
    public abstract List<KotlinFile> getKotlinFiles();

    private final String project;

    protected AbstractKotlinGenerator(String project) {
        this.project = project;
    }

    public String getProject() {
        return project;
    }

    public List<KotlinFile> listOf(KotlinFile...kotlinFiles) {
        return Arrays.asList(kotlinFiles);
    }
}
