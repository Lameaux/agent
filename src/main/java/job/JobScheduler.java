package job;

import java.lang.reflect.Constructor;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import service.Service;
import service.ServiceState;
import agent.Agent;
import agent.Configuration;

public class JobScheduler implements Service {

	public static final String SERVICE_NAME = "job";

	private static final int SLEEP_TIME = 1000;

	private static final Logger LOG = LoggerFactory.getLogger(JobScheduler.class);

	private volatile ServiceState serviceState = ServiceState.STOPPED;
	private volatile boolean interrupted = false;

	private JobManager jobManager;

	private Configuration config;
	private ExecutorService executor;
	private ExecutorCompletionService<JobDetail> completionService;

	public JobScheduler() {
		config = Agent.get().getConfig();
		jobManager = Agent.get().getJobManager();

		executor = Executors.newFixedThreadPool(config.getJobPoolSize());
		completionService = new ExecutorCompletionService<JobDetail>(executor);
	}

	@Override
	public void run() {

		interrupted = false;
		LOG.info("JobScheduler started");

		while (!interrupted) {

			// check for completed jobs
			Future<JobDetail> jobStatusFuture = completionService.poll();
			if (jobStatusFuture != null) {
				try {
					jobManager.notify(jobStatusFuture.get());
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					interrupted = true;
					break;
				} catch (ExecutionException e) {
					LOG.warn("Job terminated with error", e.getCause());
				}
			}

			// check for new job
			if (jobManager.hasNewJob()) {
				JobDetail jobDetail = jobManager.getNextJob();
				jobManager.notify(jobDetail);				
				try {
					Job job = createJob(jobDetail);
					completionService.submit(job);
					jobDetail.setState(JobState.WAITING);
				} catch (Exception e) {
					jobDetail.setError(true);
					jobDetail.setMessage(e.getMessage());
					jobDetail.setState(JobState.FAILED);
				} finally {
					jobManager.notify(jobDetail);
				}
			}

			// sleep
			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException ie) {
				Thread.currentThread().interrupt();
				interrupted = true;
				break;
			}

		}
		serviceState = ServiceState.STOPPED;
		LOG.info("JobScheduler stopped");
	}

	private Job createJob(JobDetail jobDetail) throws Exception {
		Class<?> clazz = Class.forName(jobDetail.getJobClass());
		Constructor<?> ctor = clazz.getConstructor(JobDetail.class);
		Job job = (Job) ctor.newInstance(jobDetail);
		return job;

	}

	@Override
	public void startService() {
		serviceState = ServiceState.RUNNING;
		new Thread(this).start();
	}

	@Override
	public void stopService() {
		interrupted = true;
		try {
			Thread.sleep(SLEEP_TIME);
		} catch (InterruptedException ie) {
			Thread.currentThread().interrupt();
		}
	}

	@Override
	public String getServiceName() {
		return SERVICE_NAME;
	}

	@Override
	public ServiceState getServiceState() {
		return serviceState;
	}

}