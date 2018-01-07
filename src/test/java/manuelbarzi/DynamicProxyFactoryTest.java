package manuelbarzi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import manuelbarzi.DynamicProxyFactory.InvocationHandler;

/**
 * Unit test for DynamicProxyFactory.
 */
@SuppressWarnings({ "serial" })
public class DynamicProxyFactoryTest {

	private static Logger log = LoggerFactory.getLogger(DynamicProxyFactoryTest.class);

	private static String SOME_VALID_DATA = "some valid data", OTHER_VALID_DATA = "other valid data";

	private IMyBean myBean = new MyBean(SOME_VALID_DATA);

	private IMyBean myBeanProxy = DynamicProxyFactory.newInstance(new InvocationHandler<IMyBean>(myBean) {
		private long timing;

		/**
		 * Marks timing before invocation and adds some trivial validation on data
		 * interception to match SOME_VALID_DATA or OTHER_VALID_DATA, otherwise raises
		 * an error (InvalidDataException).
		 * 
		 * @param methodName
		 * @param args
		 */
		public void onBeforeInvocation(String methodName, Object[] args) {
			invokedMethod = methodName;

			if ("setData".equals(methodName)) {
				log.info("Executing {} with args {}", methodName, args);

				if (args.length < 1 || (args[0] != SOME_VALID_DATA && args[0] != OTHER_VALID_DATA))
					throw new InvalidDataException();
			} else if ("getData".equals(methodName))
				timing = System.nanoTime();
		}

		/**
		 * Logs execution timing.
		 * 
		 * @param methodName
		 * @param result
		 */
		public void onAfterInvocation(String methodName, Object result) {
			invocationSucceeded = true;

			if ("getData".equals(methodName))
				log.info("Executing {} finished in {} ns", methodName, System.nanoTime() - timing);
		}

		/**
		 * Invokes the given method in the original object and captures
		 * InvalidDataException in case data validation fails.
		 * 
		 * @param proxy
		 * @param method
		 * @param args
		 * @return
		 * @throws Throwable
		 */
		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			try {
				onBeforeInvocation(method.getName(), args);

				Object result = super.invoke(proxy, method, args);

				onAfterInvocation(method.getName(), result);

				return result;
			} catch (InvalidDataException e) {
				log.error("Invalid data submitted!");

				return myBean.getData();
			}
		}
	});

	private String invokedMethod;
	private boolean invocationSucceeded;

	@Before
	public void init() {
		invokedMethod = null;
		invocationSucceeded = false;
	}

	/**
	 * Data retrieval succeeds trough proxy.
	 */
	@Test
	public void dataRetrievalSucceeds() {
		assertEquals(myBean.getData(), myBeanProxy.getData());
		assertEquals("getData", invokedMethod);
		assertTrue(invocationSucceeded);
	}

	/**
	 * Data update succeeds trough proxy.
	 */
	@Test
	public void dataUpdateSucceeds() {
		myBeanProxy.setData(OTHER_VALID_DATA);
		
		assertEquals("setData", invokedMethod);
		assertTrue(invocationSucceeded);
		assertEquals(OTHER_VALID_DATA, myBean.getData());
	}
	
	/**
	 * Data update fails trough proxy because of invalid data passed.
	 */
	@Test
	public void dataUpdateFailsCauseOfValidationError() {
		myBean.setData(SOME_VALID_DATA);
		myBeanProxy.setData("invalid data");
		
		assertEquals("setData", invokedMethod);
		assertFalse(invocationSucceeded);
		assertEquals(SOME_VALID_DATA, myBean.getData());
	}

	/**
	 * Thrown in case data validation fails. 
	 * 
	 * @author manuelbarzi
	 */
	public static class InvalidDataException extends RuntimeException {
	}
}
