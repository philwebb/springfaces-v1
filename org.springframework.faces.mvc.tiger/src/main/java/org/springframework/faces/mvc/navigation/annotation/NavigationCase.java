/*
 * Copyright 2004-2008 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.faces.mvc.navigation.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;

import javax.faces.application.NavigationHandler;

import org.springframework.faces.mvc.annotation.support.FacesWebArgumentResolver;
import org.springframework.faces.mvc.navigation.NavigationOutcomeExpressionElResolver;
import org.springframework.faces.mvc.navigation.NavigationRequestEvent;
import org.springframework.faces.mvc.servlet.annotation.FacesAnnotationMethodHandlerAdapter;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Annotation that can be used to map JSF navigation cases. This annotation can be used as part of the
 * {@link NavigationRules} annotation or placed on a {@link Method}, {@link Class} or {@link Package}.
 * 
 * @author Phillip Webb
 */
@Retention(RetentionPolicy.RUNTIME)
@Target( { ElementType.PACKAGE, ElementType.METHOD, ElementType.TYPE })
public @interface NavigationCase {

	/**
	 * The 'on' cases that the navigation case applies to. If this value is omitted (and onException is also not
	 * specified) the name of the method will be used. The value '*' can be used if the navigation case applies for all
	 * views that the handler controls.
	 * @return The 'on' cases
	 */
	public String[] on() default {};

	/**
	 * The action expression that the navigation case applies to. This is the expression as defined on the component
	 * that caused the action. For example "#{controller.continue}".
	 * @return The action expression
	 */
	public String fromAction() default "";

	/**
	 * An {@link Exception} class that the navigation case applies to. Navigation cases that include this attribute will
	 * only be considered if an exception is thrown during the processing of the JSF request. The navigation will also
	 * apply if a sub-classes of the specified exception is throw. The full exception stack will be considered when
	 * determining if the navigation case applies.
	 * @return A class of exception that should trigger the navigation
	 */
	public Class<?> onException() default void.class;

	/**
	 * The navigation outcome used to redirect the user when the navigation case applies. If this value is omitted the
	 * result of the method will be used or, if the result is unavailable, a <tt>null</tt> outcome will be returned.
	 * <p>
	 * The value can also include expressions that will be resolved before navigation occurs. By default, EL expressions
	 * are supported (see {@link NavigationOutcomeExpressionElResolver} for details). Custom expression resolvers can
	 * also be used (see
	 * {@link FacesAnnotationMethodHandlerAdapter#setNavigationOutcomeExpressionResolver(org.springframework.faces.mvc.navigation.NavigationOutcomeExpressionResolver)
	 * FacesAnnotationMethodHandlerAdapter.setNavigationOutcomeExpressionResolver}).
	 * <p>
	 * When applied to a method the method should return an appropriate outcome for the navigation. The method can also
	 * declare parameters of the following type:
	 * <ul>
	 * <li>String - Will contain the <tt>outcome</tt> value as passed to the JSF {@link NavigationHandler}.</li>
	 * <li>{@link NavigationRequestEvent} - Will contain the event that requested the navigation, this can be used to
	 * access the <tt>outcome</tt> and <tt>fromAction</tt> values as passed to the JSF {@link NavigationHandler}</li>
	 * <li>{@link NavigationCase} - Will contain the actual annotation instance that is handling the navigation.</li>
	 * </ul>
	 * In addition any of the parameter types supported by {@link FacesWebArgumentResolver} and any
	 * {@link ModelAttribute} annotated parameters can also be used. Parameters can be declared in any order.
	 * <p>
	 * Note: Methods will not be called if there is also a {@link RequestMapping} annotation contained on the method.
	 * @return The navigation outcome.
	 */
	public String to() default "";

	/**
	 * Determine if a popup dialog box should be used when redirecting the user. If this value is omitted popup dialogs
	 * will not be used. Note: popup dialog boxes only apply for navigation cases that redirect the user, if the current
	 * view is re-rendered this value will be ignored. The {@link #fragments()} value is often used in combination with
	 * popups in order to render a limited subset of the page components.
	 * @return <tt>true</rr> if a popup dialog box should be used.
	 */
	public boolean popup() default false;

	/**
	 * Determine a subset of the page that will be re-rendered for ajax requests. Fragments can be used when a page is
	 * refreshed (i.e. when {@link #to()} is <tt>null</tt>) or when issuing a popup redirect.
	 * @return The IDs of the view element(s) that should be re-rendered
	 */
	public String[] fragments() default {};
}
