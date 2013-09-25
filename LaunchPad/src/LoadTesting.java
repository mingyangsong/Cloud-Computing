import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;
import com.amazonaws.services.cloudwatch.model.Datapoint;
import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsRequest;
import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsResult;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeInstanceStatusRequest;
import com.amazonaws.services.ec2.model.DescribeInstanceStatusResult;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceStatus;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;

public class LoadTesting {

	public static void main(String[] args) throws IOException,
			InterruptedException {

		String[] instance_types = new String[] { "m1.small", "m1.medium",
				"m1.large" };

		Properties properties = new Properties();
		properties.load(LoadTesting.class
				.getResourceAsStream("/AwsCredentials.properties"));
		BasicAWSCredentials credentials = new BasicAWSCredentials(
				properties.getProperty("aws_access_key"),
				properties.getProperty("aws_secret_access_key"));

		AmazonEC2Client ec2_conn = new AmazonEC2Client(credentials);

		for (int i = 0; i < 3; i++) {
			// Create Instance Request
			RunInstancesRequest runInstancesRequest = new RunInstancesRequest();

			System.out.println("Creating " + (i + 1) + " instance: "
					+ instance_types[i]);
			// Configure Instance Request
			runInstancesRequest.withImageId("ami-700e4a19")
					.withInstanceType(instance_types[i]).withMinCount(1)
					.withMaxCount(1).withKeyName("mingyans-cloudcomputing")
					.withSecurityGroups("CloudComputing");

			Date datestart = new Date(System.currentTimeMillis() - 600000);

			// Launch Instance
			RunInstancesResult runInstancesResult = ec2_conn
					.runInstances(runInstancesRequest);

			// Return the Object Reference of the Instance just Launched
			Instance cur_instance = runInstancesResult.getReservation()
					.getInstances().get(0);

			String cur_id = cur_instance.getInstanceId();
			
			// wait until the instance is running
			while (!cur_instance.getState().getName().equals("running")) {
				cur_instance = refreshInstanceState(ec2_conn, cur_instance);
				Thread.sleep(10000);
			}

			// wait until the instance is initialized
			while (!requestInstanceStatus(ec2_conn, cur_instance).equals("ok")) {
				Thread.sleep(10000);
			}

			System.out.println("Runing " + cur_instance.getPublicDnsName());

			for (int j = 0; j < 10; j++) {
				try {
					System.out.println("Testing" + (j + 1));
					Process proc = Runtime.getRuntime().exec(
							"bash /home/ubuntu/benchmark/apache_bench.sh sample.jpg 100000 100 "
									+ cur_instance.getPublicDnsName() + ":8080/upload "
									+ instance_types[i] + "_mingyans_stdout");
					BufferedReader read = new BufferedReader(
							new InputStreamReader(proc.getInputStream()));
					try {
						proc.waitFor();
					} catch (InterruptedException e) {
						System.out.println(e.getMessage());
					}
					while (read.ready()) {
						System.out.println(read.readLine());
					}
				} catch (IOException e) {
					System.out.println(e.getMessage());
				}

			}

			// Date datestart=instance.getLaunchTime();
			Date dateend = new Date(System.currentTimeMillis() + 600000);
			SimpleDateFormat dateFormat = new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss");

			String tmp = dateFormat.format(datestart);
			tmp = tmp.replace(' ', 'T');
			String tmp2 = dateFormat.format(dateend);
			tmp2 = tmp2.replace(' ', 'T');

			System.out.println("Start: " + tmp);
			System.out.println("Start: " + tmp2);
			String cloudwatchcmd = "mon-get-stats CPUUtilization --start-time "
					+ tmp
					+ " --end-time "
					+ tmp2
					+ " --period 60 --namespace \"AWS/EC2\" --statistics \"Maximum\" --dimensions \"InstanceId="
					+ cur_id + "\" --headers";
			System.out.println(cloudwatchcmd);

			Process p = Runtime.getRuntime().exec(cloudwatchcmd);
			BufferedReader in = new BufferedReader(new InputStreamReader(
					p.getInputStream()));
			String stdin_write;
			while ((stdin_write = in.readLine()) != null) {
				System.out.println(stdin_write);
			}

			AmazonCloudWatchClient cloudWatchClient = new AmazonCloudWatchClient(
					credentials);
			GetMetricStatisticsRequest getMetricStatisticsRequest = new GetMetricStatisticsRequest();

			Dimension dimension = new Dimension();
			dimension.withName("InstanceId").withValue(cur_id);

			getMetricStatisticsRequest.withDimensions(dimension)
					.withMetricName("CPUUtilization").withNamespace("AWS/EC2")
					.withStartTime(cur_instance.getLaunchTime())
					.withEndTime(new Date(System.currentTimeMillis()))
					.withPeriod(60).withStatistics("Average");
			System.out.println("Start: "
					+ cur_instance.getLaunchTime().toString());
			System.out.println("Start: "
					+ new Date(System.currentTimeMillis()).toString());

			GetMetricStatisticsResult metricStatisticsResult = cloudWatchClient
					.getMetricStatistics(getMetricStatisticsRequest);
			List<Datapoint> dataPoints = metricStatisticsResult.getDatapoints();
			BufferedWriter bwCPU = new BufferedWriter(
					new FileWriter("CPUUsage"));
			for (Datapoint datapoint : dataPoints) {
				bwCPU.write(datapoint.getTimestamp().toString() + " "
						+ datapoint.getAverage() + " Percent");
				System.out.println(datapoint.getTimestamp().toString() + " "
						+ datapoint.getAverage() + " Percent");
			}
			bwCPU.close();

			List<String> instancesToStop = new ArrayList<String>();
			instancesToStop.add(cur_id);
			TerminateInstancesRequest stoptr = new TerminateInstancesRequest();
			stoptr.setInstanceIds(instancesToStop);
			ec2_conn.terminateInstances(stoptr);
		}
	}
	
	private static Instance refreshInstanceState(AmazonEC2Client ec2,
			Instance instance) {
		DescribeInstancesRequest describeInstancesRequest = new DescribeInstancesRequest();
		describeInstancesRequest.withInstanceIds(instance.getInstanceId());
		DescribeInstancesResult describeInstancesResult = ec2
				.describeInstances(describeInstancesRequest);
		List<Reservation> reservations = describeInstancesResult
				.getReservations();
		Set<Instance> instances = new HashSet<Instance>();

		for (Reservation reservation : reservations) {
			instances.addAll(reservation.getInstances());
		}

		for (Instance ins : instances) {
			if (ins.getInstanceId().equals(instance.getInstanceId())) {
				instance = ins;
			}
		}
		System.out.println(instance.getInstanceId() + " "
				+ instance.getState().getName() + " "
				+ instance.getPublicDnsName() + " "
				+ instance.getPublicIpAddress());

		return instance;
	}

	private static String requestInstanceStatus(AmazonEC2Client ec2,
			Instance instance) {
		String instanceStatus = null;

		DescribeInstanceStatusRequest describeInstanceStatusRequest = new DescribeInstanceStatusRequest();
		describeInstanceStatusRequest.withInstanceIds(instance.getInstanceId());

		DescribeInstanceStatusResult describeInstanceStatusResult = ec2
				.describeInstanceStatus(describeInstanceStatusRequest);
		List<InstanceStatus> statuses = describeInstanceStatusResult
				.getInstanceStatuses();
		for (InstanceStatus status : statuses) {
			if (status.getInstanceId().equals(instance.getInstanceId())) {
				System.out.println(status.getInstanceId() + " "
						+ status.getInstanceStatus().getStatus() + " "
						+ status.getInstanceState().getName());
				instanceStatus = status.getInstanceStatus().getStatus();
			}
		}
		return instanceStatus;
	}
}
