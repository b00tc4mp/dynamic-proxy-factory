package manuelbarzi;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;

/**
 * Creates dynamic proxy instances by means of Dynamic Proxy API.
 * 
 * @see https://docs.oracle.com/javase/8/docs/technotes/guides/reflection/proxy.html
 * 
 * @author manuelbarzi
 */
public class DynamicProxyFactory {

	/**
	 * @param handler
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T newInstance(InvocationHandler<T> handler) {
		return (T) java.lang.reflect.Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(),
				new Class[] { (Class<T>) ((ParameterizedType) handler.getClass().getGenericSuperclass())
						.getActualTypeArguments()[0] },
				handler);
	}

	/**
	 * Serializable base implementation of java.lang.reflect.InvocationHandler.
	 * 
	 * @see java.lang.reflect.InvocationHandler
	 *
	 * @param <T>
	 */
	@SuppressWarnings("serial")
	public static abstract class InvocationHandler<T> implements java.lang.reflect.InvocationHandler, Serializable {
		private T target;

		/**
		 * @param target
		 */
		public InvocationHandler(T target) {
			this.target = target;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object,
		 * java.lang.reflect.Method, java.lang.Object[])
		 */
		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			return method.invoke(target, args);
		}
	}
}
