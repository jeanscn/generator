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
package org.mybatis.generator.api.dom.html;

import java.util.ArrayList;
import java.util.List;

public class HtmlElement implements VisitableElement {

    private final List<Attribute> attributes = new ArrayList<>();

    private final List<VisitableElement> elements = new ArrayList<>();

    private String name;

    public HtmlElement(String name) {
        this.name = name;
    }

    /**
     * Copy constructor. Not a truly deep copy, but close enough for most purposes.
     *
     * @param original
     *            the original
     */
    public HtmlElement(HtmlElement original) {
        super();
        attributes.addAll(original.attributes);
        elements.addAll(original.elements);
        this.name = original.name;
    }

    public List<Attribute> getAttributes() {
        return attributes;
    }

    public void addAttribute(Attribute attribute) {
        attributes.add(attribute);
    }

    public List<VisitableElement> getElements() {
        return elements;
    }

    public HtmlElement addDivWithClass(String className) {
        HtmlElement div = new HtmlElement("div");
        if (!className.isEmpty()) {
            div.addClassName(className);
        }
        this.addElement(div);
        return div;
    }

    protected void addClassName(String className) {
        Attribute htmlClass = this.getAttributes().stream().filter(attribute -> "class".equalsIgnoreCase(attribute.getName())).findFirst().orElse(null);
        if (htmlClass == null) {
            this.addAttribute(new Attribute("class", className));
        } else {
            if (!htmlClass.getValue().contains(className)) {
                htmlClass.setValue(htmlClass.getValue() + " " + className);
            }
        }
    }

    public List<VisitableElement> getAllElements(){
        List<VisitableElement> result = new ArrayList<>();
        getAllElements(this,result);
        return result;
    }
    /*递归获得所有子节点*/
    private void getAllElements(HtmlElement rElement,List<VisitableElement> result) {
        if (rElement.hasChildren()) {
            result.addAll(rElement.getElements());
            for (VisitableElement element : rElement.getElements()) {
                getAllElements((HtmlElement)element,result);
            }
        }
    }



    public void addElement(VisitableElement element) {
        elements.add(element);
    }

    public void addElement(int index, VisitableElement element) {
        elements.add(index, element);
    }

    public String getName() {
        return name;
    }

    public boolean hasChildren() {
        return !elements.isEmpty();
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
