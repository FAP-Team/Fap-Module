package logger;

import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import play.Logger;
import play.classloading.ApplicationClasses.ApplicationClass;
import play.classloading.enhancers.Enhancer;

public class FapLogsEnhancer extends Enhancer {
	@Override
	public void enhanceThisClass(final ApplicationClass applicationClass) throws Exception {
		final CtClass ctClass = makeClass(applicationClass);
		for(final CtBehavior behavior : ctClass.getDeclaredBehaviors()) {
			behavior.instrument(new ExprEditor() {
				@Override
				public void edit(MethodCall m) throws CannotCompileException {
					try {
						if("play.Logger".equals(m.getClassName())) {
							String name = m.getMethodName();
							if("fatal".equals(name)) {
								String code = String.format("{logger.FapLogs.logPlay(%s);}", "$args"); // original args
								m.replace(code);
							}
						}
						else if ("org.apache.log4j.Logger".equals(m.getClassName())){
							String name = m.getMethodName();
							if("fatal".equals(name)) {
								String code = String.format("{logger.FapLogs.logLog4j(%s);}", "$args"); // original args
								m.replace(code);
							}
						}
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
			});
		}
		applicationClass.enhancedByteCode = ctClass.toBytecode();
		ctClass.defrost();
	}
}