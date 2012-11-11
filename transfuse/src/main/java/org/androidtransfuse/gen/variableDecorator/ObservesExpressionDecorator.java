package org.androidtransfuse.gen.variableDecorator;

import com.google.inject.assistedinject.Assisted;
import com.sun.codemodel.*;
import org.androidtransfuse.analysis.TransfuseAnalysisException;
import org.androidtransfuse.analysis.adapter.ASTMethod;
import org.androidtransfuse.analysis.adapter.ASTType;
import org.androidtransfuse.analysis.astAnalyzer.ObservesAspect;
import org.androidtransfuse.event.EventObserver;
import org.androidtransfuse.event.EventObserverTuple;
import org.androidtransfuse.event.EventTending;
import org.androidtransfuse.event.WeakObserver;
import org.androidtransfuse.gen.InjectionBuilderContext;
import org.androidtransfuse.gen.InjectionExpressionBuilder;
import org.androidtransfuse.gen.UniqueVariableNamer;
import org.androidtransfuse.gen.variableBuilder.TypedExpressionFactory;
import org.androidtransfuse.model.InjectionNode;
import org.androidtransfuse.model.TypedExpression;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

/**
 * @author John Ericksen
 */
public class ObservesExpressionDecorator extends VariableExpressionBuilderDecorator {

    private static final String SUPER_REF = "super";

    private final JCodeModel codeModel;
    private final UniqueVariableNamer namer;
    private final InjectionExpressionBuilder injectionExpressionBuilder;
    private final TypedExpressionFactory typedExpressionFactory;

    @Inject
    public ObservesExpressionDecorator(@Assisted VariableExpressionBuilder decorated,
                                       JCodeModel codeModel,
                                       UniqueVariableNamer namer,
                                       InjectionExpressionBuilder injectionExpressionBuilder,
                                       TypedExpressionFactory typedExpressionFactory) {
        super(decorated);
        this.codeModel = codeModel;
        this.namer = namer;
        this.injectionExpressionBuilder = injectionExpressionBuilder;
        this.typedExpressionFactory = typedExpressionFactory;
    }

    @Override
    public TypedExpression buildVariableExpression(InjectionBuilderContext injectionBuilderContext, InjectionNode injectionNode) {
        TypedExpression typedExpression = getDecorated().buildVariableExpression(injectionBuilderContext, injectionNode);

        if(injectionNode.containsAspect(ObservesAspect.class)){
            try {
                JBlock block = injectionBuilderContext.getBlock();
                JDefinedClass definedClass = injectionBuilderContext.getDefinedClass();
                ObservesAspect aspect = injectionNode.getAspect(ObservesAspect.class);
                InjectionNode observerTendingInjectionNode = aspect.getObserverTendingInjectionNode();

                //mapping from event type -> observer
                Map<JClass, JVar> observerTuples = new HashMap<JClass, JVar>();

                for (ASTType event : aspect.getEvents()) {

                    //generate WeakObserver<E, T> (E = event, T = target injection node)
                    JClass eventRef = codeModel.ref(event.getName());
                    JClass targetRef = codeModel.ref(typedExpression.getType().getName());

                    JDefinedClass observerClass = definedClass._class(JMod.PROTECTED | JMod.STATIC | JMod.FINAL, namer.generateName(typedExpression.getType()));

                    //match default constructor public WeakObserver(T target){
                    JMethod constructor = observerClass.constructor(JMod.PUBLIC);
                    JVar constTargetParam = constructor.param(targetRef, namer.generateClassName(targetRef));
                    constructor.body().invoke(SUPER_REF).arg(constTargetParam);

                    observerClass._extends(
                            codeModel.ref(WeakObserver.class)
                                    .narrow(eventRef)
                                    .narrow(targetRef));


                    JMethod triggerMethod = observerClass.method(JMod.PUBLIC, codeModel.VOID, EventObserver.TRIGGER);
                    JVar eventParam = triggerMethod.param(eventRef, namer.generateName(event));
                    JVar targetParam = triggerMethod.param(targetRef, namer.generateName(typedExpression.getType()));
                    JBlock triggerBody = triggerMethod.body();

                    for (ASTMethod observerMethod : aspect.getObserverMethods(event)) {
                        triggerBody.invoke(targetParam, observerMethod.getName()).arg(eventParam);
                    }

                    JVar observer = block.decl(observerClass, namer.generateName(EventObserver.class), JExpr._new(observerClass).arg(typedExpression.getExpression()));

                    observerTuples.put(eventRef, observer);
                }

                //build observer tuple array and observer tending class
                JClass tendingClass = codeModel.ref(observerTendingInjectionNode.getClassName());
                JFieldVar observerTending = definedClass.field(JMod.PRIVATE, tendingClass, namer.generateName(observerTendingInjectionNode));

                JClass tupleRef = codeModel.ref(EventObserverTuple.class);
                JArray observerTupleArray = JExpr.newArray(tupleRef);

                for (Map.Entry<JClass, JVar> tupleEntry : observerTuples.entrySet()) {
                    observerTupleArray.add(
                            JExpr._new(tupleRef.narrow(tupleEntry.getKey()))
                                    .arg(tupleEntry.getKey().dotclass())
                                    .arg(tupleEntry.getValue()));
                }

                block.assign(observerTending, JExpr._new(tendingClass).arg(observerTupleArray).arg(getEventManager(injectionBuilderContext, aspect)));

                injectionBuilderContext.getVariableMap().put(observerTendingInjectionNode, typedExpressionFactory.build(EventTending.class, observerTending));

            } catch (JClassAlreadyExistsException e) {
                throw new TransfuseAnalysisException("Tried to generate a class that alread exists", e);
            }
        }
        return typedExpression;
    }

    private JExpression getEventManager(InjectionBuilderContext injectionBuilderContext, ObservesAspect aspect) {
        return injectionExpressionBuilder.buildVariable(injectionBuilderContext, aspect.getEventManagerInjectionNode()).getExpression();
    }
}
