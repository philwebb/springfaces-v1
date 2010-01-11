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
package org.springframework.faces.mvc.view;

import javax.servlet.ServletContext;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.context.WebApplicationContext;

/**
 * Implementation of {@link FacesViewIdResolver} that build JSF views using a defined <tt>prefix</tt> and
 * <tt>suffix</tt>. By default this implementation will use a prefix of '/WEB-INF/pages/' and use the suffix defined by
 * the web.xml parameter 'javax.faces.DEFAULT_SUFFIX' (falling back to '.xhtml').
 * 
 * @author Phillip Webb
 */
public class SimpleFacesViewIdResolver implements FacesViewIdResolver, ApplicationContextAware, InitializingBean {

	private static final String DEFAULT_PREFIX = "/WEB-INF/pages/";
	private static final String DEFAULT_SUFFIX = ".xhtml";
	private static final String DEFAULT_SUFFIX_PARAM = "javax.faces.DEFAULT_SUFFIX";

	private ServletContext servletContext;

	private String prefix = DEFAULT_PREFIX;
	private String suffix = null;

	protected String getDefaultSuffix() {
		String rtn = (servletContext == null ? null : servletContext.getInitParameter(DEFAULT_SUFFIX_PARAM));
		rtn = (rtn == null ? DEFAULT_SUFFIX : rtn);
		return rtn;
	}

	public String resolveViewId(String viewName) {
		String rtn = viewName;
		if (prefix != null) {
			rtn = prefix + rtn;
		}
		if (suffix != null) {
			rtn = rtn + suffix;
		}
		return rtn;
	}

	public String resolveViewName(String viewId) {
		String rtn = viewId;
		if (prefix != null && rtn.startsWith(prefix)) {
			rtn = rtn.substring(prefix.length());
		}
		if (rtn.endsWith(suffix)) {
			rtn = rtn.substring(0, rtn.length() - suffix.length());
		}
		return rtn;
	}

	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		if (applicationContext instanceof WebApplicationContext) {
			this.servletContext = ((WebApplicationContext) applicationContext).getServletContext();
		}
	}

	public void afterPropertiesSet() throws Exception {
		if (suffix == null) {
			suffix = getDefaultSuffix();
		}
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}
}
