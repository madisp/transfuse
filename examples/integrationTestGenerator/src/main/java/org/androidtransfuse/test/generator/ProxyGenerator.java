package org.androidtransfuse.test.generator;

import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.io.Serializable;
import java.util.Set;

/**
 * Creates a basic proxy for a class
 *
 * @author John Ericksen
 */
@SupportedAnnotationTypes({"org.androidtransfuse.test.generator.Proxy"})
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class ProxyGenerator extends AbstractProcessor {

    private Filer filer;
    private Messager messager;
    private JCodeModel codeModel = new JCodeModel();
    private boolean ran = false;

    @Override
    public synchronized void init(final ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        this.filer = processingEnv.getFiler();
        this.messager = processingEnv.getMessager();
    }

    @Override
    public boolean process(Set<? extends TypeElement> typeElements, RoundEnvironment roundEnvironment) {

        if (ran) {
            return false;
        }
        try {

            Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(Proxy.class);

            for (Element element : elements) {
                //generate a proxy for the given element
                JDefinedClass jDefinedClass = codeModel._class(((TypeElement) element).getQualifiedName() + "Proxy");

                jDefinedClass._implements(Serializable.class);

                messager.printMessage(Diagnostic.Kind.NOTE, "Wrote " + jDefinedClass.fullName());
            }

            codeModel.build(new FilerSourceCodeWriter(filer));
            ran = true;
        } catch (JClassAlreadyExistsException e) {
            throw new ProxyGenerationRuntimeException("Class already exists", e);
        } catch (IOException e) {
            throw new ProxyGenerationRuntimeException("IOException while writing proxy class", e);
        }

        return false;
    }
}
