package jeeves.config.springutil;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;

import org.springframework.beans.BeanUtils;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * A Spring Bean that will update a tag in a String property of the provided
 * beans when updateServerURL is called with the new server URL.
 * 
 * This allows the spring configuration to be configured without knowing the
 * actual server URL, instead the place holder can be put in the urls
 * 
 * @author jeichar
 */
public class ServerBeanPropertyUpdater {
	
	private String urlPlaceHolder;
	/* Bean, original property value from spring xml */
	private Map<Object, Object> originalURLs = new HashMap<Object, Object>();
	/* Bean, property name to set */
	private Map<Object, String> beans;

	/**
	 * @param urlPlaceHolder
	 *            the tag in the urls to replace with the server name. The url
	 *            is or example: https://localhost:8080/geoserver
	 * @param beans
	 *            references to the beans and the property to update
	 */
	public ServerBeanPropertyUpdater(String urlPlaceHolder,
			Map<Object, String> beans) {
		if(urlPlaceHolder.startsWith("@") && urlPlaceHolder.endsWith("@")) {
			this.urlPlaceHolder = urlPlaceHolder;
		}
		this.beans = beans;
	}

	/**
	 * If baseurlTag is not null then the method will replace the baseurlTag in
	 * the proxyCallbackURL with the provided baseURL. This allows a
	 * geonetwork.war to be moved from one server to another or the url can be
	 * changed without having to change the url.
	 * 
	 * @param baseURL
	 */
	public void updateServerURL(String baseURL) throws Exception {
		if (urlPlaceHolder != null) {
			for (Map.Entry<Object, String> bean : beans.entrySet()) {
				Object originalURL = originalURLs.get(bean.getKey());
				if (originalURL == null) {
					originalURL = getValue(bean.getKey(), bean.getValue());
					originalURLs.put(bean.getKey(), originalURL);
				}
				if (originalURL instanceof String) {
					String url = (String) originalURL;
					String updatedURL = url.replace(urlPlaceHolder, baseURL);
					setValue(bean.getKey(), bean.getValue(), updatedURL);
				}
			}
		}
	}

	private Object getValue(Object bean, String propertyName) throws Exception {
		PropertyDescriptor desc = BeanUtils.getPropertyDescriptor(
				bean.getClass(), propertyName);
		Method readMethod = desc.getReadMethod();

		if (readMethod != null) {
			readMethod.setAccessible(true);
			return readMethod.invoke(bean);
		}

		return lookUpField(bean.getClass(), propertyName).get(bean);
	}

	private Field lookUpField(Class<?> beanClass, String propertyName)
			throws NoSuchFieldException {
		Field field;
		try {
			field = beanClass.getDeclaredField(propertyName);
		} catch (NoSuchFieldException e) {
			Class<?> superClass = beanClass.getSuperclass();
			if (superClass == null) {
				throw new NoSuchFieldException(propertyName + " not found");
			}
			field = lookUpField(superClass, propertyName);
		}
		
		field.setAccessible(true);
		return field;

	}

	private void setValue(Object bean, String propertyName, String newValue)
			throws Exception {
		PropertyDescriptor desc = BeanUtils.getPropertyDescriptor(
				bean.getClass(), propertyName);
		Method writeMethod = desc.getWriteMethod();

		if (writeMethod != null) {
			writeMethod.setAccessible(true);
			writeMethod.invoke(bean, newValue);
		}
		lookUpField(bean.getClass(), propertyName).set(bean, newValue);
	}

	public static void updateURL(String newURL, ServletContext servletContext) throws Exception {
		WebApplicationContext context = WebApplicationContextUtils
				.getWebApplicationContext(servletContext);
		Map<String, ServerBeanPropertyUpdater> updaters = context
				.getBeansOfType(ServerBeanPropertyUpdater.class);
		for (ServerBeanPropertyUpdater updater : updaters.values()) {
			updater.updateServerURL(newURL);
		}

	}
}
