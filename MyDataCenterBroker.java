/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package MinMin_Algorithm;

import java.util.ArrayList;
import java.util.List;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.lists.VmList;

public class MyDataCenterBroker extends DatacenterBroker {

    public MyDataCenterBroker(String name) throws Exception {
        super(name);

    }

    public int step = 0; // number of steps

    // Gets the vm list
    @Override
    public List<Myvm> getVmsCreatedList() {
        return (List<Myvm>) vmsCreatedList;
    }

    // Gets the cloudlet list.
    @Override
    public List<MyCloudlet> getCloudletList() {
        return (List<MyCloudlet>) cloudletList;
    }

    // Submit cloudlets to the created VMs
    @Override
    protected void submitCloudlets() {

        for (int i = 0; i < getCloudletList().size(); i++) {

            MinMin_algorithm();
        }

        Log.printLine("===========================================");
        int vmIndex = 0;

        for (MyCloudlet cloudlet : getCloudletList()) {
            Myvm vm;

            // if user didn't bind this cloudlet and it has not been executed yet
            if (cloudlet.getVmId() == -1) {
                vm = getVmsCreatedList().get(vmIndex);
            } else { // submit to the specific vm
                vm = VmList.getById(getVmsCreatedList(), cloudlet.getVmId());
                if (vm == null) { // vm was not created
                    Log.printLine(CloudSim.clock() + ": " + getName() + ": Postponing execution of cloudlet "
                            + cloudlet.getCloudletId() + ": bount VM not available");
                    continue;
                }
            }

            Log.printLine(CloudSim.clock() + ": " + getName() + ": Sending cloudlet "
                    + cloudlet.getCloudletId() + " to VM #" + vm.getId());
            cloudlet.setVmId(vm.getId());
            //****
            vm.setstate();

            sendNow(getVmsToDatacentersMap().get(vm.getId()), CloudSimTags.CLOUDLET_SUBMIT, cloudlet);
            cloudletsSubmitted++;
            getCloudletSubmittedList().add(cloudlet);

            vmIndex = (vmIndex + 1) % getVmsCreatedList().size();
        }

        // remove submitted cloudlets from waiting list
        for (Cloudlet cloudlet : getCloudletSubmittedList()) {
            getCloudletList().remove(cloudlet);
        }
    }

    // Min-Min algorithm
    public void MinMin_algorithm() {

        int vm_index = 0;   // the variable is for keep index of selected machine(vm)
        int task_index = 0; // the variable is for keep index of selected task

        double[] waiting_time = new double[cloudletList.size()];

        ArrayList<double[]> run_Time = new ArrayList<double[]>();
        ArrayList<double[]> temp = new ArrayList<double[]>();  // list of machines 
        double[] inf = new double[2];
        for (int i = 0; i < cloudletList.size(); i++) {
            temp.add(inf);
        }

        for (int i = 0; i < vmList.size(); i++) {

            Vm vm = vmList.get(i);
            waiting_time[i] = calculatewaitingtimeofvm(i);

            double[] rt = new double[cloudletList.size()]; // keeping run time of cloudlets

            // For each job Calculate the execution time
            for (int j = 0; j < cloudletList.size(); j++) {

                Cloudlet cloudlet = cloudletList.get(j);

                rt[j] = (double) cloudlet.getCloudletLength() / vm.getHost().getTotalAllocatedMipsForVm(vm) + waiting_time[i];

            }

            run_Time.add(rt);

        }

        // For each job Find the minimum completion time and the machine that obtains it
        for (int i = 0; i < cloudletList.size(); i++) {
            double minvalue = 500;
            double min_completiontime = 0;

            for (int j = 0; j < vmList.size(); j++) {
                min_completiontime = run_Time.get(j)[i];
                if (min_completiontime < minvalue) {
                    minvalue = min_completiontime;
                    double[] temp1 = new double[2];
                    temp1[0] = j;
                    temp1[1] = minvalue;
                    temp.set(i, temp1);
                }
            }
        }
        // Search the job having minimum completion time among all unassigned jobs.
        // Allocate the job to machine the vm that has resulted in obtaining minimum completion time of the job
        double min_completiontimeoftask = 90;
        for (int i = 0; i < cloudletList.size(); i++) {
            if (getCloudletList().get(i).select_task == 0) {
                if (temp.get(i)[1] < min_completiontimeoftask) {
                    min_completiontimeoftask = temp.get(i)[1];
                    vm_index = (int) temp.get(i)[0];
                    task_index = i;

                    cloudletList.get(i).setVmId(vm_index);
                }

            }

        }
        Myvm vm = getVmsCreatedList().get(vm_index);

        // Update and Delete the job from the job set
        double Update = cloudletList.get(task_index).getCloudletLength() / (vm.getHost().getTotalAllocatedMipsForVm(vm));
        getCloudletList().get(task_index).select_task = 1;
        vm.setwaitingtime(Update);

        // Print of results
        step++;
        Log.printLine("======MinMin_algorithm======");
        Log.printLine("for stage " + step + "  task " + task_index + " assign to vm " + vm_index + " time " + temp.get(task_index)[1]);
    }

    public double calculatewaitingtimeofvm(int vmid) {
        double waitingtime = 0.0;
        waitingtime = getVmsCreatedList().get(vmid).getWaitingtime();

        return waitingtime;
    }

}
