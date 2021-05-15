package com.tvd12.ezyfoxserver.support.entry;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;

import com.tvd12.ezyfox.bean.EzyBeanContext;
import com.tvd12.ezyfox.bean.EzyBeanContextBuilder;
import com.tvd12.ezyfox.binding.EzyBindingContext;
import com.tvd12.ezyfox.binding.EzyBindingContextBuilder;
import com.tvd12.ezyfox.binding.EzyMarshaller;
import com.tvd12.ezyfox.binding.EzyUnmarshaller;
import com.tvd12.ezyfox.binding.impl.EzySimpleBindingContext;
import com.tvd12.ezyfox.core.annotation.EzyEventHandler;
import com.tvd12.ezyfox.core.annotation.EzyExceptionHandler;
import com.tvd12.ezyfox.core.annotation.EzyRequestController;
import com.tvd12.ezyfox.core.annotation.EzyRequestInterceptor;
import com.tvd12.ezyfox.core.util.EzyEventHandlerAnnotations;
import com.tvd12.ezyfox.reflect.EzyReflection;
import com.tvd12.ezyfox.reflect.EzyReflectionProxy;
import com.tvd12.ezyfoxserver.app.EzyAppRequestController;
import com.tvd12.ezyfoxserver.command.EzyAppSetup;
import com.tvd12.ezyfoxserver.command.EzySetup;
import com.tvd12.ezyfoxserver.constant.EzyEventType;
import com.tvd12.ezyfoxserver.context.EzyAppContext;
import com.tvd12.ezyfoxserver.controller.EzyEventController;
import com.tvd12.ezyfoxserver.ext.EzyAbstractAppEntry;
import com.tvd12.ezyfoxserver.support.controller.EzyUserRequestAppPrototypeController;
import com.tvd12.ezyfoxserver.support.factory.EzyAppResponseFactory;
import com.tvd12.ezyfoxserver.support.factory.EzyResponseFactory;

@SuppressWarnings({"unchecked", "rawtypes"})
public abstract class EzySimpleAppEntry extends EzyAbstractAppEntry {

	@Override
	public final void config(EzyAppContext context) {
		preConfig(context);
		EzyBeanContext beanContext = createBeanContext(context);
		addEventControllers(context, beanContext);
		setAppRequestController(context, beanContext);
		postConfig(context);
		postConfig(context, beanContext);
	}
	
	protected void preConfig(EzyAppContext context) {}
	protected void postConfig(EzyAppContext context) {}
	protected void postConfig(EzyAppContext context, EzyBeanContext beanContext) {}
	
	private void addEventControllers(EzyAppContext appContext, EzyBeanContext beanContext) {
		EzySetup setup = appContext.get(EzySetup.class);
		List<Object> eventControllers = beanContext.getSingletons(EzyEventHandler.class);
		for (Object controller : eventControllers) {
			Class<?> controllerType = controller.getClass();
			EzyEventHandler annotation = controllerType.getAnnotation(EzyEventHandler.class);
			String eventName = EzyEventHandlerAnnotations.getEvent(annotation);
			setup.addEventController(EzyEventType.valueOf(eventName), (EzyEventController) controller);
			logger.info("add  event {} controller {}", eventName, controller);
		}
	}
	
	private void setAppRequestController(EzyAppContext appContext, EzyBeanContext beanContext) {
		EzyAppSetup setup = appContext.get(EzyAppSetup.class);
		EzyAppRequestController controller = newUserRequestController(beanContext);
		setup.setRequestController(controller);
	}
	
	protected EzyAppRequestController newUserRequestController(EzyBeanContext beanContext) {
		return EzyUserRequestAppPrototypeController.builder()
				.beanContext(beanContext)
				.build();
	}

	protected EzyBeanContext createBeanContext(EzyAppContext context) {
		EzyBindingContext bindingContext = createBindingContext();
		EzyMarshaller marshaller = bindingContext.newMarshaller();
		EzyUnmarshaller unmarshaller = bindingContext.newUnmarshaller();
		EzyResponseFactory appResponseFactory = createAppResponseFactory(context, marshaller);
		ScheduledExecutorService executorService = context.get(ScheduledExecutorService.class);
		EzyBeanContextBuilder beanContextBuilder = EzyBeanContext.builder()
				.addSingleton("appContext", context)
				.addSingleton("marshaller", marshaller)
				.addSingleton("unmarshaller", unmarshaller)
				.addSingleton("executorService", executorService)
				.addSingleton("zoneContext", context.getParent())
				.addSingleton("serverContext", context.getParent().getParent())
				.addSingleton("userManager", context.getApp().getUserManager())
				.addSingleton("appResponseFactory", appResponseFactory);
		Class[] singletonClasses = getSingletonClasses();
		beanContextBuilder.addSingletonClasses(singletonClasses);
		Class[] prototypeClasses = getPrototypeClasses();
		beanContextBuilder.addPrototypeClasses(prototypeClasses);
		String[] scanablePackages = getScanableBeanPackages();
		if(scanablePackages.length > 0) {
			EzyReflection reflection = new EzyReflectionProxy(Arrays.asList(scanablePackages));
			beanContextBuilder.addSingletonClasses(
					(Set)reflection.getAnnotatedClasses(EzyEventHandler.class));
			beanContextBuilder.addSingletonClasses(
					(Set)reflection.getAnnotatedClasses(EzyRequestController.class));
			beanContextBuilder.addSingletonClasses(
					(Set)reflection.getAnnotatedClasses(EzyExceptionHandler.class));
			beanContextBuilder.addSingletonClasses(
					(Set)reflection.getAnnotatedClasses(EzyRequestInterceptor.class));
			beanContextBuilder.addAllClasses(reflection);
		}
		setupBeanContext(context, beanContextBuilder);
		return beanContextBuilder.build();
	}
	
	protected EzyBindingContext createBindingContext() {
		EzyBindingContextBuilder builder = EzyBindingContext.builder();
		String[] scanablePackages = getScanableBindingPackages();
		if(scanablePackages.length > 0)
			builder.scan(scanablePackages);
		EzySimpleBindingContext answer = builder.build();
		return answer;
	}
	
	private EzyResponseFactory createAppResponseFactory(
			EzyAppContext appContext, EzyMarshaller marshaller) {
		EzyAppResponseFactory factory = new EzyAppResponseFactory();
		factory.setAppContext(appContext);
		factory.setMarshaller(marshaller);
		return factory;
	}
	
	protected Class[] getSingletonClasses() {
		return new Class[0];
	}
	
	protected Class[] getPrototypeClasses() {
		return new Class[0];
	}
	
	protected abstract String[] getScanableBeanPackages();
	protected abstract String[] getScanableBindingPackages();
	
	protected void setupBeanContext(EzyAppContext context, EzyBeanContextBuilder builder) {}

	@Override
	public void start() throws Exception {}

	@Override
	public void destroy() {}
}
