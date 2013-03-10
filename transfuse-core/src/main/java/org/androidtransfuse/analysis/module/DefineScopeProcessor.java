/**
 * Copyright 2013 John Ericksen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.androidtransfuse.analysis.module;

import com.google.common.collect.ImmutableMap;
import org.androidtransfuse.adapter.ASTAnnotation;
import org.androidtransfuse.adapter.ASTDefinedAnnotation;
import org.androidtransfuse.adapter.ASTType;
import org.androidtransfuse.adapter.classes.ASTClassFactory;
import org.androidtransfuse.annotations.ScopeReference;
import org.androidtransfuse.gen.variableBuilder.ScopeReferenceInjectionFactory;
import org.androidtransfuse.util.matcher.Matchers;

import javax.inject.Inject;

/**
 * @author John Ericksen
 */
public class DefineScopeProcessor implements TypeProcessor {

    private final ModuleRepository moduleRepository;
    private final ASTClassFactory astClassFactory;
    private final ScopeReferenceInjectionFactory scopeReferenceInjectionFactory;

    @Inject
    public DefineScopeProcessor(ModuleRepository moduleRepository,
                                ASTClassFactory astClassFactory,
                                ScopeReferenceInjectionFactory scopeReferenceInjectionFactory) {
        this.moduleRepository = moduleRepository;
        this.astClassFactory = astClassFactory;
        this.scopeReferenceInjectionFactory = scopeReferenceInjectionFactory;
    }

    @Override
    public ModuleConfiguration process(ASTAnnotation typeAnnotation) {
        ASTType annotation = typeAnnotation.getProperty("annotation", ASTType.class);
        ASTType scope = typeAnnotation.getProperty("scope", ASTType.class);

        return new ScopeModuleConfiguration(annotation, scope);
    }

    private final class ScopeModuleConfiguration implements ModuleConfiguration {

        private final ASTType annotationType;
        private final ASTType scope;

        private ScopeModuleConfiguration(ASTType annotationType, ASTType scope) {
            this.annotationType = annotationType;
            this.scope = scope;
        }

        @Override
        public void setConfiguration() {

            moduleRepository.addScopeConfig(annotationType, scope);

            ASTType scopeReference = astClassFactory.getType(ScopeReference.class);

            ASTAnnotation annotation = new ASTDefinedAnnotation(scopeReference, ImmutableMap.<String, Object>of("value", annotationType));

            //todo: validate proper type.
            moduleRepository.putInjectionSignatureConfig(Matchers.annotated().byAnnotation(annotation).build(),
                    scopeReferenceInjectionFactory.buildInjectionNodeBuilder(annotationType));
        }
    }
}