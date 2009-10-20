package org.springframework.faces.mvc;

import java.util.Map;

import javax.faces.context.FacesContext;


/**
 * Interface that can be used to execute {@link ModelBinder}s . Often the {@link ModelBinder#bindModel(Map)} method
 * cannot be called at the point that a JSF view is created as it will want to bind elements to {@link PageScope} . As
 * page scope depends on a valid {@link FacesContext#getViewRoot()} binding can only occur after the view has been
 * created and attached to the FacesContext. In order to overcome this limitation this executor will allow bind
 * operations to occur to two phases. The {@link #storeModelToBind(FacesContext, Map)} method will be called when the
 * view is first created and the {@link #bindStoredModel(FacesContext)} method will be called before the RENDER_RESPONSE
 * phase.
 * 
 * @author Phillip Webb
 */
public interface ModelBindingExecutor {

	/**
	 * Called when a view is created so that the executor can store the model for the
	 * {@link #bindStoredModel(FacesContext)} to later retrieve.
	 * 
	 * @param facesContext The current FacesContext.
	 * @param model The model to store.
	 */
	void storeModelToBind(FacesContext facesContext, Map model);

	/**
	 * Called before the RENDER_RESPONSE phase to bind the model that was stored during
	 * {@link #storeModelToBind(FacesContext, Map)}.
	 * 
	 * @param facesContext The current FacesContext.
	 */
	void bindStoredModel(FacesContext facesContext);

}
