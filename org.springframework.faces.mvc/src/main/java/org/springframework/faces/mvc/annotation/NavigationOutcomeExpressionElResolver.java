package org.springframework.faces.mvc.annotation;

import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.el.ValueExpression;
import javax.faces.context.FacesContext;

//FIXME DC
public class NavigationOutcomeExpressionElResolver implements NavigationOutcomeExpressionResolver {

	private static final Pattern EL_PATTERN = Pattern.compile("\\#\\{.+?\\}");

	public Object resolveNavigationOutcome(Object outcome) {
		if (outcome == null || !(outcome instanceof String)) {
			return outcome;
		}

		String s = (String) outcome;
		StringBuffer resolved = new StringBuffer();
		Matcher matcher = EL_PATTERN.matcher(s);
		int i = 0;
		while (matcher.find()) {
			resolved.append(s.substring(i, matcher.start()));
			resolved.append(resolveAndUrlEncode(matcher.group()));
			i = matcher.end();
		}
		resolved.append(s.substring(i, s.length()));
		return resolved.toString();
	}

	private String resolveAndUrlEncode(String expression) {
		Object resolved = resolve(expression);
		if (resolved == null) {
			return "";
		}
		// FIXME encoding charset
		return URLEncoder.encode(String.valueOf(resolved));
	}

	protected Object resolve(String expression) {

		ExpressionFactory expressionFactory = FacesContext.getCurrentInstance().getApplication().getExpressionFactory();
		ELContext elContext = FacesContext.getCurrentInstance().getELContext();

		// FIXME if we are to support JSF 1.1 we might need to do this
		// ValueBinding valueBinding =
		// FacesContext.getCurrentInstance().getApplication().createValueBinding(expression);
		// return valueBinding.getValue(FacesContext.getCurrentInstance());

		ValueExpression valueExpression = expressionFactory.createValueExpression(elContext, expression, Object.class);
		return valueExpression.getValue(elContext);
	}
}
