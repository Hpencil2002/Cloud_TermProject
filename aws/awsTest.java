package aws;

/*
* Cloud Computing
* 
* Dynamic Resource Management Tool
* using AWS Java SDK Library
* 
*/
import java.util.Iterator;
import java.util.Scanner;
import java.io.InputStream;
import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.DescribeAvailabilityZonesResult;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeRegionsResult;
import com.amazonaws.services.ec2.model.Region;
import com.amazonaws.services.ec2.model.AvailabilityZone;
import com.amazonaws.services.ec2.model.DryRunSupportedRequest;
import com.amazonaws.services.ec2.model.StopInstancesRequest;
import com.amazonaws.services.ec2.model.StartInstancesRequest;
import com.amazonaws.services.ec2.model.InstanceType;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.RebootInstancesRequest;
import com.amazonaws.services.ec2.model.RebootInstancesResult;
import com.amazonaws.services.ec2.model.DescribeImagesRequest;
import com.amazonaws.services.ec2.model.DescribeImagesResult;
import com.amazonaws.services.ec2.model.Image;
import com.amazonaws.services.ec2.model.Filter;
import com.jcraft.jsch.*;

public class awsTest {

	static AmazonEC2      ec2;

	private static void init() throws Exception {

		ProfileCredentialsProvider credentialsProvider = new ProfileCredentialsProvider();
		try {
			credentialsProvider.getCredentials();
		} catch (Exception e) {
			throw new AmazonClientException(
					"Cannot load the credentials from the credential profiles file. " +
					"Please make sure that your credentials file is at the correct " +
					"location (~/.aws/credentials), and is in valid format.",
					e);
		}
		ec2 = AmazonEC2ClientBuilder.standard()
			.withCredentials(credentialsProvider)
			.withRegion("us-east-1")	/* check the region at AWS console */
			.build();
	}

	public static void main(String[] args) throws Exception {

		init();

		Scanner menu = new Scanner(System.in);
		Scanner id_string = new Scanner(System.in);
		Scanner instance_count = new Scanner(System.in);
		Scanner job_name = new Scanner(System.in);
		Scanner output_name = new Scanner(System.in);
		int number = 0;
		
		while(true)
		{
			System.out.println("                                                            ");
			System.out.println("                                                            ");
			System.out.println("------------------------------------------------------------");
			System.out.println("           Amazon AWS Control Panel using SDK               ");
			System.out.println("------------------------------------------------------------");
			System.out.println("  1. list instance                2. available zones        ");
			System.out.println("  3. start instance               4. available regions      ");
			System.out.println("  5. stop instance                6. create instance        ");
			System.out.println("  7. reboot instance              8. list images            ");
			System.out.println("  9. show condor status          10. create several instaces");
			System.out.println(" 11. start instance by number    12. stop instance by number");
			System.out.println(" 13. send job to instance        14. check output of the job");
			System.out.println("                                 99. quit                   ");
			System.out.println("------------------------------------------------------------");
			
			System.out.print("Enter an integer: ");
			
			if(menu.hasNextInt()){
				number = menu.nextInt();
				}else {
					System.out.println("concentration!1");
					break;
				}
			

			String instance_id = "";

			switch(number) {
			case 1: 
				listInstances();
				break;
				
			case 2: 
				availableZones();
				break;
				
			case 3: 
				System.out.print("Enter instance id: ");
				if(id_string.hasNext())
					instance_id = id_string.nextLine();
				
				if(!instance_id.trim().isEmpty()) 
					startInstance(instance_id);
				break;

			case 4: 
				availableRegions();
				break;

			case 5: 
				System.out.print("Enter instance id: ");
				if(id_string.hasNext())
					instance_id = id_string.nextLine();
				
				if(!instance_id.trim().isEmpty()) 
					stopInstance(instance_id);
				break;

			case 6: 
				System.out.print("Enter ami id: ");
				String ami_id = "";
				if(id_string.hasNext())
					ami_id = id_string.nextLine();
				
				if(!ami_id.trim().isEmpty()) 
					createInstance(ami_id);
				break;

			case 7: 
				System.out.print("Enter instance id: ");
				if(id_string.hasNext())
					instance_id = id_string.nextLine();
				
				if(!instance_id.trim().isEmpty()) 
					rebootInstance(instance_id);
				break;

			case 8: 
				listImages();
				break;

			case 9:
				showCondorStatus();
				break;

			case 10:
				System.out.print("Enter ami id: ");
				String ami = "";
				if(id_string.hasNext())
					ami = id_string.nextLine();
				
				if(!ami.trim().isEmpty()) {
					System.out.print("Enter the number of instance: ");
					int count = instance_count.nextInt();
					for (int i = 0; i < count; i++) {
						createInstance(ami);
					}
				}
				break;

			case 11:
				startInstanceByNumber();
				break;

			case 12:
				stopInstanceByNumber();
				break;

			case 13:
				System.out.print("Enter job number: ");
				String job = "";
				if (job_name.hasNext()) {
					job = job_name.nextLine();
				}
				sendJob(job);
				break;

			case 14:
				System.out.print("Enter output number: ");
				String output = "";
				if (output_name.hasNext()) {
					output = output_name.nextLine();
				}
				checkOutput(output);
				break;

			case 99: 
				System.out.println("bye!");
				menu.close();
				id_string.close();
				return;
			default: System.out.println("concentration!2");
			}

		}
		
	}

	public static void listInstances() {
		
		System.out.println("Listing instances....");
		boolean done = false;
		
		DescribeInstancesRequest request = new DescribeInstancesRequest();
		
		while(!done) {
			DescribeInstancesResult response = ec2.describeInstances(request);

			for(Reservation reservation : response.getReservations()) {
				for(Instance instance : reservation.getInstances()) {
					System.out.printf(
						"[id] %s, " +
						"[AMI] %s, " +
						"[type] %s, " +
						"[state] %10s, " +
						"[monitoring state] %s",
						instance.getInstanceId(),
						instance.getImageId(),
						instance.getInstanceType(),
						instance.getState().getName(),
						instance.getMonitoring().getState());
				}
				System.out.println();
			}

			request.setNextToken(response.getNextToken());

			if(response.getNextToken() == null) {
				done = true;
			}
		}
	}
	
	public static void availableZones()	{

		System.out.println("Available zones....");
		try {
			DescribeAvailabilityZonesResult availabilityZonesResult = ec2.describeAvailabilityZones();
			Iterator <AvailabilityZone> iterator = availabilityZonesResult.getAvailabilityZones().iterator();
			
			AvailabilityZone zone;
			while(iterator.hasNext()) {
				zone = iterator.next();
				System.out.printf("[id] %s,  [region] %15s, [zone] %15s\n", zone.getZoneId(), zone.getRegionName(), zone.getZoneName());
			}
			System.out.println("You have access to " + availabilityZonesResult.getAvailabilityZones().size() +
					" Availability Zones.");

		} catch (AmazonServiceException ase) {
				System.out.println("Caught Exception: " + ase.getMessage());
				System.out.println("Reponse Status Code: " + ase.getStatusCode());
				System.out.println("Error Code: " + ase.getErrorCode());
				System.out.println("Request ID: " + ase.getRequestId());
		}
	
	}

	public static void startInstance(String instance_id)
	{
		
		System.out.printf("Starting .... %s\n", instance_id);

		DryRunSupportedRequest<StartInstancesRequest> dry_request =
			() -> {
			StartInstancesRequest request = new StartInstancesRequest()
				.withInstanceIds(instance_id);

			return request.getDryRunRequest();
		};

		StartInstancesRequest request = new StartInstancesRequest()
			.withInstanceIds(instance_id);

		ec2.startInstances(request);

		System.out.printf("Successfully started instance %s", instance_id);
	}

	public static void startInstanceByNumber() {
		System.out.println("Listing instances....");
		boolean done = false;
		
		DescribeInstancesRequest request = new DescribeInstancesRequest();
		
		while(!done) {
			DescribeInstancesResult response = ec2.describeInstances(request);

			for(Reservation reservation : response.getReservations()) {
				for(Instance instance : reservation.getInstances()) {
					System.out.printf(
						"[id] %s, " +
						"[AMI] %s, " +
						"[type] %s, " +
						"[state] %10s, " +
						"[monitoring state] %s",
						instance.getInstanceId(),
						instance.getImageId(),
						instance.getInstanceType(),
						instance.getState().getName(),
						instance.getMonitoring().getState());
				}
				System.out.println();
			}

			request.setNextToken(response.getNextToken());

			if(response.getNextToken() == null) {
				done = true;
			}
		}

		System.out.print("Select the instance number to start: ");
		Scanner instance_number = new Scanner(System.in);
		int number = -1;
		int index = 0;
		boolean started = false;
		
		if (instance_number.hasNextInt()) {
			number = instance_number.nextInt();
			instance_number.nextLine();
		}
		else {
			System.out.println("There isn't an instance");
			return;
		}

		done = false;
		while (!done) {
			DescribeInstancesResult instance_result = ec2.describeInstances(request);
			for(Reservation reservation : instance_result.getReservations()) {
				for(Instance instance : reservation.getInstances()) {
					if (index == number) {
						startInstance(instance.getInstanceId());
						started = true;
					}
				}
				index += 1;
			}

			request.setNextToken(instance_result.getNextToken());

			if(instance_result.getNextToken() == null) {
				done = true;
			}
		}

		if (!started) {
			System.out.println("Failed to start the instance");
		}
	}
	
	
	public static void availableRegions() {
		
		System.out.println("Available regions ....");

		DescribeRegionsResult regions_response = ec2.describeRegions();

		for(Region region : regions_response.getRegions()) {
			System.out.printf(
				"[region] %15s, " +
				"[endpoint] %s\n",
				region.getRegionName(),
				region.getEndpoint());
		}
	}
	
	public static void stopInstance(String instance_id) {

		DryRunSupportedRequest<StopInstancesRequest> dry_request =
			() -> {
			StopInstancesRequest request = new StopInstancesRequest()
				.withInstanceIds(instance_id);

			return request.getDryRunRequest();
		};

		try {
			StopInstancesRequest request = new StopInstancesRequest()
				.withInstanceIds(instance_id);
	
			ec2.stopInstances(request);
			System.out.printf("Successfully stop instance %s\n", instance_id);

		} catch(Exception e)
		{
			System.out.println("Exception: "+e.toString());
		}

	}

	public static void stopInstanceByNumber() {
		System.out.println("Listing instances....");
		boolean done = false;
		
		DescribeInstancesRequest request = new DescribeInstancesRequest();
		
		while(!done) {
			DescribeInstancesResult response = ec2.describeInstances(request);

			for(Reservation reservation : response.getReservations()) {
				for(Instance instance : reservation.getInstances()) {
					System.out.printf(
						"[id] %s, " +
						"[AMI] %s, " +
						"[type] %s, " +
						"[state] %10s, " +
						"[monitoring state] %s",
						instance.getInstanceId(),
						instance.getImageId(),
						instance.getInstanceType(),
						instance.getState().getName(),
						instance.getMonitoring().getState());
				}
				System.out.println();
			}

			request.setNextToken(response.getNextToken());

			if(response.getNextToken() == null) {
				done = true;
			}
		}

		System.out.print("Select the instance number to stop: ");
		Scanner instance_number = new Scanner(System.in);
		int number = -1;
		int index = 0;
		boolean stoped = false;
		
		if (instance_number.hasNextInt()) {
			number = instance_number.nextInt();
			instance_number.nextLine();
		}
		else {
			System.out.println("There isn't an instance");
			return;
		}

		done = false;
		while (!done) {
			DescribeInstancesResult instance_result = ec2.describeInstances(request);
			for(Reservation reservation : instance_result.getReservations()) {
				for(Instance instance : reservation.getInstances()) {
					if (index == number) {
						stopInstance(instance.getInstanceId());
						stoped = true;
					}
				}
				index += 1;
			}

			request.setNextToken(instance_result.getNextToken());

			if(instance_result.getNextToken() == null) {
				done = true;
			}
		}

		if (!stoped) {
			System.out.println("Failed to stop the instance");
		}
	}
	
	public static void createInstance(String ami_id) {
		
		RunInstancesRequest run_request = new RunInstancesRequest()
			.withImageId(ami_id)
			.withInstanceType(InstanceType.T2Micro)
			.withMaxCount(1)
			.withMinCount(1);

		RunInstancesResult run_response = ec2.runInstances(run_request);

		String reservation_id = run_response.getReservation().getInstances().get(0).getInstanceId();

		System.out.printf(
			"Successfully started EC2 instance %s based on AMI %s\n",
			reservation_id, ami_id);
	
	}

	public static void rebootInstance(String instance_id) {
		
		System.out.printf("Rebooting .... %s\n", instance_id);

		try {
			RebootInstancesRequest request = new RebootInstancesRequest()
					.withInstanceIds(instance_id);

				RebootInstancesResult response = ec2.rebootInstances(request);

				System.out.printf(
						"Successfully rebooted instance %s", instance_id);

		} catch(Exception e)
		{
			System.out.println("Exception: "+e.toString());
		}

		
	}
	
	public static void listImages() {
		System.out.println("Listing images....");
		
		
		DescribeImagesRequest request = new DescribeImagesRequest();
		ProfileCredentialsProvider credentialsProvider = new ProfileCredentialsProvider();
		
		request.getFilters().add(new Filter("owner-id").withValues("537124939003"));
		
		DescribeImagesResult results = ec2.describeImages(request);
		
		for(Image images :results.getImages()){
			System.out.printf("[ImageID] %s, [Name] %s, [Owner] %s\n", 
					images.getImageId(), images.getName(), images.getOwnerId());
		}
		
	}

	public static void showCondorStatus() {
		String host = "";
		String user = "ec2-user";
		String privateKeyPath = "/home/chomingyu/Downloads/cloud-test.pem";
		String command = "condor_status";
		String instanceId = "i-0df88b25ae24fec89";

		DescribeInstancesRequest request = new DescribeInstancesRequest().withInstanceIds(instanceId);
		DescribeInstancesResult response = ec2.describeInstances(request);
		for (Reservation reservation : response.getReservations()) {
			for (Instance instance : reservation.getInstances()) {
				host = instance.getPublicDnsName();
			}
		}

		try {
			JSch jsch = new JSch();
			jsch.addIdentity(privateKeyPath);
			Session session = jsch.getSession(user, host, 22);
			session.setConfig("StrictHostKeyChecking", "no");
			session.connect();

			System.out.println("Connection Success!");

			ChannelExec channel = (ChannelExec) session.openChannel("exec");
			channel.setCommand(command);
			channel.setErrStream(System.err);

			InputStream in = channel.getInputStream();
			channel.connect();

			byte[] tmp = new byte[1024];
			while (true) {
				while (in.available() > 0) {
					int i = in.read(tmp, 0, 1024);
					if (i < 0) {
						break;
					}
					System.out.print(new String(tmp, 0, i));
				}

				if (channel.isClosed()) {
					System.out.println("Exit Status: " + channel.getExitStatus());
					break;
				}
				Thread.sleep(1000);
			}

			channel.disconnect();
			session.disconnect();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void sendJob(String num) {
		String host = "";
		String user = "ec2-user";
		String privateKeyPath = "/home/chomingyu/Downloads/cloud-test.pem";
		String instanceId = "i-0df88b25ae24fec89";
		String localJobPath = "/home/chomingyu/cloud_project/job" + num + ".jds";
		String localScriptPath = "/home/chomingyu/cloud_project/job" + num + ".sh";
		String remoteDirectory = "/home/ec2-user";
		String command = "condor_submit job" + num + ".jds";

		DescribeInstancesRequest request = new DescribeInstancesRequest().withInstanceIds(instanceId);
		DescribeInstancesResult response = ec2.describeInstances(request);
		for (Reservation reservation : response.getReservations()) {
			for (Instance instance : reservation.getInstances()) {
				host = instance.getPublicDnsName();
			}
		}

		try {
			JSch jsch = new JSch();
			jsch.addIdentity(privateKeyPath);
			Session session = jsch.getSession(user, host, 22);
			session.setConfig("StrictHostKeyChecking", "no");
			session.connect();

			System.out.println("Connection Success!");

			ChannelSftp sftpChannel = (ChannelSftp) session.openChannel("sftp");
			sftpChannel.connect();
			System.out.println("SFTP Channel connected");

			sftpChannel.put(localJobPath, remoteDirectory + "/job" + num + ".jds");
			System.out.println("jds file uploaded");

			sftpChannel.put(localScriptPath, remoteDirectory + "/job" + num + ".sh");
			System.out.println("sh file uploaded");

			sftpChannel.disconnect();

			ChannelExec channel = (ChannelExec) session.openChannel("exec");
			channel.setCommand("cd " + remoteDirectory + " && chmod +x job" + num + ".sh && " + command);
			channel.setErrStream(System.err);

			InputStream in = channel.getInputStream();
			channel.connect();

			byte[] tmp = new byte[1024];
			while (true) {
				while (in.available() > 0) {
					int i = in.read(tmp, 0, 1024);
					if (i < 0) {
						break;
					}
					System.out.print(new String(tmp, 0, i));
				}

				if (channel.isClosed()) {
					System.out.println("Exit Status: " + channel.getExitStatus());
					break;
				}
				Thread.sleep(1000);
			}

			channel.disconnect();
			session.disconnect();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void checkOutput(String num) {
		String host = "";
		String user = "ec2-user";
		String privateKeyPath = "/home/chomingyu/Downloads/cloud-test.pem";
		String command = "cat /home/ec2-user/out" + num + ".txt";
		String instanceId = "i-0df88b25ae24fec89";

		DescribeInstancesRequest request = new DescribeInstancesRequest().withInstanceIds(instanceId);
		DescribeInstancesResult response = ec2.describeInstances(request);
		for (Reservation reservation : response.getReservations()) {
			for (Instance instance : reservation.getInstances()) {
				host = instance.getPublicDnsName();
			}
		}

		try {
			JSch jsch = new JSch();
			jsch.addIdentity(privateKeyPath);
			Session session = jsch.getSession(user, host, 22);
			session.setConfig("StrictHostKeyChecking", "no");
			session.connect();

			System.out.println("Connection Success!");

			ChannelExec channel = (ChannelExec) session.openChannel("exec");
			channel.setCommand(command);
			channel.setErrStream(System.err);

			InputStream in = channel.getInputStream();
			channel.connect();

			byte[] tmp = new byte[1024];
			while (true) {
				while (in.available() > 0) {
					int i = in.read(tmp, 0, 1024);
					if (i < 0) {
						break;
					}
					System.out.print(new String(tmp, 0, i));
				}

				if (channel.isClosed()) {
					System.out.println("Exit Status: " + channel.getExitStatus());
					break;
				}
				Thread.sleep(1000);
			}

			channel.disconnect();
			session.disconnect();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
	