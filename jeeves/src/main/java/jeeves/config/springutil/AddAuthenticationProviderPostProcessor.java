package jeeves.config.springutil;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;

public class AddAuthenticationProviderPostProcessor implements BeanPostProcessor {

	private final int addIndex;
	private final AuthenticationProvider providerToAdd;

	/**
	 * @param addIndex the index of the location in the provider list to add the new provider.  If < 0 then add to end of list
	 */
	public AddAuthenticationProviderPostProcessor(int addIndex, AuthenticationProvider providerToAdd) {
		this.addIndex = addIndex;
		this.providerToAdd = providerToAdd;
	}
	public AddAuthenticationProviderPostProcessor(AuthenticationProvider providerToAdd) {
		this(-1, providerToAdd);
	}
	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName)
			throws BeansException {
		// Nothing to do
		return bean;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName)
			throws BeansException {
		if(beanName.equals("authenticationManager")) {
			ProviderManager authManager = (ProviderManager) bean;
			if (addIndex < 0) {
				authManager.getProviders().add(providerToAdd);
			} else {
				authManager.getProviders().add(addIndex, providerToAdd );
			}
		}
		return bean;
	}

}
