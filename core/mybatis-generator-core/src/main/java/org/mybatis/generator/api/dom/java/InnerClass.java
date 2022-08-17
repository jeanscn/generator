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
package org.mybatis.generator.api.dom.java;

import com.vgosoft.core.util.VReflectionUtil;
import org.mybatis.generator.internal.ObjectFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * This class encapsulates the idea of an inner class - it has methods that make
 * it easy to generate inner classes.
 *
 * @author Jeff Butler
 */
public class InnerClass extends AbstractJavaType {

    private final List<TypeParameter> typeParameters = new ArrayList<>();

    private FullyQualifiedJavaType superClass;

    private boolean isAbstract;

    private final List<InitializationBlock> initializationBlocks = new ArrayList<>();

    private boolean isFinal;

    public InnerClass(FullyQualifiedJavaType type) {
        super(type);
    }

    public InnerClass(String type) {
        super(type);
    }

    public Optional<FullyQualifiedJavaType> getSuperClass() {
        return Optional.ofNullable(superClass);
    }

    public void setSuperClass(FullyQualifiedJavaType superClass) {
        this.superClass = superClass;
    }

    public void setSuperClass(String superClassType) {
        this.superClass = new FullyQualifiedJavaType(superClassType);
    }

    public List<TypeParameter> getTypeParameters() {
        return this.typeParameters;
    }

    public void addTypeParameter(TypeParameter typeParameter) {
        this.typeParameters.add(typeParameter);
    }

    public List<InitializationBlock> getInitializationBlocks() {
        return initializationBlocks;
    }

    public void addInitializationBlock(InitializationBlock initializationBlock) {
        initializationBlocks.add(initializationBlock);
    }

    public boolean isAbstract() {
        return isAbstract;
    }

    public void setAbstract(boolean isAbtract) {
        this.isAbstract = isAbtract;
    }

    public boolean isFinal() {
        return isFinal;
    }

    public void setFinal(boolean isFinal) {
        this.isFinal = isFinal;
    }

    public boolean isContainField(String fieldName){
        long count = this.getFields().stream().filter(f-> f.getName().equals(fieldName)).count();
        if (count>0) {
            return true;
        }
        if (this.getSuperClass().isPresent()) {
            try {
                Class<?> aClass = ObjectFactory.internalClassForName(this.getSuperClass().get().getFullyQualifiedName());
                java.lang.reflect.Field field = VReflectionUtil.getField(aClass, fieldName);
                return field!=null;
            } catch (ClassNotFoundException e) {
                return false;
            }
        }
        return false;
    }

    public boolean addField(org.mybatis.generator.api.dom.java.Field field, Integer index, boolean checkUnique) {
        if (index == null) index = getFields().size();
        if (getFields().size()==0) {
            getFields().add(index,field);
            return true;
        }else if (checkUnique) {
            if (!isContainField(field.getName())) {
                getFields().add(index,field);
                return true;
            }
        }
        return false;
    }
}
