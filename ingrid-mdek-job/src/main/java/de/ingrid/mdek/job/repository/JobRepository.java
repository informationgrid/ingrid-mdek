package de.ingrid.mdek.job.repository;

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import de.ingrid.mdek.job.IJob;
import de.ingrid.mdek.job.MdekException;
import de.ingrid.mdek.job.register.IRegistrationService;
import de.ingrid.utils.IngridDocument;

public class JobRepository implements IJobRepository {

	private static final Logger LOG = Logger.getLogger(JobRepository.class);

	private final IRegistrationService _registrationService;

	public JobRepository(IRegistrationService registrationService) {
		_registrationService = registrationService;
	}

	@SuppressWarnings("unchecked")
	public IngridDocument register(IngridDocument document) {
		String jobId = (String) document.get(JOB_ID);
		String jobDescription = (String) document.get(JOB_DESCRIPTION);
		boolean persist = false;
		if (document.containsKey(IJobRepository.JOB_PERSIST)) {
			persist = document.getBoolean(IJobRepository.JOB_PERSIST);
		}
		IngridDocument ret = new IngridDocument();
		try {
			if (LOG.isInfoEnabled()) {
				LOG.info("try to register job [" + jobId + "]");
			}
			_registrationService.register(jobId, jobDescription, persist);
			// invoke getResults
			IJob job = (IJob) _registrationService.getRegisteredJob(jobId);
			ret.putAll(job.getResults());
			ret.putBoolean(JOB_REGISTER_SUCCESS, true);
		} catch (Throwable e) {
			if (LOG.isEnabledFor(Level.WARN)) {
				LOG.warn("job regsitering failed [" + jobId + "]", e);
			}
			ret.putBoolean(JOB_REGISTER_SUCCESS, false);
			ret.put(JOB_REGISTER_ERROR_MESSAGE, e.getMessage());
		}
		return ret;
	}

	public IngridDocument deRegister(IngridDocument document) {
		String jobId = (String) document.get(JOB_ID);
		IngridDocument ret = new IngridDocument();
		_registrationService.deRegister(jobId);
		ret.putBoolean(IJobRepository.JOB_DEREGISTER_SUCCESS, true);
		return ret;
	}

	@SuppressWarnings("unchecked")
	public IngridDocument invoke(IngridDocument document) {
		IngridDocument ret = new IngridDocument();
		String methodName = "";
		String jobId = (String) document.get(JOB_ID);
		try {
			List<Pair> methods = (List<Pair>) document.get(JOB_METHODS);
			IJob registeredJob = _registrationService.getRegisteredJob(jobId);
			if (registeredJob == null) {
				if (LOG.isEnabledFor(Level.WARN)) {
					LOG.warn("job not found [" + jobId + "]");
				}
				ret.put(JOB_INVOKE_ERROR_MESSAGE, "job not found [" + jobId
						+ "]");
				ret.putBoolean(JOB_INVOKE_SUCCESS, false);
			} else {
				List<Pair> methodResults = new ArrayList<Pair>();
				for (Pair pair : methods) {
					methodName = pair.getKey();
					Object value = pair.getValue();
					if (LOG.isDebugEnabled()) {
						LOG.debug("try to invoke method [" + methodName
								+ "] for jobid [" + jobId + "]");
					}
					Method method = getMethodToInvoke(registeredJob, methodName);
					Object object = value != null ? method.invoke(
							registeredJob, new Object[] { value }) : method
							.invoke(registeredJob, new Object[] {});
					methodResults.add(new Pair(getClass().getName() + "."
							+ method.getName(), (Serializable) object));
				}
				ret.put(JOB_INVOKE_RESULTS, methodResults);
				ret.putBoolean(JOB_INVOKE_SUCCESS, true);
			}
			
		} catch (Throwable e) {
			// is it a "handled" exception
			if (e.getCause() instanceof MdekException) {
				MdekException mdekExc = (MdekException) e.getCause();
				ret.put(JOB_INVOKE_ERROR_MDEK, mdekExc.getMdekErrors());

				ret.put(JOB_INVOKE_ERROR_MESSAGE, "Mdek Error Codes: " + mdekExc.getMdekErrors());

			// or an "unhandled" exception
			} else {
				if (LOG.isEnabledFor(Level.WARN)) {
					LOG.warn("method invoke failed for jobid [" + jobId + "]", e);
				}
				
				String msg = e.getMessage();
				if (msg == null) {
					StringWriter sw = new StringWriter();
					e.printStackTrace(new PrintWriter(sw));
					msg = sw.toString();
				}
				ret.put(JOB_INVOKE_ERROR_MESSAGE, msg);
			}

			ret.putBoolean(JOB_INVOKE_SUCCESS, false);
		}

		return ret;
	}

	private Method getMethodToInvoke(IJob registeredJob, String methodName) {
		Method method = getSetterMethod(methodName, registeredJob.getClass());
		if (method == null) {
			method = getMethod(registeredJob.getClass(), methodName);
		}

		if (method == null) {
			throw new IllegalArgumentException("method not found ["
					+ methodName + "]");
		}
		return method;
	}

	private Method getSetterMethod(String methodString, Class class1) {
		String methodPrefix = "set";
		String methodName = methodPrefix + methodString;
		return getMethod(class1, methodName);
	}

	private Method getMethod(Class class1, String methodName) {
		Method ret = null;
		Method[] methods = class1.getMethods();
		for (int i = 0; i < methods.length; i++) {
			if (methodName.equalsIgnoreCase(methods[i].getName())) {
				ret = methods[i];
				break;
			}
		}
		return ret;
	}

}
