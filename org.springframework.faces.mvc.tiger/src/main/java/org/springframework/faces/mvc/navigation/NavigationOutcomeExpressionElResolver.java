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
package org.springframework.faces.mvc.navigation;

import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.el.ValueExpression;
import javax.faces.context.FacesContext;

import org.springframework.beans.PropertyValue;
import org.springframework.beans.PropertyValues;
import org.springframework.faces.mvc.bind.ReverseDataBinder;
import org.springframework.web.bind.WebDataBinder;

/**
 * Implementation of {@link NavigationOutcomeExpressionResolver} that can be used to resolve EL expressions. Expressions
 * should be escaped using the standard <tt>#{}</tt> form, for example, <tt>#{hotels.selectedRow.id}</tt>. Expressions
 * can be specified in any part of the URL (e.g. <tt>'/view/#{hotel.id}?bid=#{booking.id}'</tt>).
 * <p>
 * In addition any expression specified in the query part of the URL (without an attribute name) will be fully expanded
 * (e.g. <tt>'/search?#{queryParams}'</tt> would be expanded to <tt>'/search?name=search&pagesize=20'</tt> (assuming that <tt>queryParams</tt> contains <tt>name</tt> and <tt>pagesize</tt>
 * properties).
 * 
 * @see ReverseDataBinder
 * 
 * @author Phillip Webb
 */
public class NavigationOutcomeExpressionElResolver implements NavigationOutcomeExpressionResolver {

	private static final Pattern EL_PATTERN = Pattern.compile("(?:([A-Za-z0-9\\.\\-\\*\\_\\%]+)\\=)?+(\\#\\{.+?\\})");

	/**
	 * Where in the URL the expression appears
	 */
	protected enum Position {
		URL, QUERY
	}

	public NavigationLocation resolveNavigationOutcome(NavigationOutcomeExpressionContext context,
			NavigationLocation outcome) throws Exception {
		if (outcome == null || outcome.getLocation() == null || !(outcome.getLocation() instanceof String)) {
			return outcome;
		}
		Position position = Position.URL;
		String s = (String) outcome.getLocation();
		StringBuffer resolvedLocation = new StringBuffer();
		Matcher matcher = EL_PATTERN.matcher(s);
		int i = 0;
		while (matcher.find()) {
			String beforeMatch = s.substring(i, matcher.start());
			if (beforeMatch.indexOf('?') != -1) {
				position = Position.QUERY;
			}
			resolvedLocation.append(beforeMatch);
			String attribute = matcher.group(1);
			String expression = matcher.group(2);
			String converted = resolveConvertAndUrlEncode(context, position, attribute, expression);
			if (converted == null) {
				throw new IllegalStateException("Unable resolve and convert expression '" + expression
						+ "' for outcome '" + s + "'");
			}
			resolvedLocation.append(converted);
			i = matcher.end();
		}
		resolvedLocation.append(s.substring(i, s.length()));
		return new NavigationLocation(resolvedLocation.toString(), outcome.isPopup(), outcome.getFragments());
	}

	/**
	 * Resolve, Convert and URL encode the specified expression.
	 * @param context The navigation outcome expression context
	 * @param position The position of the expression inside the URL
	 * @param attribute The attribute from the URL that the expression is linked to or <tt>null</tt> when resolving
	 * expression from {@link Position#URL}
	 * @param expression The expression to resolve
	 * @return The resolved, converted and URL encoded result
	 * @throws Exception on error
	 */
	protected String resolveConvertAndUrlEncode(NavigationOutcomeExpressionContext context, Position position,
			String attribute, String expression) throws Exception {
		Object resolved = resolve(context, position, attribute, expression);
		return convertAndUrlEncode(context, position, attribute, expression, resolved);
	}

	/**
	 * Resolve the specified expression.
	 * @param context The navigation outcome expression context
	 * @param position The position of the expression inside the URL
	 * @param attribute The attribute from the URL that the expression is linked to or <tt>null</tt> when resolving
	 * expression from {@link Position#URL}
	 * @param expression The expression to resolve
	 * @return The resolved expression
	 * @throws Exception on error
	 */
	protected Object resolve(NavigationOutcomeExpressionContext context, Position position, String attribute,
			String expression) throws Exception {
		ExpressionFactory expressionFactory = FacesContext.getCurrentInstance().getApplication().getExpressionFactory();
		ELContext elContext = FacesContext.getCurrentInstance().getELContext();
		ValueExpression valueExpression = expressionFactory.createValueExpression(elContext, expression, Object.class);
		return valueExpression.getValue(elContext);
	}

	/**
	 * Convert and URL encode the specified resolved expression. By default this method will use a
	 * {@link ReverseDataBinder} in order to convert resolved expressions.
	 * @param context The navigation outcome expression context
	 * @param position The position of the expression inside the URL
	 * @param attribute The attribute from the URL that the expression is linked to or <tt>null</tt> when resolving
	 * expression from {@link Position#URL}
	 * @param expression The expression that was resolved
	 * @param resolved The result of the resolved expression
	 * @return A converted and URL encoded result
	 * @throws Exception on error
	 */
	protected String convertAndUrlEncode(NavigationOutcomeExpressionContext context, Position position,
			String attribute, String expression, Object resolved) throws Exception {
		if (resolved == null) {
			return null;
		}
		String encoding = context.getEncoding();
		StringBuilder rtn = new StringBuilder();
		if ((position == Position.QUERY) && attribute == null) {
			// Expression to expand
			WebDataBinder dataBinder = context.createDataBinder(attribute, resolved, null);
			ReverseDataBinder reverseDataBinder = new ReverseDataBinder(dataBinder);
			PropertyValues values = reverseDataBinder.reverseBind();
			if (values == null) {
				return null;
			}
			for (PropertyValue value : values.getPropertyValues()) {
				rtn.append(rtn.length() == 0 ? "" : "&");
				rtn.append(URLEncoder.encode(value.getName(), encoding));
				rtn.append("=");
				rtn.append(URLEncoder.encode((String) value.getValue(), encoding));
			}
		} else {
			WebDataBinder dataBinder = context.createDataBinder(attribute, null, null);
			ReverseDataBinder reverseDataBinder = new ReverseDataBinder(dataBinder);
			String converted = reverseDataBinder.reverseConvert(resolved);
			if (converted == null) {
				return null;
			}
			if (attribute != null) {
				rtn.append(attribute);
				rtn.append("=");
			}
			rtn.append(URLEncoder.encode(converted, encoding));
		}
		return rtn.toString();
	}
}
