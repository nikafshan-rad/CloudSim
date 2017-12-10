/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package MinMin_Algorithm;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.CloudletSchedulerSpaceShared;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.UtilizationModelStochastic;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.VmSchedulerSpaceShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

public class Test {

    /**
     * The cloudlet list.
     */
    private static List<MyCloudlet> cloudletList;

    /**
     * The vmlist.
     */
    private static List<Myvm> vmlist;

    /**
     * Creates main() to run this example
     */
    public static void main(String[] args) {

        Log.printLine("Starting Test...");

        try {
			// First step: Initialize the CloudSim package. It should be called
            // before creating any entities.
            int num_user = 1;   // number of cloud users
            Calendar calendar = Calendar.getInstance();
            boolean trace_flag = false;  // mean trace events

            // Initialize the CloudSim library
            CloudSim.init(num_user, calendar, trace_flag);

			// Second step: Create Datacenters
            //Datacenters are the resource providers in CloudSim. We need at list one of them to run a CloudSim simulation
            Datacenter datacenter0 = createDatacenter("Datacenter_0");

            //Third step: Create Broker
            MyDataCenterBroker broker = createBroker();
            int brokerId = broker.getId();

            //Fourth step: Create one virtual machine
            vmlist = new ArrayList<Myvm>();

            //VM description
            int vmid = 0;
            int mips = 10000;
            long size = 10000; //image size (MB)
            int ram = 2048; //vm memory (MB)
            long bw = 1000;
            int pesNumber = 1; //number of cpus
            String vmm = "Xen"; //VMM name

            Myvm vm1 = new Myvm(vmid, brokerId, 500, pesNumber, ram, bw, size, vmm, new CloudletSchedulerTimeShared());

            //the second VM will have twice the priority of VM1 and so will receive twice CPU time
            vmid++;
            Myvm vm2 = new Myvm(vmid, brokerId, 1000, pesNumber, ram, bw, size, vmm, new CloudletSchedulerTimeShared());

            vmid++;
            Myvm vm3 = new Myvm(vmid, brokerId, 2000, pesNumber, ram, bw, size, vmm, new CloudletSchedulerTimeShared());

            vmid++;
            Myvm vm4 = new Myvm(vmid, brokerId, 800, pesNumber, ram, bw, size, vmm, new CloudletSchedulerTimeShared());
            vmid++;

            //add the VMs to the vmList
            vmlist.add(vm1);
            vmlist.add(vm2);
//            vmlist.add(vm3);
//            vmlist.add(vm4);
            //submit vm list to the broker
            broker.submitVmList(vmlist);

            //Fifth step: Create two Cloudlets
            cloudletList = new ArrayList<MyCloudlet>();

            //Cloudlet properties
            int id = 0;
            //long length = 250;
            long fileSize = 300;
            long outputSize = 300;
            UtilizationModel utilizationModel = new UtilizationModelFull();

            MyCloudlet cloudlet1 = new MyCloudlet(id, 500, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
            cloudlet1.setUserId(brokerId);

            id++;
            MyCloudlet cloudlet2 = new MyCloudlet(id, 1000, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
            cloudlet2.setUserId(brokerId);

            id++;
            MyCloudlet cloudlet3 = new MyCloudlet(id, 1000, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
            cloudlet3.setUserId(brokerId);

            id++;
            MyCloudlet cloudlet4 = new MyCloudlet(id, 500, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
            cloudlet4.setUserId(brokerId);

            id++;
            MyCloudlet cloudlet5 = new MyCloudlet(id, 100, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
            cloudlet5.setUserId(brokerId);
            //*(0.5);
            id++;
            MyCloudlet cloudlet6 = new MyCloudlet(id, 1500, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
            cloudlet6.setUserId(brokerId);

            //add the cloudlets to the list
            cloudletList.add(cloudlet1);
            cloudletList.add(cloudlet2);
            cloudletList.add(cloudlet3);
            cloudletList.add(cloudlet4);
                      //   cloudletList.add(cloudlet5);
            //   cloudletList.add(cloudlet6);

            //submit cloudlet list to the broker
            broker.submitCloudletList(cloudletList);

			// Sixth step: Starts the simulation
            CloudSim.startSimulation();

            // Final step: Print results when simulation is over
            List<MyCloudlet> newList = broker.getCloudletReceivedList();

            CloudSim.stopSimulation();

            printCloudletList(newList);

			//Print the debt of each user to each datacenter
//			datacenter0.printDebts();
            Log.printLine("Stopping Test finished!");
        } catch (Exception e) {
            e.printStackTrace();
            Log.printLine("The simulation has been terminated due to an unexpected error");
        }
    }

    private static Datacenter createDatacenter(String name) {

		// Here are the steps needed to create a PowerDatacenter:
        // 1. We need to create a list to store
        //    our machine
        List<Host> hostList = new ArrayList<Host>();

		// 2. A Machine contains one or more PEs or CPUs/Cores.
        // In this example, it will have only one core.
        List<Pe> peList = new ArrayList<Pe>();

        int mips = 500000;

        // 3. Create PEs and add these into a list.
        peList.add(new Pe(0, new PeProvisionerSimple(mips))); // need to store Pe id and MIPS Rating
        peList.add(new Pe(1, new PeProvisionerSimple(mips))); // need to store Pe id and MIPS Rating
        peList.add(new Pe(2, new PeProvisionerSimple(mips))); // need to store Pe id and MIPS Rating
        peList.add(new Pe(3, new PeProvisionerSimple(mips))); // need to store Pe id and MIPS Rating
        //4. Create Hosts with its id and list of PEs and add them to the list of machines
        int hostId = 0;
        int ram = 2048000; //host memory (MB)
        long storage = 100000000; //host storage
        int bw = 10000;

        hostList.add(
                new Host(
                        hostId,
                        new RamProvisionerSimple(ram),
                        new BwProvisionerSimple(bw),
                        storage,
                        peList,
                        new VmSchedulerTimeShared(peList)
                )
        ); // This is our first machine

        int hostId1 = 1;
        int ram1 = 102400; //host memory (MB)
        long storage1 = 50000000; //host storage
        int bw1 = 10000;
        int mips1 = 25000;
        //create another machine in the Data center
        List<Pe> peList2 = new ArrayList<Pe>();
        peList2.add(new Pe(0, new PeProvisionerSimple(mips1)));
        peList2.add(new Pe(1, new PeProvisionerSimple(mips1)));

        hostList.add(
                new Host(
                        hostId1,
                        new RamProvisionerSimple(ram1),
                        new BwProvisionerSimple(bw1),
                        storage1,
                        peList2,
                        new VmSchedulerTimeShared(peList2)
                )
        ); // This is our second machine

        //4. Create Hosts with its id and list of PEs and add them to the list of machines
        int hostId2 = 2;
        int ram2 = 51200; //host memory (MB)
        long storage2 = 25000000; //host storage
        int bw2 = 10000;
        int mips2 = 10000;
        List<Pe> peList3 = new ArrayList<Pe>();
        peList3.add(new Pe(0, new PeProvisionerSimple(mips2)));
        peList3.add(new Pe(1, new PeProvisionerSimple(mips)));

        hostList.add(
                new Host(
                        hostId2,
                        new RamProvisionerSimple(ram2),
                        new BwProvisionerSimple(bw2),
                        storage2,
                        peList3,
                        new VmSchedulerTimeShared(peList3)
                )
        );

		// 5. Create a DatacenterCharacteristics object that stores the
        //    properties of a data center: architecture, OS, list of
        //    Machines, allocation policy: time- or space-shared, time zone
        //    and its price (G$/Pe time unit).
        String arch = "x86";      // system architecture
        String os = "Linux";          // operating system
        String vmm = "Xen";
        double time_zone = 10.0;         // time zone this resource located
        double cost = 3.0;              // the cost of using processing in this resource
        double costPerMem = 0.05;		// the cost of using memory in this resource
        double costPerStorage = 0.001;	// the cost of using storage in this resource
        double costPerBw = 0.0;			// the cost of using bw in this resource
        LinkedList<Storage> storageList = new LinkedList<Storage>();	//we are not adding SAN devices by now

        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                arch, os, vmm, hostList, time_zone, cost, costPerMem, costPerStorage, costPerBw);

        // 6. Finally, we need to create a PowerDatacenter object.
        Datacenter datacenter = null;
        try {
            datacenter = new Datacenter(name, characteristics, new VmAllocationPolicySimple(hostList), storageList, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return datacenter;
    }

	//We strongly encourage users to develop their own broker policies, to submit vms and cloudlets according
    //to the specific rules of the simulated scenario
    private static MyDataCenterBroker createBroker() {

        MyDataCenterBroker broker = null;
        try {
            broker = new MyDataCenterBroker("Broker");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return broker;
    }

    /**
     * Prints the Cloudlet objects
     *
     * @param list list of Cloudlets
     */
    private static void printCloudletList(List<MyCloudlet> list) {
        int size = list.size();
        MyCloudlet cloudlet;

        String indent = "    ";
        Log.printLine();
        Log.printLine("========== OUTPUT ==========");
        Log.printLine("Cloudlet ID" + indent + "STATUS" + indent
                + "Data center ID" + indent + "VM ID" + indent + "Time" + indent + "Start Time" + indent + "Finish Time");

        DecimalFormat dft = new DecimalFormat("###.##");
        for (int i = 0; i < size; i++) {
            cloudlet = list.get(i);
            Log.print(indent + cloudlet.getCloudletId() + indent + indent);

            if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS) {
                Log.print("SUCCESS");

                Log.printLine(indent + indent + cloudlet.getResourceId() + indent + indent + indent + cloudlet.getVmId()
                        + indent + indent + dft.format(cloudlet.getActualCPUTime()) + indent + indent + dft.format(cloudlet.getExecStartTime())
                        + indent + indent + dft.format(cloudlet.getFinishTime()));
            }
        }

    }
}
