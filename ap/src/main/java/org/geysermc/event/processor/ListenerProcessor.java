/*
 * Copyright (c) 2022-2023 GeyserMC <https://geysermc.org>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 * @author GeyserMC
 * @link https://github.com/GeyserMC/Events
 */
package org.geysermc.event.processor;

import com.google.auto.common.MoreElements;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import org.geysermc.event.Listener;
import org.geysermc.event.PostOrder;
import org.geysermc.event.bus.impl.util.GeneratedSubscriberInfo;
import org.geysermc.event.subscribe.Subscribe;

@AutoService(Processor.class)
@SuppressWarnings("UnstableApiUsage")
public class ListenerProcessor extends AbstractProcessor {
    @Override
    public void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
        if (env.processingOver()) {
            return false;
        }

        for (Element element : env.getElementsAnnotatedWith(Listener.class)) {
            // can only be present on class types and interfaces
            if (!MoreElements.isType(element)) {
                continue;
            }

            try {
                processType(MoreElements.asType(element));
            } catch (IOException exception) {
                throw new RuntimeException(exception);
            }
        }

        return false;
    }

    private void processType(TypeElement typeElement) throws IOException {
        var className = ClassName.get(typeElement);

        var spec = TypeSpec.classBuilder('$' + className.simpleName())
                .addModifiers(Modifier.FINAL)
                .addJavadoc("Automatically generated event method references")
                .addField(
                        ParameterizedTypeName.get(List.class, GeneratedSubscriberInfo.class),
                        "events",
                        Modifier.PRIVATE,
                        Modifier.STATIC,
                        Modifier.FINAL);

        var staticInit = CodeBlock.builder().addStatement("events = new $T<>()", ArrayList.class);
        addSubscribeMethods(staticInit, typeElement);
        spec.addStaticBlock(staticInit.build());

        JavaFile.builder(className.packageName(), spec.build()).build().writeTo(processingEnv.getFiler());
    }

    private void addSubscribeMethods(CodeBlock.Builder staticInit, TypeElement type) {
        for (Element enclosedElement : type.getEnclosedElements()) {
            // Subscribe is always part of a method
            if (enclosedElement.getKind() != ElementKind.METHOD) {
                continue;
            }

            if (!MoreElements.isAnnotationPresent(enclosedElement, Subscribe.class)) {
                continue;
            }

            var modifiers = enclosedElement.getModifiers();
            if (modifiers.contains(Modifier.PRIVATE) || modifiers.contains(Modifier.PROTECTED)) {
                throw new IllegalStateException("@Subscribe method cannot be private or protected!");
            }

            if (!modifiers.contains(Modifier.DEFAULT) && type.getKind() == ElementKind.INTERFACE) {
                throw new IllegalStateException("@Subscribe method must have a body");
            }

            var element = MoreElements.asExecutable(enclosedElement);

            var parameters = element.getParameters();
            if (parameters.size() != 1) {
                throw new IllegalStateException(String.format(
                        "@Subscribe method %s doesn't have 1 parameter (the event type)!", element.getSimpleName()));
            }

            var eventTypeName = TypeName.get(parameters.get(0).asType());
            var subscribe = element.getAnnotation(Subscribe.class);

            staticInit.addStatement(
                    "events.add(new $T<>($T.class, $T.$L, $L, $T::$L))",
                    GeneratedSubscriberInfo.class,
                    eventTypeName,
                    PostOrder.class,
                    subscribe.postOrder(),
                    subscribe.ignoreCancelled(),
                    type.asType(),
                    element.getSimpleName());
        }
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(Listener.class.getCanonicalName());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }
}
