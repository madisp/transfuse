package org.androidtransfuse.gen.componentBuilder;

import com.google.inject.assistedinject.Assisted;
import com.sun.codemodel.*;
import org.androidtransfuse.analysis.adapter.ASTMethod;
import org.androidtransfuse.analysis.adapter.ASTParameter;
import org.androidtransfuse.analysis.astAnalyzer.MethodCallbackAspect;
import org.androidtransfuse.gen.ComponentDescriptor;
import org.androidtransfuse.model.InjectionNode;
import org.androidtransfuse.model.r.RResource;

import javax.inject.Inject;
import java.util.*;

/**
 * @author John Ericksen
 */
public class MethodCallbackGenerator implements ExpressionVariableDependentGenerator {

    private String name;
    private MethodGenerator methodGenerator;

    @Inject
    public MethodCallbackGenerator(@Assisted String name, @Assisted MethodGenerator methodGenerator) {
        this.name = name;
        this.methodGenerator = methodGenerator;
    }

    public void generate(JDefinedClass definedClass, Map<InjectionNode, JExpression> expressionMap, ComponentDescriptor descriptor, RResource rResource) {
        MethodDescriptor methodDescriptor = null;
        for (Map.Entry<InjectionNode, JExpression> injectionNodeJExpressionEntry : expressionMap.entrySet()) {
            MethodCallbackAspect methodCallbackAspect = injectionNodeJExpressionEntry.getKey().getAspect(MethodCallbackAspect.class);

            if (methodCallbackAspect != null && methodCallbackAspect.contains(name)) {
                Set<MethodCallbackAspect.MethodCallback> methods = methodCallbackAspect.getMethodCallbacks(name);

                //define method on demand for possible lazy init
                if (methodDescriptor == null) {
                    methodDescriptor = methodGenerator.buildMethod(definedClass);
                }
                JBlock body = methodDescriptor.getMethod().body();


                for (MethodCallbackAspect.MethodCallback methodCallback : methods) {
                    List<JExpression> arguments = matchMethodArguments(methodDescriptor, methodCallback.getMethod());
                    //todo: non-public access
                    JInvocation methodInvocation = injectionNodeJExpressionEntry.getValue().invoke(methodCallback.getMethod().getName());

                    for (JExpression argument : arguments) {
                        methodInvocation.arg(argument);
                    }
                    body.add(methodInvocation);
                }
            }
        }

        methodGenerator.closeMethod(methodDescriptor);
    }

    private List<JExpression> matchMethodArguments(MethodDescriptor overrideMethodDescriptor, ASTMethod callMethod) {
        List<JExpression> arguments = new ArrayList<JExpression>();

        List<ASTParameter> overrideParameters = new ArrayList<ASTParameter>();
        overrideParameters.addAll(overrideMethodDescriptor.getASTMethod().getParameters());

        for (ASTParameter callParameter : callMethod.getParameters()) {
            Iterator<ASTParameter> overrideParameterIterator = overrideParameters.iterator();
            JExpression parameter = null;
            while (overrideParameterIterator.hasNext()) {
                ASTParameter overrideParameter = overrideParameterIterator.next();
                if (overrideParameter.getASTType().equals(callParameter.getASTType())) {
                    parameter = overrideMethodDescriptor.getParameter(overrideParameter);
                    overrideParameterIterator.remove();
                    break;
                }
            }

            if (parameter == null) {
                arguments.add(JExpr._null());
            } else {
                arguments.add(parameter);
            }
        }

        return arguments;
    }
}
