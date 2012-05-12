package org.androidtransfuse.gen.componentBuilder;

import com.sun.codemodel.JExpression;
import org.androidtransfuse.analysis.adapter.ASTMethod;
import org.androidtransfuse.model.InjectionNode;
import org.androidtransfuse.model.r.RResource;

/**
 * @author John Ericksen
 */
public interface ComponentBuilderFactory {

    OnCreateComponentBuilder buildOnCreateComponentBuilder(InjectionNode injectionNode, LayoutBuilder layoutBuilder, ASTMethod onCreateMethod, RResource rResource);

    MethodCallbackGenerator buildMethodCallbackGenerator(String eventName, MethodGenerator methodGenerator);

    RLayoutBuilder buildRLayoutBuilder(Integer layout);

    SimpleMethodGenerator buildSimpleMethodGenerator(ASTMethod method, boolean superCall);

    ReturningMethodGenerator buildReturningMethodGenerator(ASTMethod method, boolean superCall, JExpression expression);

    LayoutHandlerBuilder buildLayoutHandlerBuilder(InjectionNode layoutHandlerInjectionNode);
}
